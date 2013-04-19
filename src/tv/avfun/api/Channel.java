
package tv.avfun.api;

import java.io.Serializable;
import java.util.List;

import tv.avfun.entity.Contents;

/**
 * 频道。
 * @author Yrom
 *
 */
public class Channel implements Serializable {
    private static final long serialVersionUID = 11L;
    /**
     * 推荐视频
     */
    public List<Contents> recommends;
    
    
    
    
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
    /**
     * compare with channel id 
     */
    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof Channel)
            return this.channelId == ((Channel)o).getChannelId();
        return super.equals(o);
    }
    
    
    
}
