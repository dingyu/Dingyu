package com.example.dingyu.dao.user;

import com.example.dingyu.dao.URLHelper;
import com.example.dingyu.support.error.WeiboException;
import com.example.dingyu.support.http.HttpMethod;
import com.example.dingyu.support.http.HttpUtility;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.UserBean;

import java.util.HashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 12-10-1
 */
public class RemarkDao {

    public UserBean updateRemark() throws WeiboException {
        String url = URLHelper.REMARK_UPDATE;
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", access_token);
        map.put("uid", uid);
        map.put("remark", remark);

        String jsonData = HttpUtility.getInstance().executeNormalTask(HttpMethod.Post, url, map);


        Gson gson = new Gson();

        UserBean value = null;
        try {
            value = gson.fromJson(jsonData, UserBean.class);
        } catch (JsonSyntaxException e) {
            AppLogger.e(e.getMessage());
        }


        return value;

    }

    private String access_token;
    private String uid;
    private String remark;


    public RemarkDao (String access_token, String uid, String remark) {
        this.access_token = access_token;
        this.uid = uid;
        this.remark = remark;
    }

}
