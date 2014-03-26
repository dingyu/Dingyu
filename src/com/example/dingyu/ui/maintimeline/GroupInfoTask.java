package com.example.dingyu.ui.maintimeline;

import com.example.dingyu.bean.GroupListBean;

import com.example.dingyu.dao.maintimeline.FriendGroupDao;
import com.example.dingyu.support.database.GroupDBTask;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-12-28
 */
public class GroupInfoTask extends MyAsyncTask<Void, GroupListBean, GroupListBean> {


    private WeiboException e;

    private String token;

    public GroupInfoTask(String token) {
        this.token = token;
    }

    @Override
    protected GroupListBean doInBackground(Void... params) {
        try {
            return new FriendGroupDao(token).getGroup();
        } catch (WeiboException e) {
            this.e = e;
            cancel(true);
        }
        return null;
    }


    @Override
    protected void onPostExecute(GroupListBean groupListBean) {
        GroupDBTask.update(groupListBean, GlobalContext.getInstance().getCurrentAccountId());
        GlobalContext.getInstance().setGroup(groupListBean);

        super.onPostExecute(groupListBean);
    }

}