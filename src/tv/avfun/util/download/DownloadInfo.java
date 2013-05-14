
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
    public int totalBytes = -1;
    /** segment num in part*/
    public int snum;
    public int retryTimes;
    public DownloadManager manager;
    public String etag;
    /**
     * @param aid ac id
     * @param vid video part id
     * @param snum segment num in video part
     */
    public DownloadInfo(String aid, String vid, int snum, DownloadManager manager) {
        this.aid = aid;
        this.vid = vid;
        this.snum = snum;
        this.manager = manager;
    }
    
}
