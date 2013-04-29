
package tv.avfun.entity;

import java.util.List;
import java.util.Map;

public class VideoInfo {

    public String          aid;
    public String          title;
    public String          description;
    public String          upman;
    public String          titleImage;
    public String          channelId;
    public long            postTime;
    public String[]        tags;
    public int             views;
    public int             comments;
    public List<VideoItem> parts;

    public class VideoItem {
        public String       vid;
        public String       vtype;
        public String       subtitle;
        /** key:url  value:duration*/
        public Map<String,Long> files;
        //TODO danmu info
    }
}
