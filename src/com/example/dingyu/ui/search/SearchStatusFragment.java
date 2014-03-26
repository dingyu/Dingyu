package com.example.dingyu.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import com.example.dingyu.bean.SearchStatusListBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.basefragment.AbstractMessageTimeLineFragment;
import com.example.dingyu.ui.browser.BrowserWeiboMsgActivity;
import com.example.dingyu.ui.interfaces.ICommander;
import com.example.dingyu.ui.loader.SearchStatusLoader;

/**
 * User: 
 * Date: 12-11-10
 */
public class SearchStatusFragment extends AbstractMessageTimeLineFragment<SearchStatusListBean> {

    private int page = 1;

    private SearchStatusListBean bean = new SearchStatusListBean();

    @Override
    public SearchStatusListBean getList() {
        return bean;
    }

    public SearchStatusFragment() {

    }

    public void search() {
        pullToRefreshListView.startRefreshNow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((ICommander) getActivity()).getBitmapDownloader();
        if (savedInstanceState != null && bean.getItemList().size() == 0) {
            clearAndReplaceValue((SearchStatusListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();

        }

        refreshLayout(bean);


    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("msg", bean.getItem(position));
        startActivity(intent);
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
    protected Loader<AsyncTaskLoaderResult<SearchStatusListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String word = ((SearchMainParentFragment) getParentFragment()).getSearchWord();
        page = 1;
        return new SearchStatusLoader(getActivity(), token, word, String.valueOf(page));
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<SearchStatusListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String word = ((SearchMainParentFragment) getParentFragment()).getSearchWord();
        return new SearchStatusLoader(getActivity(), token, word, String.valueOf(page + 1));
    }


    @Override
    protected void newMsgOnPostExecute(SearchStatusListBean newValue, Bundle loaderArgs) {
        if (newValue != null && getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
        }

    }

    @Override
    protected void oldMsgOnPostExecute(SearchStatusListBean newValue) {

        if (newValue != null && newValue.getSize() > 0) {
            getList().addOldData(newValue);
            getAdapter().notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();
            page++;
        }
    }
}
