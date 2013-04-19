package tv.avfun.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.external.JSONArray;
import org.json.external.JSONException;
import org.json.external.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tv.avfun.api.ChannelApi.id;
import tv.avfun.entity.Article;
import tv.avfun.entity.Contents;
import tv.avfun.util.DataStore;
import tv.avfun.util.Logger;
import android.accounts.NetworkErrorException;
import android.util.Log;

public class ApiParser {

    public static List<Contents> getChannelContents(String address) throws Exception {
        List<Contents> contents = new ArrayList<Contents>();
        JSONObject jsonObject = getJsonObj(address);
        JSONArray jsarray = jsonObject.getJSONArray("contents");
        for (int i = 0; i < jsarray.length(); i++) {
            JSONObject jobj = (JSONObject) jsarray.get(i);
            Contents c = new Contents();
            c.setTitle(jobj.getString("title"));
            c.setUsername(jobj.getString("username"));
            c.setDescription(jobj.getString("description").replace("&nbsp;", " ")
                    .replace("&amp;", "&").replaceAll("\\<.*?>", ""));
            c.setViews(jobj.getLong("views"));
            c.setTitleImg(jobj.getString("titleImg"));
            c.setAid(jobj.getString("aid"));
            c.setChannelId(jobj.getInt("channelId"));
            c.setComments(jobj.getInt("comments"));

            contents.add(c);
        }
        return contents;

    }
    /*
     * 首页主频道列表
     */
    private static Channel[] channels = new Channel[]{
          new Channel("动画", id.ANIMATION),
          new Channel("音乐", id.MUSIC),
          new Channel("娱乐", id.FUN),
          new Channel("短影", id.MOVIE),
          new Channel("游戏", id.GAME),
          new Channel("番剧", id.BANGUMI)
        };
    /**
     * 获取首页频道列表 推荐内容
     * @param count 每个频道推荐个数
     * @return 获取失败，返回null
     */
    public static Channel[] getRecommendChannels(int count) {

        try {
            for (int i = 0; i < channels.length; i++) {
                Channel c = channels[i];
                c.recommends = getChannelRecommends(c.getChannelId(), count);
            }
            return channels;
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Log.e(ApiParser.class.getSimpleName(), "获取失败", e);
            }
            return null;
        }
    }
    public static List<Contents> getChannelRecommends(int channelId, int count) throws Exception{
        String url = "http://www.acfun.tv/api/getlistbyorder.aspx?orderby=6&channelIds="+channelId + "&count=" + count;
        return getChannelContents(url);
    }
    public static List<Map<String, Object>> getComment(String aid, int page) throws Exception {
        String url = "http://www.acfun.tv/comment_list_json.aspx?contentId=" + aid
                + "&currentPage=" + page;
        ArrayList<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
        JSONObject jsonObject = getJsonObj(url);
        JSONArray jsonArray = jsonObject.getJSONArray("commentList");
        int totalPage = jsonObject.getInt("totalPage");
        if (jsonArray.length() > 0) {
            JSONObject comjsonobj = (JSONObject) jsonObject.get("commentContentArr");
            for (int i = 0; i < jsonArray.length(); i++) {
                Map<String, Object> map = new HashMap<String, Object>();
                JSONObject contentobj = comjsonobj.getJSONObject("c" + jsonArray.get(i).toString());
                map.put("userName", contentobj.getString("userName"));
                map.put("content",
                        contentobj.getString("content").replace("&nbsp;", " ")
                                .replace("&amp;", "&").replaceAll("\\<.*?>", "")
                                .replaceAll("\\[.*?]", "").replaceFirst("\\s+", ""));
                map.put("userImg", contentobj.getString("userImg"));
                map.put("totalPage", totalPage);
                comments.add(map);
            }

            return comments;
        } else {
            return comments;
        }
    }

    public static List<Bangumi[]> getBangumiTimeList() throws Exception {

        Connection c = Jsoup.connect("http://www.acfun.tv/v/list67/index.htm").timeout(6000);
        Document doc = c.get();
        Elements ems = doc.getElementsByAttributeValue("id", "bangumi").get(0)
                .getElementsByTag("li");
        ems.remove(ems.size() - 1);
        List<Bangumi[]> timelist = new ArrayList<Bangumi[]>();

        for (Element element : ems) {
            Elements videoems = element.getElementsByClass("title");
            Bangumi[] bangumis = new Bangumi[videoems.size()];

            for (int i = 0; i < videoems.size(); i++) {
                bangumis[i] = new Bangumi();
                bangumis[i].title = videoems.get(i).text();
                bangumis[i].aid = videoems.get(i).attr("data-aid");
            }
            timelist.add(bangumis);

        }
        return timelist;

    }

    public static HashMap<String, Object> ParserAcId(String id, boolean isfromtime)
            throws Exception {

        ArrayList<HashMap<String, Object>> parts = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> video = new HashMap<String, Object>();

        String url = "http://www.acfun.tv/api/content.aspx?query=" + id;
        JSONObject jsonObject = getJsonObj(url);

        JSONArray jsonArray = jsonObject.getJSONArray("content");
        if (Integer.parseInt(id) > 327496) {
            for (int i = 0; i < jsonArray.length(); i++) {

                HashMap<String, Object> map = new HashMap<String, Object>();
                JSONObject job = (JSONObject) jsonArray.get(i);
                String id1 = null;
                String regex = "\\[video\\](.\\d+)\\[/video\\]";
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(job.getString("content"));
                while (matcher.find()) {
                    id1 = matcher.group(1);
                    break;
                }

                String title = (String) job.get("subtitle");
                map.put("title", title.replace("&amp;", "&"));
                String urlp = "http://www.acfun.tv/api/player/vids/" + id1 + ".aspx";
                JSONObject vidjsonObject = getJsonObj(urlp);

                map.put("vtype", vidjsonObject.get("vtype").toString());
                map.put("vid", vidjsonObject.get("vid").toString());
                map.put("success", vidjsonObject.getBoolean("success"));
                parts.add(map);
            }
        } else {

            for (int i = 0; i < jsonArray.length(); i++) {
                String vid = "";
                HashMap<String, Object> map = new HashMap<String, Object>();
                JSONObject job = (JSONObject) jsonArray.get(i);
                String ContentStr = job.toString();
                System.out.println(ContentStr);
                ContentStr = ContentStr.replace("id='ACFlashPlayer'", "").replace(
                        "id=\\\"ACFlashPlayer\\\"", "");
                ;
                System.out.println(ContentStr);
                Pattern p = Pattern.compile("id=(.[0-9a-zA-Z]+)");
                Matcher matcher = p.matcher(ContentStr);
                if (matcher.find()) {
                    vid = matcher.group(1);
                }

                String title = (String) job.get("subtitle");
                map.put("title", title.replace("&amp;", "&"));

                map.put("vtype", ParseVideoType(ContentStr));
                map.put("vid", vid);
                map.put("success", true);
                parts.add(map);

            }

        }

        if (isfromtime) {
            JSONObject jsoninfo = jsonObject.getJSONObject("info");
            HashMap<String, String> info = new HashMap<String, String>();
            info.put("description", jsoninfo.get("description").toString().replace("&nbsp;", " ")
                    .replace("&amp;", "&").replaceAll("\\<.*?>", ""));
            info.put("username", jsoninfo.getJSONObject("postuser").getString("name").toString());
            info.put("views", jsoninfo.getJSONArray("statistics").getInt(0) + "");
            info.put("comments", jsoninfo.getJSONArray("statistics").getInt(1) + "");
            info.put("titleimage", jsoninfo.getString("titleimage"));
            video.put("info", info);
        }

        video.put("pts", parts);

        return video;
    }

    public static final String ParseVideoType(String str) {
        String Type = "";

        if (str.contains("youku")) {
            Type = "youku";
        }
        if (str.contains("sina")) {
            Type = "sina";
        }
        if (str.contains("tudou")) {
            Type = "tudou";
        }
        if (str.contains("qq")) {
            Type = "qq";
        }
        if (Type.equals("video")) {
            Type = "sina";
        }
        if (Type.equals("")) {
            Type = "sina";
        }

        return Type;
    }

    public static Article getArticle(String aid) throws Exception {
        Article article = new Article();

        String url = "http://www.acfun.tv/api/content.aspx?query=" + aid;
        JSONObject jsonObject = getJsonObj(url);

        JSONObject infoobj = jsonObject.getJSONObject("info");
        article.setTitle(infoobj.getString("title"));
        article.setPosttime(infoobj.getLong("posttime"));
        article.setName(infoobj.getJSONObject("postuser").getString("name"));
        article.setUid(infoobj.getJSONObject("postuser").get("uid").toString());
        article.setId(aid);
        JSONArray statistics = infoobj.getJSONArray("statistics");

        article.setViews(statistics.getInt(0));
        article.setComments(statistics.getInt(1));
        article.setStows(statistics.getInt(5));

        JSONArray jsonArray = jsonObject.getJSONArray("content");
        ArrayList<HashMap<String, String>> contents = new ArrayList<HashMap<String, String>>();
        ArrayList<String> imgs = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            JSONObject job = jsonArray.getJSONObject(i);
            map.put("subtitle", job.getString("subtitle"));
            String content = job.getString("content");
            map.put("content", content);

            String regex = "<img.+?src=[\"|'](.+?)[\"|']";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                imgs.add(matcher.group(1));
            }

            contents.add(map);
        }
        article.setImgUrls(imgs);
        article.setContents(contents);
        return article;
    }

    /**
     * 获取搜索结果集
     * 
     * @param word
     * @param page
     * @return 搜索不到或者结果无，返回null
     * @throws Exception
     */
    public static List<Contents> getSearchContents(String word, int page) throws Exception {
        String url = "http://www.acfun.tv/api/search.aspx?query="
                + URLEncoder.encode(word, "utf-8") + "&orderId=0&channelId=0&pageNo=" + page
                + "&pageSize=20";
        JSONObject jsonObject = getJsonObj(url);
        succeeded = jsonObject.getBoolean("success");
        if (!succeeded || (totalcount = jsonObject.getInt("totalcount")) == 0) {
            return null;
        }
        List<Contents> cs = new ArrayList<Contents>();
        JSONArray jsonArray = jsonObject.getJSONArray("contents");
        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject job = (JSONObject) jsonArray.get(i);
            Contents c = new Contents();
            c.setAid(job.getString("aid"));
            c.setTitle(job.getString("title"));
            c.setUsername(job.getString("author"));
            c.setViews(job.getLong("views"));
            c.setTitleImg(job.getString("titleImg"));
            c.setDescription(job.getString("description").replace("&nbsp;", " ")
                    .replace("&amp;", "&").replaceAll("\\<.*?>", ""));
            c.setChannelId(job.getInt("channelId"));
            c.setComments(job.getInt("comments"));
            cs.add(c);
        }

        return cs;
    }

    private static boolean succeeded  = false;
    private static int     totalcount = 0;

    /**
     * 获取搜索结果页数，之前需调用{@link #getSearchContents(String,int)}，否则返回值为-1
     * 
     * @return
     */
    public static int getCountPage() {
        if (!succeeded) {
            return -1; // TODO 1?
        }
        int countpage;
        if (totalcount % 20 == 0) {
            countpage = totalcount / 20;
        } else {
            countpage = totalcount / 20 + 1;
        }
        return countpage;
    }

    public static ArrayList<Object> getSearchResults(String word, int page) throws Exception {
        ArrayList<Object> rsandtotalpage = new ArrayList<Object>();
        String url = "http://www.acfun.tv/api/search.aspx?query="
                + URLEncoder.encode(word, "utf-8") + "&orderId=0&channelId=0&pageNo="
                + String.valueOf(page) + "&pageSize=20";

        List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();

        JSONObject jsonObject = getJsonObj(url);
        Boolean success = jsonObject.getBoolean("success");
        if (success) {
            int totalcount = jsonObject.getInt("totalcount");
            if (totalcount == 0) {
                return null;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject job = (JSONObject) jsonArray.get(i);
                String aid = job.optString("aid");
                // if(Integer.parseInt(aid)<327496){
                // continue;
                // }
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("aid", aid);
                map.put("title", job.optString("title"));
                map.put("username", job.optString("author"));
                // map.put("url", job.optString("url"));
                map.put("views", String.valueOf(job.optInt("views")));
                // map.put("uptime", String.valueOf(job.optInt("releaseDate")));
                map.put("titleImg", job.get("titleImg").toString());
                // map.put("stows", String.valueOf(job.optInt("stows")));
                map.put("description",
                        job.optString("description").replace("&nbsp;", " ").replace("&amp;", "&")
                                .replaceAll("\\<.*?>", ""));
                map.put("comments", job.get("comments").toString());
                map.put("channelId", job.optInt("channelId"));
                contents.add(map);
            }

            rsandtotalpage.add(contents);
            int countpage;
            if (totalcount % 20 == 0) {
                countpage = totalcount / 20;
            } else {
                countpage = totalcount / 20 + 1;
            }
            rsandtotalpage.add(countpage);
        } else {
            return null;
        }

        return rsandtotalpage;

    }

    public static JSONObject getJsonObj(String url) throws MalformedURLException, IOException,
            JSONException, Exception, NetworkErrorException {
        URL lurl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) lurl.openConnection();
        conn.setConnectTimeout(6 * 1000);

        if (conn.getResponseCode() != 200) {
            throw new NetworkErrorException("连接错误:" + conn.getResponseCode());
        }
        InputStream is = conn.getInputStream();
        String jsonstring = DataStore.readData(is, "UTF8");
        conn.disconnect();

        JSONObject jsonObject = new JSONObject(jsonstring);

        return jsonObject;
    }

    public static ArrayList<String> ParserVideopath(String type, String id) throws Exception {
        if (type.equals("sina")) {
            // 新浪
            return getSinaflv(id);
        } else if (type.equals("youku")) {
            return ParserYoukuFlv(id);
        } else if (type.equals("qq")) {
            return ParserQQvideof(id);
        } else if (type.equals("tudou")) {
            return ParserTudouvideo(id);
        }

        return null;
    }

    public static ArrayList<String> getSinaflv(String id) throws IOException {
        ArrayList<String> paths = new ArrayList<String>();
        String url = "http://v.iask.com/v_play.php?vid=" + id;
        Connection c = Jsoup
                .connect(url)
                .userAgent(
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        Document doc = c.get();
        Elements ems = doc.getElementsByTag("url");
        for (Element em : ems) {
            paths.add(em.text());
        }

        return paths;
    }

    public static ArrayList<String> ParserQQvideof(String vid) throws IOException {
        String url = "http://web.qqvideo.tc.qq.com/" + vid + ".flv";
        ArrayList<String> urls = new ArrayList<String>();
        urls.add(url);
        return urls;
    }

    public static ArrayList<String> ParserTudouvideo(String iid) throws IOException {
        ArrayList<String> urls = new ArrayList<String>();
        String url = "http://v2.tudou.com/v?it=" + iid + "&hd=2&st=1%2C2%2C3%2C99";
        Connection c = Jsoup
                .connect(url)
                .userAgent(
                        "Mozilla/5.0（iPad; U; CPU iPhone OS 3_2 like Mac OS X; en-us） AppleWebKit/531.21.10 （KHTML， like Gecko） Version/4.0.4 Mobile/7B314 Safari/531.21.10");
        Document doc = c.get();
        Elements ems = doc.getElementsByTag("f");

        for (Element em : ems) {
            String vurl = em.text();
            urls.add(vurl);
        }
        return urls;
    }

    public static ArrayList<String> ParserYoukuFlv(String id) throws Exception {
        double seed = 0;
        String key1;
        String key2;
        String fileids = null;
        String fileid = null;
        ArrayList<String> K = new ArrayList<String>();
        URL url = new URL("http://v.youku.com/player/getPlayList/VideoIDS/" + id
                + "/timezone/+08/version/5/source/video?n=3&ran=4656");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        conn.setConnectTimeout(6 * 1000);
        if (conn.getResponseCode() != 200)
            throw new RuntimeException("请求url失败");
        InputStream is = conn.getInputStream();
        String jsonstring = DataStore.readData(is, "UTF8");
        conn.disconnect();

        String regexstring = "\"seed\":(\\d+),.+\"key1\":\"(\\w+)\",\"key2\":\"(\\w+)\"";
        Pattern pattern = Pattern.compile(regexstring);
        Matcher matcher = pattern.matcher(jsonstring);
        while (matcher.find()) {
            seed = Double.parseDouble(matcher.group(1));
            key1 = matcher.group(2);
            key2 = matcher.group(3);
        }

        Pattern patternf = Pattern.compile("\"streamfileids\":\\{(.+?)\\}");

        Matcher matcherf = patternf.matcher(jsonstring);
        while (matcherf.find()) {
            fileids = matcherf.group(1);
        }

        Pattern patternfid = Pattern.compile("\"flv\":\"(.+?)\"");
        Matcher matcherfid = patternfid.matcher(fileids);
        while (matcherfid.find()) {
            fileid = matcherfid.group(1);
        }

        String no = null;
        Pattern patternc = Pattern.compile("\"flv\":\\[(.+?)\\]");
        Matcher matcherc = patternc.matcher(jsonstring);
        while (matcherc.find()) {
            no = matcherc.group(0);
        }

        JSONArray array = new JSONArray(no.substring(6));

        for (int i = 0; i < array.length(); i++) {
            JSONObject job = (JSONObject) array.get(i);
            K.add("?K=" + job.getString("k") + ",k2:" + job.getString("k2"));
        }

        String sid = genSid();
        // 生成fileid
        String rfileid = getFileID(fileid, seed);
        ArrayList<String> paths = new ArrayList<String>();
        for (int i = 0; i < K.size(); i++) {
            // 得到地址
            String u = "http://f.youku.com/player/getFlvPath/sid/" + "00" + "_"
                    + String.format("%02d", i) + "/st/" + "flv" + "/fileid/"
                    + rfileid.substring(0, 8) + String.format("%02d", i) + rfileid.substring(10)
                    + K.get(i);
            paths.add(u);
        }

        ArrayList<String> rpaths = new ArrayList<String>();
        for (String path : paths) {
            rpaths.add(getLocationJump(path, false, false));
        }
        return rpaths;
    }

    /* 感谢c大提供的方法-cALMER-flvshow -w- */
    public static String getLocationJump(String httpurl, String agent, boolean followRedirects) {
        String location = httpurl;
        try {
            URL url = new URL(httpurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (!followRedirects) {
                conn.setInstanceFollowRedirects(false);
                conn.setFollowRedirects(false);
            }

            conn.addRequestProperty("User-Agent", agent);
            conn.setRequestProperty("User-Agent", agent);
            location = conn.getHeaderField("Location");
            if (location == null) {
                location = httpurl;
            }
            if (!location.equalsIgnoreCase(httpurl)) {
                location = getLocationJump(location, agent, followRedirects);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return location;
    }

    /* 感谢c大提供的方法-cALMER-flvshow */
    public static String getLocationJump(String paramString, boolean paramBoolean1,
            boolean paramBoolean2) {
        String str = "Lavf52.106.0";
        if (!paramBoolean1)
            str = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";
        return getLocationJump(paramString, str, paramBoolean2);
    }

    public static String genKey(String key1, String key2) {
        int key = Long.valueOf("key1", 16).intValue();
        key ^= 0xA55AA5A5;
        return "key2" + Long.toHexString(key);
    }

    public static String getFileIDMixString(double seed) {
        StringBuilder mixed = new StringBuilder();
        StringBuilder source = new StringBuilder(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
        int index, len = source.length();
        for (int i = 0; i < len; ++i) {
            seed = (seed * 211 + 30031) % 65536;
            index = (int) Math.floor(seed / 65536 * source.length());
            mixed.append(source.charAt(index));
            source.deleteCharAt(index);
        }
        return mixed.toString();
    }

    public static String getFileID(String fileid, double seed) {
        String mixed = getFileIDMixString(seed);
        String[] ids = fileid.split("\\*");
        StringBuilder realId = new StringBuilder();
        int idx;
        for (int i = 0; i < ids.length; i++) {
            idx = Integer.parseInt(ids[i]);
            realId.append(mixed.charAt(idx));
        }
        return realId.toString();
    }

    public static String genSid() {
        int i1 = (int) (1000 + Math.floor(Math.random() * 999));
        int i2 = (int) (1000 + Math.floor(Math.random() * 9000));
        return System.currentTimeMillis() + "" + i1 + "" + i2;
    }

}
