
package tv.avfun.util.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.avfun.DownloadService;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
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
        Intent service = new Intent(mContext,DownloadService.class);
        service.setAction(DownloadService.ACTION_ADD_TO_DOWNLOAD);
        service.putExtra(DownloadService.EXTRA_DOWNLOAD_ENTRY, entry);
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
        mProvider.removeDownload(job);
        deleteDownloadFile(job);
    }

    private void deleteDownloadFile(DownloadJob job) {
        String path = job.getEntry().destination;
        if(TextUtils.isEmpty(path))
            path = AcApp.getDownloadPath(job.getEntry().aid, job.getEntry().part.vid).getAbsolutePath();
        for(VideoSegment s : job.getEntry().part.segments){
            String fileName = s.fileName;
            if(TextUtils.isEmpty(fileName)){
                fileName = s.num +FileUtil.getUrlExt(s.stream);
            }
            new File(path,fileName).delete();
        }
        File f = new File(path);
        f.delete();
        File p = f.getParentFile();
        if(p.listFiles().length == 0)
            p.delete();
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

    public static boolean isRunningStatus(int status){
        return status>=DownloadDB.STATUS_PENDING && status <= DownloadDB.STATUS_RUNNING;
    }
    
    public static boolean isErrorStatus(int status){
        return status >= DownloadDB.STATUS_BAD_REQUEST;
    }
}
