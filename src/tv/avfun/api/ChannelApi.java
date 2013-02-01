package tv.avfun.api;

import java.util.ArrayList;

public class ChannelApi {
	
	public  static ArrayList<String[]> getApi(int pos){
		ArrayList<String[]> apis = new ArrayList<String[]>();
		switch (pos) {
		case 0:
			apis.add(new String[]{"动画"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=1&currentPage="});
			return apis;
		case 1:
			apis.add(new String[]{"音乐"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=58&currentPage="});
			return apis;
		case 2:
			
			apis.add(new String[]{"娱乐","科技","体育"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=60&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=70&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=69&currentPage="});
			return apis;
			
		case 3:
			apis.add(new String[]{"短影"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=68&currentPage="});
			return apis;
		case 4:
			apis.add(new String[]{"游戏","Mugen"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=59&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=72&currentPage="});
			return apis;
		case 5:
			apis.add(new String[]{"番剧"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=67&currentPage="});
			return apis;
		case 6:
			apis.add(new String[]{"综合","工作·情感","动漫文化","漫画·轻小说"});
			apis.add(new String[]{"http://www.acfun.tv/api/channel.aspx?query=63&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=73&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=74&currentPage=",
					"http://www.acfun.tv/api/channel.aspx?query=75&currentPage="});
			return apis;
		default:
			return apis;
		}
		
	}
}
