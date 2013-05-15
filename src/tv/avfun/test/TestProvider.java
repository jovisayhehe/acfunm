package tv.avfun.test;

import java.util.ArrayList;

import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadDB;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.download.DownloadProvider;
import android.content.ContentValues;
import android.test.AndroidTestCase;


public class TestProvider extends AndroidTestCase {
    private DownloadProvider provider;
    private DownloadEntry entry;
    @Override
    protected void setUp() throws Exception {
        DownloadManager manager = new DownloadManager(mContext);
        provider = manager.getProvider();
        
        entry = new DownloadEntry();
        entry.aid = "125142";
        entry.title = "testestset";
        entry.part = new VideoPart();
        entry.part.vid = "2094145";
        entry.destination = AcApp.getDownloadPath("125142", "2094145").getAbsolutePath();
        entry.part.subtitle = "dfdfvadffv";
        entry.part.vtype= "youku";
        entry.part.segments = new ArrayList<VideoSegment>();
        for(int i=0;i<3;i++){
            VideoSegment s = new VideoSegment();
            s.num = i;
            s.size = i*5+51413 ;
            s.stream = "http://youku.com/flv/1/"+i+".flv";
            entry.part.segments.add(s);
        }
    }
    public void testEnqueue(){
        entry.part.vid = "12414515";
        entry.part.subtitle = "222222222222";
        DownloadJob job = new DownloadJob(entry);
        assertEquals(true, provider.enqueue(job));
        assertEquals(false, provider.getQueuedDownloads().isEmpty());
        provider.complete(job);
        
    }
    public void testUpdate(){
        ContentValues values = new ContentValues();
        values.put(DownloadDB.COLUMN_CURRENT, 6141414);
        values.put(DownloadDB.COLUMN_MIME, "video/*");
        provider.update("12414515", 1, values);
    }
    public void test(){
        assertEquals(false,provider.getCompletedDownloads().isEmpty());    
        System.out.println(provider.getAllDownloads());
    }
}
