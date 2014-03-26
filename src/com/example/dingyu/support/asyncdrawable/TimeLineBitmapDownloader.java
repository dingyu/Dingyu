package com.example.dingyu.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.support.file.FileLocationMethod;
import com.example.dingyu.support.lib.MyAsyncTask;
import com.example.dingyu.support.lib.TimeLineImageView;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.ui.basefragment.AbstractTimeLineFragment;

/**
 * User: 
 * Date: 12-12-12
 */
public class TimeLineBitmapDownloader {

    private Drawable transPic = new ColorDrawable(DebugColor.LISTVIEW_FLING);

    private Handler handler;

    static volatile boolean pauseDownloadWork = false;
    static final Object pauseDownloadWorkLock = new Object();

    static volatile boolean pauseReadWork = false;
    static final Object pauseReadWorkLock = new Object();

    public TimeLineBitmapDownloader(Handler handler) {
        this.handler = handler;
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p/>
     * If work is paused, be sure setPauseDownloadWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseDownloadWork(boolean pauseWork) {
        synchronized (pauseDownloadWorkLock) {
            TimeLineBitmapDownloader.pauseDownloadWork = pauseWork;
            if (!TimeLineBitmapDownloader.pauseDownloadWork) {
                pauseDownloadWorkLock.notifyAll();
            }
        }
    }

    public void setPauseReadWork(boolean pauseWork) {
        synchronized (pauseReadWorkLock) {
            TimeLineBitmapDownloader.pauseReadWork = pauseWork;
            if (!TimeLineBitmapDownloader.pauseReadWork) {
                pauseReadWorkLock.notifyAll();
            }
        }
    }

    protected Bitmap getBitmapFromMemCache(String key) {
        if (TextUtils.isEmpty(key))
            return null;
        else
            return GlobalContext.getInstance().getAvatarCache().get(key);
    }


    public void downloadAvatar(ImageView view, UserBean user) {
        downloadAvatar(view, user, false);
    }


    public void downloadAvatar(ImageView view, UserBean user, AbstractTimeLineFragment fragment) {
        boolean isFling = fragment.isListViewFling();
        downloadAvatar(view, user, isFling);
    }

    public void downloadAvatar(ImageView view, UserBean user, boolean isFling) {

        if (user == null) {
            view.setImageDrawable(transPic);
            return;
        }

        String url;
        FileLocationMethod method;
        if (SettingUtility.getEnableBigAvatar()) {
            url = user.getAvatar_large();
            method = FileLocationMethod.avatar_large;
        } else {
            url = user.getProfile_image_url();
            method = FileLocationMethod.avatar_small;
        }
        display(view, url, method, isFling);
    }

    public void downContentPic(ImageView view, MessageBean msg, AbstractTimeLineFragment fragment) {
        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (SettingUtility.getEnableBigPic()) {
            picUrl = msg.getOriginal_pic();
            display(view, picUrl, FileLocationMethod.picture_large, isFling);

        } else {
            picUrl = msg.getThumbnail_pic();
            display(view, picUrl, FileLocationMethod.picture_thumbnail, isFling);

        }
    }

    public void downContentPic(TimeLineImageView view, MessageBean msg, AbstractTimeLineFragment fragment) {
        String picUrl;

        boolean isFling = ((AbstractTimeLineFragment) fragment).isListViewFling();

        if (SettingUtility.getEnableBigPic()) {
            picUrl = msg.getOriginal_pic();
            display(view, picUrl, FileLocationMethod.picture_large, isFling);

        } else {
            picUrl = msg.getThumbnail_pic();
            display(view, picUrl, FileLocationMethod.picture_thumbnail, isFling);

        }
    }


    private void display(final ImageView view, final String urlKey, final FileLocationMethod method, boolean isFling) {
        view.clearAnimation();
        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            if (view.getAlpha() != 1.0f) {
                view.setAlpha(1.0f);
            }
            cancelPotentialDownload(urlKey, view);
        } else {

            if (isFling) {
                view.setImageDrawable(transPic);
                return;
            }

            if (!cancelPotentialDownload(urlKey, view)) {
                return;
            }

            final ReadWorker newTask = new ReadWorker(view, urlKey, method);
            PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
            view.setImageDrawable(downloadedDrawable);

            //listview fast scroll performance
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (getBitmapDownloaderTask(view) == newTask) {
                        newTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    return;


                }
            }, 400);


        }

    }


    private void display(final TimeLineImageView view, final String urlKey, final FileLocationMethod method, boolean isFling) {
        view.clearAnimation();
        final Bitmap bitmap = getBitmapFromMemCache(urlKey);
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            view.getProgressBar().setVisibility(View.GONE);
            if (view.getAlpha() != 1.0f) {
                view.setAlpha(1.0f);
            }
            cancelPotentialDownload(urlKey, view.getImageView());
        } else {

            if (isFling) {
                view.setImageDrawable(transPic);
                view.getProgressBar().setVisibility(View.GONE);
                return;
            }

            if (!cancelPotentialDownload(urlKey, view.getImageView())) {
                return;
            }

            final ReadWorker newTask = new ReadWorker(view, urlKey, method);
            PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
            view.setImageDrawable(downloadedDrawable);

            //listview fast scroll performance
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (getBitmapDownloaderTask(view.getImageView()) == newTask) {
                        newTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    return;


                }
            }, 400);


        }

    }


    public void totalStopLoadPicture() {


    }


    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        IPictureWorker bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.getUrl();
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                if (bitmapDownloaderTask instanceof MyAsyncTask)
                    ((MyAsyncTask) bitmapDownloaderTask).cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }


    private static IPictureWorker getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof PictureBitmapDrawable) {
                PictureBitmapDrawable downloadedDrawable = (PictureBitmapDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }


}