
package tv.avfun.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tv.avfun.BuildConfig;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Favorite;
import tv.avfun.entity.History;
import tv.avfun.entity.VideoPart;
import tv.avfun.util.FileUtil;
import tv.avfun.util.MD5Util;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

public class DBService {

    private SQLiteDatabase db;
    private DBOpenHelper   dbHelper;

    public DBService(Context context) {
        dbHelper = new DBOpenHelper(context);
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = dbHelper.getReadableDatabase();
        }
    }

    /**
     * 添加方法
     * 
     * @param videoid
     *            视频 文章id
     * @param title
     *            标题
     * @param type
     *            类型
     * @param channelid
     *            频道id
     */
    public void addtoFav(String videoid, String title, int type, int channelid) {
        db.execSQL("INSERT INTO NFAVORITES(VIDEOID,TITLE,TPYE,CHANNELID)" + "VALUES(?,?,?,?)", new Object[] { videoid,
                title, type, channelid });
        db.close();
    }

    /**
     * 
     * @param id
     *            视频id
     */
    public void delFav(String id) {
        db.execSQL("DELETE FROM NFAVORITES WHERE VIDEOID = ?", new String[] { id });
        db.close();
    }

    /**
     * 
     * @param id
     *            视频id
     * @return 是否存在收藏表中
     */
    public boolean isFaved(String id) {
        Cursor cursor = db.rawQuery("SELECT VIDEOID FROM NFAVORITES WHERE VIDEOID = ?", new String[] { id });
        boolean isexist = cursor.moveToFirst();
        cursor.close();
        db.close();
        return isexist;
    }

    public void addtoHis(String id, String title, String time, int type, int channelid) {
        db.execSQL("INSERT INTO NHISTORY(VIDEOID,TITLE,TIME,TPYE,CHANNELID)" + "VALUES(?,?,?,?,?)", new Object[] { id,
                title, time, type, channelid });
        db.close();
    }

    public void addtoSHis(String title) {
        db.execSQL("INSERT INTO SEARCHHISTORY(TITLE)" + "VALUES(?,?,?)", new Object[] { title });
        db.close();
    }

    public void deltoSFov(String id) {

    }

    /**
     * 清除历史记录
     */
    public void cleanHis() {
        db.execSQL("DELETE FROM NHISTORY");
        db.close();
    }

    public void cleanSHis(String id, String title) {
        db.execSQL("DELETE FROM SEARCHHISTORY");
        db.close();
    }

    public List<Favorite> getFovs() {
        ArrayList<Favorite> fovs = new ArrayList<Favorite>();
        Cursor cursor = db.rawQuery("SELECT VIDEOID,TITLE,CHANNELID,TPYE FROM NFAVORITES ORDER BY _ID DESC", null);
        while (cursor.moveToNext()) {
            Favorite fov = new Favorite();
            fov.setAid(cursor.getString(cursor.getColumnIndex("VIDEOID")));
            fov.setChannelid(cursor.getInt(cursor.getColumnIndex("CHANNELID")));
            fov.setTitle(cursor.getString(cursor.getColumnIndex("TITLE")));
            fov.setTpye(cursor.getInt(cursor.getColumnIndex("TPYE")));
            fovs.add(fov);
        }
        cursor.close();
        db.close();

        return fovs;
    }

    public ArrayList<History> getHiss() {
        ArrayList<History> hiss = new ArrayList<History>();
        Cursor cursor = db.rawQuery("SELECT VIDEOID,TITLE,TIME,CHANNELID,TPYE FROM NHISTORY ORDER BY _ID DESC", null);
        while (cursor.moveToNext()) {
            History his = new History();
            his.setAid(cursor.getString(cursor.getColumnIndex("VIDEOID")));
            his.setTime(cursor.getString(cursor.getColumnIndex("TIME")));
            his.setTitle(cursor.getString(cursor.getColumnIndex("TITLE")));
            his.setChannelid(cursor.getString(cursor.getColumnIndex("CHANNELID")));
            his.setTpye(cursor.getInt(cursor.getColumnIndex("TPYE")));
            hiss.add(his);
        }
        cursor.close();
        db.close();

        return hiss;

    }

    public ArrayList<String> getSHiss() {
        ArrayList<String> shiss = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM SEARCHHISTORY", null);
        while (cursor.moveToNext()) {
            shiss.add(cursor.getString(cursor.getColumnIndex("TITLE")));
        }
        cursor.close();
        db.close();
        return shiss;
    }

    public void saveUser(HashMap<String, Object> map) {

        Cookie[] cks = (Cookie[]) map.get("Cookies");
        JSONArray jsonarray = new JSONArray();
        for (Cookie ck : cks) {

            JSONObject job = new JSONObject();
            try {
                job.put("name", ck.getName());
                job.put("value", ck.getValue());
                job.put("path", ck.getPath());
                job.put("domain", ck.getDomain());
            } catch (JSONException e) {

                e.printStackTrace();

            }
            jsonarray.put(job);
        }

        db.execSQL("INSERT INTO USER (USERID,USERNAME,AVATAR,SIGNATURE,COOKIES)" + "VALUES(?,?,?,?,?)",
                new Object[] { String.valueOf(map.get("uid")), (String) map.get("uname"), (String) map.get("avatar"),
                        (String) map.get("signature"), jsonarray.toString() });
        db.close();
    }

    public HashMap<String, Object> getUser() {
        HashMap<String, Object> user = new HashMap<String, Object>();
        Cursor cursor = db.rawQuery("SELECT USERID,USERNAME,AVATAR,SIGNATURE,COOKIES FROM USER", null);
        if (cursor.moveToFirst()) {
            user.put("uid", cursor.getString(cursor.getColumnIndex("USERID")));
            user.put("uname", cursor.getString(cursor.getColumnIndex("USERNAME")));
            user.put("avatar", cursor.getString(cursor.getColumnIndex("AVATAR")));
            user.put("signature", cursor.getString(cursor.getColumnIndex("SIGNATURE")));
            String ckstring = cursor.getString(cursor.getColumnIndex("COOKIES"));
            try {
                JSONArray jsonarray = new JSONArray(ckstring);
                Cookie[] jcks = new Cookie[jsonarray.length()];

                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject localJSONObject = jsonarray.getJSONObject(i);
                    Cookie localCookie = new Cookie();
                    localCookie.setName(localJSONObject.getString("name"));
                    localCookie.setValue(localJSONObject.getString("value"));
                    localCookie.setPath(localJSONObject.getString("path"));
                    localCookie.setDomain(localJSONObject.getString("domain"));
                    jcks[i] = localCookie;
                }

                user.put("cookies", jcks);
                cursor.close();
                db.close();
                return user;
            } catch (JSONException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            cursor.close();
            db.close();
            return null;
        }

        return null;
    }

    public void user_cancel() {
        db.execSQL("DELETE FROM USER");
        db.close();
    }

}
