package com.example.dingyu.ui.dm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.ui.interfaces.AbstractAppActivity;
import com.example.dingyu.ui.main.MainTimeLineActivity;

/**
 * User: 
 * Date: 12-11-10
 */
public class DMActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        UserBean bean = (UserBean) getIntent().getSerializableExtra("user");

        setTitle(bean.getScreen_name());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new DMConversationListFragment(bean))
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
