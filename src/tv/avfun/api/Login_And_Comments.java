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

import tv.avfun.entity.Comment;


public class Login_And_Comments {
	
	public static HashMap<String, Object> login(String username,String password) throws HttpException, IOException,UnknownHostException, JSONException{
			HashMap<String, Object> map = new HashMap<String, Object>();
			PostMethod localPostMethod = new PostMethod("/login.aspx");
	        NameValuePair localNameValuePair1 = new NameValuePair("username", username);
	        NameValuePair localNameValuePair2 = new NameValuePair("password", password);
	        NameValuePair[] arrayOfNameValuePair = new NameValuePair[2];
	        arrayOfNameValuePair[0] = localNameValuePair1;
	        arrayOfNameValuePair[1] = localNameValuePair2;
	        localPostMethod.setRequestBody(arrayOfNameValuePair);
	        localPostMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
	        HttpClient localHttpClient = new HttpClient();
	        localHttpClient.getParams().setParameter("http.protocol.single-cookie-header", true);
	        localHttpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
	        localHttpClient.getHostConfiguration().setHost("www.acfun.tv", 80, "http");
	        int state = localHttpClient.executeMethod(localPostMethod);
	        
	        if(state>200){
	        	map.put("success", false);
	        	map.put("result", "ac娘大姨妈？");
	        }else{
	            JSONObject re = new JSONObject(localPostMethod.getResponseBodyAsString());
		        
		        if(re.getBoolean("success")){
			        Cookie[] cks = localHttpClient.getState().getCookies();
			        map.put("Cookies", cks);
			        PostMethod mempost = new PostMethod("/user_check.aspx");
			        HttpState localHttpState = new HttpState();
			        localHttpState.addCookies(cks);
			        localHttpClient.setState(localHttpState);
			        
			        localHttpClient.executeMethod(mempost);
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
	    PostMethod postMethod = new PostMethod("/comment.aspx");
        NameValuePair np1 = new NameValuePair("text", comment);
        NameValuePair np2 = new NameValuePair("quoteId", quote == null? "0": quote.cid+"");
        NameValuePair np3 = new NameValuePair("contentId", aid);
        NameValuePair np4 = new NameValuePair("quoteName",quote == null? "": quote.userName);
        NameValuePair[] nps = new NameValuePair[4];
        nps[0] = np1;
        nps[1] = np2;
        nps[2] = np3;
        nps[3] = np4;
        postMethod.setRequestBody(nps);
        postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        HttpClient localHttpClient = new HttpClient();
        localHttpClient.getParams().setParameter("http.protocol.single-cookie-header", true);
        localHttpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        localHttpClient.getHostConfiguration().setHost("www.acfun.tv", 80, "http");
        HttpState localHttpState = new HttpState();
        localHttpState.addCookies(cks);
        localHttpClient.setState(localHttpState);
        int state  = localHttpClient.executeMethod(postMethod);
        if(state>200){
            return false;
        }else{
            return true;
        }
	}
	
}
