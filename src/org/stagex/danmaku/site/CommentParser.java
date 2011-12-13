package org.stagex.danmaku.site;

import java.io.InputStream;
import java.util.ArrayList;

public abstract class CommentParser {

	public abstract boolean parse(InputStream is);

	public abstract ArrayList<Comment> getParserResult();

	public String getParserName() {
		String prefix = CommentParser.class.getName();
		String content = this.getClass().getName();
		return content.substring(prefix.length());
	}

}
