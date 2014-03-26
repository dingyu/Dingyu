package com.example.dingyu.ui.userinfo;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import com.example.dingyu.bean.UserListBean;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.actionmenu.MyFanSingleChoiceModeListener;
import com.example.dingyu.ui.actionmenu.NormalFriendShipSingleChoiceModeListener;
import com.example.dingyu.ui.basefragment.AbstractFriendsFanListFragment;
import com.example.dingyu.ui.loader.FanUserLoader;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FanListFragment extends AbstractFriendsFanListFragment {

    public FanListFragment() {
        super();
    }

    public FanListFragment(String uid) {
        super(uid);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new FanListOnItemLongClickListener());
    }


    private class FanListOnItemLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            if (position - 1 < getList().getUsers().size() && position - 1 >= 0) {
                if (mActionMode != null) {
                    mActionMode.finish();
                    mActionMode = null;
                    getListView().setItemChecked(position, true);
                    getAdapter().notifyDataSetChanged();
                    if (currentUser.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
                        mActionMode = getActivity().startActionMode(new MyFanSingleChoiceModeListener(getListView(), getAdapter(), FanListFragment.this, bean.getUsers().get(position - 1)));
                    } else {
                        mActionMode = getActivity().startActionMode(new NormalFriendShipSingleChoiceModeListener(getListView(), getAdapter(), FanListFragment.this, bean.getUsers().get(position - 1)));
                    }
                    return true;
                } else {
                    getListView().setItemChecked(position, true);
                    getAdapter().notifyDataSetChanged();
                    if (currentUser.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
                        mActionMode = getActivity().startActionMode(new MyFanSingleChoiceModeListener(getListView(), getAdapter(), FanListFragment.this, bean.getUsers().get(position - 1)));
                    } else {
                        mActionMode = getActivity().startActionMode(new NormalFriendShipSingleChoiceModeListener(getListView(), getAdapter(), FanListFragment.this, bean.getUsers().get(position - 1)));
                    }
                    return true;
                }
            }
            return false;
        }
    }


    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(0);
        return new FanUserLoader(getActivity(), token, uid, cursor);
    }

    @Override
    protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldMsgLoader(int id, Bundle args) {

        if (getList().getUsers().size() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
            return null;
        }


        String token = GlobalContext.getInstance().getSpecialToken();
        String cursor = String.valueOf(bean.getNext_cursor());

        return new FanUserLoader(getActivity(), token, uid, cursor);
    }


}


