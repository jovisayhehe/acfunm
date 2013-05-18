
package tv.avfun.util.download;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadTask.DownloadTaskListener;

/**
 * Job of download(TODO)
 * 
 * @author Yrom
 * 
 */
public class DownloadJob {

    protected static final String TAG = "DownloadJob";

    private DownloadEntry       mEntry;

    private int                 mStartId        = 0;
    private int                 mDownloadedSize = 0; // part总已下载
    private int                 mTotalSize      = 0; // part总量
    private DownloadJobListener mListener;
    private DownloadManager     mDownloadMan;
    private String              mUserAgent;
    private List<DownloadTask>  mTasks;
    private boolean             isRunning;


    public DownloadJob(DownloadEntry entry) {
        this(entry, 0);
    }
    public DownloadJob(DownloadEntry entry, int startId) {
        this(entry, startId, null);
    }
    /**
     * for test only
     * @param entry
     * @param startId
     * @param manager
     */
    public DownloadJob(DownloadEntry entry, int startId, DownloadManager manager){
        mEntry = entry;
        mStartId = startId;
        if(manager == null)
            mDownloadMan = AcApp.instance().getDownloadManager();
        else 
            mDownloadMan = manager;
        // FIXME: 从数据库中查到的job不会初始化Task！
        // initTask(); 
    }
    private void initTask() {
        mTasks = new LinkedList<DownloadTask>();
        int i=0;
        for (VideoSegment s : mEntry.part.segments) {
            DownloadInfo info = new DownloadInfo(mDownloadMan,
                    mEntry.aid, mEntry.part.vid, s.num, s.stream == null? s.url : s.stream,
                    mEntry.destination, s.fileName, mUserAgent, (int) s.size, s.etag);
            DownloadTask task = new DownloadTask(i,info);
            task.setDownloadTaskListener(mTaskListener);
            mTasks.add(i++, task);
        }
    }

    public void start() {
        initTask();
        
        notifyDownloadStarted();
        for (DownloadTask task : mTasks) {
            task.execute();
        }
    }
    public void cancel() {
        for (DownloadTask task : mTasks) {
            if (task.isCancelled())
                continue;
            task.cancel();
        }
    }

    public void pause() {
        for (DownloadTask task : mTasks) {
            task.pause();
        }
    }

    public void resume() {
        notifyDownloadStarted();
        if(mTasks == null) initTask();
        for (DownloadTask task : mTasks) {
            task.resume();
        }
    }

    /**
     * 重新开启
     */
    public void restart() {
        reset();
        start();
    }

    public void setListener(DownloadJobListener listener) {
        mListener = listener;
    }

    /**
     * 获得进度 (%)
     * 
     * @return
     */
    public int getProgress() {
        if (mTotalSize > 0) {
            // 避免数值溢出
            return (int)(mDownloadedSize * 100L / mTotalSize);
        }
        return 0;
    }
    public void setRunning(boolean running){
        isRunning = running;
    }
   
    /**
     * 添加到part总大小
     * 
     * @param totalSize
     */
    public void addTotalSize(int totalSize) {
        this.mTotalSize += totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.mTotalSize = totalSize;
    }
    public int getTotalSize(){
        return mTotalSize;
    }
    public int getDownloadedSize() {
        return mDownloadedSize;
    }

    public void setDownloadedSize(int downloadedSize) {
        this.mDownloadedSize = downloadedSize;
    }

    /**
     * 重置，清除下载数据
     */
    public void reset() {
        mDownloadedSize = 0;
    }

    /**
     * 添加到part已下载大小
     * 
     * @param downloadedSize
     */
    public void addDownloadedSize(int downloadedSize) {
        this.mDownloadedSize += downloadedSize;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public DownloadEntry getEntry() {
        return mEntry;
    }

    public void setEntry(DownloadEntry entry) {
        this.mEntry = entry;
    }

    public int getStartId() {
        return mStartId;
    }

    public void setStartId(int startId) {
        this.mStartId = startId;
    }

    public String getUserAgent() {
        return mUserAgent;
    }

    public void setUserAgent(String userAgent) {
        this.mUserAgent = userAgent;
    }

    public void notifyDownloadStarted() {
        if (mListener != null)
            mListener.onDownloadStarted(this);
    }

    public void notifyDownloadCompleted(int status) {
        if (mListener != null) {
            mListener.onDownloadFinished(status, this);
        }
        // TODO
    }

    /**
     * Download envent listener
     * 
     * @author Yrom
     */
    public interface DownloadJobListener {

        /**
         * Callback when a download finished
         */
        public void onDownloadFinished(int status, DownloadJob job);

        /**
         * Callback when a download started
         */
        public void onDownloadStarted(DownloadJob job);

    }
    
    private DownloadTaskListener mTaskListener = new DownloadTaskListener() {
        int lastUpdateTaskId = -1;
        @Override
        public void onStart(DownloadTask task) {

            isRunning = true;
        }
        
        @Override
        public void onRetry(DownloadTask task) {
            // TODO Auto-generated method stub
            //notifyDownloadStarted();
            isRunning =true;
        }
        
        @Override
        public void onResume(DownloadTask task) {
            // TODO Auto-generated method stub
            isRunning = true;
        }
        
        @Override
        public void onProgress(int bytesRead, DownloadTask task) {
            int total = task.getTotalBytes();
            if(total < 0) return;
            if(lastUpdateTaskId == -1){
                if(mTotalSize<0){
                    addTotalSize(task.getTotalBytes());
                    lastUpdateTaskId = task.getId();
                }
            }else if(lastUpdateTaskId != task.getId()){
                addTotalSize(task.getTotalBytes());
                lastUpdateTaskId = task.getId();
            }
            if(bytesRead > 0){
                addDownloadedSize(bytesRead);
//                mProgress += currentBytes * 100 / task.getTotalBytes();
                mDownloadMan.notifyAllObservers();
            }
        }
        
        @Override
        public void onPause(DownloadTask task) {
            isRunning =false;
//            mDownloadMan.notifyAllObservers();
        }
        
        @Override
        public void onCompleted(int status, DownloadTask task) {
            isRunning = false;
            notifyDownloadCompleted(status);
        }
        
        @Override
        public void onCancel(DownloadTask task) {
            Log.i(TAG, "canel");
            mDownloadMan.notifyAllObservers();
            
        }
    };
    @Override
    public String toString() {
        return "[aid=" + mEntry.aid + ", vid=" + mEntry.part.vid + ", bytes=" + mDownloadedSize
                + ", TotalSize=" + mTotalSize + "]";
    }
    
    
}
