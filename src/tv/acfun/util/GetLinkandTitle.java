package tv.acfun.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import acfun.domain.Acfun;
import acfun.domain.AcfunContent;
import acfun.domain.Article;
import acfun.domain.SearchResults;
import acfun.domain.Video;
import android.util.Log;

public class GetLinkandTitle {
	
	//获取首页视频
	public List<Video>  getHomeVideoandTitle(String address) throws IOException {
		List<Video> videos = new ArrayList<Video>();
		Connection c = Jsoup.connect(address);
		
			
			Document doc = c.get();
			Elements elms = doc.getElementsByAttributeValue("cellpadding", "2");
			
			for(Element em:elms){
					Video video = new Video();
					Elements link = em.select("a[href]");
					String id = link.attr("href");
					String strs[] = id.split("/");
					id = strs[2].substring(2);
					video.setVideolink(id);
					video.setVideotitle(link.text());
					videos.add(video);
			}

		return videos;
	}
	
	// 获取最新
	public List<Article> getNewArtTitleandLink(String address){

		List<Article> arts = new ArrayList<Article>();
		Connection c = Jsoup.connect(address);
		try {
			Document doc = c.get();
			Elements ems = doc.select("b");
			for(Element em:ems){
				Article article = new Article();
				Elements link = em.select("a[href]");
				article.setArtlink(Acfun.getAcfun()+link.attr("href"));
				article.setArttitle(link.text());
				arts.add(article);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arts;
	}
	//获取一周排行文章
	public List<Article> getTopArtLinkandTilte(String address){
		
		List<Article> arts = new ArrayList<Article>();
		Connection c = Jsoup.connect(address);
		 try {
			 Document doc = c.get();
			 Elements ems =  doc.getElementsByAttributeValue("class", "t1");
			 for(Element em:ems){
				 Article article = new Article();
				 Elements link =  em.select("a[href]");
				 article.setArtlink(Acfun.getAcfun()+link.attr("href"));
				 article.setArttitle(link.text());
				 Elements hits = em.select("font");
				 article.setHits(hits.text());
				 
				 arts.add(article);
			 }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arts;
		
	}
	
	
	public Article getArt(String address){
		
		Connection c = Jsoup.connect(address);
		Article article = new Article();
		List<String> imgs = new ArrayList<String>();
		try {
			Document doc = c.get();
			Elements ems = doc.select("table");
			ems.remove(0);
			ems.remove(0);
			ems.remove(2);
			ems.remove(2);
			ems.remove(2);
			ems.remove(3);
			ems.remove(3);
			
			Element em0 = ems.get(0);
			Elements autmms = em0.select("td");
			autmms.remove(0);
			autmms.remove(0);
			autmms.remove(0);
			autmms.remove(1);
			autmms.text();
			String text = autmms.text();
			//获取时间和投稿
			String timeandaut = text.substring(7, text.length()-2);		
			article.setUptime(timeandaut);
			
			//获取正文
			Element em1 = ems.get(1);
			Elements tdem = em1.select("td");
			String art = tdem.html().
			replaceAll("\\<.*?>","\r\n")
			;
			article.setArt(art);
			
			//获取图片链接
			Elements imgems = tdem.select("img");
			for(Element em:imgems){
			
			
			imgs.add(em.attr("src"));
			
			}
			article.setImgpath(imgs);
			
			Element em2 = ems.get(2);
			//获取评论链接
			String feedblack = em2.select("iframe").attr("src");
			
			article.setReviewlink(feedblack);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return article;
	}
	public Video getVideo(String address){
		Connection c = Jsoup.connect(address);
		Video video = new Video();
		try {
			Document doc = c.get();
			Elements ems = doc.select("table");
			
			if(ems!=null){
			ems.remove(0);
			ems.remove(0);
			ems.remove(3);
			ems.remove(3);
			ems.remove(3);
			ems.remove(4);
			ems.remove(4);
			}
			Element em0 = ems.get(0);
			Elements autmms = em0.select("td");
			autmms.remove(0);
			autmms.remove(0);
			autmms.remove(0);
			autmms.remove(0);
			autmms.remove(1);
			autmms.text();
			String text = autmms.text();
			//获取投稿时间和作者
			String timeandaut = text.substring(7, text.length()-2);
			video.setUptime(timeandaut);
			Elements options = autmms.select("option");
			if(options.size()!=0){
				ArrayList<String> ops = new ArrayList<String>();
				ArrayList<String> opstitles = new ArrayList<String>();
				for(Element em :options){
					opstitles.add(em.text());
				//	String value = em.attr("value");
					ops.add(em.attr("value"));
				}
				video.setOptions(ops);
				video.setOpttitles(opstitles);
			}
			
			Element em1 = ems.get(1);
			Elements flashems =  em1.select("embed");
			//String src = flashems.attr("src");
			String vars = flashems.attr("flashvars");
			//得到播放地址
			 Pattern pattern = Pattern.compile("\\d+");
			 Matcher matcher = pattern.matcher(vars);
			 if(matcher.find()){
				 vars =  matcher.group();
			 }

			 String path = "http://v.iask.com/v_play.php?vid=" + vars;
			 Connection c1 = Jsoup.connect(path);
			 Document doc1 = c1.get();
			 Elements ems1 = doc1.select("url");
			 if(ems1.size()!=0){
				 ArrayList<String> adds = new ArrayList<String>();
				 String add = null;
				 for(Element em:ems1){
					add =  em.text();
					adds.add(add);
				 }
				video.setPlaylink(adds);
			 }
			 
			 
			Element em2 = ems.get(2);
			//获得描述
			String conts = em2.select("span").text();
			video.setVideodes(conts);
			
			Element em3 = ems.get(3);
			//获得评论地址
			String feedblack = em3.select("iframe").attr("src");
			video.setReviewlink(feedblack);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return video;
		
	}
	
	public Map<String,Object> getFeedBlack(String address){
		
		Connection c = Jsoup.connect(address);
		Map<String,Object> feeds = new HashMap<String, Object>();
		List<String> feedstring = new ArrayList<String>();
		try {
			Document doc = c.get();
			Elements ems = doc.getElementsByAttributeValue("class", "i");
			if(!ems.isEmpty()){
			for(Element em:ems){
				feedstring.add(em.html().replaceAll("\\<.*?>","").
						replace("&nbsp;","").
						replaceAll("\\[.*?]","")
						+"\r\n------------------------------------------------------------\r\n");
				
			}
			feeds.put("feedstring", feedstring);
			}
			else{
				feeds.put("feedstring", null);
			}
			Elements ems2 = doc.getElementsByAttributeValue("accesskey", "4");
			Elements ems3 = doc.getElementsByAttributeValue("accesskey", "5");
			String han = "http://www.acfun.tv/m/";
			//feeds.put("hander", hander);
			int size = ems3.size();
			if(ems2.size()==1&&size==0){
				String link = ems2.attr("href");
				feeds.put("linkl", han+link);
				feeds.put("hander", "");
				}else if(ems2.size()==1&&size!=0){
					String link = ems2.attr("href");
					feeds.put("hander", han+link);
					feeds.put("linkl", "");
				}
				else{
					String linkh = ems2.first().attr("href");
					feeds.put("hander", han+linkh);
					String linkl = ems2.last().attr("href");
					feeds.put("linkl", han+linkl);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feeds;
	}
	
	public Map<String,Object> getFeedBlackFF(String address){
		
		String id = address.substring(address.indexOf("="));
		String add = "http://www.acfun.tv/m/art.php?aid"+id;
		Connection c = Jsoup.connect(add);
		Map<String,Object> feeds = new HashMap<String, Object>();
		List<String> feedstring = new ArrayList<String>();
		try {
			Document doc = c.get();
			Elements ems = doc.getElementsByAttributeValue("class", "i");
			ems.remove(0);
			if(!ems.isEmpty()){
			for(Element em:ems){
				feedstring.add(em.html().replaceAll("\\<.*?>","").
						replace("&nbsp;","").
						replaceAll("\\[.*?]","")
						+"\r\n------------------------------------------------------------\r\n");
				
			}
			feeds.put("feedstring", feedstring);
			}
			else{
				feeds.put("feedstring", null);
			}
			Elements ems2 = doc.getElementsByAttributeValue("accesskey", "4");
			Elements ems3 = doc.getElementsByAttributeValue("accesskey", "5");
			String han = "http://www.acfun.tv/m/";
			//feeds.put("hander", hander);
			int size = ems3.size();
			if(ems2.size()==1&&size==0){
				String link = ems2.attr("href");
				feeds.put("linkl", han+link);
				feeds.put("hander", "");
				}else if(ems2.size()==1&&size!=0){
					String link = ems2.attr("href");
					feeds.put("hander", han+link);
					feeds.put("linkl", "");
				}
				else if(ems2.size()==2){
					String linkh = ems2.first().attr("href");
					feeds.put("hander", han+linkh);
					String linkl = ems2.last().attr("href");
					feeds.put("linkl", han+linkl);
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feeds;
	}
	//我来!!
	public List<Article> getTitleandLink(String address) throws IOException{

		List<Article> arts = new ArrayList<Article>();
		Connection c = Jsoup.connect(address);
	
			Document doc = c.get();
			Elements ems =doc.getElementsByAttributeValue("class", "i");
			for(Element em:ems){
				Article article = new Article();
				Elements link = em.select("a[href]");
				String id = link.attr("href").split("&")[0].substring(12);
				article.setArtlink(id);
				article.setArttitle(link.text());
				article.setArt(em.getElementsByAttributeValue("class", "g").text());
				article.setUptime(em.getElementsByAttributeValue("class", "b").text());
				arts.add(article);
			}
		return arts;
	}
	
public Map<String,Object> getPage(String address) throws IOException{
		
		Connection c = Jsoup.connect(address);
		Map<String,Object> feeds = new HashMap<String, Object>();

			Document doc = c.get();
			Elements ems2 = doc.getElementsByAttributeValue("accesskey", "4");
			Elements ems3 = doc.getElementsByAttributeValue("accesskey", "5");
			String han = "http://www.acfun.tv/m/";
			int size = ems3.size();
			if(ems2.size()==1&&size==0){
				String link = ems2.attr("href");
				feeds.put("linkl", han+link);
				feeds.put("hander", "");
				}else if(ems2.size()==1&&size!=0){
					String link = ems2.attr("href");
					feeds.put("hander", han+link);
					feeds.put("linkl", "");
				}
				else{
					String linkh = ems2.first().attr("href");
					feeds.put("hander", han+linkh);
					String linkl = ems2.last().attr("href");
					feeds.put("linkl", han+linkl);
				}

		return feeds;
	}

	public Video getNVideo(String address) throws IOException {
		Connection c = Jsoup.connect(address);
		Document doc = c.get();
		Elements ems = doc.select("embed");
		Video video = new Video();

		Elements ems2 = doc.getElementsByAttributeValue("class", "g");
		Elements ems3 = doc.getElementsByAttributeValue("class", "b");
		video.setUptime(ems2.text() + ems3.text());
		String vars = ems.attr("flashvars");
		// 得到播放地址
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(vars);
		if (matcher.find()) {
			vars = matcher.group();
		}

		String path = "http://v.iask.com/v_play.php?vid=" + vars;
		Connection c1 = Jsoup.connect(path);
		Document doc1 = c1.get();
		Elements ems1 = doc1.select("url");
		if (ems1.size() != 0) {
			ArrayList<String> adds = new ArrayList<String>();
			String add = null;
			for (Element em : ems1) {
				add = em.text();
				adds.add(add);
			}
			video.setPlaylink(adds);
		}
		video.setVideodes("");
		video.setReviewlink(address);
		return video;
	}
	
	
	private static String binaryToString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		sb.append("%");
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (i > 0)
				sb.append("%");
			sb.append(Integer.toHexString(v / 16));
			sb.append(Integer.toHexString(v % 16));
		}
		return sb.toString();
	}
	
	public ArrayList<Object> GetSearchResults(String word,String sort,String group,int page) throws IOException{
		
		byte[] utf8 = word.getBytes("UTF8");
		Connection c = Jsoup.connect("http://search.acfun.tv/Search.aspx?"+"page="+String.valueOf(page)+"&q="+binaryToString(utf8)+"&order="+sort+"&group="+group);
			ArrayList<Object> rsandtotalpage = new ArrayList<Object>();
			ArrayList<SearchResults> results = new ArrayList<SearchResults>();
			Document doc = c.get();
			Elements ems = doc.getElementsByAttributeValue("class", "leftA");
			for(Element em:ems){
				SearchResults result = new SearchResults();
				Elements aems = em.select("a");
				result.setTitle(aems.get(0).html());
				String id = aems.get(0).attr("href");
				id = id.split("/")[4].substring(2);
				result.setLink(id);
				result.setCon(aems.get(1).text());
				result.setInfo(em.getElementsByAttributeValue("class", "leftIntro").html());
				String info = em.getElementsByAttributeValue("class", "info").text();
				
				String[] strs = info.split("：");
				result.setDate(strs[1].substring(0, strs[1].length()-3));
				result.setHit(strs[3].substring(0, strs[3].length()-2));
				result.setFavor(strs[4]);
				
				results.add(result);
			}
			rsandtotalpage.add(results);
			Elements pageems = doc.getElementsByAttributeValue("id", "index");
			if(pageems!=null&&pageems.size()>0){
				Elements aems = pageems.first().getElementsByTag("a");
				if(aems!=null&&aems.size()>0){
					Integer totalpage = Integer.parseInt(aems.last().text());
					rsandtotalpage.add(totalpage);
				}else{
					rsandtotalpage.add(0);
				}
			}else{
				rsandtotalpage.add(0);
			}
			
			return rsandtotalpage;
	}
	
}
