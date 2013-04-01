
package tv.avfun.api;

import java.util.ArrayList;
import java.util.List;

public class ChannelApi {

    public static List<Channel> getApi(int pos) {

        List<Channel> apis = new ArrayList<Channel>();
        switch (pos) {
        case 0:
            apis.add(new Channel("动画", Channel.id.ANIMATION));
            break;
        case 1:
            apis.add(new Channel("音乐", Channel.id.MUSIC));
            break;
        case 2:
            apis.add(new Channel("娱乐", Channel.id.FUN));
            apis.add(new Channel("科技", Channel.id.SCIENCE));
            apis.add(new Channel("体育", Channel.id.SPORT));
            break;
        case 3:
            apis.add(new Channel("短影", Channel.id.MOVIE));
            break;
        case 4:
            apis.add(new Channel("游戏", Channel.id.GAME));
            apis.add(new Channel("Mugen", Channel.id.MUGEN));
            break;
        case 5:
            apis.add(new Channel("番剧", Channel.id.BANGUMI));
            break;
        case 6:
            apis.add(new Channel("综合", Channel.id.ARTICLE.COLLECTION));
            apis.add(new Channel("工作·情感", Channel.id.ARTICLE.WORK_EMOTION));
            apis.add(new Channel("动漫文化", Channel.id.ARTICLE.AN_CULTURE));
            apis.add(new Channel("漫画·轻小说", Channel.id.ARTICLE.COMIC_LIGHT_NOVEL));
            break;
        }
        return apis;
    }

}
