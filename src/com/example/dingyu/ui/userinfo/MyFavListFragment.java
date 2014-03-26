package com.example.dingyu.ui.userinfo;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.example.dingyu.R;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.FavListBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.bean.android.FavouriteTimeLineData;
import com.example.dingyu.bean.android.TimeLinePosition;
import com.example.dingyu.support.database.FavouriteDBTask;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.basefragment.AbstractMessageTimeLineFragment;
import com.example.dingyu.ui.browser.BrowserWeiboMsgActivity;
import com.example.dingyu.ui.interfaces.ICommander;
import com.example.dingyu.ui.loader.MyFavMsgLoader;
import com.example.dingyu.ui.main.LeftMenuFragment;
import com.example.dingyu.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-8-18
 * this class need to refactor
 */
public class MyFavListFragment extends AbstractMessageTimeLineFragment<FavListBean> implements MainTimeLineActivity.ScrollableListFragment {

    private int page = 1;

    private FavListBean bean = new FavListBean();

    private TimeLinePosition position;

    private DBCacheTask dbTask;

    private AccountBean account;

    @Override
    public FavListBean getList() {
        return bean;
    }

    public MyFavListFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putSerializable("bean", bean);
//        outState.putInt("page", page);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.actionbar_menu_myfavlistfragment, menu);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        commander = ((ICommander) getActivity()).getBitmapDownloader();
        account = GlobalContext.getInstance().getAccountBean();
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                readDBCache();
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(bean);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                readDBCache();
                break;
        }

        if ((((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.FAV_INDEX) {
            buildActionBarAndViewPagerTitles();
        }
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
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.favourite));
            getActivity().getActionBar().setIcon(R.drawable.ic_menu_fav);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.favourite));
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
    }


    private void buildActionBarSubtitle() {
        if (bean != null) {
            int newSize = bean.getTotal_number();
            String number = bean.getSize() + "/" + newSize;
//            getActivity().getActionBar().setSubtitle(number);
        }
    }

    private void readDBCache() {
        if (Utility.isTaskStopped(dbTask) && getList().getSize() == 0) {
            dbTask = new DBCacheTask();
            dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getPullToRefreshListView().startRefreshNow();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        savePositionToDB();
    }


    @Override
    protected void newMsgOnPostExecute(FavListBean newValue, Bundle loaderArgs) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            addNewDataWithoutRememberPosition(newValue);
            buildActionBarSubtitle();
            FavouriteDBTask.asyncReplace(getList(), page, account.getUid());

        }

    }

    @Override
    protected void oldMsgOnPostExecute(FavListBean newValue) {

        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            buildActionBarSubtitle();
            page++;
            FavouriteDBTask.asyncReplace(getList(), page, account.getUid());
        }
    }

    private void addNewDataWithoutRememberPosition(FavListBean newValue) {
        getList().replaceData(newValue);
        getAdapter().notifyDataSetChanged();
        getListView().setSelectionAfterHeaderView();
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

    @Override
    protected Loader<AsyncTaskLoaderResult<FavListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        page = 1;
        return new MyFavMsgLoader(getActivity(), token, String.valueOf(page));
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<FavListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        return new MyFavMsgLoader(getActivity(), token, String.valueOf(page + 1));
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }

    @Override
    protected void onListViewScrollStop() {
        savePositionToPositionsCache();

    }

    private void savePositionToDB() {
        if (position == null) {
            savePositionToPositionsCache();
        }
        position.newMsgIds = newMsgTipBar.getValues();
        FavouriteDBTask.asyncUpdatePosition(position, account.getUid());
    }


    private void savePositionToPositionsCache() {
        position = Utility.getCurrentPositionFromListView(getListView());
    }


    private void setListViewPositionFromPositionsCache() {
        TimeLinePosition p = position;
        if (p != null)
            getListView().setSelectionFromTop(p.position + 1, p.top);
        else
            getListView().setSelectionFromTop(0, 0);


    }


    private class DBCacheTask extends MyAsyncTask<Void, FavouriteTimeLineData, FavouriteTimeLineData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
        }

        @Override
        protected FavouriteTimeLineData doInBackground(Void... params) {

            return FavouriteDBTask.getFavouriteMsgList(account.getUid());
        }

        @Override
        protected void onPostExecute(FavouriteTimeLineData result) {
            super.onPostExecute(result);
            getPullToRefreshListView().setVisibility(View.VISIBLE);

            if (result != null) {
                bean.replaceData(result.favList);
                page = result.page;
                position = result.position;
                getAdapter().notifyDataSetChanged();
                setListViewPositionFromPositionsCache();

            }

            refreshLayout(getList());

            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();

            }
        }
    }
}
