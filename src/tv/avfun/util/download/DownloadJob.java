
package tv.avfun.util.download;

import java.util.LinkedList;
import java.util.List;

import tv.avfun.entity.VideoSegment;

/**
 * Job of download(TODO)
 * 
 * @author Yrom
 * 
 */
public class DownloadJob {

    private DownloadEntry       mEntry;

    private int                 mStartId        = 0;
    private int                 mProgress       = 0; // percent
    private int                 mDownloadedSize = 0; // part总已下载
    private int                 mTotalSize      = 0; // part总量
    private DownloadJobListener mListener;
    private DownloadManager     mDownloadMan;
    private String              userAgent;
    private List<DownloadTask>  mTasks;

    public DownloadJob(DownloadEntry entry) {
        this(entry, 0);
    }

    public DownloadJob(DownloadEntry entry, int startId) {
        mEntry = entry;
        mStartId = startId;
        initTask();
    }

    private void initTask() {
        if (mTasks != null) {
            cancel();
            mTasks.clear();
        } else
            mTasks = new LinkedList<DownloadTask>();
        for (VideoSegment s : mEntry.part.segments) {
            DownloadTask task = new DownloadTask(new DownloadInfo(mEntry.aid, mEntry.part.vid, s.num, mDownloadMan));
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
        if (mTotalSize > 0) {
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
        return mProgress < 100;
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
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void notifyDownloadStarted() {
        if (mListener != null)
            mListener.onDownloadStarted();
        mProgress = 0;
    }

    public void notifyDownloadCompleted(boolean result) {
        if (mListener != null) {
            if (result)
                mListener.onDownloadSuccess(this);
            else
                mListener.onDownloadError(this);
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
        public void onDownloadSuccess(DownloadJob job);

        /**
         * Callback when a download failed
         */
        public void onDownloadError(DownloadJob job);

        /**
         * Callback when a download started
         */
        public void onDownloadStarted();

    }
}
