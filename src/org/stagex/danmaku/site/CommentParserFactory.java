package org.stagex.danmaku.site;

import java.io.IOException;
import java.io.InputStream;

public class CommentParserFactory {

	private static String[] sParserList = new String[] { "Acfun", "Bilibili",
			"Ichiba", "Nico" };

	public static String[] getParserList() {
		return sParserList;
	}

	@SuppressWarnings("rawtypes")
	public static CommentParser createParser(String name) {
		CommentParser parser = null;
		try {
			String cln = CommentParser.class.getName() + name;
			Class clz = Class.forName(cln);
			parser = (CommentParser) clz.newInstance();
		} catch (Exception e) {
		}
		return parser;
	}

	public static CommentParser parse(InputStream in) {
		String[] list = CommentParserFactory.getParserList();
		for (String name : list) {
			CommentParser parser = CommentParserFactory.createParser(name);
			if (parser == null)
				continue;
			try {
				in.reset();
			} catch (IOException e) {
			}
			boolean result = parser.parse(in);
			if (result)
				return parser;
		}
		return null;
	}

}
