
package tv.avfun.util.download;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.FileUtil;
import tv.avfun.util.download.exception.IllegalEntryException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class DownloadDBImpl implements DownloadDB {

    private SQLiteDatabase mDb;

    public DownloadDBImpl(Context context) {
        DownloadDBHelper helper = new DownloadDBHelper(context);
        mDb = helper.getWritableDatabase();
    }

    @Override
    public List<DownloadJob> getAllDownloads() {
        List<DownloadJob> all = new ArrayList<DownloadJob>();
        if(mDb != null){
            Cursor query = mDb.query(DOWNLOAD_TABLE, null, null, null, null, null, null);
            DownloadJob job = null;
            try{
                for(query.moveToFirst();!query.isAfterLast();query.moveToNext()){
                    String aid = query.getString(query.getColumnIndex(COLUMN_AID));
                    String vid = query.getString(query.getColumnIndex(COLUMN_VID));
                    // 是否同一个part
                    if(job == null || !(job.getEntry().aid.equals(aid) && job.getEntry().part.vid.equals(vid))){
                        job = buildJob(query);
                        all.add(job);
                    }
                    VideoSegment s = new VideoSegment();
                    s.num = query.getInt(query.getColumnIndex(COLUMN_NUM));
                    s.size = query.getLong(query.getColumnIndex(COLUMN_TOTAL));
                    s.stream = query.getString(query.getColumnIndex(COLUMN_URL));
                    job.getEntry().part.segments.add(s);
                    
                    int downloadedSize = query.getInt(query.getColumnIndex(COLUMN_CURRENT));
                    job.addDownloadedSize(downloadedSize);
                    int totalSize = query.getInt(query.getColumnIndex(COLUMN_TOTAL));
                    if(totalSize != -1) job.addTotalSize(totalSize);
                }
            }catch (Exception e) {
            }finally{
                query.close();
            }
        }
        return all;
    }
    private DownloadJob buildJob(Cursor cursor){
        DownloadEntry entry = new DownloadEntry();
        entry.aid = cursor.getString(cursor.getColumnIndex(COLUMN_AID));
        entry.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        entry.destination = cursor.getString(cursor.getColumnIndex(COLUMN_DEST));
        
        entry.part = new VideoPart();
        entry.part.vid = cursor.getString(cursor.getColumnIndex(COLUMN_VID));
        entry.part.subtitle = cursor.getString(cursor.getColumnIndex(COLUMN_SUBTITLE));
        entry.part.vtype = cursor.getString(cursor.getColumnIndex(COLUMN_VTYPE));
        entry.part.segments = new ArrayList<VideoSegment>();
        DownloadJob job = new DownloadJob(entry);
        return job;
    }
    @Override
    public int remove(DownloadJob job) {
        if (mDb == null)
            return 0;
        DownloadEntry entry = job.getEntry();
        String whereClause = COLUMN_AID+"=? and "+COLUMN_VID + "=?";
        String[] args = new String[]{entry.aid,entry.part.vid};
        int delete = mDb.delete(DOWNLOAD_TABLE, whereClause, args);
        return delete;
    }

    @Override
    public void addDownload(DownloadEntry entry) {
        if(entry == null || entry.part == null || entry.part.segments == null)
            throw new IllegalEntryException("check your entry data!");
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, STATUS_PENDING);
        values.put(COLUMN_CURRENT, 0);
        values.put(COLUMN_AID, entry.aid);
        values.put(COLUMN_TITLE, entry.title);
        values.put(COLUMN_VID, entry.part.vid);
        values.put(COLUMN_VTYPE, entry.part.vtype);
        values.put(COLUMN_SUBTITLE, entry.part.subtitle);
        String path = entry.destination;
        if(TextUtils.isEmpty(path))
            path = AcApp.getDownloadPath(entry.aid, entry.part.vid).getAbsolutePath();
        values.put(COLUMN_DEST, path);
        String whereClause = COLUMN_AID+"=? and "+COLUMN_VID + "=? and " + COLUMN_NUM +"=?";
        for(int i =0;i < entry.part.segments.size(); i++){
            VideoSegment s = entry.part.segments.get(i);
            values.put(COLUMN_TOTAL,s.size);
            values.put(COLUMN_NUM, s.num);
            values.put(COLUMN_URL, s.stream);
            values.put(COLUMN_DATA, s.num+FileUtil.getUrlExt(s.stream));
            int rowCount = mDb.update(DOWNLOAD_TABLE, values, whereClause, new String[]{entry.aid,entry.part.vid,String.valueOf(s.num)});
            if(rowCount == 0) mDb.insert(DOWNLOAD_TABLE, null, values);
        }
    }

    @Override
    protected void finalize(){
        mDb.close();
    }
    public int getStatus(String vid, int num) {
        String[] columns = new String[]{COLUMN_STATUS};
        String[] args = new String[]{vid, ""+num};
        
        Cursor query = mDb.query(DOWNLOAD_TABLE, columns,
                COLUMN_VID +"=? and "+ COLUMN_NUM +"=?",args, null, null, null);
        int result = -1;
        if(query!= null && query.moveToFirst()){
            result = query.getInt(0);
            query.close();
        }
        return result;
    }

    @Override
    // TODO 这个方法似乎没有用了
    public int updateDownload(DownloadJob job) {
        if (mDb == null || job == null) {
            return 0;
        }
        
        DownloadEntry entry = job.getEntry();
        if(entry == null || entry.part == null || entry.part.segments == null)
            throw new IllegalEntryException("check your entry data!");
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, STATUS_PENDING);
        values.put(COLUMN_CURRENT, 0);
        values.put(COLUMN_AID, entry.aid);
        values.put(COLUMN_TITLE, entry.title);
        values.put(COLUMN_DEST, entry.destination);
        values.put(COLUMN_VID, entry.part.vid);
        values.put(COLUMN_VTYPE, entry.part.vtype);
        values.put(COLUMN_SUBTITLE, entry.part.subtitle);
        String whereClause = COLUMN_AID+"=? and "+COLUMN_VID + "=? and " + COLUMN_NUM +"=?";
        int rowCount = 0;
        for(int i =0;i < entry.part.segments.size(); i++){
            VideoSegment s = entry.part.segments.get(i);
            values.put(COLUMN_TOTAL,s.size);
            values.put(COLUMN_NUM, s.num);
            values.put(COLUMN_URL, s.stream);
            rowCount = mDb.update(DOWNLOAD_TABLE, values, whereClause, new String[]{entry.aid,entry.part.vid,String.valueOf(s.num)});
        }
        return rowCount;
    }

    @Override
    public int updateDownload(String vid, int num, ContentValues values) {
        String whereClause = COLUMN_VID + "=? and " + COLUMN_NUM +"=?";
        return mDb.update(DOWNLOAD_TABLE, values, whereClause, new String[]{vid,String.valueOf(num)});
    }
}