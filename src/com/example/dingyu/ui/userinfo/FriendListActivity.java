package com.example.dingyu.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.dingyu.R;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.ui.interfaces.AbstractAppActivity;
import com.example.dingyu.ui.interfaces.IUserInfo;
import com.example.dingyu.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class FriendListActivity extends AbstractAppActivity implements IUserInfo {

    private UserBean bean;


    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.following_list));
        getActionBar().setIcon(R.drawable.ic_ab_friendship);
        bean = (UserBean) getIntent().getSerializableExtra("user");
        if (getSupportFragmentManager().findFragmentByTag(FriendsListFragment.class.getName()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new FriendsListFragment(bean.getId()), FriendsListFragment.class.getName())
                    .commit();
        }


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
        }
        return false;
    }
}

