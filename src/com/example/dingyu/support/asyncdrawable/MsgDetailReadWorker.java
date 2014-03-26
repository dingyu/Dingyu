package com.example.dingyu.support.asyncdrawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.example.dingyu.bean.MessageBean;

import com.example.dingyu.support.file.FileDownloaderHttpHelper;
import com.example.dingyu.support.file.FileLocationMethod;
import com.example.dingyu.support.file.FileManager;
import com.example.dingyu.support.imagetool.ImageTool;
import com.example.dingyu.support.lib.MyAsyncTask;

import java.io.File;

/**
 * User: qii
 * Date: 13-2-8
 * insert progress update listener into  download worker if it exists
 * or create a new download worker
 */
public class MsgDetailReadWorker extends MyAsyncTask<Void, Integer, Bitmap> {

    private ImageView view;
    private ProgressBar pb;

    private boolean pbFlag = false;

    private MessageBean msg;

    public MsgDetailReadWorker(ImageView view, ProgressBar pb, MessageBean msg) {
        this.view = view;
        this.pb = pb;
        this.msg = msg;
    }

    @Override
    protected Bitmap doInBackground(Void... arg) {
        if (isCancelled()) {
            return null;
        }

        TaskCache.waitForMsgDetailPictureDownload(msg, downloadListener);

        FileLocationMethod method;
        String middlePath = FileManager.getFilePathFromUrl(msg.getBmiddle_pic(), FileLocationMethod.picture_bmiddle);
        String largePath = FileManager.getFilePathFromUrl(msg.getOriginal_pic(), FileLocationMethod.picture_large);
        String data = "";
        if (new File(largePath).exists()) {
            data = msg.getOriginal_pic();
            method = FileLocationMethod.picture_large;
        } else if (new File(middlePath).exists()) {
            data = msg.getBmiddle_pic();
            method = FileLocationMethod.picture_bmiddle;
        } else {
            data = msg.getBmiddle_pic();
            method = FileLocationMethod.picture_bmiddle;
        }

        return ImageTool.getMiddlePictureInBrowserMSGActivity(data, method, downloadListener);

    }


    FileDownloaderHttpHelper.DownloadListener downloadListener = new FileDownloaderHttpHelper.DownloadListener() {
        @Override
        public void pushProgress(int progress, int max) {
            publishProgress(progress, max);
        }

        @Override
        public void completed() {

        }

        @Override
        public void cancel() {

        }
    };

    /**
     * sometime picture has been cached in sd card,so only set indeterminate equal false to show progress when downloading
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (pb != null) {
            if (pb.getVisibility() != View.VISIBLE) {
                pb.setVisibility(View.VISIBLE);
            }
            if (!pbFlag) {
                pb.setIndeterminate(false);
                pbFlag = true;
            }
            Integer progress = values[0];
            Integer max = values[1];
            pb.setMax(max);
            pb.setProgress(progress);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {

        if (pb != null)
            pb.setVisibility(View.INVISIBLE);

        super.onCancelled(bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {
            view.setTag(true);
            view.setVisibility(View.VISIBLE);
            view.setImageBitmap(bitmap);
            view.setAlpha(0.0f);
            view.animate().alpha(1.0f).setDuration(200);
        } else {
            view.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        if (pb != null) {
            pb.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    pb.setVisibility(View.INVISIBLE);
                }
            });
        }

    }

}