package org.stagex.danmaku.site;

public class Comment {

	public final static int TYPE_FLY = 1;
	public final static int TYPE_TOP = 2;
	public final static int TYPE_BOT = 4;

	/* parse elements */
	public int time = -1;
	public int type = -1;
	public int size = -1;
	public int color = -1;
	public String text = null;

	/* site specific elements */
	public int x;
	public int y;
	public int width = -1;
	public int height = -1;
	public float speed = -1;
	public int duration = -1;

	/* for internal usage */
	public int id = 0;

	public String toString() {
		return String.format("%d|%d|%d|%d|%s - %d|%d", time, type, size, color,
				text, width, height);
	}

}
