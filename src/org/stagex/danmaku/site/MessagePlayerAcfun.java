package org.stagex.danmaku.site;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.SurfaceHolder;

public class MessagePlayerAcfun extends MessagePlayer {

	private static final String LOGTAG = "DANMAKU-MessagePlayerAcfun";

	/* all loaded comments */
	private ArrayList<Comment> mCommentList = null;
	/* currently valid fly comments */
	private LinkedList<Comment> mFlyCommentList = new LinkedList<Comment>();
	/* currently valid bottom comments */
	private LinkedList<Comment> mBottomCommentList = new LinkedList<Comment>();
	/* currently valid top comments */
	private LinkedList<Comment> mTopCommentList = new LinkedList<Comment>();
	/* the paint */
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	/* the display */
	private SurfaceHolder mDisplay = null;
	private Object mDisplayLock = new Object();
	/* for frame control */
	private int mRenderTimeTotal = 0;
	private int mRenderTimeAverage = 0;
	private int mRenderCount = 0;
	/* renderer thread */
	private Thread mRendererThread = new Thread(new Runnable() {
		@Override
		public void run() {
			/* ??? */
			int fps = 25;
			/* the time first started to render */
			long referenceTime = -1;
			while (!mExited) {
				/* wait to start */
				boolean isPaused = mPaused;
				long pauseStartTime = System.currentTimeMillis();
				try {
					mPausedLock.lock();
					while (mPaused)
						mPausedCond.await();
				} catch (InterruptedException e) {
					break;
				} finally {
					mPausedLock.unlock();
				}
				long pauseEndTime = System.currentTimeMillis();
				// Log.d(LOGTAG, "looping");
				/* check if exit is requested */
				if (mExited) {
					break;
				}
				/* check if it was paused */
				if (isPaused && referenceTime > 0)
					referenceTime += (pauseEndTime - pauseStartTime);
				/* check if seek was requested */
				if (mSeeked) {
					int diff = mSeekTime - mTime;
					referenceTime += diff;
					mSeeked = false;
					mSeekTime = 0;
				}
				/* check if this is the first start */
				long startTime = System.currentTimeMillis();
				if (referenceTime < 0)
					referenceTime = startTime;
				/* calculate where it is */
				mTime = (int) (startTime - referenceTime) + mRenderTimeAverage;
				if (mOnProgressUpdateListener != null)
					mOnProgressUpdateListener
							.onProgressUpdate(mTime, mDuration);
				/* play the comments */
				expireComments(mTime);
				/* sort to compute less or keep original post order? */
				for (int i = 0; i < mCommentList.size(); i++) {
					Comment c = mCommentList.get(i);
					if (mTime < c.time)
						continue;
					if (mTime >= c.time + c.duration)
						continue;
					addComment(c);
				}
				/* now paint */
				drawComments();
				/* update statics */
				long endTime = System.currentTimeMillis();
				mRenderCount += 1;
				mRenderTimeTotal += (endTime - startTime);
				mRenderTimeAverage = mRenderTimeTotal / mRenderCount;
				/* here we can sleep */
			}
		}
	});
	/* events */
	private OnCompletionListener mOnCompletionListener = null;
	private OnProgressUpdateListener mOnProgressUpdateListener = null;
	/* misc */
	int mTime = -1;
	int mDuration = -1;
	boolean mExited = false;
	ReentrantLock mPausedLock = new ReentrantLock();
	Condition mPausedCond = mPausedLock.newCondition();
	boolean mPaused = true;
	boolean mSeeked = false;
	int mSeekTime = 0;

	/* find a position from up to down */
	private static int findDown(LinkedList<Comment> list, Comment c) {
		if (list.size() == 0) {
			c.y = 0;
			list.addFirst(c);
			return c.y;
		}
		Comment t = null;
		int p = list.size() - 1;
		int y0 = 0;
		int h0 = 0;
		int y1 = 0;
		int h1 = 0;
		for (int i = 0; i < list.size(); i++) {
			t = list.get(i);
			y1 = t.y;
			h1 = t.height;
			if (y1 - y0 + h0 >= c.height) {
				p = i;
				break;
			}
			y0 = y1;
			h0 = h1;
		}
		t = list.get(p);
		c.y = t.y + t.height;
		if (c.y > 384)
			c.y = 0;
		list.add(p, c);
		return c.y;
	}

	/* find a position from down to up */
	private static int findUp(LinkedList<Comment> list, Comment c) {
		if (list.size() == 0) {
			c.y = 384 - c.height;
			list.addFirst(c);
			return c.y;
		}
		Comment t = null;
		int p = list.size() - 1;
		int y0 = 384;
		int h0 = 0;
		int y1 = 0;
		int h1 = 0;
		for (int i = 0; i < list.size(); i++) {
			t = list.get(i);
			y1 = t.y;
			h1 = t.height;
			if (y0 - y1 + h1 >= c.height) {
				p = i;
				break;
			}
			y0 = y1;
			h0 = h1;
		}
		t = list.get(p);
		c.y = t.y - c.height;
		if (c.y < 0)
			c.y = 384 - c.height;
		list.add(p, c);
		return c.y;
	}

