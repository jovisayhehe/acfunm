
package tv.avfun.util.download;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.DownloadTask.DownloadTaskListener;

/**
 * Job of download
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

    private int mStatus;


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
    }
    private void initTask() {
        if(mTasks == null)
            mTasks = new LinkedList<DownloadTask>();
        else{
            mTasks.clear();
        }
        mTotalSize = 0;
        int i=0;
        for (VideoSegment s : mEntry.part.segments) {
            DownloadInfo info = new DownloadInfo(mDownloadMan,
                    mEntry.aid, mEntry.part.vid, s.num, s.stream == null? s.url : s.stream,
                    mEntry.destination, s.fileName, mUserAgent, (int) s.size, s.etag);
            mTotalSize += s.size;
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
            if (task.isCancelled)
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
        start();
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
    public int getStatus(){
        return mStatus;
    }
    public void setStatus(int status){
        this.mStatus = status;
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
            if(status == DownloadDB.STATUS_PAUSED)
                mListener.onDownloadPaused(this);
            else if(status == DownloadDB.STATUS_CANCELED)
                mListener.onDownloadCancelled(this);
            else 
                mListener.onDownloadFinished(status, this);
        }
        mDownloadMan.notifyAllObservers();
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
        
        public void onDownloadCancelled(DownloadJob job);

        public void onDownloadPaused(DownloadJob job);
        /**
         * Callback when a download started
         */
        public void onDownloadStarted(DownloadJob job);

    }
    
    int lastUpdateTaskId = -1;
    private DownloadTaskListener mTaskListener = new DownloadTaskListener() {
        @Override
        public void onStart(DownloadTask task) {
            lastUpdateTaskId = -1;
            mStatus = DownloadDB.STATUS_PENDING;
            if(task.getLocalUri() != null && 
                    !mEntry.part.segments.get(task.getId()).url.equals(task.getLocalUri()))
                mEntry.part.segments.get(task.getId()).url = task.getLocalUri();
        }
        
        @Override
        public void onRetry(DownloadTask task) {
            //notifyDownloadStarted();
            mStatus = DownloadDB.STATUS_PENDING;
        }
        
        @Override
        public void onResume(DownloadTask task) {
            mStatus = DownloadDB.STATUS_PENDING;
        }
        
        @Override
        public void onProgress(int bytesRead, DownloadTask task) {
            mStatus = DownloadDB.STATUS_RUNNING;
            int total = task.getTotalBytes();
            if(total < 0) return;
            if(lastUpdateTaskId == -1){
                if(mTotalSize<=0){              // 保证TotalSize只被初始化一次
                                                // FIXME:优化逻辑
                    addTotalSize(task.getTotalBytes());
                    lastUpdateTaskId = task.getId();
                }
            }else if(lastUpdateTaskId != task.getId()){
                addTotalSize(task.getTotalBytes());
                lastUpdateTaskId = task.getId();
            }
            if(bytesRead > 0){
                addDownloadedSize(bytesRead);
                mDownloadMan.notifyAllObservers();
            }
        }
        
        @Override
        public void onPause(DownloadTask task) {
            mStatus = DownloadDB.STATUS_PAUSED;
//            mDownloadMan.notifyAllObservers();
        }
        
        @Override
        public void onCompleted(int status, DownloadTask task) {
            notifyDownloadCompleted(status);
            mStatus = status;
        }
        
        @Override
        public void onCancel(DownloadTask task) {
            Log.i(TAG, task +" caneled");
//            notifyDownloadCompleted(DownloadDB.STATUS_CANCELED);
            
        }
    };
    @Override
    public String toString() {
        return "[aid=" + mEntry.aid + ", vid=" + mEntry.part.vid + ", bytes=" + mDownloadedSize
                + ", TotalSize=" + mTotalSize + "]";
    }
    
    
}
