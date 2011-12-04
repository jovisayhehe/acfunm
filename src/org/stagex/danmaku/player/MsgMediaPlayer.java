package org.stagex.danmaku.player;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.stagex.danmaku.helper.SystemUtility;
import org.stagex.danmaku.site.CommentParser;
import org.stagex.danmaku.site.CommentParserFactory;
import org.stagex.danmaku.site.MessagePlayer;
import org.stagex.danmaku.site.MessagePlayerFactory;

import android.graphics.PixelFormat;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

public class MsgMediaPlayer extends AbsMediaPlayer implements
		MessagePlayer.OnCompletionListener,
		MessagePlayer.OnProgressUpdateListener {

	private static final String LOGTAG = "DANMAKU-MsgMediaPlayer";

	protected static MsgMediaPlayer sInstance = null;

	protected Thread mPrepareThread = null;

	protected String mDataSource = null;

	protected SurfaceHolder mDisplay = null;

	protected CommentParser mCommentParser = null;
	protected MessagePlayer mMessagePlayer = null;

	protected OnBufferingUpdateListener mOnBufferingUpdateListener = null;
	protected OnCompletionListener mOnCompletionListener = null;
	protected OnErrorListener mOnErrorListener = null;
	protected OnPreparedListener mOnPreparedListener = null;
	protected OnProgressUpdateListener mOnProgressUpdateListener = null;
	protected OnVideoSizeChangedListener mOnVideoSizeChangedListener = null;

	public static MsgMediaPlayer getInstance() {
		if (sInstance == null)
			sInstance = new MsgMediaPlayer();
		return sInstance;
	}

	protected MsgMediaPlayer() {

	}

	@Override
	public int getCurrentPosition() {
		if (mMessagePlayer != null)
			return mMessagePlayer.getCurrentPosition();
		return -1;
	}

	@Override
	public int getDuration() {
		/* not useful, should synchronize with the video */
		if (mMessagePlayer != null)
			return mMessagePlayer.getDuration();
		return -1;
	}

	@Override
	public int getVideoHeight() {
		if (mMessagePlayer != null)
			return mMessagePlayer.getVideoHeight();
		return -1;
	}

	@Override
	public int getVideoWidth() {
		if (mMessagePlayer != null)
			return mMessagePlayer.getVideoWidth();
		return -1;
	}

	@Override
	public boolean isLooping() {
		/* not useful, should synchronize with the video */
		return false;
	}

	@Override
	public boolean isPlaying() {
		if (mMessagePlayer != null)
			return mMessagePlayer.isPlaying();
		return false;
	}

	@Override
	public void pause() {
		if (mMessagePlayer != null)
			mMessagePlayer.pause();
	}

	@Override
	public void prepare() {
		/* only file and HTTP is supported */
		if (mDataSource == null
				|| (!mDataSource.startsWith("/")
						&& !mDataSource.startsWith("file://") && !mDataSource
							.startsWith("http://"))) {
			Log.d(LOGTAG, String.format("not supported input %s", mDataSource));
			if (mOnErrorListener != null) {
				mOnErrorListener.onError(this, 0, 0);
			}
			return;
		}
		/* fake buffering update */
		if (mOnBufferingUpdateListener != null) {
			mOnBufferingUpdateListener.onBufferingUpdate(this, 0);
		}
		try {
			/* prepare the file that may contain messages */
			if (mDataSource.startsWith("file://")) {
				mDataSource = Uri.decode(mDataSource);
				mDataSource = mDataSource.substring(7);
			}
			InputStream in = null;
			if (mDataSource.startsWith("/")) {
				in = new FileInputStream(mDataSource);
			} else {
				String fileName = mDataSource.substring(mDataSource
						.lastIndexOf('/'));
				int indexOfDot = fileName.lastIndexOf('.');
				if (indexOfDot >= 0) {
					fileName = fileName.substring(0, indexOfDot);
				}
				/* TODO: where to delete or place it? */
				fileName = String.format("%s/%s.xml",
						SystemUtility.getTempPath(), fileName);
				boolean downloaded = SystemUtility.simpleHttpGet(mDataSource,
						fileName);
				if (!downloaded) {
					Log.d(LOGTAG,
							String.format("failed to download %s", mDataSource));
					if (mOnErrorListener != null) {
						mOnErrorListener.onError(this, 0, 0);
					}
					return;
				}
				in = new FileInputStream(fileName);
			}
			/* parse it */
			mCommentParser = CommentParserFactory.parse(in);
			in.close();
			if (mCommentParser == null) {
				Log.d(LOGTAG, String.format("failed to parse %s", mDataSource));
				if (mOnErrorListener != null) {
					mOnErrorListener.onError(this, 0, 0);
				}
				return;
			}
			/* create corresponding message player */
			String site = mCommentParser.getParserName();
			mMessagePlayer = MessagePlayerFactory.createPlayer(site);
			if (mMessagePlayer == null) {
				Log.d(LOGTAG,
						String.format("failed to create a player for %s", site));
				if (mOnErrorListener != null) {
					mOnErrorListener.onError(this, 0, 0);
				}
				return;
			}
			/* display */
			mMessagePlayer.setDisplay(mDisplay);
			/* message player is fix-sized so here we have size */
			if (mOnVideoSizeChangedListener != null) {
				int videoWidth = mMessagePlayer.getVideoWidth();
				int videoHeight = mMessagePlayer.getVideoHeight();
				mOnVideoSizeChangedListener.onVideoSizeChangedListener(this,
						videoWidth, videoHeight);
			}
			/* listen to available message player events */
			mMessagePlayer.setOnCompletionListener(this);
			mMessagePlayer.setOnProgressUpdateListener(this);
			/* fake buffering update */
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(this, 50);
			}
			/* message player will deal with comments, this may take time */
			mMessagePlayer.setDataSource(mCommentParser.getParserResult());
			/* fake buffering update */
			if (mOnBufferingUpdateListener != null) {
				mOnBufferingUpdateListener.onBufferingUpdate(this, 100);
			}
		} catch (IOException e) {
			Log.d(LOGTAG, "message player failed to prepare");
			if (mOnErrorListener != null) {
				mOnErrorListener.onError(this, 0, 0);
			}
			return;
		}
		/* everything is fine */
		Log.d(LOGTAG, "message player successfully prepared");
		if (mOnPreparedListener != null) {
			mOnPreparedListener.onPrepared(this);
		}
	}

	@Override
	public void prepareAsync() {
		if (mPrepareThread != null && mPrepareThread.isAlive())
			return;
		mPrepareThread = new Thread(new Runnable() {
			@Override
			public void run() {
				MsgMediaPlayer.this.prepare();
			}
		});
		mPrepareThread.start();
	}

	@Override
	public void release() {
		if (mPrepareThread.isAlive()) {
			try {
				mPrepareThread.join();
			} catch (InterruptedException e) {
			}
		}
		if (mCommentParser != null)
			mCommentParser = null;
		if (mMessagePlayer != null) {
			mMessagePlayer.stop();
			mMessagePlayer.release();
			mMessagePlayer = null;
		}
		mOnBufferingUpdateListener = null;
		mOnCompletionListener = null;
		mOnErrorListener = null;
		mOnPreparedListener = null;
		mOnProgressUpdateListener = null;
		mOnVideoSizeChangedListener = null;
	}

	@Override
	public void reset() {
		if (mMessagePlayer != null)
			mMessagePlayer.reset();
	}

	@Override
	public void seekTo(int msec) {
		if (mMessagePlayer != null)
			mMessagePlayer.seekTo(msec);
	}

	@Override
	public void setDataSource(String path) {
		mDataSource = path;
	}

	@Override
	public void setDisplay(SurfaceHolder display) {
		mDisplay = display;
		if (display != null)
			mDisplay.setFormat(PixelFormat.RGBA_8888);
		if (mMessagePlayer != null)
			mMessagePlayer.setDisplay(display);
	}

	@Override
	public void setLooping(boolean looping) {
		/* not used for now */
	}

	@Override
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	@Override
	public void setOnInfoListener(OnInfoListener listener) {
		/* not used */
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	@Override
	public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
		mOnProgressUpdateListener = listener;
	}

	@Override
	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListener = listener;
	}

	@Override
	public void start() {
		if (mMessagePlayer != null)
			mMessagePlayer.start();
	}

	@Override
	public void stop() {
		if (mMessagePlayer != null)
			mMessagePlayer.stop();
	}

	@Override
	public int getAudioTrackCount() {
		return 0;
	}

	@Override
	public int getAudioTrack() {
		return -1;
	}

	@Override
	public void setAudioTrack(int index) {
		/* not possible */
	}

	@Override
	public int getSubtitleTrackCount() {
		return 0;
	}

	@Override
	public int getSubtitleTrack() {
		return -1;
	}

	@Override
	public void setSubtitleTrack(int index) {
		/* not possible */
	}

	/* bridged events */

	@Override
	public void onCompletion() {
		if (mOnCompletionListener != null)
			mOnCompletionListener.onCompletion(this);
	}

	@Override
	public void onProgressUpdate(int time, int length) {
		if (mOnProgressUpdateListener != null)
			mOnProgressUpdateListener.onProgressUpdate(this, time, length);
	}

}
