package com.example.dingyu.dao.fav;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.FavBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: 
 * Date: 12-8-22
 */
public class FavDao {
    private String access_token;
    private String id;

    public FavDao(String token, String id) {
        this.access_token = token;
        this.id = id;
    }

    public FavBean favIt() throws WeiboException {

        String url = URLHelper.FAV_CREATE;
        return executeTask(url);
    }

    public FavBean unFavIt() throws WeiboException {

        String url = URLHelper.FAV_DESTROY;
        return executeTask(url);

    }

    private FavBean executeTask(String url) throws WeiboException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("id", id);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);

        try {
            FavBean value = new Gson().fromJson(jsonData, FavBean.class);
            if (value != null)
                return value;
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }

        return null;
    }
}
