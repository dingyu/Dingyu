package com.example.dingyu.othercomponent.unreadnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.CommentListBean;
import com.example.dingyu.bean.MessageListBean;
import com.example.dingyu.bean.UnreadBean;

import com.example.dingyu.support.utils.BundleArgsConstants;
import com.example.dingyu.support.utils.Utility;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class UnreadMsgReceiver extends BroadcastReceiver {

    private Context context;
    private AccountBean accountBean;

    private int sum;

    private CommentListBean commentsToMeData;
    private MessageListBean mentionsWeiboData;
    private CommentListBean mentionsCommentData;
    private UnreadBean unreadBean;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        accountBean = (AccountBean) intent.getSerializableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        commentsToMeData = (CommentListBean) intent.getSerializableExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA);
        mentionsWeiboData = (MessageListBean) intent.getSerializableExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA);
        mentionsCommentData = (CommentListBean) intent.getSerializableExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA);
        unreadBean = (UnreadBean) intent.getSerializableExtra(BundleArgsConstants.UNREAD_EXTRA);

        sum = unreadBean.getMention_cmt() + unreadBean.getMention_status() + unreadBean.getCmt();

        if (sum == 0 && accountBean != null) {
            clearNotification(accountBean);
        } else if (allowShowNotification()) {
            showNotification();
        }
    }

    private boolean allowShowNotification() {
        return sum > 0 && (commentsToMeData != null || mentionsWeiboData != null || mentionsCommentData != null);
    }

    private void clearNotification(AccountBean accountBean) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Long.valueOf(accountBean.getUid()).intValue());

    }

    private void showNotification() {


        if (!Utility.isJB()) {
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new ICSNotification(context, accountBean, commentsToMeData, mentionsWeiboData, mentionsCommentData, unreadBean).get();
            notificationManager.notify(Integer.valueOf(accountBean.getUid()), notification);
        } else {
            if (mentionsWeiboData != null && mentionsWeiboData.getSize() > 0) {
                Intent intent = new Intent(context, JBMentionsWeiboNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.MENTIONS_WEIBO_ARG, mentionsWeiboData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                context.startService(intent);
            }

            if (mentionsCommentData != null && mentionsCommentData.getSize() > 0) {
                Intent intent = new Intent(context, JBMentionsCommentNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.MENTIONS_COMMENT_ARG, mentionsCommentData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                context.startService(intent);
            }

            if (commentsToMeData != null && commentsToMeData.getSize() > 0) {
                Intent intent = new Intent(context, JBCommentsToMeNotificationServiceHelper.class);
                intent.putExtra(NotificationServiceHelper.ACCOUNT_ARG, accountBean);
                intent.putExtra(NotificationServiceHelper.COMMENTS_TO_ME_ARG, commentsToMeData);
                intent.putExtra(NotificationServiceHelper.UNREAD_ARG, unreadBean);
                intent.putExtra(NotificationServiceHelper.CURRENT_INDEX_ARG, 0);
                context.startService(intent);
            }
        }
    }
}
