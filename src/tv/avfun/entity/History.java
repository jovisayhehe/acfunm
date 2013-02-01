package tv.avfun.entity;

public class History {
	
	public String title;
	public String aid;
	public String time;
	public String username;
	public int tpye;
	public String channelid;
	
	
	
	public int getTpye() {
		return tpye;
	}
	public void setTpye(int tpye) {
		this.tpye = tpye;
	}
	public String getChannelid() {
		return channelid;
	}
	public void setChannelid(String channelid) {
		this.channelid = channelid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
