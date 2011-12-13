package org.stagex.danmaku.site;

import java.util.ArrayList;

import android.view.SurfaceHolder;

public abstract class MessagePlayer {

	public interface OnProgressUpdateListener {
		public void onProgressUpdate(int time, int length);
	}

	public interface OnCompletionListener {
		public void onCompletion();
	}

	public abstract int getCurrentPosition();

	public abstract int getDuration();

	public abstract int getVideoHeight();

	public abstract int getVideoWidth();

	public abstract boolean isPlaying();

	public abstract void pause();

	public abstract void release();

	public abstract void reset();

	public abstract void seekTo(int msec);

	public abstract void setDataSource(ArrayList<Comment> data);

	public abstract void setDisplay(SurfaceHolder display);

	public abstract void setOnCompletionListener(OnCompletionListener listener);

	public abstract void setOnProgressUpdateListener(
			OnProgressUpdateListener listener);

	public abstract void start();

	public abstract void stop();

}
