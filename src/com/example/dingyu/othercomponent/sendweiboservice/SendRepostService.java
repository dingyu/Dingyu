package com.example.dingyu.othercomponent.sendweiboservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import com.example.dingyu.R;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.MessageBean;

import com.example.dingyu.dao.send.RepostNewMsgDao;
import com.example.dingyu.support.database.DraftDBManager;
import com.example.dingyu.support.database.draftbean.RepostDraftBean;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.NotificationUtility;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.send.WriteRepostActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * User: qii
 * Date: 13-1-20
 */
public class SendRepostService extends Service {

    private Map<WeiboSendTask, Boolean> tasksResult = new HashMap<WeiboSendTask, Boolean>();
    private Map<WeiboSendTask, Integer> tasksNotifications = new HashMap<WeiboSendTask, Integer>();

    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int lastNotificationId = intent.getIntExtra("lastNotificationId", -1);
        if (lastNotificationId != -1) {
            NotificationUtility.cancel(lastNotificationId);
        }

        String token = intent.getStringExtra("token");
        AccountBean account = (AccountBean) intent.getSerializableExtra("account");
        String content = intent.getStringExtra("content");
        MessageBean oriMsg = (MessageBean) intent.getSerializableExtra("oriMsg");
        String is_comment = intent.getStringExtra("is_comment");

        RepostDraftBean repostDraftBean = (RepostDraftBean) intent.getSerializableExtra("draft");

        WeiboSendTask task = new WeiboSendTask(token, account, content, oriMsg, is_comment, repostDraftBean);
        task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        tasksResult.put(task, false);

        return START_REDELIVER_INTENT;

    }


    private class WeiboSendTask extends MyAsyncTask<Void, Long, Void> {

        Notification notification;
        WeiboException e;

        String token;
        AccountBean account;
        String content;
        MessageBean oriMsg;
        String is_comment;
        RepostDraftBean repostDraftBean;


        public WeiboSendTask(String token,
                             AccountBean account,
                             String content,
                             MessageBean oriMsg,
                             String is_comment,
                             RepostDraftBean repostDraftBean) {
            this.token = token;
            this.account = account;
            this.content = content;
            this.oriMsg = oriMsg;
            this.is_comment = is_comment;
            this.repostDraftBean = repostDraftBean;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Notification.Builder builder = new Notification.Builder(SendRepostService.this)
                    .setTicker(getString(R.string.sending_repost))
                    .setContentTitle(getString(R.string.sending_repost))
                    .setContentText(content)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.upload_white);


            builder.setProgress(0, 100, true);


            int notificationId = new Random().nextInt(Integer.MAX_VALUE);


            notification = builder.getNotification();

            NotificationUtility.show(notification, notificationId);

            tasksNotifications.put(WeiboSendTask.this, notificationId);

        }


        private MessageBean sendText() throws WeiboException {
            RepostNewMsgDao dao = new RepostNewMsgDao(token, oriMsg.getId());
            if (!TextUtils.isEmpty(is_comment)) {
                dao.setIs_comment(is_comment);
            }
            dao.setStatus(content);
            return dao.sendNewMsg();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendText();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (repostDraftBean != null)
                DraftDBManager.getInstance().remove(repostDraftBean.getId());
            showSuccessfulNotification(WeiboSendTask.this);

        }

        @Override
        protected void onCancelled(Void aVoid) {
            super.onCancelled(aVoid);
            showFailedNotification(WeiboSendTask.this);

        }

        private void showSuccessfulNotification(final WeiboSendTask task) {
            Notification.Builder builder = new Notification.Builder(SendRepostService.this)
                    .setTicker(getString(R.string.send_successfully))
                    .setContentTitle(getString(R.string.send_successfully))
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.send_successfully)
                    .setOngoing(false);
            Notification notification = builder.getNotification();

            final int id = tasksNotifications.get(task);
            NotificationUtility.show(notification, id);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NotificationUtility.cancel(id);
                    stopServiceIfTasksAreEnd(task);
                }
            }, 3000);
        }

        private void showFailedNotification(final WeiboSendTask task) {
            Notification.Builder builder = new Notification.Builder(SendRepostService.this)
                    .setTicker(getString(R.string.send_failed))
                    .setContentTitle(getString(R.string.send_faile_click_to_open))
                    .setContentText(content)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.send_failed)
                    .setOngoing(false);


            Intent notifyIntent = WriteRepostActivity.startBecauseSendFailed(
                    SendRepostService.this, account, content, oriMsg, repostDraftBean, e.getError());

            PendingIntent pendingIntent = PendingIntent.getActivity(SendRepostService.this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);

            Notification notification;
            if (Utility.isJB()) {
                Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle(builder);
                bigTextStyle.setBigContentTitle(getString(R.string.send_faile_click_to_open));
                bigTextStyle.bigText(content);
                bigTextStyle.setSummaryText(account.getUsernick());
                builder.setStyle(bigTextStyle);

                Intent intent = new Intent(SendRepostService.this, SendRepostService.class);
                intent.putExtra("oriMsg", oriMsg);
                intent.putExtra("content", content);
                intent.putExtra("is_comment", is_comment);
                intent.putExtra("token", token);
                intent.putExtra("account", account);

                intent.putExtra("lastNotificationId", tasksNotifications.get(task));

                PendingIntent retrySendIntent = PendingIntent.getService(SendRepostService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.send_light, getString(R.string.retry_send), retrySendIntent);
                notification = builder.build();

            } else {
                notification = builder.getNotification();
            }

            final int id = tasksNotifications.get(task);

            NotificationUtility.show(notification, id);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopServiceIfTasksAreEnd(task);
                }
            }, 3000);
        }

    }

    private void stopServiceIfTasksAreEnd(WeiboSendTask currentTask) {

        tasksResult.put(currentTask, true);

        boolean isAllTaskEnd = true;
        Set<WeiboSendTask> taskSet = tasksResult.keySet();
        for (WeiboSendTask task : taskSet) {
            if (!tasksResult.get(task)) {
                isAllTaskEnd = false;
                break;
            }
        }
        if (isAllTaskEnd) {
            stopForeground(true);
            stopSelf();
        }
    }


}