	/* remove invalid comments */
	private static void expire(LinkedList<Comment> list, int time) {
		Iterator<Comment> it = list.iterator();
		while (it.hasNext()) {
			Comment c = it.next();
			if (time < c.time || time >= c.time + c.duration) {
				it.remove();
				continue;
			}
			if (time >= c.time && c.type <= 3) {
				c.x = 540 - 70 - (int) Math.ceil((float) (time - c.time)
						* c.speed);
				if (c.x + c.width < 0) {
					it.remove();
					continue;
				}
			}
		}
	}

	protected void measureComment(Comment c) {
		mPaint.setTextSize(c.size);
		FontMetrics fm = mPaint.getFontMetrics();
		c.height = (int) Math.ceil(fm.descent - fm.ascent);
		c.width = (int) Math.ceil(mPaint.measureText(c.text));
		/* decompiled from AcfunPlayer and converted */
		if (c.type <= 3) {
			c.x = 540 - 70;
			c.speed = (float) (540 - 70 + c.width) / (float) 10000;
			c.duration = (int) ((float) (540 - 70 + c.width) / c.speed);
		} else {
			c.x = (540 - c.width) / 2;
			c.duration = 3000;
		}
	}

	protected void addComment(Comment c) {
		if (c.type <= 3) {
			/* looks needs more work */
			findDown(mFlyCommentList, c);
		} else if (c.type == 4) {
			findUp(mBottomCommentList, c);
		} else if (c.type == 5) {
			findDown(mTopCommentList, c);
		}
	}

	protected void drawComments() {
		synchronized (mDisplayLock) {
			if (mDisplay != null) {
				Canvas canvas = mDisplay.lockCanvas();
				drawComments(canvas, mPaint, mTopCommentList);
				drawComments(canvas, mPaint, mBottomCommentList);
				// drawComments(canvas, mPaint, mFlyCommentList);
				mDisplay.unlockCanvasAndPost(canvas);
			}
		}
	}

	protected void drawComments(Canvas canvas, Paint paint,
			LinkedList<Comment> list) {
		for (Comment c : list) {
			canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			paint.setTextSize(c.size);
			paint.setColor(c.color | 0xff000000);
			canvas.drawText(c.text, c.x, c.y, paint);
		}
	}

	protected void expireComments(int time) {
		expire(mFlyCommentList, time);
		expire(mBottomCommentList, time);
		expire(mTopCommentList, time);
	}

	@Override
	public int getCurrentPosition() {
		return mTime;
	}

	@Override
	public int getDuration() {
		return mDuration;
	}

	@Override
	public int getVideoHeight() {
		return 384;
	}

	@Override
	public int getVideoWidth() {
		return 540;
	}

	@Override
	public boolean isPlaying() {
		boolean paused = true;
		mPausedLock.lock();
		paused = mPaused;
		mPausedLock.unlock();
		return !paused;
	}

	@Override
	public void pause() {
		mPausedLock.lock();
		mPaused = true;
		mPausedLock.unlock();
	}

	@Override
	public void release() {
		if (mRendererThread.isAlive()) {
			/* request to exit thread */
			mExited = true;
			/* wake it up if needed */
			mPausedLock.lock();
			mPaused = false;
			mPausedCond.signal();
			mPausedLock.unlock();
			/* wait until really exited */
			try {
				mRendererThread.join();
			} catch (InterruptedException e) {
			}
		}
		mOnCompletionListener = null;
		mOnProgressUpdateListener = null;
		mCommentList = null;
		mFlyCommentList.clear();
		mBottomCommentList.clear();
		mTopCommentList.clear();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void seekTo(int msec) {
		mSeekTime = msec;
		mSeeked = true;
	}

	@Override
	public void setDataSource(ArrayList<Comment> data) {
		mCommentList = data;
		for (Comment c : mCommentList) {
			measureComment(c);
			int n = c.time + c.duration;
			if (n > mDuration)
				mDuration = n;
		}
		Log.d(LOGTAG,
				String.format("comment count = %d, duration = %d",
						mCommentList.size(), mDuration));
	}

	@Override
	public void setDisplay(SurfaceHolder display) {
		synchronized (mDisplayLock) {
			mDisplay = display;
		}
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
		mOnProgressUpdateListener = listener;
	}

	@Override
	public void start() {
		if (!mRendererThread.isAlive())
			mRendererThread.start();
		mPausedLock.lock();
		mPaused = false;
		mPausedCond.signal();
		mPausedLock.unlock();
	}

	@Override
	public void stop() {
		if (mRendererThread.isAlive()) {
			/* request to exit thread */
			mExited = true;
			/* wake it up if needed */
			mPausedLock.lock();
			mPaused = false;
			mPausedCond.signal();
			mPausedLock.unlock();
			/* wait until really exited */
			try {
				mRendererThread.join();
			} catch (InterruptedException e) {
			}
		}
	}

}
