package com.example.dingyu.ui.dm;

import android.os.Bundle;
import com.example.dingyu.R;

import com.example.dingyu.ui.interfaces.AbstractAppActivity;

/**
 * User: 
 * Date: 13-3-2
 */
public class DMSelectUserActivity extends AbstractAppActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.select_dm_receiver);
    }
}
