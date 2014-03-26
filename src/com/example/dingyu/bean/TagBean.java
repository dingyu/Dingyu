package com.example.dingyu.bean;

import com.example.dingyu.support.utils.ObjectToStringUtility;

/**
 * User: qii
 * Date: 12-8-5
 */


public class TagBean {

    private int id;
    private String name;

    private String weight;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
