package com.example.dingyu.othercomponent;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.CommentListBean;
import com.example.dingyu.bean.MessageListBean;
import com.example.dingyu.bean.UnreadBean;

import com.example.dingyu.bean.android.CommentTimeLineData;
import com.example.dingyu.bean.android.MentionTimeLineData;
import com.example.dingyu.dao.maintimeline.MainCommentsTimeLineDao;
import com.example.dingyu.dao.maintimeline.MainMentionsTimeLineDao;
import com.example.dingyu.dao.maintimeline.MentionsCommentTimeLineDao;
import com.example.dingyu.dao.unread.UnreadDao;
import com.example.dingyu.support.database.AccountDBTask;
import com.example.dingyu.support.database.CommentToMeTimeLineDBTask;
import com.example.dingyu.support.database.MentionCommentsTimeLineDBTask;
import com.example.dingyu.support.database.MentionWeiboTimeLineDBTask;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.AppEventAction;
import com.example.dingyu.support.utils.BundleArgsConstants;

import java.util.Calendar;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class FetchNewMsgService extends IntentService {

    //close service between 1 clock and 8 clock
    private static final int NIGHT_START_TIME_HOUR = 1;
    private static final int NIGHT_END_TIME_HOUR = 7;


    public FetchNewMsgService() {
        super("FetchNewMsgService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (SettingUtility.disableFetchAtNight() && isNowNight()) {
            return;
        }

        List<AccountBean> accountBeanList = AccountDBTask.getAccountList();
        if (accountBeanList.size() == 0) {
            return;
        }
        for (AccountBean account : accountBeanList) {
            try {
                fetchMsg(account);
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isNowNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= NIGHT_START_TIME_HOUR && hour <= NIGHT_END_TIME_HOUR;
    }


    private void fetchMsg(AccountBean accountBean) throws WeiboException {
        CommentListBean commentResult = null;
        MessageListBean mentionStatusesResult = null;
        CommentListBean mentionCommentsResult = null;
        UnreadBean unreadBean = null;


        String token = accountBean.getAccess_token();

        UnreadDao unreadDao = new UnreadDao(token, accountBean.getUid());
        unreadBean = unreadDao.getCount();
        if (unreadBean == null) {
            return;
        }
        int unreadCommentCount = unreadBean.getCmt();
        int unreadMentionStatusCount = unreadBean.getMention_status();
        int unreadMentionCommentCount = unreadBean.getMention_cmt();

        if (unreadCommentCount > 0 && SettingUtility.allowCommentToMe()) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
            CommentListBean oldData = null;
            CommentTimeLineData commentTimeLineData = CommentToMeTimeLineDBTask.getCommentLineMsgList(accountBean.getUid());
            if (commentTimeLineData != null) {
                oldData = commentTimeLineData.cmtList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            commentResult = dao.getGSONMsgListWithoutClearUnread();
        }

        if (unreadMentionStatusCount > 0 && SettingUtility.allowMentionToMe()) {
            MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
            MessageListBean oldData = null;
            MentionTimeLineData commentTimeLineData = MentionWeiboTimeLineDBTask.getRepostLineMsgList(accountBean.getUid());
            if (commentTimeLineData != null) {
                oldData = commentTimeLineData.msgList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            mentionStatusesResult = dao.getGSONMsgListWithoutClearUnread();
        }

        if (unreadMentionCommentCount > 0 && SettingUtility.allowMentionCommentToMe()) {
            MainCommentsTimeLineDao dao = new MentionsCommentTimeLineDao(token);
            CommentListBean oldData = null;
            CommentTimeLineData commentTimeLineData = MentionCommentsTimeLineDBTask.getCommentLineMsgList(accountBean.getUid());
            if (commentTimeLineData != null) {
                oldData = commentTimeLineData.cmtList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            mentionCommentsResult = dao.getGSONMsgListWithoutClearUnread();
        }

        boolean mentionsWeibo = (mentionStatusesResult != null && mentionStatusesResult.getSize() > 0);
        boolean menttinosComment = (mentionCommentsResult != null && mentionCommentsResult.getSize() > 0);
        boolean commentsToMe = (commentResult != null && commentResult.getSize() > 0);
        if (mentionsWeibo || menttinosComment || commentsToMe) {
            sendTwoKindsOfBroadcast(accountBean, commentResult, mentionStatusesResult, mentionCommentsResult, unreadBean);
        }
    }

    private void sendTwoKindsOfBroadcast(AccountBean accountBean,
                                         CommentListBean commentResult,
                                         MessageListBean mentionStatusesResult,
                                         CommentListBean mentionCommentsResult,
                                         UnreadBean unreadBean) {
        Intent intent = new Intent(AppEventAction.NEW_MSG_PRIORITY_BROADCAST);
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        intent.putExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA, commentResult);
        intent.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, mentionStatusesResult);
        intent.putExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA, mentionCommentsResult);
        intent.putExtra(BundleArgsConstants.UNREAD_EXTRA, unreadBean);
        sendOrderedBroadcast(intent, null);

        intent.setAction(AppEventAction.NEW_MSG_BROADCAST);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
}
