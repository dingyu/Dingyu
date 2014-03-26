package com.example.dingyu.support.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.dingyu.bean.android.CommentTimeLineData;
import com.example.dingyu.bean.android.TimeLinePosition;
import com.example.dingyu.support.database.table.CommentByMeTable;
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
public class CommentByMeTimeLineDBTask {

    private CommentByMeTimeLineDBTask() {

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
                cv.put(CommentByMeTable.CommentByMeDataTable.MBLOGID, msg.getId());
                cv.put(CommentByMeTable.CommentByMeDataTable.ACCOUNTID, accountId);
                String json = gson.toJson(msg);
                cv.put(CommentByMeTable.CommentByMeDataTable.JSONDATA, json);
                getWsd().insert(CommentByMeTable.CommentByMeDataTable.TABLE_NAME,
                        CommentByMeTable.CommentByMeDataTable.ID, cv);
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
        String sql = "select * from " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME + " where " + CommentByMeTable.CommentByMeDataTable.ACCOUNTID + "  = "
                + accountId + " order by " + CommentByMeTable.CommentByMeDataTable.MBLOGID + " desc";
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentByMeTable.CommentByMeDataTable.JSONDATA));
            try {
                CommentBean value = gson.fromJson(json, CommentBean.class);
                if (value != null)
                    value.getListViewSpannableString();
                msgList.add(value);
            } catch (JsonSyntaxException e) {
                AppLogger.e(e.getMessage());
            }
        }

        result.setComments(msgList);
        c.close();
        return new CommentTimeLineData(result, getPosition(accountId));

    }


    private static void reduceCommentTable(String accountId) {
        String searchCount = "select count(" + CommentByMeTable.CommentByMeDataTable.ID + ") as total" + " from " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME + " where " + CommentByMeTable.CommentByMeDataTable.ACCOUNTID
                + " = " + accountId;
        int total = 0;
        Cursor c = getRsd().rawQuery(searchCount, null);
        if (c.moveToNext()) {
            total = c.getInt(c.getColumnIndex("total"));
        }

        c.close();

        AppLogger.e("total=" + total);

        int needDeletedNumber = total - AppConfig.DEFAULT_COMMENTS_BY_ME_DB_CACHE_COUNT;

        if (needDeletedNumber > 0) {
            AppLogger.e("" + needDeletedNumber);
            String sql = " delete from " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME + " where " + CommentByMeTable.CommentByMeDataTable.ID + " in "
                    + "( select " + CommentByMeTable.CommentByMeDataTable.ID + " from " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME + " where "
                    + CommentByMeTable.CommentByMeDataTable.ACCOUNTID
                    + " in " + "(" + accountId + ") order by " + CommentByMeTable.CommentByMeDataTable.ID + " asc limit " + needDeletedNumber + " ) ";

            getWsd().execSQL(sql);
        }
    }

    private void replaceCommentLineMsg(CommentListBean list, String accountId) {

        deleteAllComments(accountId);

        //need modification
        //        wsd.execSQL("DROP TABLE IF EXISTS " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME);
        //        wsd.execSQL(DatabaseHelper.CREATE_COMMENTS_TABLE_SQL);

        addCommentLineMsg(list, accountId);
    }

    static void deleteAllComments(String accountId) {
        String sql = "delete from " + CommentByMeTable.CommentByMeDataTable.TABLE_NAME + " where " + CommentByMeTable.CommentByMeDataTable.ACCOUNTID + " in " + "(" + accountId + ")";

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
        String sql = "select * from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        if (c.moveToNext()) {
            try {
                String[] args = {accountId};
                ContentValues cv = new ContentValues();
                cv.put(CommentByMeTable.TIMELINEDATA, gson.toJson(position));
                getWsd().update(CommentByMeTable.TABLE_NAME, cv, CommentByMeTable.ACCOUNTID + "=?", args);
            } catch (JsonSyntaxException e) {

            }
        } else {

            ContentValues cv = new ContentValues();
            cv.put(CommentByMeTable.ACCOUNTID, accountId);
            cv.put(CommentByMeTable.TIMELINEDATA, gson.toJson(position));
            getWsd().insert(CommentByMeTable.TABLE_NAME,
                    CommentByMeTable.ID, cv);
        }
    }

    private static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + CommentByMeTable.TABLE_NAME + " where " + CommentByMeTable.ACCOUNTID + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(CommentByMeTable.TIMELINEDATA));
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

    public static void asyncReplace(final CommentListBean data, final String accountId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAllComments(accountId);
                addCommentLineMsg(data, accountId);
            }
        }).start();
    }
}
