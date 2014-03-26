package com.example.dingyu.dao.user;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.example.dingyu.support.utils.ListViewTool;
import com.example.dingyu.support.utils.TimeTool;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.MessageListBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-16
 */
public class StatusesTimeLineDao {

    public MessageListBean getGSONMsgList() throws WeiboException {

        String url = URLHelper.STATUSES_TIMELINE_BY_ID;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);
        map.put("since_id", since_id);
        map.put("max_id", max_id);
        map.put("count", count);
        map.put("page", page);
        map.put("screen_name", screen_name);
        map.put("base_app", base_app);
        map.put("feature", feature);
        map.put("trim_user", trim_user);


        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);


        Gson gson = new Gson();

        MessageListBean value = null;
        try {
            value = gson.fromJson(jsonData, MessageListBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        if (value != null && value.getSize() > 0) {
            for (MessageBean b : value.getItemList()) {
                TimeTool.dealMills(b);
                ListViewTool.addJustHighLightLinks(b);

            }
        }

        return value;
    }


    public StatusesTimeLineDao(String token, String uid) {
        this.access_token = token;
        this.uid = uid;
        this.count = SettingUtility.getMsgCount();
    }

    public void setScreen_name(String screen_name) {
        this.screen_name = screen_name;
    }

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setBase_app(String base_app) {
        this.base_app = base_app;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public void setTrim_user(String trim_user) {
        this.trim_user = trim_user;
    }

    private String access_token;
    private String uid;
    private String screen_name;
    private String since_id;
    private String max_id;
    private String count;
    private String page;
    private String base_app;
    private String feature;
    private String trim_user;
}
