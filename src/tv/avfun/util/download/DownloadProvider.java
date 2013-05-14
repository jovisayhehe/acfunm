
package tv.avfun.util.download;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;


import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.download.exception.IllegalEntryException;
/**
 * Download jobs provider.
 * @author Yrom
 *
 */
public class DownloadProvider {
    private static final String TAG = "DownloadProvider";
    private List<DownloadJob> mQueuedJobs;
    private List<DownloadJob> mCompletedJobs;
    private DownloadManager mDownloadManager;
    private DownloadDB mDb;
    
    public DownloadProvider(Context context, DownloadManager manager) {
        this.mDownloadManager = manager;
        mQueuedJobs = new ArrayList<DownloadJob>();
        mCompletedJobs = new ArrayList<DownloadJob>();
        mDb = new DownloadDBImpl(context);
        loadOldDownloads();
    }

    private void loadOldDownloads() {
        List<DownloadJob> oldDownloads = mDb.getAllDownloads();
        for(DownloadJob j : oldDownloads){
            if(!j.isRunning()){
                mCompletedJobs.add(j); // complete
            }else
                // start failed job
                mDownloadManager.download(j.getEntry());
        }
        mDownloadManager.notifyObservers();
    }

    public List<DownloadJob> getAllDownloads() {
        // mDb.getAllDownloads();
        List<DownloadJob> all = new ArrayList<DownloadJob>();
        all.addAll(mCompletedJobs);
        all.addAll(mQueuedJobs);
        return all;
    }

    public List<DownloadJob> getCompletedDownloads() {
        return mCompletedJobs;
    }

    public List<DownloadJob> getQueuedDownloads() {
        return mQueuedJobs;
    }
    /**
     * mark job completed
     */
    public void complete(DownloadJob job) {
        mQueuedJobs.remove(job);
        mCompletedJobs.add(job);
        for(VideoSegment s :job.getEntry().part.segments){
            mDb.setStatus(job.getEntry().part.vid, s.num, DownloadDB.STATUS_SUCCESS);
        }
        mDownloadManager.notifyObservers();
    }
    public void update(String vid, int num, ContentValues values){
        mDb.updateDownload(vid,num,values);
    }
    public boolean enqueue(DownloadJob job) {
        if(contains(mCompletedJobs,job.getEntry().part)
                ||contains(mQueuedJobs,job.getEntry().part))
                return false;
        
        try {
            mDb.addDownload(job.getEntry());
            mQueuedJobs.add(job);
            mDownloadManager.notifyObservers();
            return true;
        } catch (IllegalEntryException e) {
            Log.e(TAG, "fail to enqueue",e);
            return false;
        }
    }
    /**
     * Cancel download job and remove it from db
     * @param job
     */
    public void removeDownload(DownloadJob job) {
        if(job.isRunning()){ 
            job.cancel();
            mQueuedJobs.remove(job);
        }else{
            mCompletedJobs.remove(job);
        }
        mDb.remove(job);
        mDownloadManager.notifyObservers();
    }
    private boolean contains(List<DownloadJob> jobs, VideoPart part){
        for(DownloadJob j : jobs){
            if(j.getEntry().part.equals(part)) 
                return true;
        }
        return false;
        
    }
    /**
     * Is video part available?
     */
    public boolean isPartDownloaded(VideoPart part){
        return contains(mCompletedJobs,part);
    }
}
