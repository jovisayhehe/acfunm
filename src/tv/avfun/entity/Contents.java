package tv.avfun.entity;

import java.io.Serializable;

public class Contents implements Serializable{
    private static final long serialVersionUID = 1L;
    private String title;
    private String username;
    private String description;
    private String titleImg; 
    private long views; 
    private String aid;
    private int comments;
    private int channelId;
    private long releaseDate;
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getViews() {
        return views;
    }
    
    public void setViews(long views) {
        this.views = views;
    }
    
    public String getTitleImg() {
        return titleImg;
    }
    
    public void setTitleImg(String titleImg) {
        this.titleImg = titleImg;
    }
    
    public String getAid() {
        return aid;
    }
    
    public void setAid(String aid) {
        this.aid = aid;
    }
    
    public int getChannelId() {
        return channelId;
    }
    
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
    
    public int getComments() {
        return comments;
    }
    
    public void setComments(int comments) {
        this.comments = comments;
    }

    
    public long getReleaseDate() {
        return releaseDate;
    }

    
    public void setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    
    
}
