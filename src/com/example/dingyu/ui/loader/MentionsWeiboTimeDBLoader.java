package com.example.dingyu.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.dingyu.bean.android.MentionTimeLineData;
import com.example.dingyu.support.database.MentionWeiboTimeLineDBTask;

/**
 * User: qii
 * Date: 13-4-10
 */
public class MentionsWeiboTimeDBLoader extends AsyncTaskLoader<MentionTimeLineData> {

    private String accountId;
    private MentionTimeLineData result;

    public MentionsWeiboTimeDBLoader(Context context, String accountId) {
        super(context);
        this.accountId = accountId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (result == null) {
            forceLoad();
        } else {
            deliverResult(result);
        }
    }

    public MentionTimeLineData loadInBackground() {
        result = MentionWeiboTimeLineDBTask.getRepostLineMsgList(accountId);
        return result;
    }

}
