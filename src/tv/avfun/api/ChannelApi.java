
package tv.avfun.api;

import java.util.ArrayList;
import java.util.List;

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
        public static final int[] CHANNEL_IDS = {ANIMATION,MUSIC,GAME,FUN,BANGUMI,MOVIE,SPORT,SCIENCE,MUGEN};
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
    
}
