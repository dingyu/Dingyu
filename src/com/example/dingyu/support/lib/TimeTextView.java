package com.example.dingyu.support.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.example.dingyu.support.utils.TimeTool;

/**
 * User: qii
 * Date: 12-12-18
 */
public class TimeTextView extends TextView {

    public TimeTextView(Context context) {
        super(context);
    }

    public TimeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TimeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTime(long mills) {

        String time = TimeTool.getListTime(mills);
        if (!getText().toString().equals(time))
            setText(time);

    }
}
