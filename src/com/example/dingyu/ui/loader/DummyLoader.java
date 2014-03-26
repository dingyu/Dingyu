package com.example.dingyu.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.dingyu.bean.android.AsyncTaskLoaderResult;

/**
 * User: 
 * Date: 13-5-15
 */
public class DummyLoader<T> extends AsyncTaskLoader<AsyncTaskLoaderResult<T>> {
    public DummyLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public AsyncTaskLoaderResult<T> loadInBackground() {
        return null;
    }
}
