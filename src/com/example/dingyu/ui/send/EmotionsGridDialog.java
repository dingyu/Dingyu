package com.example.dingyu.ui.send;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.example.dingyu.R;

import com.example.dingyu.support.utils.GlobalContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: qii
 * Date: 12-9-25
 */
public class EmotionsGridDialog extends DialogFragment {
    Map<String, Bitmap> emotions;
    List<String> index = new ArrayList<String>();

    public static interface IEmotions {
        public void insertEmotion(String emotionStr);

        public Map<String, Bitmap> getEmotionsPic();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        emotions = GlobalContext.getInstance().getEmotionsPics();
        index.addAll(emotions.keySet());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        emotions = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.emotions_fragment_gridview_layout, null);
        GridView gridview = (GridView) view.findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(getActivity()));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((IEmotions) getActivity()).insertEmotion(index.get(position));
                dismiss();
            }
        });

        builder.setView(view);

        return builder.create();
    }


    class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return emotions.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }


            imageView.setImageBitmap(emotions.get(index.get(position)));
            return imageView;
        }


    }
}


