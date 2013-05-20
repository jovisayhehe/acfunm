
package tv.avfun.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 视频分p，一个视频可能有很多的视频分段segments
 * 
 * @author Yrom
 * 
 */
public class VideoPart implements Serializable {

    private static final long      serialVersionUID = 976124L;
    /**
     * 视频编号
     */
    public String                  vid;
    /**
     * 视频源类型
     */
    public String                  vtype;
    /**
     * 分p的标题
     */
    public String                  subtitle;
    /**
     * 总持续时间 second
     */
    public int                     totalDur;
    /**
     * 视频的分段
     */
    public ArrayList<VideoSegment> segments;
    public boolean                 isDownloaded;
    public boolean                 isDownloading;
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vid == null) ? 0 : vid.hashCode());
        result = prime * result + ((vtype == null) ? 0 : vtype.hashCode());
        return result;
    }

    /**
     * vid vtype 相等 认为是相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VideoPart other = (VideoPart) obj;

        if (vid == null) {
            if (other.vid != null)
                return false;
        } else if (!vid.equals(other.vid))
            return false;
        if (vtype == null) {
            if (other.vtype != null)
                return false;
        } else if (!vtype.equals(other.vtype))
            return false;
        return true;
    }
    // TODO danmu info
}
