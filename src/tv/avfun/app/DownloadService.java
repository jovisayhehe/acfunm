
package tv.avfun.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tv.avfun.BuildConfig;
import tv.avfun.api.net.UserAgent;
import tv.avfun.db.DBService;
import tv.avfun.entity.VideoInfo.VideoItem;
import tv.avfun.util.ArrayUtil;
import tv.avfun.util.FileUtil;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DownloadService extends Service {

    private static final String TAG                     = DownloadService.class.getSimpleName();
    
    public static final Uri     CONTENT_URI             = Uri.parse("content://downloads/my_downloads");
    public static final String  ACTION_VIEW_PROGRESS    = "tv.avfun.action.VIEW_PROGRESS";
    public static final String  ACTION_DOWNLOAD_SUCCESS = "tv.avfun.action.DOWNLOAD_SUCCESS";
    public static final String  ACTION_DOWNLOAD_FAIL    = "tv.avfun.action.DOWNLOAD_FAIL";
    public static final String  ACTION_DOWNLOAD_START   = "tv.avfun.action.DOWNLOAD_START";
    public static final String  ACTION_DOWNLOAD_ENQUEUE = "tv.avfun.action.ENQUEUE";
    public static final String  EXTRAS_ITEM_VID         = "item_vid";

    public static final int     STATUS_SUCCESS          = 200;
    public static final int     STATUS_PENDING          = 190;
    public static final int     STATUS_RUNNING          = 192;
    public static final int     STATUS_PAUSED           = 193;
    public static final int     STATUS_ERROR            = 400;
    
    private DownloadManager     dm;
    private boolean             isBound;

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 绑定服务，意味着，可能需要下载
        isBound = true;
        new Thread(new DownloadTask()).start();
        return new IDownloadBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        for (Map.Entry<String, Boolean> e : vids.entrySet()) {
            e.setValue(true);
        }
        pool.stop();
        unregisterReceiver(receiver);
        receiver = null;
    }

    private CompletedReceiver           receiver = new CompletedReceiver();
    private BlockingQueue<DownloadInfo> queue    = new LinkedBlockingQueue<DownloadInfo>(1);
    private Map<String, Boolean>        vids     = Collections.synchronizedMap(new HashMap<String, Boolean>());
    

    private static ThreadPool           pool     = new ThreadPool(10);
    protected long interval = 1000;

    protected void startDownload(DownloadInfo dinfo) {

        File file = AcApp.getDownloadPath(dinfo.aid, dinfo.item.vid);
        file.mkdirs();
        if (dm == null)
            dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        dinfo.item.downloadIDs = new ArrayList<Long>();
        for (int i = 0; i < dinfo.item.urlList.size(); i++) {

            String url = dinfo.item.urlList.get(i);
            String filename = i + FileUtil.getUrlExt(url);
            Request request = new Request(Uri.parse(url));
            request.setAllowedNetworkTypes(Request.NETWORK_WIFI).setAllowedOverRoaming(false)
                    .setDescription(dinfo.item.subtitle);

            int len = 10;
            if (dinfo.item.subtitle.length() < len)
                len = dinfo.item.subtitle.length();
            request.setTitle(dinfo.item.subtitle.substring(0, len) + "_" + filename)
                    .addRequestHeader("User-Agent", UserAgent.DEFAULT)
                    .setDestinationInExternalPublicDir("Download/AcFun/Videos/" + dinfo.aid + "/" + dinfo.item.vid,
                            filename);
            dinfo.item.downloadIDs.add(dm.enqueue(request));
            pool.post(new QueryTask(dinfo.item));
        }

    }

    /**
     * 插入下载队列，成功，发送 ACTIONO_DOWNLOAD_START ，
     */
    public void enqueue(String aid, VideoItem item) {
        if (item == null || item.urlList == null || item.urlList.isEmpty())
            throw new IllegalArgumentException("item 验证不通过");
        // 排队
        sendBroadcast(new Intent(ACTION_DOWNLOAD_ENQUEUE));
        pool.post(new Poster(new DownloadInfo(aid, item)));

    }

    protected void queryProgress(String vid, List<Long> ids) {
        Cursor c = getContentResolver().query(Uri.parse("content://downloads/my_downloads"), null,
                Downloader.getWhereClauseForIds(ids), Downloader.getWhereArgsForIds(ids), null);
        if (c == null) {
            Log.i(TAG, "cursor =null !");
            return;
        }
        int progress = 0;
        int totalbytes = 0;
        while (c.moveToNext()) {
            // TODO 过滤状态
            int status = c.getInt(c.getColumnIndex("status"));
            long id = c.getLong(c.getColumnIndex("_id"));
            boolean b = true;
            if (status == STATUS_SUCCESS) {
                new DBService(getApplicationContext()).changeDownloadState(id, 3);
                b &= true; // 下载成功，标记为true
                progress += c.getInt(c.getColumnIndex("current_bytes")); // 累计所有任务当前下载量，即为总进度
            } else if (status >= STATUS_ERROR) {
                new DBService(getApplicationContext()).changeDownloadState(id, 2);
                b &= true; // 下载失败，也标记true
                Intent intent = new Intent(ACTION_DOWNLOAD_FAIL); // TODO
                                                                  // 有一个任务失败了，应该提示用户重试
                intent.putExtra("vid", vid);
                sendBroadcast(intent);
            } else if (status == STATUS_PAUSED) {
                new DBService(getApplicationContext()).changeDownloadState(id, 1);
                b &= false; // 下载暂停，标记任务为false
                progress += c.getInt(c.getColumnIndex("current_bytes"));
            } else if (status == STATUS_RUNNING) {
                new DBService(getApplicationContext()).changeDownloadState(id, 0);
                b &= false; // 只要有一个任务在执行，标记为false
                progress += c.getInt(c.getColumnIndex("current_bytes"));
            } else if (status == STATUS_PENDING) {
                new DBService(getApplicationContext()).changeDownloadState(id, 0);
                b = false;
            }
            if (BuildConfig.DEBUG) { // print debug info
                Log.d(TAG, "_id=" + id);
                Log.d(TAG,
                        "Progress: " + c.getInt(c.getColumnIndex("current_bytes")) + "/"
                                + c.getInt(c.getColumnIndex("total_bytes")));
                Log.d(TAG, "Download status :  " + status);
            }
            int bytes = c.getInt(c.getColumnIndex("total_bytes"));
            if (bytes > 0)
                totalbytes += bytes;
            vids.put(vid, b);
        }
        c.close();
        // send the progress broadcast to reciever
        int total = new DBService(getApplicationContext()).getTotalSize(vid);
        if (total < 0)
            total = totalbytes; // 数据库里没有准确的长度信息，只能将不确定的长度赋予它
        if (progress > 0) {
            if (progress == total) { // 累计的总进度 与总大小 相等了，downloaded
                Intent intent = new Intent();
                intent.setAction(ACTION_DOWNLOAD_SUCCESS);
                intent.putExtra("vid", vid);
                sendBroadcast(intent);
            } else { // send progress
                Intent intent = new Intent();
                intent.setAction(ACTION_VIEW_PROGRESS);
                intent.putExtra("vid", vid);
                intent.putExtra("progress", progress);
                intent.putExtra("total", total);
                sendBroadcast(intent);
            }
        }

    }

    private class DownloadTask implements Runnable {

        @Override
        public void run() {
            try {
                while (isBound) {
                    startDownload(queue.take());
                }
            } catch (InterruptedException ex) {}

        }

    }

    /** 阻塞，直到put成功，发布 ACTIONO_DOWNLOAD_START。 */
    private class Poster implements Runnable {

        DownloadInfo dinfo;

        public Poster(DownloadInfo dinfo) {
            this.dinfo = dinfo;
        }

        @Override
        public void run() {
            try {
                queue.put(dinfo);
                Intent intent = new Intent(ACTION_DOWNLOAD_START);
                intent.putExtra("vid", dinfo.item.vid);
                sendBroadcast(intent);
            } catch (InterruptedException e) {}
        }

    }
    /** download service */
    public class IDownloadBinder extends Binder {

        /**
         * 下载！到下载队列中排队
         * @param aid
         * @param item
         */
        public void download(String aid, VideoItem item){
            DownloadService.this.enqueue(aid, item);
        }
        /** 设置进度更新频率 */
        public void setInterval(long interval){
            DownloadService.this.interval  = interval;
        }
        /** 取消下载 */
        public void cancel(String vid){
            if (dm == null)
                dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            List<Long> downloadIds = new DBService(getApplicationContext()).getDownloadIds(vid);
            long[] array = ArrayUtil.toLongArray(downloadIds);
            
            if(array != null){
                int n = dm.remove(array);
                if(n>0){
                    Toast.makeText(getApplicationContext(), "删除成功", 0).show();
                }
            }
            vids.remove(vid);
            new DBService(getApplicationContext()).removeDownload(vid);
        }
        /** 是否下载完毕 */
        public boolean isComplete(String vid){
            return Downloader.isDownloaded(getApplicationContext(), vid); 
        }
        /** 是否正在下载 */
        public boolean isDownloading(String vid){
            return Downloader.isDownloading(getApplicationContext(), vid);
        }
        
    }

    private class QueryTask extends TimerTask {

        String     vid;
        List<Long> downloadIDs;

        public QueryTask(VideoItem item) {
            this.vid = item.vid;
            downloadIDs = item.downloadIDs;
            // 不应为空的！
            if (downloadIDs == null || downloadIDs.isEmpty())
                throw new IllegalStateException(vid+" 的download id 被吃掉了！！！");
            vids.put(vid, false);
        }

        @Override
        public void run() {
            while (vids.get(vid)!= null && !vids.get(vid)) {
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "------- querying status ---------");
                queryProgress(vid, downloadIDs);
                try {
                    Thread.sleep(DownloadService.this.interval);
                } catch (InterruptedException e) {}
            }
        }

    }

    private class CompletedReceiver extends BroadcastReceiver {

        private volatile int mNumCompleted = 0;
        private Set<Long>    downloadIds   = Collections.synchronizedSet(new HashSet<Long>());

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                synchronized (this) {
                    long id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                    if (BuildConfig.DEBUG)
                        Log.i(TAG, "Received Notification for download: " + id);
                    if (!downloadIds.contains(id)) {
                        ++mNumCompleted;
                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "CompletedReceiver got intent: " + intent.getAction() + " --> total count: "
                                    + mNumCompleted);
                        downloadIds.add(id);
                        if (dm == null)
                            dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                        Cursor cursor = dm.query(new Query().setFilterById(id));
                        try {
                            if (cursor.moveToFirst()) {
                                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    String title = cursor
                                            .getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                                    new DBService(getApplicationContext()).changeDownloadState(id, 3);
                                    Toast.makeText(getApplicationContext(), title + " - 下载完成", 0).show();

                                } else if (status == DownloadManager.STATUS_FAILED)
                                    new DBService(getApplicationContext()).changeDownloadState(id, 2);
                                if (BuildConfig.DEBUG)
                                    Log.i(TAG, "Download status is: " + status);
                            } else {
                                if (BuildConfig.DEBUG)
                                    Log.w(TAG, "No status found for completed download!");
                            }
                        }
                        finally {
                            cursor.close();
                        }
                    } else {
                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "Notification for id: " + id + " has already been made.");
                    }
                }
            }
        }

        /**
         * Gets the number of times the {@link #onReceive} callback has been
         * called for the {@link DownloadManager.ACTION_DOWNLOAD_COMPLETED}
         * action, indicating the number of downloads completed thus far.
         * 
         * @return the number of downloads completed so far.
         */
        public int numDownloadsCompleted() {
            return mNumCompleted;
        }

        /**
         * Gets the list of download IDs.
         * 
         * @return A Set<Long> with the ids of the completed downloads.
         */
        public Set<Long> getDownloadIds() {
            synchronized (this) {
                Set<Long> returnIds = new HashSet<Long>(downloadIds);
                return returnIds;
            }
        }

    }

    static class DownloadInfo {

        String    aid;
        VideoItem item;

        DownloadInfo(String aid, VideoItem item) {
            this.aid = aid;
            this.item = item;
        }
    }

    @Override
    public void onLowMemory() {
        System.gc();
    }
}
