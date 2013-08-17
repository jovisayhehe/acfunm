
package tv.avfun.entity;

import java.io.Serializable;

/**
 * 视频分段
 * 
 * @author Yrom
 * 
 */
public class VideoSegment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 在视频分段列表中的索引
     */
    public int    num      = 0;
    /**
     * 分段持续时间 ms
     */
    public long    duration = 0;
    /**
     * 播放的url，可能是远程的，或者本地的
     */
    public String url      = null;
    /**
     * 用于下载的url（XXX 有些视频貌似播放和下载的不是同一个文件）
     */
    public String stream   = null;
    /**
     * -1，表示未读到有效数据。需要重新获取
     */
    public long   size     = -1;
    
    public String fileName = null;
    public String etag     = null;
    @Override
    public String toString() {
        return "Segment [num=" + num + ", url=" + url +"]";
    }

}
