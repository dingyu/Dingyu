package com.example.dingyu.ui.adapter;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.example.dingyu.R;
import com.example.dingyu.bean.CommentBean;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.UserBean;

import com.example.dingyu.support.asyncdrawable.TimeLineBitmapDownloader;
import com.example.dingyu.support.lib.AutoScrollListView;
import com.example.dingyu.support.lib.TopTipBar;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.GlobalContext;
import com.example.dingyu.support.utils.Utility;
import com.example.dingyu.ui.send.WriteReplyToCommentActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: qii
 * Date: 12-9-8
 */
public class CommentListAdapter extends AbstractAppListAdapter<CommentBean> {

    private Drawable replyPic = null;
    private Drawable commentPic = null;

    private Map<ViewHolder, Drawable> bg = new WeakHashMap<ViewHolder, Drawable>();

    private TopTipBar topTipBar;

    private Handler handler = new Handler();

    private AbsListView.OnScrollListener onScrollListener;

    public CommentListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<CommentBean> bean, ListView listView, boolean showOriStatus) {
        this(fragment, commander, bean, listView, showOriStatus, false);
    }

    public CommentListAdapter(Fragment fragment, TimeLineBitmapDownloader commander, List<CommentBean> bean, ListView listView, boolean showOriStatus, boolean pref) {
        super(fragment, commander, bean, listView, showOriStatus, pref);

        int[] attrs = new int[]{R.attr.timeline_reply_flag};
        TypedArray ta = fragment.getActivity().obtainStyledAttributes(attrs);
        replyPic = ta.getDrawable(0);

        attrs = new int[]{R.attr.timeline_comment_flag};
        ta = fragment.getActivity().obtainStyledAttributes(attrs);
        commentPic = ta.getDrawable(0);
    }

    public void setTopTipBar(TopTipBar bar) {
        this.topTipBar = bar;
        AutoScrollListView autoScrollListView = (AutoScrollListView) listView;
        autoScrollListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                VelocityListView velocityListView = (VelocityListView) view;
//                if (velocityListView.getVelocity() < 0) {
//                    topTipBar.hideCount();
//                } else if (velocityListView.getVelocity() > 0) {
//                    if (topTipBar.getValues().size() == 0) {
//                        return;
//                    }

                View childView = Utility.getListViewItemViewFromPosition(listView, firstVisibleItem);

                if (childView == null) {
                    return;
                }

                int position = firstVisibleItem - ((ListView) view).getHeaderViewsCount();

                if (childView.getTop() == 0 && position <= 0) {
                    topTipBar.clearAndReset();
                } else {
                    handle(position + 1);
                }
            }
//            }

            private void handle(int position) {
                if (position > 0 && topTipBar != null && position < bean.size()) {
                    CommentBean next = bean.get(position);
                    if (next != null) {
                        CommentBean helperMsg = bean.get(position - 1);
                        long helperId = 0L;
                        if (helperMsg != null) {
                            helperId = helperMsg.getIdLong();
                        }
                        topTipBar.handle(next.getIdLong(), helperId);
                    }
                }

            }
        });
    }

    @Override
    protected void bindViewData(final ViewHolder holder, int position) {

        Drawable drawable = bg.get(holder);
        if (drawable != null) {
            holder.listview_root.setBackgroundDrawable(drawable);

        } else {
            drawable = holder.listview_root.getBackground();
            bg.put(holder, drawable);
        }

        if (listView.getCheckedItemPosition() == position + 1)
            holder.listview_root.setBackgroundColor(checkedBG);

        final CommentBean comment = getList().get(position);

        UserBean user = comment.getUser();
        if (user != null) {
            holder.username.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(user.getRemark())) {
                holder.username.setText(new StringBuilder(user.getScreen_name()).append("(").append(user.getRemark()).append(")").toString());
            } else {
                holder.username.setText(user.getScreen_name());
            }
            if (!showOriStatus && !SettingUtility.getEnableCommentRepostListAvatar()) {
                holder.avatar.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
            } else {
                buildAvatar(holder.avatar, position, user);
            }

        } else {
            holder.username.setVisibility(View.INVISIBLE);
            holder.avatar.setVisibility(View.INVISIBLE);
        }

        holder.content.setText(comment.getListViewSpannableString());

        holder.time.setTime(comment.getMills());
        if (holder.source != null)
            holder.source.setText(Html.fromHtml(comment.getSource()).toString());

        holder.repost_content.setVisibility(View.GONE);
        holder.repost_content_pic.setVisibility(View.GONE);

        CommentBean reply = comment.getReply_comment();
        if (holder.replyIV != null)
            holder.replyIV.setVisibility(View.GONE);
        if (reply != null && showOriStatus) {
            if (holder.repost_layout != null)
                holder.repost_layout.setVisibility(View.VISIBLE);
            holder.repost_flag.setVisibility(View.VISIBLE);
            holder.repost_content.setVisibility(View.VISIBLE);
            holder.repost_content.setText(reply.getListViewSpannableString());
        } else {

            MessageBean repost_msg = comment.getStatus();

            if (repost_msg != null && showOriStatus) {
                buildRepostContent(repost_msg, holder, position);
            } else {
                if (holder.repost_layout != null)
                    holder.repost_layout.setVisibility(View.GONE);
                holder.repost_flag.setVisibility(View.GONE);
                if (holder.replyIV != null) {
                    holder.replyIV.setVisibility(View.VISIBLE);
                    holder.replyIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), WriteReplyToCommentActivity.class);
                            intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                            intent.putExtra("msg", comment);
                            getActivity().startActivity(intent);
                        }
                    });
                }
            }

        }


    }

}


