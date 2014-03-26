package com.example.dingyu.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.dingyu.bean.android.MessageTimeLineData;
import com.example.dingyu.bean.android.TimeLinePosition;
import com.example.dingyu.support.database.table.HomeTable;
import com.example.dingyu.support.utils.AppConfig;
import com.example.dingyu.support.utils.AppLogger;
import com.example.dingyu.support.utils.GlobalContext;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.GroupBean;
import com.example.dingyu.bean.GroupListBean;
import com.example.dingyu.bean.MessageBean;
import com.example.dingyu.bean.MessageListBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-7
 */
public class FriendsTimeLineDBTask {

    private FriendsTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    private static void addHomeLineMsg(MessageListBean list, String accountId) {

        if (list == null || list.getSize() == 0) {
            return;
        }

        Gson gson = new Gson();
        List<MessageBean> msgList = list.getItemList();
        DatabaseUtils.InsertHelper ih = new DatabaseUtils.InsertHelper(getWsd(), HomeTable.HomeDataTable.TABLE_NAME);
        final int mblogidColumn = ih.getColumnIndex(HomeTable.HomeDataTable.MBLOGID);
        final int accountidColumn = ih.getColumnIndex(HomeTable.HomeDataTable.ACCOUNTID);
        final int jsondataColumn = ih.getColumnIndex(HomeTable.HomeDataTable.JSONDATA);
        try {
            getWsd().beginTransaction();
            for (int i = 0; i < msgList.size(); i++) {
                MessageBean msg = msgList.get(i);
                ih.prepareForInsert();
                if (msg != null) {
                    ih.bind(mblogidColumn, msg.getId());
                    ih.bind(accountidColumn, accountId);
                    String json = gson.toJson(msg);
                    ih.bind(jsondataColumn, json);
                } else {
                    ih.bind(mblogidColumn, "-1");
                    ih.bind(accountidColumn, accountId);
                    ih.bind(jsondataColumn, "");
                }
                ih.execute();
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
            ih.close();
        }
        reduceHomeTable(accountId);
    }

    private static void reduceHomeTable(String accountId) {
        String searchCount = "select count(" + HomeTable.HomeDataTable.ID + ") as total" + " from " + HomeTable.HomeDataTable.TABLE_NAME + " where " + HomeTable.HomeDataTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getWsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - AppConfig.DEFAULT_HOME_DB_CACHE_COUNT;

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + HomeTable.HomeDataTable.TABLE_NAME + " where " + HomeTable.HomeDataTable.ID + " in "
                    + "( select " + HomeTable.HomeDataTable.ID + " from " + HomeTable.HomeDataTable.TABLE_NAME + " where "
                    + HomeTable.HomeDataTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + HomeTable.HomeDataTable.ID + " desc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private static void replace(MessageListBean list, String accountId, String groupId) {
        if (groupId.equals("0")) {
            deleteAllHomes(accountId);
            addHomeLineMsg(list, accountId);
        } else {
            HomeOtherGroupTimeLineDBTask.replace(list, accountId, groupId);
        }
    }

    public static void asyncReplace(final MessageListBean list, final String accountId, final String groupId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                replace(list, accountId, groupId);
            }
        }).start();

    }

    public static void asyncUpdatePosition(final TimeLinePosition position, final String accountId, final String groupId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FriendsTimeLineDBTask.updatePosition(position, accountId, groupId);
            }
        };

        new Thread(runnable).start();
    }

    private static void updatePosition(TimeLinePosition position, String accountId, String groupId) {
        if (groupId.equals("0")) {
            updatePosition(position, accountId);
        } else {
            HomeOtherGroupTimeLineDBTask.updatePosition(position, GlobalContext.getInstance().getCurrentAccountId(), groupId);
        }
    }


    private static void updatePosition(TimeLinePosition position, String accountId) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(HomeTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(HomeTable.TABLE_NAME, cv, HomeTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(HomeTable.ACCOUNTID, accountId);
            cv.put(HomeTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(HomeTable.TABLE_NAME,
                    HomeTable.ID, cv);
        }
    }

    private static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeTable.TIMELINEDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    TimeLinePosition value = gson.fromJson(json, TimeLinePosition.class);
                    return value;

                } catch (JsonSyntaxException e) {

                }
            }

        }
        c.close();
        return new TimeLinePosition(0, 0);
    }

    public static String getRecentGroupId(String accountId) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeTable.RECENT_GROUP_ID));
            if (!TextUtils.isEmpty(id)) {
                return id;
            }

        }
        c.close();
        return "0";
    }

    public static void asyncUpdateRecentGroupId(String accountId, final String groupId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FriendsTimeLineDBTask.updateRecentGroupId(GlobalContext.getInstance().getCurrentAccountId(), groupId);
            }
        };

        new Thread(runnable).start();
    }

    private static void updateRecentGroupId(String accountId, String groupId) {

        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(HomeTable.RECENT_GROUP_ID, groupId);
                getWsd().update(HomeTable.TABLE_NAME, cv, HomeTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(HomeTable.ACCOUNTID, accountId);
            cv.put(HomeTable.RECENT_GROUP_ID, groupId);
            getWsd().insert(HomeTable.TABLE_NAME,
                    HomeTable.ID, cv);
        }
    }

    static void deleteAllHomes(String accountId) {
        String sql = "delete from " + HomeTable.HomeDataTable.TABLE_NAME + " where " + HomeTable.HomeDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static void asyncUpdateCount(final String msgId, final int commentCount, final int repostCount) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                FriendsTimeLineDBTask.updateCount(msgId, commentCount, repostCount);
                HomeOtherGroupTimeLineDBTask.updateCount(msgId, commentCount, repostCount);
            }
        }).start();

    }

    private static void updateCount(String msgId, int commentCount, int repostCount) {
        String sql = "select * from " + HomeTable.HomeDataTable.TABLE_NAME + " where " + HomeTable.HomeDataTable.MBLOGID + "  = "
                + msgId + " order by "
                + HomeTable.HomeDataTable.ID + " asc limit 50";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeTable.HomeDataTable.ID));
            String json = c.getString(c.getColumnIndex(HomeTable.HomeDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.setComments_count(commentCount);
                    value.setReposts_count(repostCount);
                    String[] args = {id};
                    ContentValues cv = new ContentValues();
                    cv.put(HomeTable.HomeDataTable.JSONDATA, gson.toJson(value));
                    getWsd().update(HomeTable.HomeDataTable.TABLE_NAME, cv, HomeTable.HomeDataTable.ID + "=?", args);
                } catch (JsonSyntaxException e) {

                }

            }
        }
        c.close();
    }

    public static MessageTimeLineData getRecentGroupData(String accountId) {
        String groupId = getRecentGroupId(accountId);
        MessageListBean msgList;
        TimeLinePosition position;
        if (groupId.equals("0")) {
            msgList = getHomeLineMsgList(accountId);
            position = getPosition(accountId);
        } else {
            msgList = HomeOtherGroupTimeLineDBTask.get(accountId, groupId);
            position = HomeOtherGroupTimeLineDBTask.getPosition(accountId, groupId);
        }

        return new MessageTimeLineData(groupId, msgList, position);
    }

    public static List<MessageTimeLineData> getOtherGroupData(String accountId, String exceptGroupId) {
        List<MessageTimeLineData> data = new ArrayList<MessageTimeLineData>();

        MessageListBean msgList = getHomeLineMsgList(accountId);
        MessageTimeLineData home = new MessageTimeLineData("0", msgList, getPosition(accountId));
        data.add(home);

        MessageTimeLineData biGroup = HomeOtherGroupTimeLineDBTask.getTimeLineData(accountId, "1");
        data.add(biGroup);

        GroupListBean groupListBean = GroupDBTask.get(accountId);

        if (groupListBean != null) {
            List<GroupBean> lists = groupListBean.getLists();
            for (GroupBean groupBean : lists) {
                MessageTimeLineData dbMsg = HomeOtherGroupTimeLineDBTask.getTimeLineData(accountId, groupBean.getId());
                data.add(dbMsg);
            }
        }

        Iterator<MessageTimeLineData> iterator = data.iterator();
        while (iterator.hasNext()) {
            MessageTimeLineData single = iterator.next();
            if (single.groupId.equals(exceptGroupId)) {
                iterator.remove();
                break;
            }
        }

        return data;
    }

    private static MessageListBean getHomeLineMsgList(String accountId) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();

        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeTable.HomeDataTable.TABLE_NAME + " where " + HomeTable.HomeDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + HomeTable.HomeDataTable.ID + " asc";
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeTable.HomeDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    value.getListViewSpannableString();
                    msgList.add(value);
                } catch (JsonSyntaxException e) {
                    AppLogger.e(e.getMessage());
                }

            } else {
                msgList.add(null);
            }
        }

        //delete the null flag at the head positon and the end position
        for (int i = msgList.size() - 1; i >= 0; i--) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }

        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }

        result.setStatuses(msgList);
        c.close();
        return result;

    }
}
