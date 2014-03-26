package com.example.dingyu.ui.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

import com.example.dingyu.support.lib.AppFragmentPagerAdapter;
import com.example.dingyu.ui.main.MainTimeLineActivity;
import com.example.dingyu.ui.maintimeline.MentionsCommentTimeLineFragment;
import com.example.dingyu.ui.maintimeline.MentionsWeiboTimeLineFragment;

/**
 * User: qii
 * Date: 13-5-11
 */
public class SearchTimeLinePagerAdapter extends AppFragmentPagerAdapter {

    private SparseArray<Fragment> fragmentList;

    public SearchTimeLinePagerAdapter(SearchMainParentFragment fragment, ViewPager viewPager, FragmentManager fm, MainTimeLineActivity activity, SparseArray<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
        fragmentList.append(0, fragment.getSearchWeiboFragment());
        fragmentList.append(1, fragment.getSearchUserFragment());
        FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        if (!fragmentList.get(0).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(0), SearchStatusFragment.class.getName());
        if (!fragmentList.get(1).isAdded())
            transaction.add(viewPager.getId(), fragmentList.get(1), SearchUserFragment.class.getName());
        if (!transaction.isEmpty()) {
            transaction.commit();
            fragment.getChildFragmentManager().executePendingTransactions();
        }
    }


    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    protected String getTag(int position) {
        SparseArray<String> tagList = new SparseArray<String>();
        tagList.append(0, MentionsWeiboTimeLineFragment.class.getName());
        tagList.append(0, MentionsCommentTimeLineFragment.class.getName());

        return tagList.get(position);
    }


    @Override
    public int getCount() {
        return 2;
    }


}
