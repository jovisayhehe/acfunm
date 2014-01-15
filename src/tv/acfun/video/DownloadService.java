package tv.acfun.video;

import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.util.download.DownloadDB;
import tv.acfun.video.util.download.DownloadEntry;
import tv.acfun.video.util.download.DownloadJob;
import tv.acfun.video.util.download.DownloadJob.DownloadJobListener;
import tv.acfun.video.util.download.DownloadProvider;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

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
    public DownloadProvider mDownloadProvider;
    
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
        mDownloadProvider = AcApp.getDownloadManager().getProvider();
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.setPriority(1000);
        registerReceiver(sdCardMounted, filter);
    }
    private BroadcastReceiver sdCardMounted = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())){
                mDownloadProvider.loadOldDownloads();
                Log.d(TAG, "sdcard mounted");
            }
        }
        
    };
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!AcApp.isExternalStorageAvailable()){
            Toast.makeText(getApplicationContext(),"SD卡未找到！",0).show();
            return 0;
        }
        if(intent != null){
            String action = intent.getAction();
            Log.d(TAG, "onStart - "+action);
            if(ACTION_ADD_TO_DOWNLOAD.equals(action)){
                DownloadEntry entry = (DownloadEntry) intent.getParcelableExtra(EXTRA_DOWNLOAD_ENTRY);
                String ua = intent.getStringExtra(EXTRA_DOWNLOAD_UA);
                if(ua == null)
                    ua = BaseResolver.UA_DEFAULT;
                
                enqueue(ua, entry, startId);
            }
               
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void enqueue(String ua, DownloadEntry entry, int startId) {
        if(TextUtils.isEmpty(entry.destination))
            entry.destination = AcApp.getDownloadPath(entry.aid, entry.part.sourceId).getAbsolutePath();
        DownloadJob job  = new DownloadJob(entry,startId);
        job.setUserAgent(ua);
        if(mDownloadProvider.enqueue(job)){
            Log.d(TAG, "new job!");
            job.setListener(mJobListener);
            job.start();
        }else{
            Toast.makeText(getApplicationContext(), "下载任务已存在", 0).show();
        }
        
    }
    public DownloadJobListener mJobListener = new DownloadJobListener() {
        
        @Override
        public void onDownloadStarted(DownloadJob job) {
            // TODO 在notification上显示一个下载进度条
            showStartNoti(job);
        }
        
        @Override
        public void onDownloadFinished(int status, DownloadJob job) {
            Log.i(TAG, "service: got finished");
            mDownloadProvider.complete(status, job);
            showDownloadedNoti(status,job);
        }

        @Override
        public void onDownloadPaused(DownloadJob job) {
            Log.i(TAG, "service: got paused");
            showDownloadedNoti(DownloadDB.STATUS_PAUSED,job);
        }

        @Override
        public void onDownloadCancelled(DownloadJob job) {
            Log.i(TAG, "service: got cancelled");
            showDownloadedNoti(DownloadDB.STATUS_CANCELED,job);
        }
    };
    private void showNoti(String text,int icon, CharSequence title, int flag){
        Intent mIntent = new Intent(this, DownloadManActivity.class);
        AcApp.showNotification(mIntent, DOWNLOAD_NOTIFICATION_ID, text, icon, title, flag);
    }
    private void showDownloadedNoti(int status,DownloadJob job) {
        String contentText = job.getEntry().part.name;
        // FIXME: hard code
        String title = "";
        if (status == DownloadDB.STATUS_SUCCESS)
            title = "下载完成";
        else if (status == DownloadDB.STATUS_CANCELED)
            title = "已取消";
        else if (status == DownloadDB.STATUS_BAD_REQUEST || status == DownloadDB.STATUS_HTTP_DATA_ERROR)
            title = "网络错误";
        else if (status == DownloadDB.STATUS_PAUSED)
            title = "已暂停";
        else if (status == DownloadDB.STATUS_QUEUED_FOR_WIFI)
            title = "等待WIFI";
        else
            title = "下载失败 - 状态码：" + status;
        showNoti(contentText, android.R.drawable.stat_sys_download_done, title, Notification.FLAG_AUTO_CANCEL);

    }
    private void showStartNoti(DownloadJob job){
        String contentText = job.getEntry().part.name;
        showNoti(contentText, android.R.drawable.stat_sys_download, "开始下载",Notification.FLAG_NO_CLEAR);
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(sdCardMounted);
        Log.d(TAG, "Download Service Destory!!");
    }
    
    
}
