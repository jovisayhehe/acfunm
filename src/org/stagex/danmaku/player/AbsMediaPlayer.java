package org.stagex.danmaku.player;

import android.util.Log;
import android.view.SurfaceHolder;

public abstract class AbsMediaPlayer {

	private static final String LOGTAG = "DANMAKU-AbsMediaPlayer";

	public interface OnBufferingUpdateListener {
		public void onBufferingUpdate(AbsMediaPlayer mp, int percent);
	}

	public interface OnCompletionListener {
		public void onCompletion(AbsMediaPlayer mp);
	}

	public interface OnErrorListener {
		public boolean onError(AbsMediaPlayer mp, int what, int extra);
	}

	public interface OnInfoListener {
		public boolean onInfo(AbsMediaPlayer mp, int what, int extra);
	}

	public interface OnPreparedListener {
		public void onPrepared(AbsMediaPlayer mp);
	}

	public interface OnProgressUpdateListener {
		public void onProgressUpdate(AbsMediaPlayer mp, int time, int length);
	}

	public interface OnVideoSizeChangedListener {
		public void onVideoSizeChangedListener(AbsMediaPlayer mp, int width,
				int height);
	}

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract int getVideoHeight();

	public abstract int getVideoWidth();

	public abstract boolean isLooping();

	public abstract boolean isPlaying();

	public abstract void pause();

	public abstract void prepare();

	public abstract void prepareAsync();

	public abstract void release();

	public abstract void reset();

	public abstract void seekTo(int msec);

	public abstract void setDataSource(String path);

	public abstract void setDisplay(SurfaceHolder holder);

	public abstract void setLooping(boolean looping);

	public abstract void setOnBufferingUpdateListener(
			AbsMediaPlayer.OnBufferingUpdateListener listener);

	public abstract void setOnCompletionListener(
			AbsMediaPlayer.OnCompletionListener listener);

	public abstract void setOnErrorListener(
			AbsMediaPlayer.OnErrorListener listener);

	public abstract void setOnInfoListener(
			AbsMediaPlayer.OnInfoListener listener);

	public abstract void setOnPreparedListener(
			AbsMediaPlayer.OnPreparedListener listener);

	public abstract void setOnProgressUpdateListener(
			AbsMediaPlayer.OnProgressUpdateListener listener);

	public abstract void setOnVideoSizeChangedListener(
			AbsMediaPlayer.OnVideoSizeChangedListener listener);

	public abstract void start();

	public abstract void stop();

	/* */

	public abstract int getAudioTrackCount();

	public abstract int getAudioTrack();

	public abstract void setAudioTrack(int index);

	public abstract int getSubtitleTrackCount();

	public abstract int getSubtitleTrack();

	public abstract void setSubtitleTrack(int index);

	protected static AbsMediaPlayer getDefMediaPlayer() {
		Log.d(LOGTAG, "using DefMediaPlayer");
		return DefMediaPlayer.getInstance();
	}

	protected static AbsMediaPlayer getVlcMediaPlayer() {
		/* this is not needed to be singleton */
		Log.d(LOGTAG, "using VlcMediaPlayer");
		return VlcMediaPlayer.getInstance();
	}

	public static AbsMediaPlayer getMediaPlayer(boolean useDefault) {
		return useDefault ? getDefMediaPlayer() : getVlcMediaPlayer();
	}
	public static AbsMediaPlayer getMessagePlayer() {
		/* this is not needed to be singleton */
		return MsgMediaPlayer.getInstance();
	}
}
