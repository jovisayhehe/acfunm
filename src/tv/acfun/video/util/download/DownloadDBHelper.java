
package tv.acfun.video.util.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static tv.acfun.video.util.download.DownloadDB.*;
/**
 * 存储下载的数据
 * TODO 将download.db放在下载跟目录，以便重装后也能管理下载
 * @author Yrom
 * 
 */
public class DownloadDBHelper extends SQLiteOpenHelper {

    public DownloadDBHelper(Context context) {
        super(context, DOWNLOAD_DB, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DOWNLOAD_TABLE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                + COLUMN_AID + " VARCHAR," 
                + COLUMN_VID + " VARCHAR,"
                + COLUMN_DATA + " TEXT,"
                + COLUMN_VTYPE + " VARCHAR,"
                + COLUMN_NUM + " INTEGER,"
                + COLUMN_STATUS + " INTEGER,"
                + COLUMN_TOTAL + " INTEGER,"
                + COLUMN_CURRENT + " INTEGER,"
                + COLUMN_ETAG + " TEXT,"
                + COLUMN_URL + " VARCHAR,"
                + COLUMN_DEST + " VARCHAR," 
                + COLUMN_TITLE + " VARCHAR," 
                + COLUMN_SUBTITLE + " VARCHAR," 
                + COLUMN_UA + " TEXT,"
                + COLUMN_MIME + " VARCHAR(20),"
                + COLUMN_DURATION + " INTEGER,"
                + COLUMN_CID +" VARCHAR"
                + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
        if(oldVersion <2){
            db.execSQL("ALTER TABLE " + DOWNLOAD_TABLE + " ADD " + COLUMN_DURATION +" INTEGER"); 
        }
            
        if(oldVersion <4){
            db.execSQL("ALTER TABLE " + DOWNLOAD_TABLE + " ADD " + COLUMN_CID +" VARCHAR"); 
        }
    }

}
