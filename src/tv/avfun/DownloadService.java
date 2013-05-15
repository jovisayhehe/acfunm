package tv.avfun;

import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadProvider;
import tv.avfun.util.download.DownloadJob.DownloadJobListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Background downloader
 * TODO: sd card,battery,net work listener
 * @author Yrom
 *
 */
public class DownloadService extends Service {
    public static final String ACTION_ADD_TO_DOWNLOAD = "tv.avfun.action.ADD_TO_DOWNLOAD";
    public static final String EXTRA_DOWNLOAD_ENTRY = "download_entry";
    public static final int DOWNLOAD_NOTIFICATION_ID = 250;
    
    private NotificationManager mNotificationManager;
    private DownloadProvider mDownloadProvider;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mDownloadProvider = AcApp.instance().getDownloadManager().getProvider();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(ACTION_ADD_TO_DOWNLOAD.equals(action)){
            DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(EXTRA_DOWNLOAD_ENTRY);
            enqueue(entry, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void enqueue(DownloadEntry entry, int startId) {
        // TODO Auto-generated method stub
        DownloadJob job  = new DownloadJob(entry,startId);
        if(mDownloadProvider.enqueue(job)){
            job.setListener(mJobListener);
            job.start();
        }
        
    }
    private DownloadJobListener mJobListener = new DownloadJobListener() {
        
        @Override
        public void onDownloadStarted() {
            // TODO Auto-generated method stub
        }
        
        @Override
        public void onDownloadFinished(DownloadJob job) {
            // TODO Auto-generated method stub
            mDownloadProvider.complete(job);
            showNotification(job);
        }
    };
    
    private void showNotification(DownloadJob job) {
        // TODO Auto-generated method stub
        String notificationMessage = job.getEntry().part.subtitle;
//        Notification notification = new Notification.Builder(this)
//                                .setAutoCancel(true)
//                                .setContentTitle("下载完成")
//                                .setContentText(notificationMessage)
//                                .setSmallIcon(android.R.drawable.stat_sys_download)
//                                .build();
    }
}
