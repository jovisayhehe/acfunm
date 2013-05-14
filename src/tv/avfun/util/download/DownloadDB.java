
package tv.avfun.util.download;

import java.util.List;

import android.content.ContentValues;

import tv.avfun.util.download.exception.IllegalEntryException;

/**
 * The db to store all download jobs
 * 
 * @author Yrom
 * 
 */
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
    String COLUMN_VTYPE    = "vtype";
    String COLUMN_NUM      = "part_num";
    String COLUMN_STATUS   = "status";
    String COLUMN_ETAG     = "etag";
    String COLUMN_URL      = "url";
    String COLUMN_DEST     = "dest_path";
    String COLUMN_UA       = "useragent";
    String COLUMN_MIME     = "mimetype";
    String COLUMN_TOTAL    = "total_bytes";
    String COLUMN_CURRENT  = "current_bytes";

    int    STATUS_PENDING            = 190;
    int    STATUS_PAUSED             = 191;
    int    STATUS_QUEUED_FOR_WIFI    = 192;
    int    STATUS_RUNNING            = 193;
    int    STATUS_SUCCESS            = 200;
    int    STATUS_BAD_REQUEST        = 400;
    int    STATUS_CANCELED           = 420;
    int    STATUS_HTTP_DATA_ERROR    = 410;
    int    STATUS_CANNOT_RESUME      = 450;
    int    STATUS_TOO_MANY_REDIRECTS = 490;

    // ================================
    // CRUD
    // ================================

    void addDownload(DownloadEntry entry) throws IllegalEntryException;

    void setStatus(String vid, int num, int status);

    void setEtag(String vid, int num, String etag);
    
    int updateDownload(DownloadJob job);
    /**
     * @return -1, if no result
     */
    int getStatus(String vid, int num);

    List<DownloadJob> getAllDownloads();

    /**
     * 
     * @param job
     * @return the number of rows.
     */
    int remove(DownloadJob job);

    int updateDownload(String vid, int num, ContentValues values);

}
