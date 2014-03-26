package com.example.dingyu.ui.dm;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.example.dingyu.R;
import com.example.dingyu.bean.DMUserListBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.support.database.DMDBTask;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.adapter.DMUserListAdapter;
import com.example.dingyu.ui.basefragment.AbstractTimeLineFragment;
import com.example.dingyu.ui.interfaces.ICommander;
import com.example.dingyu.ui.loader.DMUserLoader;
import com.example.dingyu.ui.main.LeftMenuFragment;
import com.example.dingyu.ui.main.MainTimeLineActivity;

/**
 * User: 
 * Date: 12-11-14
 */
public class DMUserListFragment extends AbstractTimeLineFragment<DMUserListBean> implements MainTimeLineActivity.ScrollableListFragment {

    private DMUserListBean bean = new DMUserListBean();

    private DBCacheTask dbTask;


    @Override
    public DMUserListBean getList() {
        return bean;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                if (Utility.isTaskStopped(dbTask)) {
                    dbTask = new DBCacheTask();
                    dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                bean.addNewData((DMUserListBean) savedInstanceState.getSerializable("bean"));
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }
//        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
//                == LeftMenuFragment.SEARCH_INDEX) {
//            buildActionBarAndViewPagerTitles();
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(dbTask);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            buildActionBarAndViewPagerTitles();
        }
    }

    public void buildActionBarAndViewPagerTitles() {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.dm));
            getActivity().getActionBar().setIcon(R.drawable.ic_menu_message);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.dm));
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_dmuserlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_write_dm:
                Intent intent = new Intent(getActivity(), DMSelectUserActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DMActivity.class);
        intent.putExtra("user", bean.getItem(position).getUser());
        startActivity(intent);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new DMUserListAdapter(this, ((ICommander) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView());
        getListView().setAdapter(timeLineAdapter);
    }

    private class DBCacheTask extends MyAsyncTask<Void, DMUserListBean, DMUserListBean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected DMUserListBean doInBackground(Void... params) {
            return DMDBTask.get(GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        protected void onPostExecute(DMUserListBean result) {
            super.onPostExecute(result);
            if (result != null)
                getList().addNewData(result);
            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            refreshLayout(getList());

            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            }
        }
    }

    @Override
    protected void newMsgOnPostExecute(DMUserListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            DMDBTask.asyncReplace(getList(), GlobalContext.getInstance().getCurrentAccountId());

        }

    }

    @Override
    protected void oldMsgOnPostExecute(DMUserListBean newValue) {
        if (newValue != null && newValue.getSize() > 0 && getActivity() != null) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
        }
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


    protected Loader<AsyncTaskLoaderResult<DMUserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(0);
        return new DMUserLoader(getActivity(), token, cursor);
    }


    protected Loader<AsyncTaskLoaderResult<DMUserListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = null;
        if (getList().getSize() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
            return null;
        }

        if (getList().getSize() > 0) {
            cursor = String.valueOf(getList().getNext_cursor());
        }

        return new DMUserLoader(getActivity(), token, cursor);
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }
}
