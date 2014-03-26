package com.example.dingyu.dao.maintimeline;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.GroupListBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-10-17
 */
public class FriendGroupDao {


    public GroupListBean getGroup() throws WeiboException {

        String url = URLHelper.FRIENDSGROUP_INFO;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        GroupListBean value = null;
        try {
            value = gson.fromJson(jsonData, GroupListBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }


        return value;
    }


    public FriendGroupDao(String token) {
        this.access_token = token;
    }

    private String access_token;
}
