
package tv.avfun.entity;

import java.io.Serializable;
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

    public static class VideoItem implements Serializable {

        private static final long serialVersionUID = 976124L;
        public String             vid;
        public String             vtype;
        public String             subtitle;
        public List<Long>         downloadIDs;
        public List<String>       urlList;
        /** 如果值为-1则表示没能获取到content length */
        public List<Integer>      bytesList;
        public List<Long>         durationList;
        public boolean            isdownloaded     = false;

        public void put(String url, long seconds) {
            urlList.add(url);
            durationList.add(seconds);
        }

        public boolean validate() {
            return urlList != null && !urlList.isEmpty() && downloadIDs != null && !downloadIDs.isEmpty();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((subtitle == null) ? 0 : subtitle.hashCode());
            result = prime * result + ((vid == null) ? 0 : vid.hashCode());
            result = prime * result + ((vtype == null) ? 0 : vtype.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VideoItem other = (VideoItem) obj;
            if (subtitle == null) {
                if (other.subtitle != null)
                    return false;
            } else if (!subtitle.equals(other.subtitle))
                return false;
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
}
