package com.example.dingyu.bean;

import com.example.dingyu.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-10-7
 */
public class AtUserBean {
    private String uid;
    private String nickname;
    private String remark;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
