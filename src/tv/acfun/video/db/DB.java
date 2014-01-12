/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.db;

import java.util.ArrayList;
import java.util.List;

import tv.acfun.video.entity.User;
import tv.acfun.video.entity.Video;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * @author Yrom
 * 
 */
public final class DB {
    private DBOpenHelper helper;

    public DB(Context context) {
        helper = new DBOpenHelper(context);
    }

    @Override
    protected void finalize() throws Throwable {
        helper.close();
        super.finalize();
    }

    public void saveUser(User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO " + DBOpenHelper.TABLE_USER + " (uid, name, avatar, signature, cookies, time) VALUES(?,?,?,?,?,?)",
                new Object[] { user.id, user.name, user.avatar, user.signature, user.cookies, user.savedTime });
        db.close();
    }

    public User getUser() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_USER, null);
        User user = null;
        if (query.moveToFirst()) {
            user = new User();
            user.id = query.getInt(query.getColumnIndex("uid"));
            user.name = query.getString(query.getColumnIndex("name"));
            user.avatar = query.getString(query.getColumnIndex("avatar"));
            user.signature = query.getString(query.getColumnIndex("signature"));
            user.cookies = query.getString(query.getColumnIndex("cookies"));
            user.savedTime = query.getLong(query.getColumnIndex("time"));
        }
        query.close();
        db.close();
        return user;
    }

    public void logout() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DBOpenHelper.TABLE_USER);
        db.close();
    }

    public void addFav(Video video) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("INSERT INTO " + DBOpenHelper.TABLE_FAV + " (aid, title, desc, preview, channelId,favs,time) VALUES(?,?,?,?,?,?,?)",
                new Object[] {video.acId, video.name, video.desc, video.previewurl, video.channelId, video.collectnum, System.currentTimeMillis() });
        db.close();
    }

    public boolean deleteFav(int aid) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int delete = db.delete(DBOpenHelper.TABLE_FAV, "aid=?", new String[] { String.valueOf(aid) });
        db.close();
        return delete > 0;
    }

    public synchronized boolean isFav(int aid) {
        SQLiteStatement state = helper.getReadableDatabase().compileStatement(
                "select count(*) from " + DBOpenHelper.TABLE_FAV + " where aid=" + aid);
        boolean isFav = state.simpleQueryForLong() > 0;
        return isFav;
    }

    /**
     * 获取收藏列表 {@link Video#createtime} 为收藏的时间
     * 
     * @return
     */
    public List<Video> getFavList() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_FAV, null);
        if (query.getCount() <= 0) {
            query.close();
            db.close();
            return null;
        }
        List<Video> videos = new ArrayList<Video>(query.getCount());
        while (query.moveToNext()) {
            Video v = new Video();
            v.acId = query.getInt(query.getColumnIndex("aid"));
            v.name = query.getString(query.getColumnIndex("title"));
            v.desc = query.getString(query.getColumnIndex("desc"));
            v.channelId = query.getInt(query.getColumnIndex("channelId"));
            v.collectnum = query.getInt(query.getColumnIndex("favs"));
            v.previewurl = query.getString(query.getColumnIndex("preview"));
            // 收藏的时间
            v.createtime = query.getLong(query.getColumnIndex("time"));
            videos.add(v);
        }
        query.close();
        db.close();
        return videos;
    }

    public long insertHistory(Video video) {
        ContentValues values = new ContentValues();
        values.put("aid", video.acId);
        values.put("title", video.name);
        values.put("channelId", video.channelId);
        values.put("preview", video.previewurl);
        values.put("time", System.currentTimeMillis());
        return helper.getWritableDatabase().insertWithOnConflict(DBOpenHelper.TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized boolean isWatched(int aid) {
        SQLiteStatement state = helper.getReadableDatabase().compileStatement(
                "select count(*) from " + DBOpenHelper.TABLE_HISTORY + " where aid=" + aid);
        boolean isWatched = state.simpleQueryForLong() > 0;
        return isWatched;
    }
    /**
     * 仅返回最近50条历史记录
     * @return
     */
    public List<Video> getHistory() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor query = db.rawQuery("SELECT * FROM " + DBOpenHelper.TABLE_HISTORY +" ORDER BY _id DESC LIMIT 50", null);
        
        if (query.getCount() <= 0) {
            query.close();
            db.close();
            return null;
        }
        List<Video> videos = new ArrayList<Video>(query.getCount());
        while (query.moveToNext()) {
            Video v = new Video();
            v.acId = query.getInt(query.getColumnIndex("aid"));
            v.name = query.getString(query.getColumnIndex("title"));
            v.channelId = query.getInt(query.getColumnIndex("channelId"));
            v.previewurl = query.getString(query.getColumnIndex("preview"));
            // 浏览的时间
            v.createtime = query.getLong(query.getColumnIndex("time"));
            videos.add(v);
        }
        query.close();
        db.close();
        return videos;
    }
}
