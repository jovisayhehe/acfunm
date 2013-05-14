
package tv.avfun.util.download;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * Manager(TODO)
 * 
 * @author Yrom
 * 
 */
public abstract class DownloadManager {
    private Context mContext;
    private List<DownloadObserver> mObservers;
    private DownloadProvider provider;
    public DownloadManager(Context context) {
        mContext = context;
        mObservers = new ArrayList<DownloadObserver>();
        provider = new DownloadProvider(context, this);
    }
    
    
    public DownloadProvider getProvider() {
        return provider;
    }
    /**
     * Start download
     */
    public abstract void download(DownloadEntry entry);

    /**
     * Return all download jobs.
     */
    public abstract ArrayList<DownloadJob> getAllDownloads();

    /**
     * Return completed download jobs.
     */
    public abstract ArrayList<DownloadJob> getCompletedDownloads();

    /**
     * Return queued download jobs.
     */
    public abstract ArrayList<DownloadJob> getQueuedDownloads();

    /**
     * Delete the download job and related files.
     */
    public abstract void deleteDownload(DownloadJob job);

    /**
     * Add passed object to the download observers list
     */
    public abstract void registerDownloadObserver(DownloadObserver observer);

    /**
     * Remove passed object to the download observers list
     */
    public abstract void unregisterDownloadObserver(DownloadObserver observer);

    /**
     * Notifie all observers that the state of the downloads has changed.
     */
    public abstract void notifyObservers();
    /**
     * Observe download envent
     * @author Yrom
     *
     */
    public interface DownloadObserver {

        void onDownloadChanged(DownloadManager manager);

    }

}
