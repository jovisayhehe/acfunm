package tv.avfun;

import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadDB;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadJob.DownloadJobListener;
import tv.avfun.util.download.DownloadProvider;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

/**
 * Background downloader
 * TODO: sd card,battery,net work listener
 * @author Yrom
 *
 */
public class DownloadService extends Service {
    public static final String ACTION_ADD_TO_DOWNLOAD = "tv.avfun.action.ADD_TO_DOWNLOAD";
    public static final String ACTION_RESUME_DOWNLOAD = "tv.avfun.action.RESUME_DOWNLOAD";
    public static final String EXTRA_DOWNLOAD_JOB = "download_job";
    public static final String EXTRA_DOWNLOAD_ENTRY = "download_entry";
    public static final String EXTRA_DOWNLOAD_UA = "download_ua";
    public static final int DOWNLOAD_NOTIFICATION_ID = 250;
    private static final String TAG = DownloadService.class.getSimpleName();
    
    
    private NotificationManager mNotificationManager;
    private DownloadProvider mDownloadProvider;
    
    @Override
    public IBinder onBind(Intent intent) {
        return new IDownloadBinder();
    }
    private class IDownloadBinder extends Binder implements IDownloadService{

        @Override
        public DownloadService getService() {
            return DownloadService.this;
        }
        
    }
    public interface IDownloadService{
        DownloadService getService();
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
                String ua = intent.getStringExtra(EXTRA_DOWNLOAD_UA);
                if(ua == null)
                    ua = UserAgent.MY_UA;
                
                enqueue(ua, entry, startId);
            }
               
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void enqueue(String ua, DownloadEntry entry, int startId) {
        if(TextUtils.isEmpty(entry.destination))
            entry.destination = AcApp.getDownloadPath(entry.aid, entry.part.vid).getAbsolutePath();
        DownloadJob job  = new DownloadJob(entry,startId);
        job.setUserAgent(ua);
        if(mDownloadProvider.enqueue(job)){
            Log.d(TAG, "new job!");
        }
        job.setListener(mJobListener);
        job.start();
        
    }
    public DownloadJobListener mJobListener = new DownloadJobListener() {
        
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
    // TODO 移到AcApp中
    private void showNoti(String text,int icon, CharSequence title){
        Intent mIntent = new Intent(this, DownloadManActivity.class);
        AcApp.showNotification(mIntent, DOWNLOAD_NOTIFICATION_ID, text, icon, title);
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
        if (status == DownloadDB.STATUS_SUCCESS)
            title = getString(R.string.downloaded);
        else if (status == DownloadDB.STATUS_CANCELED)
            title = "已取消";
        else if (status == DownloadDB.STATUS_BAD_REQUEST || status == DownloadDB.STATUS_HTTP_DATA_ERROR)
            title = "网络错误";
        else if (status == DownloadDB.STATUS_PAUSED)
            title = "已暂停";
        else if (status == DownloadDB.STATUS_QUEUED_FOR_WIFI)
            title = "等待WIFI";
        else
            title = getString(R.string.download_fail) + " - 状态码：" + status;
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
