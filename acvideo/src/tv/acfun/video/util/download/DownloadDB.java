
package tv.acfun.video.util.download;

import java.util.List;

import android.content.ContentValues;


/**
 * The db storing all download jobs
 * 
 * @author Yrom
 * 
 */
public interface DownloadDB {

    // =============================
    // constants
    // =============================
    int    VERSION         = 5;
    String DOWNLOAD_DB     = "download.db";
    String DOWNLOAD_TABLE  = "downloads";
    /** download job's id*/
    String COLUMN_ID       = "_id";
    String COLUMN_AID      = "aid";
    String COLUMN_TITLE    = "title";
    String COLUMN_SUBTITLE = "subtitle";
    String COLUMN_VID      = "vid";
    String COLUMN_VTYPE    = "vtype";
    String COLUMN_CID      = "cid";
    /** num in part */
    String COLUMN_NUM      = "part_num";
    /**
     * @see STATUS_* 
     */
    String COLUMN_STATUS   = "status";
    String COLUMN_ETAG     = "etag";
    String COLUMN_URL      = "url";
    /** save path */
    String COLUMN_DEST     = "dest_path";
    /** user-agent header*/
    String COLUMN_UA       = "useragent";
    /** content-type header*/
    String COLUMN_MIME     = "mimetype";
    /** file length */
    String COLUMN_TOTAL    = "total_bytes";
    /** downloaded bytes */
    String COLUMN_CURRENT  = "current_bytes";
    /** media durationo */
    String COLUMN_DURATION = "duration";
    /** file name */
    String COLUMN_DATA     = "_data"; 
    
    int    STATUS_PENDING            = 190;
    int    STATUS_PAUSED             = 191;
    /** wifi !*/
    int    STATUS_QUEUED_FOR_WIFI    = 192;
    int    STATUS_RUNNING            = 193;
    int    STATUS_SUCCESS            = 200;
    /** download request error */
    int    STATUS_BAD_REQUEST        = 400;
    int    STATUS_CANCELED           = 420;
    /** illegal download url */  
    int    STATUS_HTTP_DATA_ERROR    = 410;
    /** http stream cannot be resume by etag! */
    int    STATUS_CANNOT_RESUME      = 450;
    /** too many redirects! */
    int    STATUS_TOO_MANY_REDIRECTS = 490;

    // ================================
    // CRUD
    // ================================

    void addDownload(DownloadEntry entry) throws IllegalEntryException;

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
