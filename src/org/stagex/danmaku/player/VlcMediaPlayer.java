package org.stagex.danmaku.player;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

public class VlcMediaPlayer extends AbsMediaPlayer {

	static {
		System.loadLibrary("vlccore");
	}

	private static final String LOGTAG = "DANMAKU-VlcMediaPlayer";

	protected AbsMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = null;
	protected AbsMediaPlayer.OnCompletionListener mOnCompletionListener = null;
	protected AbsMediaPlayer.OnErrorListener mOnErrorListener = null;
	protected AbsMediaPlayer.OnInfoListener mOnInfoListener = null;
	protected AbsMediaPlayer.OnPreparedListener mOnPreparedListener = null;
	protected AbsMediaPlayer.OnProgressUpdateListener mOnProgressUpdateListener = null;
	/* double check this */
	protected AbsMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = null;

	/* */
	private int mTime = -1;

	/*  */
	protected native void nativeAttachSurface(Surface s);

	protected native void nativeDetachSurface();

	/* */
	protected native void nativeCreate();

	protected native void nativeRelease();

	protected native int nativeGetCurrentPosition();

	protected native int nativeGetDuration();

	protected native int nativeGetVideoHeight();

	protected native int nativeGetVideoWidth();

	protected native boolean nativeIsLooping();

	protected native boolean nativeIsPlaying();

	protected native void nativePause();

	protected native void nativePrepare();

	protected native void nativePrepareAsync();

	protected native void nativeSeekTo(int msec);

	protected native void nativeSetDataSource(String path);

	protected native void nativeSetLooping(boolean looping);

	protected native void nativeStart();

	protected native void nativeStop();

	@SuppressWarnings("unused")
	private class VlcEvent {
		/* see native side */
		public final static int MediaMetaChanged = 0;
		public final static int MediaSubItemAdded = 1;
		public final static int MediaDurationChanged = 2;
		public final static int MediaParsedChanged = 3;
		public final static int MediaFreed = 4;
		public final static int MediaStateChanged = 5;
		public final static int MediaPlayerMediaChanged = 256;
		public final static int MediaPlayerNothingSpecial = 257;
		public final static int MediaPlayerOpening = 258;
		public final static int MediaPlayerBuffering = 259;
		public final static int MediaPlayerPlaying = 260;
		public final static int MediaPlayerPaused = 261;
		public final static int MediaPlayerStopped = 262;
		public final static int MediaPlayerForward = 263;
		public final static int MediaPlayerBackward = 264;
		public final static int MediaPlayerEndReached = 265;
		public final static int MediaPlayerEncounteredError = 266;
		public final static int MediaPlayerTimeChanged = 267;
		public final static int MediaPlayerPositionChanged = 268;
		public final static int MediaPlayerSeekableChanged = 269;
		public final static int MediaPlayerPausableChanged = 270;
		public final static int MediaPlayerTitleChanged = 271;
		public final static int MediaPlayerSnapshotTaken = 272;
		public final static int MediaPlayerLengthChanged = 273;
		/* the variables we received */
		public int eventType = -1;
		public boolean booleanValue = false;
		public int intValue = -1;
		public long longValue = -1;
		public float floatValue = -1.0f;
		public String stringValue = null;
	}

