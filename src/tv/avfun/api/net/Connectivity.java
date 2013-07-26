
package tv.avfun.api.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.external.JSONException;
import org.json.external.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.JsonObject;

import tv.ac.fun.BuildConfig;
import tv.avfun.app.AcApp;
import tv.avfun.util.DataStore;
import tv.avfun.util.NetWorkUtil;
import android.text.TextUtils;
import android.util.Log;

public class Connectivity {

    private static final String TAG = Connectivity.class.getSimpleName();

    /**
     * 获得原始Json数据
     * 
     * @param url
     * @return 获取失败返回null
     */
    public static String getJson(String url) {
        try {
            HttpURLConnection conn = openConnection(new URL(url), UserAgent.DEFAULT);
            if (conn.getResponseCode() != 200)
                return null;
            InputStream in = conn.getInputStream();
            String json = DataStore.readData(in, "UTF8");
            conn.disconnect();
            return json;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.w(TAG, "获取Json失败" + "\n" + e.getMessage());
        }
        return null;
    }

    /**
     * 获得Jsondu对象
     * 
     * @param url
     * @return 获取失败返回null
     */
    public static JSONObject getJSONObject(String url) throws JSONException {
        String json = getJson(url);
        if (json != null)
            return new JSONObject(json);
        else
            return null;
    }

    public static Elements getElements(String url, String tag) throws IOException {
        return getDoc(url, UserAgent.DEFAULT).getElementsByTag(tag);
    }

    /**
     * 获取指定标签的子元素集
     * 
     * @param url
     * @param userAgent
     * @param tag
     * @throws IOException
     */
    public static Elements getElements(String url, String userAgent, String tag) throws IOException {
        return getDoc(url, userAgent).getElementsByTag(tag);
    }

    public static Document getDoc(String url, String userAgent) throws IOException {
        return Jsoup.connect(url).timeout(6000).userAgent(userAgent).get();
    }

    /**
     * 打开一个新的Http连接 TODO 引入重试机制！
     * 
     * @param httpUrl
     * @param userAgent
     * @throws IOException
     */
    public static HttpURLConnection openConnection(URL httpUrl, String userAgent) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        conn.addRequestProperty("User-Agent", userAgent);
        conn.setConnectTimeout(6000);
        // conn.setReadTimeout(10000); // 似乎需要去掉这个超时..
        return conn;
    }

    /**
     * 用默认的UserAgent打开连接
     * 
     * @see #openConnection(URL, String)
     */
    public static HttpURLConnection openConnection(String url) throws IOException {
        return openConnection(new URL(url), UserAgent.DEFAULT);
    }

    /**
     * 获得重定向路径（一次重定向） 如果没有重定向，则返回传入的url
     * 
     * @param httpUrl
     * @param userAgent
     * @return
     */
    public static String getRedirectLocation(String url, String userAgent) {
        String location = url;
        try {
            HttpURLConnection conn = openConnection(new URL(url), userAgent);
            conn.setInstanceFollowRedirects(false);// 不跟随跳转
            int code = conn.getResponseCode();
            if (code == 302 || code == 301) {
                location = conn.getHeaderField("Location");
                if (BuildConfig.DEBUG)
                    Log.i("", "redirect location: " + location);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "failed to get redirect loaction :" + url, e);
        }
        return location;
    }

    /**
     * 获得重定向路径（二次重定向） 无重定向，则返回源地址
     * 
     * @param httpUrl
     * @param userAgent
     * @return
     * @throws IOException
     */
    public static String getDuplicateRedirectLocation(String httpUrl, String userAgent) throws IOException {
        String url = getRedirectLocation(httpUrl, userAgent);
        if (httpUrl.equals(url))
            return url;
        else
            return getRedirectLocation(url, userAgent);
    }

    /**
     * 获取http实体大小
     * 
     * @param url
     * @return -1，如果获取失败
     */
    public static int getContentLenth(String url, String userAgent) {
        if (TextUtils.isEmpty(userAgent))
            userAgent = UserAgent.DEFAULT;
        try {
            HttpURLConnection conn = getConn(url, userAgent);
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
                String loaction = conn.getHeaderField("Location");
                return getContentLenth(loaction, userAgent);
            } else if (code == HttpURLConnection.HTTP_OK) {
                return conn.getContentLength();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "failed to get content length of " + url, e);
        }
        return -1;

    }

    private static HttpURLConnection getConn(String url, String userAgent) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.addRequestProperty("User-Agent", userAgent);
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "close");
        conn.setDoInput(false);
        conn.setUseCaches(false);
        return conn;
    }

    public static int request(HttpMethodBase httpMethod, String host, int port, String protocal, Cookie[] cookies)
            throws HttpException, IOException {
        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.single-cookie-header", true);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(4000);
        client.getHostConfiguration().setHost(host, port == 0 ? 80 : port, protocal == null ? "http" : protocal);
        if(cookies != null){
            HttpState state = new HttpState();
            state.addCookies(cookies);
            client.setState(state);
        }
        return client.executeMethod(httpMethod);
    }

    public static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=utf-8";

    public static int doPost(PostMethod post, String host, int port, String protocal, Cookie[] cks)
            throws HttpException, IOException {
        return request(post, host, port, protocal, cks);
    }

    public static int doPost(PostMethod post, Cookie[] cks) throws HttpException, IOException {
        return doPost(post, "www.acfun.tv", 0, null, cks);
    }

    public static boolean postResultJson(String url, NameValuePair[] nps, Cookie[] cks) {
        if (TextUtils.isEmpty(url))
            throw new NullPointerException("url cannot be null!");
        PostMethod post = new PostMethod(url);
        if (nps != null) {
            post.setRequestBody(nps);
            post.setRequestHeader("Content-Type", CONTENT_TYPE_FORM);
        }
        try {
            int state = Connectivity.doPost(post, cks);
            if (state == 200) {
                String json = post.getResponseBodyAsString();
                JSONObject re = new JSONObject(json);
                return re.getBoolean("success");
            }
        } catch (Exception e) {
            Log.e(TAG, "try to post Result Json :"+url ,e);
        }
        return false;
    }

    public static int doGet(GetMethod get, String host, int port, String protocal, Cookie[] cookies)
            throws HttpException, IOException {
        return request(get, host, port == 0 ? 80 : port, protocal == null ? "http" : protocal, cookies);
    }

    public static int doGet(GetMethod get, Cookie[] cookies) throws HttpException, IOException {
        return doGet(get, "www.acfun.tv", 0, null, cookies);
    }

    public static String doGet(String url, String queryString, Cookie[] cookies) {
        if (TextUtils.isEmpty(url))
            throw new NullPointerException("url cannot be null!");
        GetMethod get = new GetMethod(url);
        get.setRequestHeader("User-Agent", UserAgent.MY_UA);
        if(queryString != null)
            get.setQueryString(queryString);
        try {
            int state = doGet(get, cookies);
            if (state == 200) {
                return DataStore.readData(get.getResponseBodyAsStream(),"utf-8");
            }
        } catch (Exception e) {
            Log.e(TAG, "try to get :"+url ,e);
        }
        return null;
    }

    public static JSONObject getResultJson(String url, String queryString, Cookie[] cookies) {
        String result = doGet(url, queryString, cookies);
        try {
            return TextUtils.isEmpty(result) ? null : new JSONObject(result);
        } catch (JSONException e) {
            Log.e(TAG, "try to get Result Json :"+url ,e);
            return null;
        }
    }
}
