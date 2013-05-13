
package tv.avfun.util.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 存储下载的数据
 * 
 * @author Yrom
 * 
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

    public DownloadDBHelper(Context context) {
        super(context, DownloadDB.DOWNLOAD_DB, null, DownloadDB.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DownloadDB.DOWNLOAD_TABLE + "("
                + DownloadDB.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + DownloadDB.COLUMN_AID + " VARCHAR," 
                + DownloadDB.COLUMN_VID+ " VARCHAR,"
                + DownloadDB.COLUMN_NUM + " INTEGER,"
                + DownloadDB.COLUMN_STATUS + " INTEGER,"
                + DownloadDB.COLUMN_TOTAL + " INTEGER,"
                + DownloadDB.COLUMN_CURRENT + " INTEGER,"
                + DownloadDB.COLUMN_ETAG + " TEXT,"
                + DownloadDB.COLUMN_URL + " VARCHAR,"
                + DownloadDB.COLUMN_DEST + " VARCHAR," 
                + DownloadDB.COLUMN_TITLE + " VARCHAR," 
                + DownloadDB.COLUMN_SUBTITLE + " VARCHAR," 
                + DownloadDB.COLUMN_UA + " TEXT,"
                + DownloadDB.COLUMN_MIME + " VARCHAR(20)"
                + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO

    }

}
