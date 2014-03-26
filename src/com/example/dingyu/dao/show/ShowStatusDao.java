package com.example.dingyu.dao.show;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.MessageBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-8-7
 */
public class ShowStatusDao {

    private String access_token;
    private String id;

    public ShowStatusDao(String access_token, String id) {

        this.access_token = access_token;
        this.id = id;
    }

    public MessageBean getMsg() throws WeiboException {

        String url = URLHelper.STATUSES_SHOW;

        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);

        String json = HttpUtility.getInstance().executeNormalTask(HttpMethod.Get, url, map);

        Gson gson = new Gson();

        MessageBean value = null;
        try {
            value = gson.fromJson(json, MessageBean.class);
        } catch (JsonSyntaxException e) {

            AppLogger.e(e.getMessage());
        }

        return value;

    }
}
