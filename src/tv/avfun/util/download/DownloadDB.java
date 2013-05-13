
package tv.avfun.util.download;

import java.util.List;

public interface DownloadDB {

    // =============================
    // constants
    // =============================
    int    VERSION         = 1;
    String DOWNLOAD_DB     = "download.db";
    String DOWNLOAD_TABLE  = "downloads";

    String COLUMN_ID       = "_id";
    String COLUMN_AID      = "aid";
    String COLUMN_TITLE    = "title";
    String COLUMN_SUBTITLE = "subtitle";
    String COLUMN_VID      = "vid";
    String COLUMN_NUM      = "part_num";
    String COLUMN_STATUS   = "status";
    String COLUMN_ETAG     = "etag";
    String COLUMN_URL      = "url";
    String COLUMN_DEST     = "dest_path";
    String COLUMN_UA       = "useragent";
    String COLUMN_MIME     = "mimetype";
    String COLUMN_TOTAL    = "total_bytes";
    String COLUMN_CURRENT  = "current_bytes";

    int    STATUS_PENDING  = 100;
    int    STATUS_PAUSED   = 101;
    int    STATUS_RUNNING  = 102;
    int    STATUS_SUCCESS  = 200;
    int    STATUS_ERROR    = 400;

    // ================================
    // CRUD
    // ================================

    void addToDownload(DownloadEntry entry);

    void setStatus(String vid,int num, int status);

    void setEtag(String vid,int num, String etag);
    
    /**
     * 分段是否下载完成
     * @param vid 分p vid
     * @param num 分段在分p中索引
     * @return
     */
    boolean isDownloaded(String vid, int num);
    
    List<DownloadJob> getAllDownloads();

    void remove(DownloadJob job);

}
