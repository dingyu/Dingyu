package com.example.dingyu.ui.loader;

import android.content.Context;
import com.example.dingyu.bean.FavListBean;

import com.example.dingyu.dao.fav.FavListDao;
import com.example.dingyu.support.error.WeiboException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: qii
 * Date: 13-5-15
 */
public class MyFavMsgLoader extends AbstractAsyncNetRequestTaskLoader<FavListBean> {

    private static Lock lock = new ReentrantLock();

    private String token;
    private String page;

    public MyFavMsgLoader(Context context, String token, String page) {
        super(context);
        this.token = token;
        this.page = page;
    }


    public FavListBean loadData() throws WeiboException {
        FavListDao dao = new FavListDao(token);
        dao.setPage(page);
        FavListBean result = null;
        lock.lock();

        try {
            result = dao.getGSONMsgList();
        } finally {
            lock.unlock();
        }


        return result;
    }

}

