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
public class FanListActivity extends AbstractAppActivity implements IUserInfo {
    private String token;

    private UserBean bean;


    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.fan_list));
        getActionBar().setIcon(R.drawable.ic_ab_friendship);

        token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");


        if (getSupportFragmentManager().findFragmentByTag(FanListFragment.class.getName()) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new FanListFragment(bean.getId()), FanListFragment.class.getName())
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

