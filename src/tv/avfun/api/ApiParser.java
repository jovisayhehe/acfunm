package tv.avfun.api;


import java.io.IOException;
import java.net.HttpURLConnection;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tv.avfun.BuildConfig;
import tv.avfun.R;
import tv.avfun.api.ChannelApi.id;
import tv.avfun.api.net.Connectivity;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Article;
import tv.avfun.entity.Contents;
import tv.avfun.entity.VideoInfo;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.avfun.util.DataStore;
import tv.avfun.util.NetWorkUtil;
import tv.avfun.util.StringUtil;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

public class ApiParser {
    /**
     * 确保解析失败时，立即重试
     */
    private class RetryParse extends Throwable{}
    private static final String TAG = ApiParser.class.getSimpleName();
    public static List<Contents> getChannelContents(int channelId, String address) throws Exception {

        JSONObject jsonObject = Connectivity.getJSONObject(address);
        return getChannelContents(channelId,jsonObject);
    }
    /**
     * 获取频道内容。
     * @param jsonObject
     * @return
     * @throws Exception
     */
    public static List<Contents> getChannelContents(int channelId,JSONObject jsonObject) throws Exception{
        List<Contents> contents = new ArrayList<Contents>();
        if(jsonObject == null) return null;
        JSONArray jsarray = jsonObject.getJSONArray("contents");
        for (int i = 0; i < jsarray.length(); i++) {
            JSONObject jobj = (JSONObject) jsarray.get(i);
            Contents c = new Contents();
            c.setTitle(jobj.getString("title"));
            c.setUsername(jobj.getString("username"));
/*            c.setDescription(jobj.getString("description").replace("&nbsp;", " ")
                    .replace("&amp;", "&").replaceAll("\\<.*?>", ""));*/
            c.setDescription(jobj.getString("description"));
            c.setViews(jobj.getLong("views"));
            c.setTitleImg(jobj.getString("titleImg"));
            c.setAid(jobj.getString("aid"));
            // 好多不知名的东西乱入... 比如 110 84 之类的id
            // 由调用者自己决定id
            // c.setChannelId(jobj.getInt("channelId"));
            c.setChannelId(channelId);
            c.setComments(jobj.getInt("comments"));

            contents.add(c);
        }
        return contents;
    }
    
