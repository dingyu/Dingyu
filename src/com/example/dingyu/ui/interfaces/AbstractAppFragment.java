package com.example.dingyu.ui.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * User: 
 * Date: 12-12-30
 */
public class AbstractAppFragment extends Fragment {
    /**
     * when activity is recycled by system, isFirstTimeStartFlag will be reset to default true,
     * when activity is recreated because a configuration change for example screen rotate, isFirstTimeStartFlag will stay false
     */
    private boolean isFirstTimeStartFlag = true;

    protected static final int FIRST_TIME_START = 0; //when activity is first time start
    protected static final int SCREEN_ROTATE = 1;    //when activity is destroyed and recreated because a configuration change, see setRetainInstance(boolean retain)
    protected static final int ACTIVITY_DESTROY_AND_CREATE = 2;  //when activity is destroyed because memory is too low, recycled by android system

    protected int getCurrentState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            isFirstTimeStartFlag = false;
            return ACTIVITY_DESTROY_AND_CREATE;
        }


        if (!isFirstTimeStartFlag) {
            return SCREEN_ROTATE;
        }

        isFirstTimeStartFlag = false;
        return FIRST_TIME_START;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }
}
