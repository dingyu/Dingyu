package com.example.dingyu.ui.actionmenu;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.example.dingyu.R;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.dao.relationship.FriendshipsDao;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.AppLogger;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.adapter.UserListAdapter;
import com.example.dingyu.ui.basefragment.AbstractUserListFragment;
import com.example.dingyu.ui.send.WriteWeiboActivity;

/**
 * User: qii
 * Date: 12-9-19
 */
public class NormalFriendShipSingleChoiceModeListener implements ActionMode.Callback {
    private ListView listView;
    private UserListAdapter adapter;
    private Fragment fragment;
    private ActionMode mode;
    private UserBean bean;

    private MyAsyncTask<Void, UserBean, UserBean> followOrUnfollowTask;


    public void finish() {
        if (mode != null)
            mode.finish();

        if (followOrUnfollowTask != null)
            followOrUnfollowTask.cancel(true);
    }

    public NormalFriendShipSingleChoiceModeListener(ListView listView, UserListAdapter adapter, Fragment fragment, UserBean bean) {
        this.listView = listView;
        this.fragment = fragment;
        this.adapter = adapter;
        this.bean = bean;
    }

    private Activity getActivity() {
        return fragment.getActivity();
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mode == null)
            this.mode = mode;

        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        menu.clear();

        //sina weibo has bug,everyone's following is false
        inflater.inflate(R.menu.contextual_menu_fragment_user_listview, menu);
//        if(bean.isFollowing()){
//           menu.findItem(R.id.menu_follow).setVisible(false);
//        }

        mode.setTitle(bean.getScreen_name());


        return true;


    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_at:
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("content", "@" + bean.getScreen_name());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                getActivity().startActivity(intent);
                listView.clearChoices();
                mode.finish();
                break;
            case R.id.menu_follow:
                if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    followOrUnfollowTask = new FollowTask();
                    followOrUnfollowTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                listView.clearChoices();
                mode.finish();
                break;
            case R.id.menu_unfollow:
                if (followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    followOrUnfollowTask = new UnFollowTask();
                    followOrUnfollowTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                listView.clearChoices();
                mode.finish();
                break;
        }


        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.mode = null;
        listView.clearChoices();
        adapter.notifyDataSetChanged();
        ((AbstractUserListFragment) fragment).setmActionMode(null);

    }


    private class FollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(GlobalContext.getInstance().getSpecialToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }
            try {
                return dao.followIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(getActivity(), getActivity().getString(R.string.follow_successfully), Toast.LENGTH_SHORT).show();
            adapter.update(bean, o);
        }
    }

    private class UnFollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(GlobalContext.getInstance().getSpecialToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }

            try {
                return dao.unFollowIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (e != null) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            Toast.makeText(getActivity(), getActivity().getString(R.string.unfollow_successfully), Toast.LENGTH_SHORT).show();
            adapter.update(bean, o);
        }
    }
}
