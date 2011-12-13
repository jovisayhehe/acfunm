package org.stagex.danmaku.site;

public class MessagePlayerFactory {

	private static String[] sPlayerList = new String[] { "Acfun", "Bilibili",
			"Ichiba", "Nico" };

	public static String[] getPlayerList() {
		return sPlayerList;
	}

	@SuppressWarnings("rawtypes")
	public static MessagePlayer createPlayer(String name) {
		MessagePlayer player = null;
		try {
			String cln = MessagePlayer.class.getName() + name;
			Class clz = Class.forName(cln);
			player = (MessagePlayer) clz.newInstance();
		} catch (Exception e) {
		}
		return player;
	}

}
