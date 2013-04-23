package tv.avfun.api;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;

public class ChannelApi {

    /**
     * 频道的id
     * 
     */
    public static final class id {

        public static final int ANIMATION = 1;
        public static final int MUSIC     = 58;
        public static final int GAME      = 59;
        public static final int FUN       = 60;
        public static final int BANGUMI   = 67;
        public static final int MOVIE     = 68;
        public static final int SPORT     = 69;
        public static final int SCIENCE   = 70;
        public static final int MUGEN     = 72;

        public static final class ARTICLE {

            public static final int COLLECTION        = 63;
            public static final int WORK_EMOTION      = 73;
            public static final int AN_CULTURE        = 74;
            public static final int COMIC_LIGHT_NOVEL = 75;
        }

        public static final int[] CHANNEL_IDS = { ANIMATION, MUSIC, GAME, FUN, BANGUMI, MOVIE,
                                                      SPORT, SCIENCE, MUGEN };
    }

    public static final SparseArray<Channel> channels;
    static {
        channels = new SparseArray<Channel>();
        channels.put(id.ANIMATION, new Channel("动画", id.ANIMATION));
        channels.put(id.MUSIC, new Channel("音乐", id.MUSIC));
        channels.put(id.FUN, new Channel("娱乐", id.FUN));
        channels.put(id.SCIENCE, new Channel("科技", id.SCIENCE));
        channels.put(id.SPORT, new Channel("体育", id.SPORT));
        channels.put(id.MOVIE, new Channel("短影", id.MOVIE));
        channels.put(id.GAME, new Channel("游戏", id.GAME));
        channels.put(id.MUGEN, new Channel("Mugen", id.MUGEN));
        channels.put(id.BANGUMI, new Channel("番剧", id.BANGUMI));
        channels.put(id.ARTICLE.COLLECTION, new Channel("综合", id.ARTICLE.COLLECTION));
        channels.put(id.ARTICLE.WORK_EMOTION, new Channel("工作·情感", id.ARTICLE.WORK_EMOTION));
        channels.put(id.ARTICLE.AN_CULTURE, new Channel("动漫文化", id.ARTICLE.AN_CULTURE));
        channels.put(id.ARTICLE.COMIC_LIGHT_NOVEL, new Channel("漫画·轻小说",
                id.ARTICLE.COMIC_LIGHT_NOVEL));
    }

    public static List<Channel> getApi(int pos) {

        List<Channel> apis = new ArrayList<Channel>();
        switch (pos) {
        case 0:
            apis.add(new Channel("动画", id.ANIMATION));
            break;
        case 1:
            apis.add(new Channel("音乐", id.MUSIC));
            break;
        case 2:
            apis.add(new Channel("娱乐", id.FUN));
            apis.add(new Channel("科技", id.SCIENCE));
            apis.add(new Channel("体育", id.SPORT));
            break;
        case 3:
            apis.add(new Channel("短影", id.MOVIE));
            break;
        case 4:
            apis.add(new Channel("游戏", id.GAME));
            apis.add(new Channel("Mugen", id.MUGEN));
            break;
        case 5:
            apis.add(new Channel("番剧", id.BANGUMI));
            break;
        case 6:
            apis.add(new Channel("综合", id.ARTICLE.COLLECTION));
            apis.add(new Channel("工作·情感", id.ARTICLE.WORK_EMOTION));
            apis.add(new Channel("动漫文化", id.ARTICLE.AN_CULTURE));
            apis.add(new Channel("漫画·轻小说", id.ARTICLE.COMIC_LIGHT_NOVEL));
            break;
        }
        return apis;
    }

    private static final int TYPE_DEFAULT      = 0;
    private static final int TYPE_HOT_LIST     = 7;
    private static final int TYPE_LATEST_REPLY = 22;
    private static String    baseUrl           = "http://www.acfun.tv/api/getlistbyorder.aspx?orderby=";

    /**
     * 获得默认形式(最新发布)列表的url
     */
    public static String getDefaultUrl(int channelId, int count) {
        return getUrl(TYPE_DEFAULT, channelId, count);
    }

    /**
     * 获得周热门列表url
     */
    public static String getHotListUrl(int channelId, int count) {
        return getUrl(TYPE_HOT_LIST, channelId, count);
    }

    /**
     * 获得最新回复列表url
     */
    public static String getLatestRepliedUrl(int channelId, int count) {
        return getUrl(TYPE_LATEST_REPLY, channelId, count);
    }

    private static String getUrl(int type, int channelId, int count) {
        return baseUrl + type + "&channelIds=" + channelId + "&count=" + count;
    }
}
