package tv.avfun.entity;

public class Favorite {

	public String title;
	public String aid;
	public String username;
	public String userid;
	public int tpye;
	public String channelid;
	
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
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
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
		int cid = Integer.parseInt(channelid);
		switch (cid) {
		case 1:
			this.channelid = "动画";
			break;
		case 59:
			this.channelid = "游戏";
			break;
		case 72:
			this.channelid = "Mugen";
			break;
		case 58:
			this.channelid = "音乐";
			break;
		case 67:
			this.channelid = "番剧";
			break;
		case 69:
			this.channelid = "体育";
			break;
		case 68:
			this.channelid = "短影";
			break;
		case 60:
			this.channelid = "娱乐";
			break;
		case 70:
			this.channelid = "科技";
			break;
		case 63:
			this.channelid = "文章";
			break;
		case 73:
			this.channelid = "工作*情感";
			break;
		case 74:
			this.channelid = "动漫文化";
			break;
		case 75:
			this.channelid = "漫画*轻小说";
			break;

		default:
			break;
		}
	}
	
	
	
}
