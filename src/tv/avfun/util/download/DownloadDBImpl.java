
package tv.avfun.util.download;

import java.util.List;

import tv.avfun.BuildConfig;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.exception.IllegalEntryException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DownloadDBImpl implements DownloadDB {

    private SQLiteDatabase mDb;

    public DownloadDBImpl(Context context) {
        DownloadDBHelper helper = new DownloadDBHelper(context);
        mDb = helper.getWritableDatabase();
    }

    @Override
    public List<DownloadJob> getAllDownloads() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(DownloadJob job) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addToDownload(DownloadEntry entry) {
        if(entry == null || entry.part == null || entry.part.segments == null)
            throw new IllegalEntryException("invalidate entry data");
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, STATUS_PENDING);
        values.put(COLUMN_CURRENT, 0);
        values.put(COLUMN_AID, entry.aid);
        values.put(COLUMN_TITLE, entry.title);
        values.put(COLUMN_VID, entry.part.vid);
        values.put(COLUMN_SUBTITLE, entry.part.subtitle);
        for(int i =0;i < entry.part.segments.size(); i++){
            VideoSegment s = entry.part.segments.get(i);
            values.put(COLUMN_TOTAL,s.size);
            values.put(COLUMN_NUM, s.num);
            values.put(COLUMN_URL, s.stream);
            mDb.insert(DownloadDB.DOWNLOAD_TABLE, null, values);
        }
    }

    @Override
    public void setStatus(String vid, int num, int status) {
        if (mDb == null) {
            return;
        }
        String[] args = new String[] { vid, "" + num };
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);
        int update = mDb.update(DOWNLOAD_TABLE, values, "vid=? and num=?", args);
        if(update == 0 && BuildConfig.DEBUG)
            Log.e("db", "Failed to set status for "+vid + " - " +num);

    }

    @Override
    public void setEtag(String vid, int num, String etag) {
        if (mDb == null) {
            return;
        }
        String[] args = new String[] { vid, "" + num };
        ContentValues values = new ContentValues();
        values.put(COLUMN_ETAG, etag);
        int update = mDb.update(DOWNLOAD_TABLE, values, "vid=? and num=?", args);
        if(update == 0 && BuildConfig.DEBUG)
            Log.e("db", "Failed to set etag for "+vid + " - " +num);

    }

    @Override
    protected void finalize(){
        mDb.close();
    }

    @Override
    public boolean isDownloaded(String vid, int num) {
        String[] args = new String[]{vid, ""+num};
        String[] columns = new String[]{COLUMN_STATUS};
        Cursor query = mDb.query(DOWNLOAD_TABLE, columns,
                COLUMN_VID +"=? and "+ COLUMN_NUM +"=? and " +COLUMN_STATUS+"=" + STATUS_SUCCESS,
                args, null, null, null);
        boolean result = false;
        if(query!= null && query.moveToFirst()){
            result = true;
            query.close();
        }
        return result;
    }
}
