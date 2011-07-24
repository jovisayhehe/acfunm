package acfun.domain;

import java.util.ArrayList;

public class Video {
	private String videotitle;
	private String videolink;
	private String videodes;
	private String videodesx;
	private String upman;
	private String reviewlink;
	private String hits;
	private String replys;
	private String uptime;
	private ArrayList<String> playlink;
	private ArrayList<String> options;
	private ArrayList<String> opttitles;
	public ArrayList<String> getOpttitles() {
		return opttitles;
	}
	public void setOpttitles(ArrayList<String> opttitles) {
		this.opttitles = opttitles;
	}
	public ArrayList<String> getOptions() {
		return options;
	}
	public void setOptions(ArrayList<String> options) {
		this.options = options;
	}
	public ArrayList<String> getPlaylink() {
		return playlink;
	}
	public void setPlaylink(ArrayList<String> playlink) {
		this.playlink = playlink;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	public String getVideotitle() {
		return videotitle;
	}
	public void setVideotitle(String videotitle) {
		this.videotitle = videotitle;
	}
	public String getVideolink() {
		return videolink;
	}
	public void setVideolink(String videolink) {
		this.videolink = videolink;
	}
	public String getVideodes() {
		return videodes;
	}
	public void setVideodes(String videodes) {
		this.videodes = videodes;
	}
	public String getVideodesx() {
		return videodesx;
	}
	public void setVideodesx(String videodesx) {
		this.videodesx = videodesx;
	}
	public String getUpman() {
		return upman;
	}
	public void setUpman(String upman) {
		this.upman = upman;
	}
	public String getReviewlink() {
		return reviewlink;
	}
	public void setReviewlink(String reviewlink) {
		this.reviewlink = reviewlink;
	}
	public String getHits() {
		return hits;
	}
	public void setHits(String hits) {
		this.hits = hits;
	}
	public String getReplys() {
		return replys;
	}
	public void setReplys(String replys) {
		this.replys = replys;
	}
	
}
