package tv.avfun;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Play_Activity extends Activity{
	private VideoView mVideoView;
	private TextView textView;
	private ProgressBar progress;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.videoview);
		String path = getIntent().getStringExtra("path");
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setVideoPath(path);
		textView = (TextView) findViewById(R.id.video_proess_text);
		progress = (ProgressBar) findViewById(R.id.video_time_progress);
		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
				// TODO Auto-generated method stub
				textView.setText(arg1+"");
				if(arg0.isBuffering()){
					textView.setVisibility(View.GONE);
					progress.setVisibility(View.GONE);
				}
			}
		});
		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
		mVideoView.setMediaController(new MediaController(this));
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
		super.onConfigurationChanged(newConfig);
	}
}
