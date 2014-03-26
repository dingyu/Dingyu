package com.example.dingyu.ui.maintimeline;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.CommentListBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.bean.android.CommentTimeLineData;
import com.example.dingyu.bean.android.TimeLinePosition;
import com.example.dingyu.dao.destroy.DestroyCommentDao;
import com.example.dingyu.support.database.CommentByMeTimeLineDBTask;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.actionmenu.CommentFloatingMenu;
import com.example.dingyu.ui.actionmenu.CommentSingleChoiceModeListener;
import com.example.dingyu.ui.adapter.CommentListAdapter;
import com.example.dingyu.ui.basefragment.AbstractTimeLineFragment;
import com.example.dingyu.ui.interfaces.ICommander;
import com.example.dingyu.ui.interfaces.IRemoveItem;
import com.example.dingyu.ui.loader.CommentsByMeDBLoader;
import com.example.dingyu.ui.loader.CommentsByMeMsgLoader;
import com.example.dingyu.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-1-22
 */
public class CommentsByMeTimeLineFragment extends AbstractTimeLineFragment<CommentListBean> implements IRemoveItem {


    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private RemoveTask removeTask;

    private CommentListBean bean = new CommentListBean();
    private TimeLinePosition timeLinePosition;


    @Override
    public CommentListBean getList() {
        return bean;
    }

    public CommentsByMeTimeLineFragment() {

    }

    public CommentsByMeTimeLineFragment(AccountBean accountBean, UserBean userBean, String token) {
        this.accountBean = accountBean;
        this.userBean = userBean;
        this.token = token;
    }


    protected void clearAndReplaceValue(CommentListBean value) {
        getList().getItemList().clear();
        getList().getItemList().addAll(value.getItemList());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("account", accountBean);
        outState.putSerializable("userBean", userBean);
        outState.putString("token", token);

        if (getActivity().isChangingConfigurations()) {
            outState.putSerializable("bean", bean);
            outState.putSerializable("timeLinePosition", timeLinePosition);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onListViewScrollStop() {
        super.onListViewScrollStop();
        timeLinePosition = Utility.getCurrentPositionFromListView(getListView());
    }

    @Override
    public void onPause() {
        super.onPause();
        CommentByMeTimeLineDBTask.asyncUpdatePosition(timeLinePosition, accountBean.getUid());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((MainTimeLineActivity) getActivity()).getBitmapDownloader();

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                userBean = (UserBean) savedInstanceState.getSerializable("userBean");
                accountBean = (AccountBean) savedInstanceState.getSerializable("account");
                token = savedInstanceState.getString("token");
                timeLinePosition = (TimeLinePosition) savedInstanceState.getSerializable("timeLinePosition");

                Loader<CommentTimeLineData> loader = getLoaderManager().getLoader(DB_CACHE_LOADER_ID);
                if (loader != null) {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }

                CommentListBean savedBean = (CommentListBean) savedInstanceState.getSerializable("bean");
                if (savedBean != null && savedBean.getSize() > 0) {
                    clearAndReplaceValue(savedBean);
                    timeLineAdapter.notifyDataSetChanged();
                    refreshLayout(getList());
                    setListViewPositionFromPositionsCache();
                } else {
                    getLoaderManager().initLoader(DB_CACHE_LOADER_ID, null, dbCallback);
                }
                break;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(onItemLongClickListener);
    }

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position - 1 < getList().getSize() && position - 1 >= 0) {
                if (mActionMode != null) {
                    mActionMode.finish();
                    mActionMode = null;
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(getListView(), timeLineAdapter, CommentsByMeTimeLineFragment.this, getList().getItemList().get(position - 1)));
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    timeLineAdapter.notifyDataSetChanged();
                    mActionMode = getActivity().startActionMode(new CommentSingleChoiceModeListener(getListView(), timeLineAdapter, CommentsByMeTimeLineFragment.this, getList().getItemList().get(position - 1)));
                    return true;
                }
            }
            return false;
        }

    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {
//            ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);
        }
    }

    @Override
    public void removeItem(int position) {
        clearActionMode();
        if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeTask = new RemoveTask(GlobalContext.getInstance().getSpecialToken(), getList().getItemList().get(position).getId(), position);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeCancel() {
        clearActionMode();
    }

    class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {

        String token;
        String id;
        int positon;
        WeiboException e;

        public RemoveTask(String token, String id, int positon) {
            this.token = token;
            this.id = id;
            this.positon = positon;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DestroyCommentDao dao = new DestroyCommentDao(token, id);
            try {
                return dao.destroy();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                ((CommentListAdapter) timeLineAdapter).removeItem(positon);

            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(false);
    }

    private void setListViewPositionFromPositionsCache() {
        if (timeLinePosition != null)
            getListView().setSelectionFromTop(timeLinePosition.position + 1, timeLinePosition.top);
        else
            getListView().setSelectionFromTop(0, 0);
    }


    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new CommentListAdapter(this, ((ICommander) getActivity()).getBitmapDownloader(), getList().getItemList(), getListView(), true, true);
        pullToRefreshListView.setAdapter(timeLineAdapter);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        CommentFloatingMenu menu = new CommentFloatingMenu(getList().getItem(position));
        menu.show(getFragmentManager(), "");
    }

    @Override
    protected void newMsgOnPostExecute(CommentListBean newValue, Bundle loaderArgs) {
        if (newValue != null && newValue.getItemList() != null && newValue.getItemList().size() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            CommentByMeTimeLineDBTask.asyncReplace(getList(), accountBean.getUid());
        }
    }

    @Override
    protected void oldMsgOnPostExecute(CommentListBean newValue) {
        if (newValue != null && newValue.getItemList().size() > 1) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
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

    private LoaderManager.LoaderCallbacks<CommentTimeLineData> dbCallback = new LoaderManager.LoaderCallbacks<CommentTimeLineData>() {
        @Override
        public Loader<CommentTimeLineData> onCreateLoader(int id, Bundle args) {
            getPullToRefreshListView().setVisibility(View.INVISIBLE);
            return new CommentsByMeDBLoader(getActivity(), GlobalContext.getInstance().getCurrentAccountId());
        }

        @Override
        public void onLoadFinished(Loader<CommentTimeLineData> loader, CommentTimeLineData result) {
            if (result != null) {
                clearAndReplaceValue(result.cmtList);
                timeLinePosition = result.position;
            }

            getPullToRefreshListView().setVisibility(View.VISIBLE);
            getAdapter().notifyDataSetChanged();
            setListViewPositionFromPositionsCache();

            refreshLayout(getList());
            /**
             * when this account first open app,if he don't have any data in database,fetch data from server automally
             */
            if (getList().getSize() == 0) {
                getPullToRefreshListView().startRefreshNow();
            }

            getLoaderManager().destroyLoader(loader.getId());

        }

        @Override
        public void onLoaderReset(Loader<CommentTimeLineData> loader) {

        }
    };

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, sinceId, null);
    }

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateMiddleMsgLoader(int id, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, middleBeginId, middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<CommentListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String accountId = accountBean.getUid();
        String token = accountBean.getAccess_token();
        String maxId = null;
        if (getList().getItemList().size() > 0) {
            maxId = getList().getItemList().get(getList().getItemList().size() - 1).getId();
        }
        return new CommentsByMeMsgLoader(getActivity(), accountId, token, null, maxId);
    }
}
