
package tv.avfun.entity;

import java.util.List;
/**
 * 视频信息，有一个以上的段落parts
 * @author Yrom
 *
 */
public class VideoInfo {
    /**
     * 番号，acxxxxx
     */
    public String          aid;
    public String          title;
    public String          description;
    public String          upman;
    public String          titleImage;
    public int             channelId;
    public long            postTime;
    /**
     * 标签
     */
    public String[]        tags;
    public int             views;
    public int             comments;
    /**
     * 分p
     */
    public List<VideoPart> parts;
    
}
