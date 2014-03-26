package com.example.dingyu.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.example.dingyu.R;
import com.example.dingyu.bean.ItemBean;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.support.asyncdrawable.PictureBitmapDrawable;
import com.example.dingyu.support.asyncdrawable.TimeLineBitmapDownloader;
import com.example.dingyu.support.lib.*;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.basefragment.AbstractTimeLineFragment;
import com.example.dingyu.ui.browser.BrowserBigPicActivity;
import com.example.dingyu.ui.userinfo.UserInfoActivity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 12-9-15
 */
public abstract class AbstractAppListAdapter<T extends ItemBean> extends BaseAdapter {
    protected List<T> bean;
    protected Fragment fragment;
    protected LayoutInflater inflater;
    protected ListView listView;
    protected TimeLineBitmapDownloader commander;
    protected boolean showOriStatus = true;
    protected int checkedBG;
    protected int defaultBG;

//    private final int TYPE_NORMAL = 0;
//    private final int TYPE_MYSELF = 1;
//    private final int TYPE_NORMAL_BIG_PIC = 2;
//    private final int TYPE_MYSELF_BIG_PIC = 3;
//    private final int TYPE_MIDDLE = 4;
//    private final int TYPE_SIMPLE = 5;

    private final int TYPE_NORMAL = 0;
    private final int TYPE_NORMAL_BIG_PIC = 1;
    private final int TYPE_MIDDLE = 2;
    private final int TYPE_SIMPLE = 3;

    public static final int NO_ITEM_ID = -1;

    private Set<Integer> tagIndexList = new HashSet<Integer>();

    private static final int PREF_LISTVIEW_ITEM_VIEW_COUNT = 6;
    private ArrayDeque<PrefView> prefNormalViews = new ArrayDeque<PrefView>(PREF_LISTVIEW_ITEM_VIEW_COUNT);
    private ArrayDeque<PrefView> prefBigPicViews = new ArrayDeque<PrefView>(PREF_LISTVIEW_ITEM_VIEW_COUNT);

    private int savedCurrentMiddleLoadingViewPosition = AbstractTimeLineFragment.NO_SAVED_CURRENT_LOADING_MSG_VIEW_POSITION;

    private class PrefView {
        View view;
        ViewHolder holder;
    }

    public void setSavedMiddleLoadingViewPosition(int position) {
        savedCurrentMiddleLoadingViewPosition = position;
    }

