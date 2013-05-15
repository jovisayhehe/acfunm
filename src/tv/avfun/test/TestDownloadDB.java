package tv.avfun.test;

import java.util.ArrayList;

import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadDB;
import tv.avfun.util.download.DownloadDBHelper;
import tv.avfun.util.download.DownloadDBImpl;
import tv.avfun.util.download.DownloadEntry;
import android.content.ContentValues;
import android.os.Environment;
import android.test.AndroidTestCase;


public class TestDownloadDB extends AndroidTestCase {
    private DownloadDB db;
    DownloadEntry entry;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = new DownloadDBImpl(mContext);
        entry = new DownloadEntry();
        entry.aid = "425142";
        entry.title = "testestset";
        entry.part = new VideoPart();
        entry.part.vid = "1094145";
        entry.destination = AcApp.getDownloadPath("425142", "1094145").getAbsolutePath();
        entry.part.subtitle = "dfdsfdfdfv";
        entry.part.vtype= "tudou";
        entry.part.segments = new ArrayList<VideoSegment>();
        for(int i=0;i<3;i++){
        VideoSegment s = new VideoSegment();
        s.num = i;
        s.size = i*5+51413 ;
        s.stream = "http://tudou.com/flv/1/"+i+".flv";
        entry.part.segments.add(s);
        }
    }
//    public void testOpen(){
//        DownloadDBHelper helper = new DownloadDBHelper(getContext());
//        assertNotNull(helper.getWritableDatabase());
//    }
    public void testAdd(){
        
        
        db.addDownload(entry);
    }
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_MIME, "video/x-flv");
        values.put(DownloadDB.COLUMN_CURRENT, 3456);
        values.put(DownloadDB.COLUMN_UA, UserAgent.MY_UA);
        db.updateDownload("1094145", 1, values);
    }
    public void testGet(){
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_CURRENT, 1321);
        values.put(DownloadDB.COLUMN_UA, UserAgent.MY_UA);
        values.put(DownloadDB.COLUMN_MIME, "video/x-flv");
        values.put(DownloadDB.COLUMN_STATUS, DownloadDB.STATUS_RUNNING);
        db.updateDownload("1094145", 0, values);
        assertEquals(db.getStatus("1094145", 0),DownloadDB.STATUS_RUNNING);
    }
}
