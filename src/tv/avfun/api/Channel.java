
package tv.avfun.api;

import java.io.Serializable;
import java.util.List;

import tv.avfun.R;
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
    public int titleBgResId;
    public int channelId;
    public String title;
    private String url;
    /**
     * 构造频道
     * @param title 名
     * @param channelId id
     * @param titleBgResId 标题背景图id
     */
    public Channel(String title, int channelId, int titleBgResId){
        this.title = title;
        this.channelId = channelId;
        this.titleBgResId = titleBgResId;
    }
    public Channel(String title, int channelId) {
        this.title = title;
        this.channelId = channelId;
        this.titleBgResId = R.drawable.title_bg_fun;
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
