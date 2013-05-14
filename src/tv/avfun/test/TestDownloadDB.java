package tv.avfun.test;

import java.util.ArrayList;

import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadDB;
import tv.avfun.util.download.DownloadDBHelper;
import tv.avfun.util.download.DownloadDBImpl;
import tv.avfun.util.download.DownloadEntry;
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
        entry.destination = Environment.getExternalStorageDirectory().getAbsolutePath()+"/download";
        entry.title = "testestset";
        entry.part = new VideoPart();
        entry.part.vid = "1094145";
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
        db.setEtag("X1r125D3", 0, "14fdf12351q");
        db.setEtag("1094145", 1, "c6242177qcb141tsdrf14");
        db.setStatus("X1r125D3", 1, DownloadDB.STATUS_RUNNING);
        
    }
}
