
package tv.avfun.util.download;
/**
 * 下载信息。从数据库中查到的Job、或是新建的Job中抽出来的
 * @author Yrom
 *
 */
public class DownloadInfo {
    public String aid;
    public String vid;
    public String userAgent;
    public String url;
    public String savePath;
    public String fileName;
    public int totalBytes = -1;
    /** segment num in part*/
    public int snum;
    public int retryTimes;
    public DownloadManager manager;
    public String etag;
    /**
     * @param manager the manager
     * @param aid ac id
     * @param vid video part id
     * @param snum segment num in video part
     * @param url segment download url
     * @param fileName segment file name,can be null
     * @param savePath can be null
     * @param userAgent can be null
     * @param totalBytes -1 means resolve content-length by task itself
     * @param etag can be null
     */
    public DownloadInfo(DownloadManager manager, String aid, String vid, int snum, String url, String savePath,
            String fileName, String userAgent, int totalBytes, String etag) {
        this.manager = manager;
        this.aid = aid;
        this.vid = vid;
        this.snum = snum;
        this.url = url;
        this.savePath = savePath;
        this.fileName = fileName;
        this.userAgent = userAgent;
        this.totalBytes = totalBytes;
        this.etag = etag;
    }
    
    
}
