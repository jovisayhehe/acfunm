
package tv.avfun.entity;

import java.util.List;
import java.util.Map;

public class VideoInfo {

    public String          aid;
    public String          title;
    public String          description;
    public String          upman;
    public String          titleImage;
    public int             channelId;
    public long            postTime;
    public String[]        tags;
    public int             views;
    public int             comments;
    public List<VideoItem> parts;

    public static class VideoItem {

        public String       vid;
        public String       vtype;
        public String       subtitle;
        public List<String> urlList;
        public List<Long>   secondList;

        public void put(String url, long seconds) {
            urlList.add(url);
            secondList.add(seconds);
        }
        // TODO danmu info
    }
}
