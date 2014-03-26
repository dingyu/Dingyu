package com.example.dingyu.dao.maintimeline;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.dao.unread.ClearUnreadDao;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.example.dingyu.support.utils.TimeTool;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.MessageListBean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: qii
 * Date: 12-7-28
 */
public class MainMentionsTimeLineDao {

    private String getMsgListJson() throws WeiboException {
        String url = URLHelper.STATUSES_MENTIONS_TIMELINE;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("filter_by_author", filter_by_author);
        map.put("filter_by_source", filter_by_source);
        map.put("filter_by_type", filter_by_type);
        map.put("trim_user", trim_user);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        return jsonData;
    }

    public MessageListBean getGSONMsgListWithoutClearUnread() throws WeiboException {

        String json = getMsgListJson();
        Gson gson = new Gson();

        MessageListBean value = null;
        try {
            value = gson.fromJson(json, MessageListBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }

        /**
         * sometime sina weibo may delete message,so data don't have any user information
         */
        if (value != null && value.getItemList().size() > 0) {
            List<MessageBean> msgList = value.getItemList();
            Iterator<MessageBean> iterator = msgList.iterator();
            while (iterator.hasNext()) {

                MessageBean msg = iterator.next();
                if (msg.getUser() == null) {
                    iterator.remove();
                } else {
                    msg.getListViewSpannableString();
                    TimeTool.dealMills(msg);
                }
            }

        }

        return value;
    }

    public MessageListBean getGSONMsgList() throws WeiboException {

        MessageListBean value = getGSONMsgListWithoutClearUnread();

        try {
            new ClearUnreadDao(access_token, ClearUnreadDao.MENTION_STATUS).clearUnread();
        } catch (WeiboException ignored) {

        }

        return value;
    }


    private String access_token;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String filter_by_author;
    private String filter_by_source;
    private String filter_by_type;
    private String trim_user;

    public MainMentionsTimeLineDao(String access_token) {
        this.access_token = access_token;
        this.count = SettingUtility.getMsgCount();
    }

    public MainMentionsTimeLineDao setSince_id(String since_id) {
        this.since_id = since_id;
        return this;
    }

    public MainMentionsTimeLineDao setMax_id(String max_id) {
        this.max_id = max_id;
        return this;
    }

    public MainMentionsTimeLineDao setCount(String count) {
        this.count = count;
        return this;
    }

    public MainMentionsTimeLineDao setPage(String page) {
        this.page = page;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_author(String filter_by_author) {
        this.filter_by_author = filter_by_author;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_source(String filter_by_source) {
        this.filter_by_source = filter_by_source;
        return this;
    }

    public MainMentionsTimeLineDao setFilter_by_type(String filter_by_type) {
        this.filter_by_type = filter_by_type;
        return this;
    }

    public MainMentionsTimeLineDao setTrim_user(String trim_user) {
        this.trim_user = trim_user;
        return this;
    }
}
