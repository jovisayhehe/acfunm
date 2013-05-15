
package tv.avfun.util.download;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.app.AcApp;

import android.content.Context;
import android.text.TextUtils;

/**
 * Manager(TODO)
 * 
 * @author Yrom
 * 
 */
public class DownloadManager {
    private Context mContext;
    private List<DownloadObserver> mObservers;
    private DownloadProvider mProvider;
    public DownloadManager(Context context) {
        mContext = context;
        mObservers = new ArrayList<DownloadObserver>();
        mProvider = new DownloadProvider(context, this);
    }
    
    
    public DownloadProvider getProvider() {
        return mProvider;
    }
    /**
     * Start download
     */
    public void download(DownloadEntry entry){
        //TODO
    }

    /**
     * Return all download jobs.
     */
    public List<DownloadJob> getAllDownloads(){
        return mProvider.getAllDownloads();
    }

    /**
     * Return completed download jobs.
     */
    public List<DownloadJob> getCompletedDownloads(){
        return mProvider.getCompletedDownloads();
    }

    /**
     * Return queued download jobs.
     */
    public List<DownloadJob> getQueuedDownloads(){
        return mProvider.getQueuedDownloads();
    }

    /**
     * Delete the download job and related files.
     */
    public void deleteDownload(DownloadJob job){
        mProvider.removeDownload(job);
        deleteDownloadFile(job);
    }

    private void deleteDownloadFile(DownloadJob job) {
        // TODO Auto-generated method stub
        String path = job.getEntry().destination;
        if(TextUtils.isEmpty(path))
            path = AcApp.getDownloadPath(job.getEntry().aid, job.getEntry().part.vid).getAbsolutePath();
        String fileName;
    }


    /**
     * Add passed object to the download observers list
     */
    public synchronized void registerDownloadObserver(DownloadObserver observer){
        mObservers.add(observer);
    }

    /**
     * Remove passed object to the download observers list
     */
    public synchronized void unregisterDownloadObserver(DownloadObserver observer){
        mObservers.remove(observer);
    }

    /**
     * Notifie all observers that the state of the downloads has changed.
     */
    public synchronized void notifyAllObservers(){
        for(DownloadObserver o : mObservers){
            o.onDownloadChanged(this);
        }
    }
    /**
     * Observe download envent
     * @author Yrom
     *
     */
    public interface DownloadObserver {

        void onDownloadChanged(DownloadManager manager);

    }

}
