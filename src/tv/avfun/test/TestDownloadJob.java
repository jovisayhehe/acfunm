package tv.avfun.test;

import java.util.ArrayList;

import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadDB;
import tv.avfun.util.download.DownloadDBImpl;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.download.DownloadJob.DownloadJobListener;
import tv.avfun.util.download.DownloadManager.DownloadObserver;
import tv.avfun.util.download.DownloadProvider;
import android.test.AndroidTestCase;
import android.util.Log;


public class TestDownloadJob extends AndroidTestCase {
    DownloadEntry entry;
    DownloadManager manager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new DownloadManager(mContext);
        entry = new DownloadEntry();
        entry.aid = "12345";
        entry.title = "testestset";
        entry.part = new VideoPart();
        entry.part.vid = "104230802";
        entry.destination = AcApp.getDownloadPath("12345", "104230802").getAbsolutePath();
        entry.part.subtitle = "dfdsfdfdfv";
        entry.part.vtype= "sina";
        entry.part.segments = new ArrayList<VideoSegment>(1);
        VideoSegment s = new VideoSegment();
        s.num =0;
        s.size = 842693;
        s.stream = "http://edge.v.iask.com/104230802.mp4?KID=sina,viask&Expires=1368806400&ssig=cVy%2FEvwxaB";
        entry.part.segments.add(s);
    }
    public void testDownload(){
        DownloadProvider provider = manager.getProvider();
        DownloadJob job = new DownloadJob(entry, 1);
        boolean b = provider.enqueue(job);
        if(!b) job = provider.getQueueJobByVid(entry.part.vid);
        job.setListener(new DownloadJobListener() {
            
            @Override
            public void onDownloadStarted(DownloadJob job) {
                Log.i("test", "onDownloadStarted");
            }
            
            @Override
            public void onDownloadFinished(int status,DownloadJob job) {
                Log.i("test", "onDownloadFinished");
                
            }

            @Override
            public void onDownloadPaused(DownloadJob job) {
                // TODO Auto-generated method stub
                
            }
        });
        manager.registerDownloadObserver(new DownloadObserver() {
            
            @Override
            public void onDownloadChanged(DownloadManager manager) {
                // TODO Auto-generated method stub
                Log.i("test", "onDownloadChanged");
            }
        });
        job.start();
    }
}
