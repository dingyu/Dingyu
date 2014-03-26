package com.example.dingyu.ui.search;

import android.os.Bundle;
import android.support.v4.content.Loader;
import com.example.dingyu.bean.UserBean;
import com.example.dingyu.bean.UserListBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.basefragment.AbstractUserListFragment;
import com.example.dingyu.ui.loader.SearchUserLoader;

import java.util.List;

/**
 * User: qii
 * Date: 12-11-10
 */
public class SearchUserFragment extends AbstractUserListFragment {

    private int page = 1;


    public SearchUserFragment() {
        super();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            clearAndReplaceValue((UserListBean) savedInstanceState.getSerializable("bean"));
            getAdapter().notifyDataSetChanged();
        }
        refreshLayout(bean);
    }


    @Override
    protected void oldUserOnPostExecute(UserListBean newValue) {
        if (newValue != null && newValue.getUsers().size() > 0) {
            List<UserBean> list = newValue.getUsers();
            getList().getUsers().addAll(list);
            page++;
        }
    }


    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String word = ((SearchMainParentFragment) getParentFragment()).getSearchWord();
        page = 1;
        return new SearchUserLoader(getActivity(), token, word, String.valueOf(page));
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String word = ((SearchMainParentFragment) getParentFragment()).getSearchWord();
        return new SearchUserLoader(getActivity(), token, word, String.valueOf(page + 1));
    }

}