	/* called by native side */
	private void onVlcEvent(VlcEvent ev) {
		Log.d(LOGTAG, String.format("received vlc event %d", ev.eventType));
		switch (ev.eventType) {
		case VlcEvent.MediaParsedChanged: {
			if (!ev.booleanValue) {
				if (mOnErrorListener != null) {
					mOnErrorListener.onError(this,
							MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
				}
			} else {
				if (mOnPreparedListener != null) {
					mOnPreparedListener.onPrepared(this);
				}
				if (mOnVideoSizeChangedListener != null) {
					int width = getVideoWidth();
					int height = getVideoHeight();
					mOnVideoSizeChangedListener.onVideoSizeChangedListener(
							this, width, height);
				}
			}
			break;
		}
		case VlcEvent.MediaPlayerBuffering: {
			if (mOnBufferingUpdateListener != null) {
				int percent = (int) (ev.floatValue);
				mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
			}
			break;
		}
		case VlcEvent.MediaPlayerEndReached: {
			if (mOnCompletionListener != null) {
				mOnCompletionListener.onCompletion(this);
			}
			break;
		}
		case VlcEvent.MediaPlayerEncounteredError: {
			if (mOnErrorListener != null) {
				mOnErrorListener.onError(this, MediaPlayer.MEDIA_ERROR_UNKNOWN,
						0);
			}
			break;
		}
		case VlcEvent.MediaPlayerTimeChanged: {
			if (mOnProgressUpdateListener != null) {
				mOnProgressUpdateListener.onProgressUpdate(this,
						(int) ev.longValue, -1);
			}
			if (mTime < 0) {
				if (mOnVideoSizeChangedListener != null) {
					int width = getVideoWidth();
					int height = getVideoHeight();
					mOnVideoSizeChangedListener.onVideoSizeChangedListener(
							this, width, height);
				}
			}
			mTime = (int) ev.longValue;
			break;
		}
		case VlcEvent.MediaPlayerSeekableChanged: {
			if (!ev.booleanValue) {
				if (mOnInfoListener != null) {
					mOnInfoListener.onInfo(this,
							MediaPlayer.MEDIA_INFO_NOT_SEEKABLE, 0);
				}
			}
			break;
		}
		case VlcEvent.MediaPlayerLengthChanged: {
			if (mOnProgressUpdateListener != null) {
				mOnProgressUpdateListener.onProgressUpdate(
						(AbsMediaPlayer) this, -1, (int) ev.longValue);
			}
			break;
		}
		}
	}

	public static VlcMediaPlayer getInstance() {
		return new VlcMediaPlayer();
	}

	protected VlcMediaPlayer() {
		nativeCreate();
	}

	@Override
	public int getCurrentPosition() {
		return nativeGetCurrentPosition();
	}

	@Override
	public int getDuration() {
		return nativeGetDuration();
	}

	@Override
	public int getVideoHeight() {
		return nativeGetVideoHeight();
	}

	@Override
	public int getVideoWidth() {
		return nativeGetVideoWidth();
	}

	@Override
	public boolean isLooping() {
		/* not implemented yet */
		return nativeIsLooping();
	}

	@Override
	public boolean isPlaying() {
		return nativeIsPlaying();
	}

	@Override
	public void pause() {
		nativePause();
	}

	@Override
	public void prepare() {
		nativePrepare();
	}

	@Override
	public void prepareAsync() {
		nativePrepareAsync();
	}

	@Override
	public void release() {
		nativeRelease();
	}

	@Override
	public void reset() {
		/* not implemented yet */
	}

	@Override
	public void seekTo(int msec) {
		nativeSeekTo(msec);
	}

	@Override
	public void setDataSource(String path) {
		nativeSetDataSource(path);
	}

	@Override
	public void setDisplay(SurfaceHolder holder) {
		if (holder != null) {
			holder.setFormat(PixelFormat.RGB_565);
			nativeAttachSurface(holder.getSurface());
		} else
			nativeDetachSurface();
	}

	@Override
	public void setLooping(boolean looping) {
		/* not implemented yet */
		nativeSetLooping(looping);
	}

	@Override
	public void setOnBufferingUpdateListener(
			AbsMediaPlayer.OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListener = listener;
	}

	@Override
	public void setOnCompletionListener(
			AbsMediaPlayer.OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	@Override
	public void setOnErrorListener(AbsMediaPlayer.OnErrorListener listener) {
		mOnErrorListener = listener;
	}

	@Override
	public void setOnInfoListener(AbsMediaPlayer.OnInfoListener listener) {
		mOnInfoListener = listener;
	}

	@Override
	public void setOnPreparedListener(AbsMediaPlayer.OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	@Override
	public void setOnProgressUpdateListener(
			AbsMediaPlayer.OnProgressUpdateListener listener) {
		mOnProgressUpdateListener = listener;
	}

	@Override
	public void setOnVideoSizeChangedListener(
			OnVideoSizeChangedListener listener) {
		mOnVideoSizeChangedListener = listener;
	}

	@Override
	public void start() {
		nativeStart();
	}

	@Override
	public void stop() {
		nativeStop();
	}

	@Override
	public int getAudioTrackCount() {
		/* not implemented yet */
		return 0;
	}

	@Override
	public int getAudioTrack() {
		/* not implemented yet */
		return 0;
	}

	@Override
	public void setAudioTrack(int index) {
		/* not implemented yet */
	}

	@Override
	public int getSubtitleTrackCount() {
		/* not implemented yet */
		return 0;
	}

	@Override
	public int getSubtitleTrack() {
		/* not implemented yet */
		return 0;
	}

	@Override
	public void setSubtitleTrack(int index) {
		/* not implemented yet */
	}

}
