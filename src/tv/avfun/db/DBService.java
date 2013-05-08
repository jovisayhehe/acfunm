
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
import tv.avfun.entity.VideoInfo.VideoItem;
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

    /**
     * 将视频片段加到列表
     * 
     * @param aid
     * @param item
     */
    public void addDownload(String aid, VideoItem item) {
        if (item == null || !item.validate())
            throw new IllegalArgumentException("item完整性验证失败");
        File downloadPath = AcApp.getDownloadPath(aid, item.vid);
        String sql = "INSERT INTO DOWNLOADLIST (AID,VID,LOCALURI,REMOTEURI,TITLE,DURATION,I,MD5,STATE,DOWNLOADID,SIZE,VTYPE) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        for (int i = 0; i < item.urlList.size(); i++) {
            long id = item.downloadIDs.get(i);
            // 判断是否原来有id信息了，删除旧数据
            db.delete("DOWNLOADLIST", "VID = ? and I = ?", new String[] { item.vid, i + "" });
            String url = item.urlList.get(i);
            String filename = i + FileUtil.getUrlExt(url);
            String localuri = Uri.fromFile(new File(downloadPath, filename)).toString();
            long duration = 0;
            if (item.durationList != null && !item.durationList.isEmpty())
                duration = item.durationList.get(i);
            db.execSQL(
                    sql,
                    new Object[] { aid, item.vid, localuri, url, item.subtitle, duration, i,
                            MD5Util.getMD5String(aid + localuri + i), 0, id, item.bytesList.get(i), item.vtype });
        }
        db.close();
    }

    /**
     * 0 下载中 1 下载暂停 2 下载失败 3 下载成功
     * 
     * @param downloadID
     * @param state
     */
    public void changeDownloadState(long downloadID, int state) {
        if (state > 3)
            throw new IllegalArgumentException("error: state>3");
        String sql = "UPDATE DOWNLOADLIST SET STATE=? WHERE DOWNLOADID=?";
        db.execSQL(sql, new Object[] { state, downloadID });
        db.close();
    }

    /**
     * 获取下载完毕的videoitem
     * 
     * @param aid
     */
    public List<VideoItem> getVideoItems(String aid) {
        String sql = "SELECT VID,LOCALURI,DURATION,DOWNLOADID,TITLE,I,SIZE,VTYPE FROM DOWNLOADLIST WHERE AID=? AND STATE=3";
        Cursor cursor = db.rawQuery(sql, new String[] { aid });

        if (cursor != null) {
            try {
                List<VideoItem> items = new ArrayList<VideoItem>();
                VideoItem item = null;
                int i = 0;
                while (cursor.moveToNext()) {
                    String vid = cursor.getString(cursor.getColumnIndex("VID"));
                    // vid 与前一个item 不一样，或者还没有
                    if (item == null || item.vid == null || !vid.equals(item.vid)) {
                        // 新建一个
                        item = new VideoItem();
                        item.vid = vid;
                        item.subtitle = cursor.getString(cursor.getColumnIndex("TITLE"));
                        item.vtype = cursor.getString(cursor.getColumnIndex("VTYPE"));
                        item.urlList = new ArrayList<String>();
                        item.downloadIDs = new ArrayList<Long>();
                        item.durationList = new ArrayList<Long>();
                        item.bytesList = new ArrayList<Integer>();
                        items.add(item);
                    }
                    i = cursor.getInt(cursor.getColumnIndex("I"));
                    // item.isdownloaded = true; // 由state=3 保证查到的数据为已下载的
                    item.urlList.add(i, cursor.getString(cursor.getColumnIndex("LOCALURI")));
                    item.downloadIDs.add(i, cursor.getLong(cursor.getColumnIndex("DOWNLOADID")));
                    item.durationList.add(i, cursor.getLong(cursor.getColumnIndex("DURATION")));
                    item.bytesList.add(i, cursor.getInt(cursor.getColumnIndex("SIZE")));
                }
                return items;
            } catch (Exception e) { // 如果有一个状态是没有下载完毕则必定会指针溢出抛异常
                if (BuildConfig.DEBUG)
                    Log.w("db", "failed to getVideoItems for " + aid + ":\n" + e.getMessage());
                return null;
            }
            finally {
                cursor.close(); // 保证关闭
                db.close();
            }
        }
        Log.w("db", "not found:" + aid);
        return null;
    }

    /**
     * 查找downloadID 对应的vid。
     * 
     * @return
     */
    public String getVid(long downloadID) {
        String sql = "SELECT VID FROM DOWNLOADLIST WHERE DOWNLOADID=?";
        Cursor query = db.rawQuery(sql, new String[] { downloadID + "" });
        String vid = null;
        if (query == null)
            return null;
        if (query.moveToFirst()) {
            vid = query.getString(query.getColumnIndexOrThrow("DOWNLOADID"));
        }
        query.close();
        db.close();
        return vid;
    }

    public List<Long> getDownloadIds(String vid) {
        String sql = "SELECT DOWNLOADID,I FROM DOWNLOADLIST WHERE VID=? ORDER BY I ASC";
        Cursor query = db.rawQuery(sql, new String[] { vid });
        List<Long> ids = new ArrayList<Long>();
        if (query == null)
            return null;
        while (query.moveToNext()) {
            int i = query.getInt(query.getColumnIndex("I"));
            long id = query.getLong(query.getColumnIndex("DOWNLOADID"));
            ids.add(i, id); // 不管之前i有没有对应id，都添加进去。以最新的id为准
        }
        query.close();
        db.close();
        return ids;

    }

    public void removeDownload(String vid) {
        db.delete("DOWNLOADLIST", "VID=?", new String[] { vid });
        db.close();
    }

    /**
     * 获取vid对应总字节数
     * 
     * @param vid
     * @return 负数，代表获取失败
     */
    public int getTotalSize(String vid) {
        String sql = "SELECT SUM(SIZE) FROM DOWNLOADLIST WHERE VID=?";
        Cursor query = db.rawQuery(sql, new String[] { vid });
        int total = -1;
        if (query != null) {
            if (query.moveToFirst()) {
                total = query.getInt(query.getColumnIndex("SUM(SIZE)"));
            }
            query.close();
        }
        db.close();
        return total;
    }
    
    public VideoItem getDownloadedItemById(String vid){
        String sql = "SELECT LOCALURI,DURATION,TITLE,I,SIZE,VTYPE FROM DOWNLOADLIST WHERE VID=? AND STATE=3";
        Cursor cursor = db.rawQuery(sql, new String[] { vid });

        if (cursor != null) {
            try{
            VideoItem item =new VideoItem();
            item.urlList  = new ArrayList<String>();
            item.durationList = new ArrayList<Long>();
            while(cursor.moveToNext()){
                int i = cursor.getInt(cursor.getColumnIndex("I"));
                if(i == 0){
                    item.vid = vid;
                    item.subtitle = cursor.getString(cursor.getColumnIndex("TITLE"));
                    item.vtype = cursor.getString(cursor.getColumnIndex("VTYPE"));
                    item.isdownloaded = true;
                }
                item.urlList.add(i,cursor.getString(cursor.getColumnIndex("LOCALURI")));
                item.durationList.add(i,cursor.getLong(cursor.getColumnIndex("DURATION")));
            }
            }catch (Exception e) {
                if(BuildConfig.DEBUG) Log.e("db", "failed to getDownloadedItemById "+vid,e);
                return null;
            }finally{
                cursor.close();
            }
        }
        db.close();
        return null;
        
    }
}
