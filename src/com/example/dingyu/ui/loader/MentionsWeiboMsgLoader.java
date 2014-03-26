package com.example.dingyu.ui.loader;

import android.content.Context;
import com.example.dingyu.bean.MessageListBean;

import com.example.dingyu.dao.maintimeline.MainMentionsTimeLineDao;
import com.example.dingyu.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-4-14
 */
public class MentionsWeiboMsgLoader extends AbstractAsyncNetRequestTaskLoader<MessageListBean> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String sinceId;
    private String maxId;
    private String accountId;

    public MentionsWeiboMsgLoader(Context context, String accountId, String token, String sinceId, String maxId) {
        super(context);
        this.token = token;
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.accountId = accountId;
    }


    public MessageListBean loadData() throws WeiboException {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
        dao.setSince_id(sinceId);
        dao.setMax_id(maxId);
        MessageListBean result = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } finally {
            lock.unlock();
        }


        return result;
    }

}

