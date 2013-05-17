
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
    private int                 mProgress       = 0; // percent
    private int                 mDownloadedSize = 0; // part总已下载
    private int                 mTotalSize      = 0; // part总量
    private DownloadJobListener mListener;
    private DownloadManager     mDownloadMan;
    private String              mUserAgent;
    private List<DownloadTask>  mTasks;


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
        initTask(); 
    }
    private void initTask() {
        if (mTasks != null) {
            cancel();
            mTasks.clear();
        } else
            mTasks = new LinkedList<DownloadTask>();
        for (VideoSegment s : mEntry.part.segments) {
            DownloadInfo info = new DownloadInfo(mDownloadMan,
                    mEntry.aid, mEntry.part.vid, s.num, s.stream == null? s.url : s.stream,
                    mEntry.destination, s.fileName, mUserAgent, (int) s.size, s.etag);
            DownloadTask task = new DownloadTask(info);
            task.setDownloadTaskListener(mTaskListener);
            mTasks.add(task);
        }
    }

    public void start() {
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
        if (mProgress == 0 && mTotalSize > 0) {
            mProgress = mDownloadedSize * 100 / mTotalSize;
        }
        return mProgress;
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
        mTotalSize = 0;
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
        return getProgress() < 100;
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
        mProgress = 0;
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
        
        @Override
        public void onStart(DownloadTask task) {
            // TODO Auto-generated method stub
        }
        
        @Override
        public void onRetry(DownloadTask task) {
            // TODO Auto-generated method stub
            notifyDownloadStarted();
        }
        
        @Override
        public void onResume(DownloadTask task) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onProgress(int currentBytes, DownloadTask task) {
            if(task.getTotalBytes() >0 && currentBytes>0){
                addDownloadedSize(currentBytes);
                mProgress += currentBytes * 100 / task.getTotalBytes();
                mDownloadMan.notifyAllObservers();
            }
            Log.i(TAG, "receive progress :"+currentBytes +"/"+task.getTotalBytes());
        }
        
        @Override
        public void onPause(DownloadTask task) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void onCompleted(int status, DownloadTask task) {
            notifyDownloadCompleted(status);
        }
        
        @Override
        public void onCancel(DownloadTask task) {
            mDownloadMan.notifyAllObservers();
            
        }
    };
    @Override
    public String toString() {
        return "[aid=" + mEntry.aid + ", vid=" + mEntry.part.vid + ", bytes=" + mDownloadedSize
                + ", TotalSize=" + mTotalSize + "]";
    }
    
    
}
