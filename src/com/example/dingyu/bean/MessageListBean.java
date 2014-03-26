package com.example.dingyu.bean;

import android.text.TextUtils;

import com.example.dingyu.support.settinghelper.SettingUtility;
import com.example.dingyu.support.utils.ObjectToStringUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MessageListBean extends ListBean<MessageBean, MessageListBean> {

    private List<MessageBean> statuses = new ArrayList<MessageBean>();


    private List<MessageBean> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<MessageBean> statuses) {
        this.statuses = statuses;
    }


    @Override
    public int getSize() {
        return statuses.size();
    }

    @Override
    public MessageBean getItem(int position) {
        return getStatuses().get(position);
    }

    @Override
    public List<MessageBean> getItemList() {
        return getStatuses();
    }

    private int removedCount = 0;

    public int getReceivedNumber() {
        return getSize() + removedCount;
    }

    public void removedCountPlus() {
        removedCount++;
    }

    @Override
    public void addNewData(MessageListBean newValue) {

        if (newValue == null || newValue.getSize() == 0) {
            return;
        }

        boolean receivedCountBelowRequestCount = newValue.getReceivedNumber() < Integer.valueOf(SettingUtility.getMsgCount());
        boolean receivedCountEqualRequestCount = newValue.getReceivedNumber() == Integer.valueOf(SettingUtility.getMsgCount());
        if (receivedCountEqualRequestCount && this.getSize() > 0) {
            newValue.getItemList().add(null);
        }
        this.getItemList().addAll(0, newValue.getItemList());
        this.setTotal_number(newValue.getTotal_number());
    }

    @Override
    public void addOldData(MessageListBean oldValue) {
        if (oldValue != null && oldValue.getSize() > 1) {
            getItemList().addAll(oldValue.getItemList().subList(1, oldValue.getSize()));
            setTotal_number(oldValue.getTotal_number());

        }
    }

    public void addMiddleData(int position, MessageListBean middleValue, boolean towardsBottom) {
        if (middleValue == null)
            return;

        if (middleValue.getSize() == 0 || middleValue.getSize() == 1) {
            getItemList().remove(position);
            return;
        }

        List<MessageBean> middleData = middleValue.getItemList().subList(1, middleValue.getSize());

        String beginId = getItem(position + 1).getId();
        String endId = getItem(position - 1).getId();
        Iterator<MessageBean> iterator = middleData.iterator();
        while (iterator.hasNext()) {
            MessageBean msg = iterator.next();
            boolean notNull = !TextUtils.isEmpty(msg.getId());
            if (notNull) {
                if (msg.getId().equals(beginId) || msg.getId().equals(endId)) {
                    iterator.remove();
                }
            }
        }

        getItemList().addAll(position, middleData);

    }

    public void replaceData(MessageListBean value) {
        if (value == null)
            return;
        getItemList().clear();
        getItemList().addAll(value.getItemList());
        setTotal_number(value.getTotal_number());
    }

    public MessageListBean copy() {
        MessageListBean object = new MessageListBean();
        object.replaceData(MessageListBean.this);
        return object;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}