package acfun.domain;

import java.util.List;

public class Article {
	private String arttitle;
	private String artlink;
	private String upman;
	private String uptime;
	private String hits;
	private String reviewlink;
	private String replys;
	private List<String> imgpath;
	private String art;
	public String getArt() {
		return art;
	}
	public void setArt(String art) {
		this.art = art;
	}
	public List<String> getImgpath() {
		return imgpath;
	}
	public void setImgpath(List<String> imgpath) {
		this.imgpath = imgpath;
	}
	public String getArttitle() {
		return arttitle;
	}
	public void setArttitle(String arttitle) {
		this.arttitle = arttitle;
	}
	public String getArtlink() {
		return artlink;
	}
	public void setArtlink(String artlink) {
		this.artlink = artlink;
	}
	public String getUpman() {
		return upman;
	}
	public void setUpman(String upman) {
		this.upman = upman;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	public String getHits() {
		return hits;
	}
	public void setHits(String hits) {
		this.hits = hits;
	}
	public String getReviewlink() {
		return reviewlink;
	}
	public void setReviewlink(String reviewlink) {
		this.reviewlink = reviewlink;
	}
	public String getReplys() {
		return replys;
	}
	public void setReplys(String replys) {
		this.replys = replys;
	}
	
}
