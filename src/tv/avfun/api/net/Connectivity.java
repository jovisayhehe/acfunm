package tv.avfun.api.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.external.JSONException;
import org.json.external.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import tv.avfun.BuildConfig;
import tv.avfun.app.AcApp;
import tv.avfun.util.DataStore;
import tv.avfun.util.NetWorkUtil;
import android.util.Log;

public class Connectivity {
    private static final String TAG = Connectivity.class.getSimpleName();

    /**
     * 获得原始Json数据
     * @param url
     * @return 获取失败返回null
     */
    public static String getJson(String url){
        try {
            HttpURLConnection conn = openConnection(new URL(url), UserAgent.CHROME_25);
            if (conn.getResponseCode() != 200)
                return null;
            InputStream in = conn.getInputStream();
            String json = DataStore.readData(in, "UTF8");
            conn.disconnect();
            return json;
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                Log.e(TAG, "获取Json失败",e);
        }
        return null;
    }
    /**
     * 获得Jsondu对象
     * @param url
     * @return 获取失败返回null
     */
    public static JSONObject getJSONObject(String url) throws JSONException {
        String json = getJson(url);
        if(json != null)
        return new JSONObject(json);
        else return null;
    }

    public static Elements getElements(String url, String tag) throws IOException {
        return getDoc(url, UserAgent.DEFAULT).getElementsByTag(tag);
    }
    /**
     * 获取指定标签的子元素集
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
     * 打开一个新的Http连接
     * @param httpUrl
     * @param userAgent
     * @throws IOException
     */
    public static HttpURLConnection openConnection(URL httpUrl, String userAgent)
            throws IOException {
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        conn.addRequestProperty("User-Agent", userAgent);
        conn.setConnectTimeout(6000);
        //conn.setReadTimeout(10000); // 似乎需要去掉这个超时..
        return conn;
    }
    /**
     * 用默认的UserAgent打开连接
     * @see #openConnection(URL, String)
     */
    public static HttpURLConnection openConnection(String url) throws IOException {
        return openConnection(new URL(url), UserAgent.DEFAULT);
    }
    /**
     * 获得重定向路径（一次重定向）
     * @param httpUrl
     * @param userAgent
     * @return
     */
    public static String getRedirectLocation(URL httpUrl, String userAgent) {
        String location = null;
        try {
            HttpURLConnection conn = openConnection(httpUrl, userAgent);
            conn.setInstanceFollowRedirects(false);// 不跟随跳转
            int code = conn.getResponseCode();
            if(code == 302){
                location = conn.getHeaderField("Location");
                if (BuildConfig.DEBUG)
                    Log.i("", "redirect location: "+location);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, "failed to get redirect loaction :"+httpUrl.toString(), e);
        }
        return location;
    }
    /**
     * 获得重定向路径（二次重定向）
     * @param httpUrl
     * @param userAgent
     * @return
     * @throws IOException 
     */
    public static String getDuplicateRedirectLocation(URL httpUrl, String userAgent) throws IOException{
        String url = getRedirectLocation(httpUrl, userAgent);
        return getRedirectLocation(new URL(url), userAgent);
    }
}
