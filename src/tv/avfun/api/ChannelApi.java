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
        public static final int VIDEO     = 68;
        public static final int SPORT     = 69;
        public static final int SCIENCE   = 70;
        public static final int FLASH     = 71;
        public static final int MUGEN     = 72;

        public static final class ARTICLE {
            
            public static final int ARTICLE           = 63;
            public static final int COLLECTION        = 110;
            public static final int WORK_EMOTION      = 73;
            public static final int AN_CULTURE        = 74;
            public static final int COMIC_LIGHT_NOVEL = 75;
        }
        public static final int BEST_GAME   = 83;
        public static final int LIVE_OB     = 84;
        public static final int LOL         = 85;
        public static final int FUNY        = 86;
        public static final int KICHIKU     = 87;
        public static final int PET         = 88;
        public static final int EAT         = 89;
        public static final int MOVIE       = 96;
        public static final int TV          = 97;
        public static final int VARIETY     = 98;
        
        public static final int PILI        = 99;
        public static final int DOCUMENTARY = 100;
        public static final int SING        = 101;
        public static final int DANCE       = 102;
        public static final int VOCALOID    = 103;
        public static final int ACG         = 104;
        public static final int POP         = 105;
        public static final int AN_LITE     = 106;
        public static final int MAD_AMV     = 107;
        public static final int MMD_3D      = 108;
        public static final int AN_COMP     = 109;
        
        
//        public static final int[] CHANNEL_IDS = { ANIMATION, MUSIC, GAME, FUN, BANGUMI, MOVIE,
//                                                      SPORT, SCIENCE, MUGEN };
    }

    public static final SparseArray<Channel> channels;
    static {
        channels = new SparseArray<Channel>();
        channels.put(id.ANIMATION, new Channel("动画", id.ANIMATION));
        channels.put(id.AN_LITE, new Channel("动画短片", id.AN_LITE));
        channels.put(id.MAD_AMV, new Channel("MAD·AMV", id.MAD_AMV));
        channels.put(id.MMD_3D, new Channel("MMD·3D", id.MMD_3D));
        channels.put(id.AN_COMP, new Channel("动画合集", id.AN_COMP));
        
        channels.put(id.MUSIC, new Channel("音乐", id.MUSIC));
        channels.put(id.SING, new Channel("演唱", id.SING));
        channels.put(id.DANCE, new Channel("宅舞", id.DANCE));
        channels.put(id.VOCALOID, new Channel("Vocaloid", id.VOCALOID));
        channels.put(id.ACG, new Channel("ACG音乐", id.ACG));
        channels.put(id.POP, new Channel("流行音乐", id.POP));
        
        channels.put(id.FUN, new Channel("娱乐", id.FUN));
        channels.put(id.FUNY, new Channel("生活娱乐", id.FUNY));
        channels.put(id.KICHIKU, new Channel("鬼畜调教", id.KICHIKU));
        channels.put(id.PET, new Channel("萌宠", id.PET));
        channels.put(id.EAT, new Channel("美食", id.EAT));
        channels.put(id.SCIENCE, new Channel("科技", id.SCIENCE));
        channels.put(id.SPORT, new Channel("体育", id.SPORT));
        
        channels.put(id.VIDEO, new Channel("影视", id.VIDEO));
        channels.put(id.MOVIE, new Channel("电影", id.MOVIE));
        channels.put(id.TV, new Channel("剧集", id.TV));
        channels.put(id.VARIETY, new Channel("综艺", id.VARIETY));
        channels.put(id.DOCUMENTARY, new Channel("纪录片", id.DOCUMENTARY));
        channels.put(id.PILI, new Channel("特摄·霹雳", id.PILI));
        
        
        channels.put(id.GAME, new Channel("游戏", id.GAME));
        channels.put(id.BEST_GAME, new Channel("游戏集锦", id.BEST_GAME));
        channels.put(id.LIVE_OB, new Channel("实况解说", id.LIVE_OB));
        channels.put(id.FLASH, new Channel("FLASH", id.FLASH));
        channels.put(id.MUGEN, new Channel("MUGEN", id.MUGEN));
        channels.put(id.LOL, new Channel("撸啊撸", id.LOL));
        
        
        channels.put(id.BANGUMI, new Channel("新番连载", id.BANGUMI));
        
        channels.put(id.ARTICLE.ARTICLE, new Channel("文章", id.ARTICLE.ARTICLE));
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
            apis.add(channels.get(id.AN_LITE));
            apis.add(channels.get(id.BANGUMI));
            apis.add(channels.get(id.MAD_AMV));
            apis.add(channels.get(id.MMD_3D));
            apis.add(channels.get(id.AN_COMP));
            break;
        case 1:
            apis.add(channels.get(id.SING));
            apis.add(channels.get(id.VOCALOID));
            apis.add(channels.get(id.DANCE));
            apis.add(channels.get(id.ACG));
            apis.add(channels.get(id.POP));
            break;
        case 2:
            apis.add(channels.get(id.FUNY));
            apis.add(channels.get(id.KICHIKU));
            apis.add(channels.get(id.PET));
            apis.add(channels.get(id.EAT));
            apis.add(channels.get(id.SCIENCE));
            apis.add(channels.get(id.SPORT));
            break;
        case 3:
            apis.add(channels.get(id.MOVIE));
            apis.add(channels.get(id.TV));
            apis.add(channels.get(id.VARIETY));
            apis.add(channels.get(id.DOCUMENTARY));
            apis.add(channels.get(id.PILI));
            break;
        case 4:
            apis.add(channels.get(id.BEST_GAME));
            apis.add(channels.get(id.LIVE_OB));
            apis.add(channels.get(id.FLASH));
            apis.add(channels.get(id.MUGEN));
            apis.add(channels.get(id.LOL));
            break;
        case 5:
//            apis.add(channels.get(id.BANGUMI));
//            break;
//        case 6:
            apis.add(channels.get(id.ARTICLE.COLLECTION));
            apis.add(channels.get(id.ARTICLE.WORK_EMOTION));
            apis.add(channels.get(id.ARTICLE.AN_CULTURE));
            apis.add(channels.get(id.ARTICLE.COMIC_LIGHT_NOVEL));
            break;
        }
        return apis;
    }
    public static String getChannelTitle(int pos){
    
        switch (pos) {
        case 0:
            return "动画";
        case 1:
            return "音乐";
        case 2:
            return "娱乐";
        case 3:
            return "短影";
        case 4:
            return "游戏";
        case 5:
            return "文章";
        default:
            return null;
        }
    }
    public static int getChannelType(int channelId){
        boolean bArticle = channelId == id.ARTICLE.ARTICLE || channelId == id.ARTICLE.COLLECTION
                || channelId == id.ARTICLE.AN_CULTURE || channelId == id.ARTICLE.COMIC_LIGHT_NOVEL
                || channelId == id.ARTICLE.WORK_EMOTION;
        return bArticle ? 1 : 0;
    }
    private static final int TYPE_DEFAULT      = 0;
    private static final int TYPE_HOT_LIST     = 6;
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
