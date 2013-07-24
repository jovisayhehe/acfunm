package tv.avfun.api;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.external.JSONException;
import org.json.external.JSONObject;

import android.util.Log;

import tv.avfun.api.net.Connectivity;
import tv.avfun.entity.Comment;
import tv.avfun.entity.Favorite;


public class MemberUtils{
	
	public static HashMap<String, Object> login(String username,String password) throws HttpException, IOException,UnknownHostException, JSONException{
			HashMap<String, Object> map = new HashMap<String, Object>();
			PostMethod post = new PostMethod("/login.aspx");
	        NameValuePair[] nps = new NameValuePair[2];
	        nps[0] = new NameValuePair("username", username);
	        nps[1] = new NameValuePair("password", password);
	        post.setRequestBody(nps);
	        post.setRequestHeader("Content-Type", Connectivity.CONTENT_TYPE_FORM);
	        HttpClient client = new HttpClient();
	        client.getParams().setParameter("http.protocol.single-cookie-header", true);
	        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	        client.getHostConfiguration().setHost("www.acfun.tv", 80, "http");
	        int state = client.executeMethod(post);
	        
	        if(state>200){
	        	map.put("success", false);
	        	map.put("result", "ac娘大姨妈？");
	        }else{
	            JSONObject re = new JSONObject(post.getResponseBodyAsString());
		        
		        if(re.getBoolean("success")){
			        Cookie[] cks = client.getState().getCookies();
			        map.put("Cookies", cks);
			        PostMethod mempost = new PostMethod("/user_check.aspx");
			        HttpState localHttpState = new HttpState();
			        localHttpState.addCookies(cks);
			        client.setState(localHttpState);
			        
			        client.executeMethod(mempost);
			        String jsonstring = mempost.getResponseBodyAsString();
			        
			        JSONObject job = new JSONObject(jsonstring);
			        String uname = job.getString("uname");
			        String signature = job.get("signature").toString();
			        String avatar = job.getString("avatar");
			        int uid = job.getInt("uid");
			        map.put("uname", uname);
			        map.put("signature", signature);
			        map.put("avatar", avatar);
			        map.put("uid", uid);
			        map.put("success", true);
		        }else{
		        	map.put("success", false);
		        	map.put("result", re.get("result"));
		        }
	        }
	    
	        return map;
	}
	
	public static boolean postComments(String comment,String aid,Cookie[] cks) throws HttpException, IOException{
	    return postComments(comment, null, aid, cks);
	}
	public static boolean postComments(String comment, Comment quote,String aid, Cookie[] cks) throws HttpException, IOException{
	    PostMethod post = new PostMethod("/comment.aspx");
        NameValuePair[] nps = new NameValuePair[4];
        nps[0] = new NameValuePair("text", comment);
        nps[1] = new NameValuePair("quoteId", quote == null? "0": quote.cid+"");
        nps[2] = new NameValuePair("contentId", aid);
        nps[3] = new NameValuePair("quoteName",quote == null? "": quote.userName);
        post.setRequestBody(nps);
        post.setRequestHeader("Content-Type",Connectivity.CONTENT_TYPE_FORM);
        int state  = Connectivity.doPost(post, cks);
        return state == 200;
	}
	public static boolean addFavourite(String cid, Cookie[] cks){
        NameValuePair[] nps = new NameValuePair[2];
        nps[0] = new NameValuePair("operate", "1");
        nps[1] = new NameValuePair("cId", cid);
        return Connectivity.postResultJson("/member/collection.aspx", nps, cks);
	}
	
	public static boolean checkIn(Cookie[] cks){
	    return Connectivity.postResultJson("/member/checkin.aspx", null, cks);
	}
}