    /*
     * 首页主频道列表
     */
    private static Channel[] channels = new Channel[]{
          new Channel("动画", id.ANIMATION, R.drawable.title_bg_anim),
          new Channel("音乐", id.MUSIC, R.drawable.title_bg_music),
          new Channel("娱乐", id.FUN, R.drawable.title_bg_fun),
          new Channel("短影", id.MOVIE, R.drawable.title_bg_movie),
          new Channel("游戏", id.GAME , R.drawable.title_bg_game),
          new Channel("番剧", id.BANGUMI, R.drawable.title_bg_anim)
        };
    /**
     * 获取首页频道列表 推荐内容
     * @param count 每个频道推荐个数
     * @param mode 显示模式 1 default, 2 hot list, 3 latest replied
     * @return 获取失败，返回null
     */
    public static Channel[] getRecommendChannels(int count, String mode) {
        if(!NetWorkUtil.isNetworkAvailable(AcApp.context())) return null;
        try {
            for (int i = 0; i < channels.length; i++) {
                Channel c = channels[i];
                if("3".equals(mode))
                    c.contents = getChannelLatestReplied(c.getChannelId(), count);
                else if("2".equals(mode))
                    c.contents  = getChannelHotList(c.getChannelId(), count);
                else
                    c.contents  = getChannelDefault(c.getChannelId(), count);
                if(c.contents == null) return null; // 有一个获取失败直接返回null
            }
            
            
            return channels;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(ApiParser.class.getSimpleName(), "获取失败", e);
            }
            return null;
        }
    }
    public static List<Contents> getChannelDefault(int channelId, int count) throws Exception{
        String url = ChannelApi.getDefaultUrl(channelId, count);
        return getChannelContents(channelId,url);
    }
    public static List<Contents> getChannelHotList(int channelId, int count) throws Exception {
        String url = ChannelApi.getHotListUrl(channelId, count);
        return getChannelContents(channelId,url);
    }
    public static List<Contents> getChannelLatestReplied(int channelId, int count) throws Exception {
        String url = ChannelApi.getLatestRepliedUrl(channelId, count);
        return getChannelContents(channelId,url);
    }
    public static List<Map<String, Object>> getComment(String aid, int page) throws Exception {
        String url = "http://www.acfun.tv/comment_list_json.aspx?contentId=" + aid
                + "&currentPage=" + page;
        ArrayList<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
        JSONObject jsonObject = Connectivity.getJSONObject(url);
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

    public static List<Bangumi[]> getBangumiTimeList(){
        Document doc;
        try {
            // http://www.acfun.tv/v/list67/index.htm
            doc = Connectivity.getDoc("http://www.acfun.tv/v/list67/index.htm", UserAgent.getRandom());
        } catch (IOException e) {
            if(BuildConfig.DEBUG)
                Log.e("Parser", "get time list failed", e);
            return null;
        }
        Elements ems = doc.getElementsByAttributeValue("id", "bangumi");
        if(ems.size() == 0) {
            if(BuildConfig.DEBUG) Log.e("Parser", "获取失败！检查网页连接！"); 
            return null;
        }
        ems = ems.get(0).getElementsByTag("li");
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
    public static VideoInfo getVideoInfoByAid(String aid) throws Exception{
        VideoInfo video = new VideoInfo();
        video.parts = new ArrayList<VideoPart>();
        String url = "http://www.acfun.tv/api/content.aspx?query=" + aid;
        JSONObject jsonObject = Connectivity.getJSONObject(url);
        // get tags
        JSONArray tagsArray = jsonObject.getJSONArray("tags");
        video.tags = new String[tagsArray.length()];
        for(int i=0; i<tagsArray.length();i++){
            video.tags[i] = tagsArray.getJSONObject(i).getString("name");
        }
        // get info 
        JSONObject info = jsonObject.getJSONObject("info");
        video.title = StringUtil.getSource(info.getString("title"));
        video.titleImage = info.getString("titleimge");
        video.description = info.getString("description");
        video.channelId = info.getJSONObject("channel").getInt("channelID");
        video.upman = info.getJSONObject("postuser").getString("name");
        // statistics Array
        JSONArray statisArray = info.getJSONArray("statistics");
        video.views = statisArray.getInt(0);
        video.comments = statisArray.getInt(1);
        // get content array
        JSONArray contentArray = jsonObject.getJSONArray("content");
        if (Integer.parseInt(aid) > 327496) {
            for (int i = 0; i < contentArray.length(); i++) {
                VideoPart item = new VideoPart();
                JSONObject job = (JSONObject) contentArray.get(i);
                String videoContent = job.getString("content"); // content: "[video]425564[/video]"
                Matcher matcher = Pattern.compile("\\[video\\](.\\d+)\\[/video\\]", Pattern.CASE_INSENSITIVE).matcher(videoContent);
                String contentId = "";
                if(matcher.find()) contentId = matcher.group(1); 
                item.subtitle = Html.fromHtml(job.getString("subtitle")).toString();
                // parse vid and type into item
                parseVideoItem(contentId, item);
                video.parts.add(item);
                
            }
        } else {
            for (int i = 0; i < contentArray.length(); i++) {
                String vid = "";
                VideoPart item = new VideoPart();
                JSONObject job = (JSONObject) contentArray.get(i);
                String ContentStr = job.toString();
                
                //System.out.println(ContentStr);
                ContentStr = ContentStr.replace("id='ACFlashPlayer'", "").replace(
                        "id=\\\"ACFlashPlayer\\\"", "");
                
                //System.out.println(ContentStr);
                Pattern p = Pattern.compile("id=(.[0-9a-zA-Z]+)");
                Matcher matcher = p.matcher(ContentStr);
                if (matcher.find()) {
                    vid = matcher.group(1);
                }

                String title = (String) job.get("subtitle");
                item.subtitle = Html.fromHtml(title).toString();
                item.vid = vid;
                item.vtype = parseVideoType(ContentStr);
                video.parts.add(item);
            }
        }
        return video;
    }
    private static void parseVideoItem(String contentId, VideoPart item) throws Exception{
        String url = "http://www.acfun.tv/api/player/vids/" + contentId + ".aspx";
        JSONObject jsonObject = Connectivity.getJSONObject(url);
        item.vtype = jsonObject.getString("vtype");
        item.vid = jsonObject.get("vid").toString();
        if(!AcApp.getConfig().getBoolean("isHD", false) && "sina".equals(parseVideoType(item.vtype))){
            item.vid = getSinaMp4Vid(item.vid);
        }
    }

    public static final String parseVideoType(String str) {
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
        JSONObject jsonObject = Connectivity.getJSONObject(url);

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
        JSONObject jsonObject = Connectivity.getJSONObject(url);
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

    /*public static ArrayList<Object> getSearchResults(String word, int page) throws Exception {
        ArrayList<Object> rsandtotalpage = new ArrayList<Object>();
        String url = "http://www.acfun.tv/api/search.aspx?query="
                + URLEncoder.encode(word, "utf-8") + "&orderId=0&channelId=0&pageNo="
                + String.valueOf(page) + "&pageSize=20";

        List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();

        JSONObject jsonObject = Connectivity.getJSONObject(url);
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

    }*/
    /**
     * FIXME 解析似乎有问题！！！
     * 解析视频地址 到item中
     * @param item
     * @param parseMode 0为标清 1高清优先 2 超清优先
     */
    public static void parseVideoParts(VideoPart item, int parseMode){
        if(item == null || TextUtils.isEmpty(item.vid))
            throw new IllegalArgumentException("item or item's vid cannot be null");
        if("sina".equals(item.vtype)){
            parseSinaVideoItem(item, parseMode);
        }else if("youku".equals(item.vtype)){
            parseYoukuVideoItem(item, parseMode); 
        }else if("tudou".equals(item.vtype)){
            parseTudouVideoItem(item);
        }else if("qq".equals(item.vtype)){
            parseQQVideoItem(item);
        }
    }

    private static String getSinaMp4Vid(String vid) throws Exception{
        String checkIdUrl = "http://video.sina.com.cn/interface/video_ids/video_ids.php?v="+vid;
        JSONObject jsonObj = Connectivity.getJSONObject(checkIdUrl);
        if(jsonObj == null) return null;
        int ipadVid = jsonObj.getInt("ipad_vid");
        if(ipadVid != 0 && ipadVid != Integer.valueOf(vid)) vid = ipadVid+""; // 赋予新Id
        return vid;
    }
    public static void parseSinaVideoItem(VideoPart item, int parseMode){
        if(item == null || TextUtils.isEmpty(item.vid))
            throw new IllegalArgumentException("item or item's vid cannot be null");
        try {
            if(parseMode<2){
                item.vid = getSinaMp4Vid(item.vid); // 获取mp4 的vid
                if(BuildConfig.DEBUG) Log.i(TAG, "获取sina MP4");
            }
            String url = "http://v.iask.com/v_play.php?vid=" + item.vid;
            Document doc = Connectivity.getDoc(url, UserAgent.IPAD);
            Elements durls = doc.getElementsByTag("durl");
            item.segments = new ArrayList<VideoSegment>();
            for(int i=0;i<durls.size();i++){
                Element durl = durls.get(i);
                String second = durl.getElementsByTag("length").get(0).text();
                String text = durl.getElementsByTag("url").get(0).text();
                if(BuildConfig.DEBUG)
                    Log.i("parse sina", "url="+text+"，lenght="+second);
                VideoSegment s = new VideoSegment();
                s.duration = Integer.parseInt(second);
                s.num = i;
                s.url = text;
                s.stream = s.url; // TODO: get download url 
                item.segments.add(s);
            }
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                Log.w(TAG, "获取sina视频失败"+item.vid,e);
        }
    }
    public static void parseQQVideoItem(VideoPart item){
        if(item == null || TextUtils.isEmpty(item.vid))
            throw new IllegalArgumentException("item or item's vid cannot be null");
        //vid=84sHlkSh6bE
        //String url = "http://video.store.qq.com/"+item.vid+".flv?channel=web&rfc=v0";
        String url = "http://vv.video.qq.com/geturl?otype=json&vid="+item.vid;
        try {
            HttpURLConnection conn = Connectivity.openConnection(url);
            if(conn.getResponseCode() == 200){
                String raw = DataStore.readData(conn.getInputStream(),"UTF-8");
                int start = "QZOutputJson=".length();
                int end = raw.lastIndexOf(';');
                end = end < 0 ? raw.length():end;
                JSONObject jsonObject = new JSONObject(raw.substring(start,end));
                JSONArray viArray = jsonObject.getJSONObject("vd").getJSONArray("vi");
                item.segments = new ArrayList<VideoSegment>();
                for(int i=0;i<viArray.length();i++){
                    JSONObject vi = viArray.getJSONObject(i);
                    VideoSegment s = new VideoSegment();
                    s.duration = (int) Float.parseFloat(vi.getString("dur")); // "dur": "6022.36"
                    s.size = vi.getLong("fs"); // "fs": 452279984
                    s.num = i;
                    s.url = vi.getString("url");// "url": "http://vhotwsh.video.qq.com/flv/76/54/84sHlkSh6bE.mp4?vkey=...
                    s.stream = s.url;
                    item.segments.add(s);
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        
    }

    public static void parseTudouVideoItem(VideoPart item){
        if(item == null || TextUtils.isEmpty(item.vid))
            throw new IllegalArgumentException("item or item's vid cannot be null");
        String url = "http://v2.tudou.com/v?it=" + item.vid;
        try {
            Elements ems = Connectivity.getElements(url, "f");
            item.segments = new ArrayList<VideoSegment>();
            String sha1 = "";
            for (int i=0;i<ems.size();i++) {
                Element em = ems.get(i);
                String eSha1 = em.attr("sha1");
                if(!sha1.equals(eSha1)){
                    sha1 = eSha1;
                    String size = em.attr("size");
                    if(BuildConfig.DEBUG){
                        String brt = em.attr("brt");
                        Log.d(TAG, Integer.parseInt(brt)+"url="+em.text()+",size="+size);
                    }
                    VideoSegment s = new VideoSegment();
                    s.url = em.text();
                    s.size = Long.parseLong(size);
                    s.num = i;
                    s.stream = s.url;
                    item.segments.add(s);
                }
            }
        } catch (Exception e) {
            if(BuildConfig.DEBUG)
                Log.w(TAG, "解析视频地址失败"+url,e);
        }
    }
    /**
     * FIXME MP4解析似乎有问题
     * @param item
     * @param parseMode 0为标清 1高清 2 超清如果有的话
     */
    public static void parseYoukuVideoItem(VideoPart item, int parseMode){
        if(item == null || TextUtils.isEmpty(item.vid))
            throw new IllegalArgumentException("item or item's vid cannot be null");
        String url = "http://v.youku.com/player/getPlayList/VideoIDS/"+item.vid;
        try {
            JSONObject jsonObject = Connectivity.getJSONObject(url);
            if(jsonObject == null) return;
            JSONObject data = jsonObject.getJSONArray("data").getJSONObject(0);
            Double seed = data.getDouble("seed");
            JSONObject fileids = data.getJSONObject("streamfileids");
            
            String seg = null;
            String fids = null;
            if(parseMode >= 2 && fileids.has("hd2")){
                seg = "hd2";
                if(BuildConfig.DEBUG) Log.i(TAG, "hd2超清模式");
            }else if(parseMode >= 1 && fileids.has("mp4")){
                seg = "mp4";
                if(BuildConfig.DEBUG) Log.i(TAG, "mp4高清模式");
            }else if(fileids.has("flv")){
                seg = "flv";
                if(BuildConfig.DEBUG) Log.i(TAG, "flv标清模式");
            }
            fids = fileids.getString(seg);
            String realFileid =getFileID(fids, seed); 
            
            JSONObject segs = data.getJSONObject("segs");
            
            JSONArray vArray = segs.getJSONArray(seg);
            
            item.segments = new ArrayList<VideoSegment>(vArray.length()); 
            String vPath = seg.equals("mp4")?"mp4":"flv";
            for(int i=0;i<vArray.length();i++){
                JSONObject part = vArray.getJSONObject(i);
                String k = part.getString("k");
                String k2 = part.getString("k2");
                VideoSegment s = new VideoSegment();
                s.duration = (int) Float.parseFloat(part.getString("seconds"));
                s.num = i;
                s.size = part.getLong("size");
                String u = "http://f.youku.com/player/getFlvPath/sid/00_"+ String.format("%02d", i)+"/st/"+vPath+"/fileid/"+ realFileid.substring(0, 8)+ String.format("%02d", i) + realFileid.substring(10)+"?K="+k+",k2:"+k2;
                if(BuildConfig.DEBUG) Log.i(TAG, "url= "+u);
                s.url = Connectivity.getRedirectLocation(u, UserAgent.DEFAULT);
                item.segments.add(s);
            }
        } catch (JSONException e) {
            if(BuildConfig.DEBUG)
                Log.w(TAG, "解析视频地址失败"+url,e);
        }
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
