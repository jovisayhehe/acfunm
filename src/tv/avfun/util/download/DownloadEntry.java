
package tv.avfun.util.download;

import java.io.Serializable;

import android.content.ContentValues;

import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import static tv.avfun.util.download.DownloadDB.*;

/**
 * 单个下载项
 * 一个视频分p对应于一个下载项
 * @author Yrom
 * 
 */
public class DownloadEntry implements Serializable {

    private static final long serialVersionUID = 1L;
    public String            aid = "";
    public String            title = "";
    public String            destination = "";
    /**
     * 欲下载的视频分p
     */
    public VideoPart         part;
    
}
