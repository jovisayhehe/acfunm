package tv.avfun.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Article {
	public String id;
	public String name;
	public String uid;
	public long posttime;
	public String title;
	public int views;
	public int comments;
	public int stows;
	public ArrayList<String> imgUrls = new ArrayList<String>();
	public List<HashMap<String, String>> contents = new ArrayList<HashMap<String,String>>();
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getPosttime() {
		return posttime;
	}
	public void setPosttime(long posttime) {
		this.posttime = posttime;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public int getComments() {
		return comments;
	}
	public void setComments(int comments) {
		this.comments = comments;
	}
	public int getStows() {
		return stows;
	}
	public void setStows(int stows) {
		this.stows = stows;
	}
	public ArrayList<String> getImgUrls() {
		return imgUrls;
	}
	public void setImgUrls(ArrayList<String> imgUrls) {
		this.imgUrls = imgUrls;
	}
	public List<HashMap<String, String>> getContents() {
		return contents;
	}
	public void setContents(List<HashMap<String, String>> contents) {
		this.contents = contents;
	}
	
	
}
