
package tv.avfun.api;

import java.io.Serializable;

/**
 * 频道。
 * @author Yrom
 *
 */
public class Channel implements Serializable {
    private static final long serialVersionUID = 11L;
    /**
     * 频道的id
     *
     */
    public static final class id {

        public static final int ANIMATION = 1;
        public static final int MUSIC     = 58;
        public static final int GAME      = 59;
        public static final int FUN       = 60;
        public static final int BANGUMI   = 67;
        public static final int MOVIE     = 68;
        public static final int SPORT     = 69;
        public static final int SCIENCE   = 70;
        public static final int MUGEN     = 72;

        public static final class ARTICLE {
            public static final int COLLECTION        = 63;
            public static final int WORK_EMOTION      = 73;
            public static final int AN_CULTURE        = 74;
            public static final int COMIC_LIGHT_NOVEL = 75;
        }
    }
    
    private String title;
    private String url;
    private int channelId;
    /**
     * 构造频道
     * @param title 名
     * @param channelId id
     * @see Channel.id
     */
    public Channel(String title, int channelId){
        this.title = title;
        this.channelId = channelId;
    }
    public String getTitle(){
        return this.title;
    }
    
    public String getUrl() {
        url = "http://www.acfun.tv/api/channel.aspx?query="+channelId+"&currentPage=";
        return url;
    }
    public int getChannelId() {
        return channelId;
    }
    
    
}
