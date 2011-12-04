package org.stagex.danmaku.site;

import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class CommentParserBilibili extends CommentParser {

	protected ArrayList<Comment> mParserResult = new ArrayList<Comment>();

	@Override
	public boolean parse(InputStream in) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(in, null);
			int commentTime = -1;
			int commentType = -1;
			int commentSize = -1;
			int commentColor = -1;
			String commentText = null;
			int currentDepth = 0;
			String tagName = null;
			for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser
					.next()) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					continue;
				}
				if (eventType == XmlPullParser.START_TAG) {
					currentDepth += 1;
					tagName = parser.getName();
					if (currentDepth == 1 && tagName.compareTo("i") != 0) {
						break;
					}
					if (currentDepth == 2) {
						if (tagName.compareTo("d") == 0) {
							int count = parser.getAttributeCount();
							for (int i = 0; i < count; i++) {
								String name = parser.getAttributeName(i);
								String value = parser.getAttributeValue(i);
								if (name.compareTo("p") == 0) {
									String[] props = value.split(",");
									if (props.length < 4) {
										continue;
									}
									commentTime = (int) (Float
											.parseFloat(props[0]) * 1000);
									commentType = Integer.parseInt(props[1]);
									commentSize = Integer.parseInt(props[2]);
									commentColor = Integer.parseInt(props[3]);
									continue;
								}
							}
						}
					}
					continue;
				}
				if (eventType == XmlPullParser.END_TAG) {
					currentDepth -= 1;
					if (currentDepth == 1 && tagName.compareTo("d") == 0) {
						Comment comment = new Comment();
						comment.time = commentTime;
						comment.type = commentType;
						comment.size = commentSize;
						comment.color = commentColor;
						comment.text = commentText;
						mParserResult.add(comment);
					}
					continue;
				}
				if (eventType == XmlPullParser.TEXT) {
					if (currentDepth == 2) {
						if (tagName.compareTo("d") == 0) {
							commentText = parser.getText();
						}
					}
					continue;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public ArrayList<Comment> getParserResult() {
		return mParserResult;
	}

}
