package tv.avfun.util.download;


public class DownloadJob {
    private DownloadEntry entry;
    private int startId;
    private int progress;
    private int downloadedSize;
    
    
    public DownloadEntry getEntry() {
        return entry;
    }

    
    public void setEntry(DownloadEntry entry) {
        this.entry = entry;
    }

    
    public int getStartId() {
        return startId;
    }

    
    public void setStartId(int startId) {
        this.startId = startId;
    }

    
    public int getProgress() {
        return progress;
    }

    
    public void setProgress(int progress) {
        this.progress = progress;
    }

    
    public int getDownloadedSize() {
        return downloadedSize;
    }

    
    public void setDownloadedSize(int downloadedSize) {
        this.downloadedSize = downloadedSize;
    }
    
}
