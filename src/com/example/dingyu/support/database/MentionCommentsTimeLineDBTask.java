package com.example.dingyu.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.dingyu.bean.android.CommentTimeLineData;
import com.example.dingyu.bean.android.TimeLinePosition;
import com.example.dingyu.support.database.table.MentionCommentsTable;
import com.example.dingyu.support.utils.AppConfig;
import com.example.dingyu.support.utils.AppLogger;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.dingyu.bean.CommentBean;
import com.example.dingyu.bean.CommentListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 13-1-23
 */
public class MentionCommentsTimeLineDBTask {

    private MentionCommentsTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static void addCommentLineMsg(CommentListBean list, String accountId) {
        Gson gson = new Gson();
        List<CommentBean> msgList = list.getItemList();
        try {
            getWsd().beginTransaction();
            for (CommentBean msg : msgList) {
                ContentValues cv = new ContentValues();
                cv.put(MentionCommentsTable.MentionCommentsDataTable.MBLOGID, msg.getId());
                cv.put(MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(MentionCommentsTable.MentionCommentsDataTable.JSONDATA, json);
                getWsd().insert(MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME,
                        MentionCommentsTable.MentionCommentsDataTable.ID, cv);
            }
            getWsd().setTransactionSuccessful();
        } catch (SQLException e) {
        } finally {
            getWsd().endTransaction();
        }
        reduceCommentTable(accountId);
    }

    public static CommentTimeLineData getCommentLineMsgList(String accountId) {

        CommentListBean result = new CommentListBean();

        List<CommentBean> msgList = new ArrayList<CommentBean>();
        String sql = "select * from " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME + " where " + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + MentionCommentsTable.MentionCommentsDataTable.MBLOGID + " desc";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(MentionCommentsTable.MentionCommentsDataTable.JSONDATA));
            try {
                CommentBean value = gson.fromJson(json, CommentBean.class);
                value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }
        }

        result.setComments(msgList);
        c.close();
        CommentTimeLineData mentionTimeLineData = new CommentTimeLineData(result, getPosition(accountId));

        return mentionTimeLineData;

    }


    private static void reduceCommentTable(String accountId) {
        String searchCount = "select count(" + MentionCommentsTable.MentionCommentsDataTable.ID + ") as total" + " from " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME + " where " + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getRsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - AppConfig.DEFAULT_MENTIONS_COMMENT_DB_CACHE_COUNT;

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME + " where " + MentionCommentsTable.MentionCommentsDataTable.ID + " in "
                    + "( select " + MentionCommentsTable.MentionCommentsDataTable.ID + " from " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME + " where "
                    + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + MentionCommentsTable.MentionCommentsDataTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    public static void asyncReplace(final CommentListBean list, final String accountId) {
        final CommentListBean data = new CommentListBean();
        data.replaceAll(list);
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAllComments(accountId);
                addCommentLineMsg(data, accountId);
            }
        }).start();

    }

    static void deleteAllComments(String accountId) {
        String sql = "delete from " + MentionCommentsTable.MentionCommentsDataTable.TABLE_NAME + " where " + MentionCommentsTable.MentionCommentsDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

        getWsd().execSQL(sql);
    }

    public static void asyncUpdatePosition(final TimeLinePosition position, final String accountId) {
        if (position == null)
            return;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updatePosition(position, accountId);
            }
        };

        new Thread(runnable).start();
    }


    private static void updatePosition(TimeLinePosition position, String accountId) {
        String sql = "select * from " + MentionCommentsTable.TABLE_NAME + " where " + MentionCommentsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(MentionCommentsTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(MentionCommentsTable.TABLE_NAME, cv, MentionCommentsTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(MentionCommentsTable.ACCOUNTID, accountId);
            cv.put(MentionCommentsTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(MentionCommentsTable.TABLE_NAME,
                    MentionCommentsTable.ID, cv);
        }
    }

    public static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + MentionCommentsTable.TABLE_NAME + " where " + MentionCommentsTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(MentionCommentsTable.TIMELINEDATA));
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
}