    public AbstractAppListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<T> bean, ListView listView, boolean showOriStatus) {
        this(fragment, commander, bean, listView, showOriStatus, false);
    }

    public AbstractAppListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<T> bean, ListView listView, boolean showOriStatus, boolean pre) {
        if (showOriStatus && SettingUtility.getAppTheme() == R.style.AppTheme_Four)
            listView.setDivider(null);

        this.bean = bean;
        this.commander = commander;
        this.inflater = fragment.getActivity().getLayoutInflater();
        this.listView = listView;
        this.showOriStatus = showOriStatus;
        this.fragment = fragment;


        defaultBG = fragment.getResources().getColor(R.color.transparent);

        int[] attrs = new int[]{R.attr.listview_checked_color};
        TypedArray ta = fragment.getActivity().obtainStyledAttributes(attrs);
        checkedBG = ta.getColor(0, 430);

        if (pre) {
            for (int i = 0; i < PREF_LISTVIEW_ITEM_VIEW_COUNT; i++) {
                PrefView prefView = new PrefView();
                prefView.view = initNormalLayout(null);
                prefView.holder = buildHolder(prefView.view);
                prefNormalViews.add(prefView);
            }

            for (int i = 0; i < PREF_LISTVIEW_ITEM_VIEW_COUNT; i++) {
                PrefView prefView = new PrefView();
                prefView.view = initBigPicLayout(null);
                prefView.holder = buildHolder(prefView.view);
                prefBigPicViews.add(prefView);
            }
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                Integer index = (Integer) view.getTag(R.string.listview_index_tag);
                if (index == null)
                    return;

                for (Integer tag : tagIndexList) {

                    ViewHolder holder = (ViewHolder) view.getTag(tag);

                    if (holder != null) {
                        Drawable drawable = holder.avatar.getImageView().getDrawable();
                        clearAvatarBitmap(holder, drawable);
                        drawable = holder.content_pic.getImageView().getDrawable();
                        clearPictureBitmap(holder, drawable);
                        drawable = holder.repost_content_pic.getImageView().getDrawable();
                        clearRepostPictureBitmap(holder, drawable);

                        if (!tag.equals(index)) {
                            holder.listview_root.removeAllViewsInLayout();
                            holder.listview_root = null;
                            view.setTag(tag, null);
                        }
                    }
                }
            }

            void clearAvatarBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    drawable.setCallback(null);
                    holder.avatar.setImageBitmap(null);
                    holder.avatar.getImageView().clearAnimation();
                }
            }

            void clearPictureBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    drawable.setCallback(null);
                    holder.content_pic.setImageBitmap(null);
                    holder.content_pic.getImageView().clearAnimation();
                }
            }

            void clearRepostPictureBitmap(ViewHolder holder, Drawable drawable) {
                if (!(drawable instanceof PictureBitmapDrawable)) {
                    drawable.setCallback(null);
                    holder.repost_content_pic.setImageBitmap(null);
                    holder.repost_content_pic.getImageView().clearAnimation();
                }
            }
        });
    }


    protected Activity getActivity() {
        return fragment.getActivity();
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {

        if (position >= bean.size())
            return -1;

        if (bean.get(position) == null)
            return TYPE_MIDDLE;

        if (!showOriStatus)
            return TYPE_SIMPLE;

        if (SettingUtility.getEnableBigPic())
            return TYPE_NORMAL_BIG_PIC;
        else
            return TYPE_NORMAL;

    }


    /**
     * use getTag(int) and setTag(int, final Object) to solve getItemViewType(int) bug.
     * When you use getItemViewType(int),getTag(),setTag() together, if getItemViewType(int) change because
     * network switch to use another layout when you are scrolling listview, bug appears,the other listviews in other tabs
     * (Actionbar tab navigation) will mix several layout up, for example, the correct layout should be TYPE_NORMAL_BIG_PIC,
     * but in the listview, you can see some row's layouts are TYPE_NORMAL, some are TYPE_NORMAL_BIG_PIC. if you print
     * getItemViewType(int) value to the console,their are same type
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PrefView prefView = null;

        if (convertView == null || convertView.getTag(R.drawable.ic_launcher + getItemViewType(position)) == null) {

            switch (getItemViewType(position)) {
                case TYPE_SIMPLE:
                    convertView = initSimpleLayout(parent);
                    break;
                case TYPE_MIDDLE:
                    convertView = initMiddleLayout(parent);
                    break;
//                case TYPE_MYSELF:
//                    convertView = initMylayout(parent);
//                    break;
//                case TYPE_MYSELF_BIG_PIC:
//                    convertView = initMylayout(parent);
//                    break;
                case TYPE_NORMAL:
                    prefView = prefNormalViews.poll();
                    if (prefView != null) {
                        convertView = prefView.view;
                    }
                    if (convertView == null) {
                        convertView = initNormalLayout(parent);
                    }
                    break;
                case TYPE_NORMAL_BIG_PIC:
                    prefView = prefBigPicViews.poll();
                    if (prefView != null) {
                        convertView = prefView.view;
                    }
                    if (convertView == null) {
                        convertView = initBigPicLayout(parent);
                    }
                    break;
                default:
                    convertView = initNormalLayout(parent);
                    break;
            }
            if (getItemViewType(position) != TYPE_MIDDLE) {
                if (prefView == null) {
                    holder = buildHolder(convertView);
                } else {
                    holder = prefView.holder;
                }
                convertView.setTag(R.drawable.ic_launcher + getItemViewType(position), holder);
                convertView.setTag(R.string.listview_index_tag, R.drawable.ic_launcher + getItemViewType(position));
                tagIndexList.add(R.drawable.ic_launcher + getItemViewType(position));
            }

        } else {
            holder = (ViewHolder) convertView.getTag(R.drawable.ic_launcher + getItemViewType(position));
        }


        if (getItemViewType(position) != TYPE_MIDDLE) {
            configLayerType(holder);
            configViewFont(holder);
            bindViewData(holder, position);
            bindOnTouchListener(holder);
        } else {
            if (savedCurrentMiddleLoadingViewPosition == position + listView.getHeaderViewsCount()) {
                ListViewMiddleMsgLoadingView loadingView = (ListViewMiddleMsgLoadingView) convertView;
                loadingView.load();
            }
        }
        return convertView;
    }

    private void bindOnTouchListener(ViewHolder holder) {
        holder.listview_root.setClickable(false);
        holder.username.setClickable(false);
        holder.time.setClickable(false);
        holder.content.setClickable(false);
        holder.repost_content.setClickable(false);

        if (holder.content != null)
            holder.content.setOnTouchListener(onTouchListener);
        if (holder.repost_content != null)
            holder.repost_content.setOnTouchListener(onTouchListener);
    }

    private View initMiddleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater.inflate(R.layout.timeline_listview_item_middle_layout, parent, false);

        return convertView;
    }

    private View initSimpleLayout(ViewGroup parent) {
        View convertView;
        convertView = inflater.inflate(R.layout.timeline_listview_item_simple_layout, parent, false);

        return convertView;
    }

    private View initMylayout(ViewGroup parent) {
        View convertView;
        if (SettingUtility.getEnableBigPic()) {
            convertView = inflater.inflate(R.layout.timeline_listview_item_big_pic_layout, parent, false);
        } else {
            convertView = inflater.inflate(R.layout.timeline_listview_item_layout, parent, false);
        }
        return convertView;
    }

    private View initNormalLayout(ViewGroup parent) {
        return inflater.inflate(R.layout.timeline_listview_item_layout, parent, false);
    }

    private View initBigPicLayout(ViewGroup parent) {
        return inflater.inflate(R.layout.timeline_listview_item_big_pic_layout, parent, false);
    }


    private ViewHolder buildHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.username = (TextView) convertView.findViewById(R.id.username);
        TextPaint tp = holder.username.getPaint();
        tp.setFakeBoldText(true);
        holder.content = (TextView) convertView.findViewById(R.id.content);
        holder.repost_content = (TextView) convertView.findViewById(R.id.repost_content);
        holder.time = (TimeTextView) convertView.findViewById(R.id.time);
        holder.avatar = (TimeLineAvatarImageView) convertView.findViewById(R.id.avatar);
        holder.content_pic = (TimeLineImageView) convertView.findViewById(R.id.content_pic);
        holder.repost_content_pic = (TimeLineImageView) convertView.findViewById(R.id.repost_content_pic);
        holder.listview_root = (RelativeLayout) convertView.findViewById(R.id.listview_root);
        holder.repost_layout = convertView.findViewById(R.id.repost_layout);
        holder.repost_flag = (ImageView) convertView.findViewById(R.id.repost_flag);
        holder.count_layout = (LinearLayout) convertView.findViewById(R.id.count_layout);
        holder.repost_count = (TextView) convertView.findViewById(R.id.repost_count);
        holder.comment_count = (TextView) convertView.findViewById(R.id.comment_count);
        holder.timeline_gps = (ImageView) convertView.findViewById(R.id.timeline_gps_iv);
        holder.timeline_pic = (ImageView) convertView.findViewById(R.id.timeline_pic_iv);
        holder.replyIV = (ImageView) convertView.findViewById(R.id.replyIV);
        holder.source = (TextView) convertView.findViewById(R.id.source);
        return holder;
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
            if (holder.repost_content != null)
                holder.repost_content.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.time != null)
                holder.time.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.repost_count != null)
                holder.repost_count.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            if (holder.comment_count != null)
                holder.comment_count.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

    }

    private void configViewFont(ViewHolder holder) {
        int prefFontSizeSp = SettingUtility.getFontSize();
        float currentWidgetTextSizePx;

        currentWidgetTextSizePx = holder.time.getTextSize();

        if (Utility.sp2px(prefFontSizeSp - 3) != currentWidgetTextSizePx) {
            holder.time.setTextSize(prefFontSizeSp - 3);
            if (holder.source != null)
                holder.source.setTextSize(prefFontSizeSp - 3);
        }

        currentWidgetTextSizePx = holder.content.getTextSize();


        if (Utility.sp2px(prefFontSizeSp) != currentWidgetTextSizePx) {
            holder.content.setTextSize(prefFontSizeSp);
            holder.username.setTextSize(prefFontSizeSp);
            holder.repost_content.setTextSize(prefFontSizeSp);

        }

        if (holder.repost_count != null) {
            currentWidgetTextSizePx = holder.repost_count.getTextSize();
            if (Utility.sp2px(prefFontSizeSp - 5) != currentWidgetTextSizePx) {
                holder.repost_count.setTextSize(prefFontSizeSp - 5);
            }
        }

        if (holder.comment_count != null) {
            currentWidgetTextSizePx = holder.comment_count.getTextSize();
            if (Utility.sp2px(prefFontSizeSp - 5) != currentWidgetTextSizePx) {
                holder.comment_count.setTextSize(prefFontSizeSp - 5);
            }
        }
    }

    protected abstract void bindViewData(ViewHolder holder, int position);

    protected List<T> getList() {
        return bean;
    }

    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {

        if (getList() != null) {
            return getList().size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && getList() != null && getList().size() > 0 && position < getList().size())
            return getList().get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getList() != null && getList().get(position) != null && getList().size() > 0 && position < getList().size())
            return Long.valueOf(getList().get(position).getId());
        else
            return NO_ITEM_ID;
    }

    protected void buildAvatar(TimeLineAvatarImageView view, int position, final UserBean user) {
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", user);
                getActivity().startActivity(intent);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                UserDialog dialog = new UserDialog(user);
                dialog.show(fragment.getFragmentManager(), "");
                return true;
            }
        });
        if (user.isVerified()) {
            view.isVerified();
        } else {
            view.reset();
        }
        buildAvatar(view.getImageView(), position, user);
    }

    protected void buildAvatar(ImageView view, int position, final UserBean user) {
        String image_url = user.getProfile_image_url();
        if (!TextUtils.isEmpty(image_url)) {
            view.setVisibility(View.VISIBLE);
            commander.downloadAvatar(view, user, (AbstractTimeLineFragment) fragment);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    protected void buildPic(final MessageBean msg, TimeLineImageView view, int position) {
        if (SettingUtility.isEnablePic()) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), BrowserBigPicActivity.class);
                    intent.putExtra("msg", msg);
                    getActivity().startActivity(intent);
                }
            });
            buildPic(msg, view);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void buildPic(final MessageBean msg, ImageView view) {
        view.setVisibility(View.VISIBLE);
        commander.downContentPic(view, msg, (AbstractTimeLineFragment) fragment);
    }

    private void buildPic(final MessageBean msg, TimeLineImageView view) {
        view.setVisibility(View.VISIBLE);
        commander.downContentPic(view, msg, (AbstractTimeLineFragment) fragment);
    }

    protected void buildRepostContent(final MessageBean repost_msg, ViewHolder holder, int position) {
        holder.repost_content.setVisibility(View.VISIBLE);
        if (!repost_msg.getId().equals((String) holder.repost_content.getTag())) {
            holder.repost_content.setText(repost_msg.getListViewSpannableString());
            holder.repost_content.setTag(repost_msg.getId());
        }

        if (!TextUtils.isEmpty(repost_msg.getBmiddle_pic())) {
            holder.repost_content_pic.setVisibility(View.VISIBLE);
            buildPic(repost_msg, holder.repost_content_pic, position);
        }
    }


    public static class ViewHolder {
        TextView username;
        TextView content;
        TextView repost_content;
        TimeTextView time;
        TimeLineAvatarImageView avatar;
        TimeLineImageView content_pic;
        TimeLineImageView repost_content_pic;
        RelativeLayout listview_root;
        View repost_layout;
        ImageView repost_flag;
        LinearLayout count_layout;
        TextView repost_count;
        TextView comment_count;
        TextView source;
        ImageView timeline_gps;
        ImageView timeline_pic;
        ImageView replyIV;
    }

    public void removeItem(final int postion) {
        if (postion >= 0 && postion < bean.size()) {
            AppLogger.e("1");
            Animation anim = AnimationUtils.loadAnimation(
                    fragment.getActivity(), R.anim.account_delete_slide_out_right
            );

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                    AppLogger.e("4");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bean.remove(postion);
                    AbstractAppListAdapter.this.notifyDataSetChanged();
                    AppLogger.e("5");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            int positonInListView = postion + 1;
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition();

            if (positonInListView >= start && positonInListView <= end) {
                int positionInCurrentScreen = postion - start;
                listView.getChildAt(positionInCurrentScreen + 1).startAnimation(anim);
                AppLogger.e("2");
            } else {
                bean.remove(postion);
                AbstractAppListAdapter.this.notifyDataSetChanged();
                AppLogger.e("3");
            }

        }
    }

    //onTouchListener has some strange problem, when user click link, holder.listview_root may also receive a MotionEvent.ACTION_DOWN event
    //the background then changed
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            ViewHolder holder = getViewHolderByView(v);

            if (holder == null) {
                return false;
            }

            Layout layout = ((TextView) v).getLayout();

            int x = (int) event.getX();
            int y = (int) event.getY();
            int offset = 0;
            if (layout != null) {

                int line = layout.getLineForVertical(y);
                offset = layout.getOffsetForHorizontal(line, x);
            }

            TextView tv = (TextView) v;
            SpannableString value = SpannableString.valueOf(tv.getText());
            MyURLSpan[] urlSpans = value.getSpans(0, value.length(), MyURLSpan.class);
            boolean result = false;
            for (MyURLSpan urlSpan : urlSpans) {
                int start = value.getSpanStart(urlSpan);
                int end = value.getSpanEnd(urlSpan);
                if (start <= offset && offset <= end) {
                    result = true;
                    break;
                }
            }

            boolean hasActionMode = ((AbstractTimeLineFragment) fragment).hasActionMode();
            if (result && !hasActionMode) {
                return LongClickableLinkMovementMethod.getInstance().onTouchEvent(tv, value, event);
            } else {
                return false;
            }

        }
    };


    //when view is recycled by listview, need to catch exception
    private ViewHolder getViewHolderByView(View view) {
        try {
            final int position = listView.getPositionForView(view);
            if (position == ListView.INVALID_POSITION) {
                return null;
            }
            return getViewHolderByView(position);
        } catch (NullPointerException e) {

        }
        return null;
    }

    private ViewHolder getViewHolderByView(int position) {

        int wantedPosition = position - 1;
        int firstPosition = listView.getFirstVisiblePosition() - listView.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;

        if (wantedChild < 0 || wantedChild >= listView.getChildCount()) {
            return null;
        }

        View wantedView = listView.getChildAt(wantedChild);
        ViewHolder holder = (ViewHolder) wantedView.getTag(R.drawable.ic_launcher + getItemViewType(position));
        return holder;

    }

}
