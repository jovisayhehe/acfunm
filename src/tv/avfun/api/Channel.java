
package tv.avfun.api;

import java.io.Serializable;
import java.util.List;

import tv.ac.fun.R;
import tv.avfun.entity.Contents;

/**
 * 频道。
 * @author Yrom
 *
 */
public class Channel implements Serializable {
    private static final long serialVersionUID = 11L;
    /**
     * 频道内容。用于缓存
     */
    public List<Contents> contents;
    public int pageIndex;
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
    
    public String getUrl(int page) {
        if(page < 1) page = 1;
        url = "http://www.acfun.tv/api/getlistbyorder.aspx?orderby=0&channelIds=" + this.channelId + "&count=20&first=" + 20 * (page-1);
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
