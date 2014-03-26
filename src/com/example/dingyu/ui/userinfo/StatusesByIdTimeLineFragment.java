package com.example.dingyu.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import com.example.dingyu.R;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.MessageListBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.support.lib.VelocityListView;
import com.example.dingyu.support.utils.AppConfig;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.basefragment.AbstractMessageTimeLineFragment;
import com.example.dingyu.ui.browser.BrowserWeiboMsgActivity;
import com.example.dingyu.ui.loader.StatusesByIdLoader;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesByIdTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean> {


    protected UserBean userBean;
    protected String token;
    private MessageListBean bean = new MessageListBean();

    public StatusesByIdTimeLineFragment() {

    }


    @Override
    public MessageListBean getList() {
        return bean;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        MessageBean msg = (MessageBean) data.getSerializableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getList().getSize(); i++) {
                if (msg.equals(getList().getItem(i))) {
                    getList().getItem(i).setReposts_count(msg.getReposts_count());
                    getList().getItem(i).setComments_count(msg.getComments_count());
                    break;
                }
            }
            getAdapter().notifyDataSetChanged();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (userBean != null
                && userBean.getId() != null
                && userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            GlobalContext.getInstance().registerForAccountChangeListener(myProfileInfoChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalContext.getInstance().unRegisterForAccountChangeListener(myProfileInfoChangeListener);
    }

    private GlobalContext.MyProfileInfoChangeListener myProfileInfoChangeListener = new GlobalContext.MyProfileInfoChangeListener() {
        @Override
        public void onChange(UserBean newUserBean) {
            for (MessageBean msg : getList().getItemList()) {
                msg.setUser(newUserBean);
            }
            getAdapter().notifyDataSetChanged();
        }
    };

    public StatusesByIdTimeLineFragment(UserBean userBean, String token) {
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", getList());
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null)
                            getPullToRefreshListView().startRefreshNow();

                    }
                }, AppConfig.REFRESH_DELAYED_MILL_SECOND_TIME);
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceData((MessageListBean) savedInstanceState.getSerializable("bean"));
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                token = savedInstanceState.getString("token");
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

        super.onActivityCreated(savedInstanceState);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("msg", getList().getItem(position));
        startActivityForResult(intent, 0);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getPullToRefreshListView().startRefreshNow();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue, Bundle loaderArgs) {
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();

        }


    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            getActivity().invalidateOptionsMenu();

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void loadMiddleMsg(String beginId, String endId, int position) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        dismissFooterView();

        Bundle bundle = new Bundle();
        bundle.putString("beginId", beginId);
        bundle.putString("endId", endId);
        bundle.putInt("position", position);
        VelocityListView velocityListView = (VelocityListView) getListView();
        bundle.putBoolean("towardsBottom", velocityListView.getTowardsOrientation() == VelocityListView.TOWARDS_BOTTOM);
        getLoaderManager().restartLoader(MIDDLE_MSG_LOADER_ID, bundle, msgCallback);

    }

    @Override
    public void loadNewMsg() {
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }


    @Override
    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgCallback);
    }


    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateMiddleMsgLoader(int id, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, middleBeginId, middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String maxId = null;

        if (getList().getSize() > 0) {
            maxId = getList().getItemList().get(getList().getSize() - 1).getId();
        }

        return new StatusesByIdLoader(getActivity(), uid, screenName, token, null, maxId);
    }
}


