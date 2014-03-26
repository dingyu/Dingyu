package com.example.dingyu.ui.userinfo;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.example.dingyu.R;
import com.example.dingyu.bean.AccountBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.support.lib.AppFragmentPagerAdapter;
import com.example.dingyu.support.lib.MyViewPager;
import com.example.dingyu.support.lib.SwipeRightToCloseOnGestureListener;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.basefragment.AbstractTimeLineFragment;
import com.example.dingyu.ui.interfaces.AbstractAppActivity;
import com.example.dingyu.ui.interfaces.IAccountInfo;
import com.example.dingyu.ui.interfaces.IUserInfo;
import com.example.dingyu.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: 
 * Date: 12-8-15
 */
public class MyInfoActivity extends AbstractAppActivity implements IUserInfo, IAccountInfo {

    private UserBean bean;

    private AccountBean account;

    private MyViewPager mViewPager = null;


    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        String token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");
        account = (AccountBean) getIntent().getSerializableExtra("account");

        setContentView(R.layout.viewpager_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.my_info));
        mViewPager = (MyViewPager) findViewById(R.id.viewpager);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
        GestureDetector gestureDetector = new GestureDetector(MyInfoActivity.this
                , new SwipeRightToCloseOnGestureListener(MyInfoActivity.this, mViewPager));
        mViewPager.setGestureDetector(gestureDetector);


        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.info))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.weibo))
                .setTabListener(tabListener));

    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean status = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());

            switch (tab.getPosition()) {

                case 1:
                    status = true;
                    break;

            }
        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    status = false;
                    break;

            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    if (status) {
                        Utility.stopListViewScrollingAndScrollToTop(getStatusFragment().getListView());
                    }
                    break;

            }
        }
    };

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_myinfoactivity, menu);
        MenuItem edit = menu.findItem(R.id.menu_edit);
        edit.setVisible(GlobalContext.getInstance().getAccountBean().isBlack_magic());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_edit:
                intent = new Intent(this, EditMyProfileActivity.class);
                intent.putExtra("userBean", GlobalContext.getInstance().getAccountBean().getInfo());
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public AccountBean getAccount() {
        return account;
    }

    private AbstractTimeLineFragment getStatusFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                StatusesByIdTimeLineFragment.class.getName()));
    }

    private Fragment getMyInfoFragment() {
        return getSupportFragmentManager().findFragmentByTag(
                MyInfoFragment.class.getName());
    }


    class TimeLinePagerAdapter extends
            AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);
            if (getMyInfoFragment() == null) {
                list.add(new MyInfoFragment());
            } else {
                list.add(getMyInfoFragment());
            }
            if (getStatusFragment() == null) {
                list.add(new StatusesByIdTimeLineFragment(getUser(), GlobalContext.getInstance().getSpecialToken()));
            } else {
                list.add(getStatusFragment());
            }
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(MyInfoFragment.class.getName());
            tagList.add(StatusesByIdTimeLineFragment.class.getName());
            return tagList.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }


}
