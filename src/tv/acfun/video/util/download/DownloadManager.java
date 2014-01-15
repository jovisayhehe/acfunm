
package tv.acfun.video.util.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.acfun.video.AcApp;
import tv.acfun.video.DownloadService;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.entity.VideoSegment;
import tv.acfun.video.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Manager
 * 
 * @author Yrom
 * 
 */
public class DownloadManager {
    
    public static final int        ON_STARTED   = 0;
    public static final int        ON_PROGRESS  = 1;
    
    private Context mContext;
    private List<DownloadObserver> mObservers;
    private DownloadProvider mProvider;
    public DownloadManager(Context context) {
        mContext = context;
        mObservers = new ArrayList<DownloadObserver>();
        mProvider = new DownloadProvider(context, this);
    }
    public boolean isRequestWifi = true;
    public DownloadProvider getProvider() {
        return mProvider;
    }
    /**
     * Start download
     */
    public void download(DownloadEntry entry){
        download(null,entry);
    }
    public void download(String ua, DownloadEntry entry){
        Intent service = new Intent(mContext,DownloadService.class);
        service.setAction(DownloadService.ACTION_ADD_TO_DOWNLOAD);
        service.putExtra(DownloadService.EXTRA_DOWNLOAD_ENTRY, entry);
        service.putExtra(DownloadService.EXTRA_DOWNLOAD_UA, ua);
        mContext.startService(service);
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
     * @param aid
     * @return null, if not found in db
     */
    public List<VideoPart> getVideoParts(String aid){
        return mProvider.getVideoParts(aid);
    }
    /**
     * Return queued download jobs.
     */
    public List<DownloadJob> getQueuedDownloads(){
        return mProvider.getQueuedDownloads();
    }
    public DownloadEntry getEntryByVid(String vid){
        return mProvider.getQueueJobByVid(vid).getEntry();
    }
    
    public void cancel(String vid, boolean shouldDeleFile) {
        DownloadJob job = mProvider.getQueueJobByVid(vid);
        if(job == null) return;
        job.cancel();
        if(shouldDeleFile)
            deleteDownload(job);
    }
    /**
     * Delete the download job and related files.
     */
    public void deleteDownload(DownloadJob job){
        if(job == null) throw new NullPointerException("job cannot be null");
        mProvider.removeDownload(job);
        deleteDownloadFile(job);
    }

    private void deleteDownloadFile(DownloadJob job) {
        String path = job.getEntry().destination;
        if(TextUtils.isEmpty(path))
            path = AcApp.getDownloadPath(job.getEntry().aid, job.getEntry().part.sourceId).getAbsolutePath();
        for(VideoSegment s : job.getEntry().part.segments){
            String fileName = s.fileName;
            if(TextUtils.isEmpty(fileName)){
                fileName = s.num +FileUtil.getUrlExt(s.stream);
            }
            new File(path,fileName).delete();
        }
        File f = new File(path);
        if(f.exists()) {
            f.delete();
            File p = f.getParentFile();
            if(p != null && p.listFiles().length == 0)
                p.delete();
        }
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
    public synchronized void notifyAllObservers(int what){
        for(DownloadObserver o : mObservers){
            o.onDownloadChanged(what);
        }
    }
    /**
     * Observe download envent
     * @author Yrom
     *
     */
    public interface DownloadObserver {

        void onDownloadChanged(int what);

    }

    public static boolean isRunningStatus(int status){
        return status>=DownloadDB.STATUS_PENDING && status <= DownloadDB.STATUS_RUNNING;
    }
    
    public static boolean isErrorStatus(int status){
        return status >= DownloadDB.STATUS_BAD_REQUEST;
    }
}
