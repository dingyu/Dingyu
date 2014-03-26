package com.example.dingyu.dao.maintimeline;

import com.example.dingyu.bean.CommentListBean;

import com.example.dingyu.support.error.WeiboException;

/**
 * User: qii
 * Date: 12-12-16
 */
public interface ICommentsTimeLineDao {
    public CommentListBean getGSONMsgList() throws WeiboException;

    public void setSince_id(String since_id);

    public void setMax_id(String max_id);
}
