package com.example.dingyu.dao.group;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.GroupBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 13-2-15
 * http://open.weibo.com/wiki/2/friendships/groups/create
 */
public class CreateGroupDao {

    public GroupBean create() throws WeiboException {

        String url = URLHelper.GROUP_CREATE;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("name", name);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

        Gson gson = new Gson();

        GroupBean value = null;
        try {
            value = gson.fromJson(jsonData, GroupBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }


        return value;
    }


    public CreateGroupDao(String token, String name) {
        this.access_token = token;
        this.name = name;
    }

    private String access_token;
    private String name;


}
