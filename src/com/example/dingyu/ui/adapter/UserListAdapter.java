package com.example.dingyu.ui.adapter;

import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.dingyu.R;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.support.asyncdrawable.TimeLineBitmapDownloader;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.basefragment.AbstractUserListFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-19
 */
public class UserListAdapter extends BaseAdapter {

    protected List<UserBean> bean;
    protected Fragment activity;
    protected LayoutInflater inflater;
    protected ListView listView;
    protected TimeLineBitmapDownloader commander;
    protected int checkedBG;
    protected int defaultBG;

    public UserListAdapter(Fragment activity, TimeLineBitmapDownloader commander, List<UserBean> bean, ListView listView) {
        this.bean = bean;
        this.commander = commander;
        this.inflater = activity.getActivity().getLayoutInflater();
        this.listView = listView;
        this.activity = activity;

        defaultBG = activity.getResources().getColor(R.color.transparent);

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = activity.getActivity().obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                ViewHolder holder = (ViewHolder) view.getTag();
                if (holder == null)
                    return;
                holder.avatar.setImageBitmap(null);
            }
        });
    }

    private List<UserBean> getList() {
        return bean;
    }

    @Override
    public int getCount() {

        if (getList() != null && getList().size() > 0) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return getList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_listview_item_layout, parent, false);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.content = (TextView) convertView.findViewById(R.id.content);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        configViewFont(holder);
        configLayerType(holder);
        bindViewData(holder, position);


        return convertView;
    }

    private void bindViewData(ViewHolder holder, int position) {

        holder.listview_root.setBackgroundColor(defaultBG);

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);


        UserBean user = getList().get(position);


        holder.username.setText(user.getScreen_name());
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            boolean isFling = ((AbstractUserListFragment) activity).isListViewFling();
            commander.downloadAvatar(holder.avatar, user, isFling);
        }
        holder.content.setText(user.getDescription());

    }

    private void configLayerType(ViewHolder holder) {

        boolean disableHardAccelerated = SettingUtility.disableHardwareAccelerated();
        if (!disableHardAccelerated)
            return;

        int currentWidgetLayerType = holder.username.getLayerType();

        if (View.LAYER_TYPE_SOFTWARE != currentWidgetLayerType) {
            holder.username.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.content != null)
                holder.content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }

    }

    private void configViewFont(ViewHolder holder) {
        int prefFontSizeSp = SettingUtility.getFontSize();
        float currentWidgetTextSizePx;

        currentWidgetTextSizePx = holder.content.getTextSize();


        if (Utility.sp2px(prefFontSizeSp) != currentWidgetTextSizePx) {
            holder.content.setTextSize(prefFontSizeSp);
            holder.username.setTextSize(prefFontSizeSp);
        }
    }

    private class ViewHolder {
        TextView username;
        TextView content;
        ImageView avatar;
        RelativeLayout listview_root;
    }

    public void removeItem(UserBean item) {
        getList().remove(item);
        notifyDataSetChanged();
    }

    public void update(UserBean oldValue, UserBean newValue) {
        int index = getList().indexOf(oldValue);
        getList().remove(index);
        getList().add(index, newValue);
        notifyDataSetChanged();
    }
}