package tv.avfun;

import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadProvider;
import tv.avfun.util.download.DownloadJob.DownloadJobListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
    private static final String TAG = DownloadService.class.getSimpleName();
    
    
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
        if(intent != null){
            String action = intent.getAction();
            Log.d(TAG, "onStart - "+action);
            if(ACTION_ADD_TO_DOWNLOAD.equals(action)){
                DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(EXTRA_DOWNLOAD_ENTRY);
                enqueue(entry, startId);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void enqueue(DownloadEntry entry, int startId) {
        DownloadJob job  = new DownloadJob(entry,startId);
        if(mDownloadProvider.enqueue(job)){
            Log.d(TAG, "new job!");
        }
        job.setListener(mJobListener);
        job.start();
        
    }
    private DownloadJobListener mJobListener = new DownloadJobListener() {
        
        @Override
        public void onDownloadStarted(DownloadJob job) {
            // TODO 在notification上显示一个下载进度条
            showStartNoti(job);
        }
        
        @Override
        public void onDownloadFinished(int status, DownloadJob job) {
            mDownloadProvider.complete(status, job);
            showDownloadedNoti(status,job);
        }
    };
    @SuppressWarnings("deprecation")
    // TODO 移到AcApp中
    private void showNoti(String text,int icon, CharSequence title){
        Notification notification = new Notification(icon,text,System.currentTimeMillis());
        Intent manIntent = new Intent(this, DownloadManActivity.class);
        manIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, manIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, title, text, contentIntent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(DOWNLOAD_NOTIFICATION_ID, notification);
        
    }
    private void showDownloadedNoti(int status,DownloadJob job) {
        String contentText = job.getEntry().part.subtitle;
//        Notification notification = new Notification.Builder(this)
//                                .setAutoCancel(true)
//                                .setContentTitle("下载完成")
//                                .setContentText(notificationMessage)
//                                .setSmallIcon(android.R.drawable.stat_sys_download)
//                                .build();
        String title = "";
        if(job.getProgress() == 100)
           title = getString(R.string.downloaded);
        else
           title = getString(R.string.download_fail)+" - 状态码：" +status;
        showNoti(contentText, android.R.drawable.stat_sys_download_done, title);

    }
    private void showStartNoti(DownloadJob job){
        String contentText = job.getEntry().part.subtitle;
        showNoti(contentText, android.R.drawable.stat_sys_download, getString(R.string.start_download));
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destory!!");
    }
}
