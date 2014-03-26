package com.example.dingyu.ui.topic;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.dingyu.R;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.ui.interfaces.AbstractAppActivity;
import com.example.dingyu.ui.main.MainTimeLineActivity;

import java.util.ArrayList;

/**
 * User: qii
 * Date: 12-11-18
 */
public class UserTopicListActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserBean userBean = (UserBean) getIntent().getSerializableExtra("userBean");
        ArrayList<String> topicList = getIntent().getStringArrayListExtra("topicList");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.topic));

        if (savedInstanceState == null) {
            UserTopicListFragment fragment;
            if (topicList != null) {
                fragment = new UserTopicListFragment(userBean, topicList);
            } else {
                fragment = new UserTopicListFragment(userBean);
            }
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

        }
        return false;
    }
}
