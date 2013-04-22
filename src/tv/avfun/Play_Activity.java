package tv.avfun;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import io.vov.utils.StringUtils;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
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
import android.widget.Toast;

public class Play_Activity extends Activity{
	private VideoView mVideoView;
	private TextView textView;
	private ProgressBar progress;
	private String path;
	private ArrayList<String> paths;
	private int index =0;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.videoview);
//		path = getIntent().getStringExtra("path");
		paths = getIntent().getStringArrayListExtra("paths");
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setVideoPath(paths.get(index));
		textView = (TextView) findViewById(R.id.video_proess_text);
		progress = (ProgressBar) findViewById(R.id.video_time_progress);
		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
				
				textView.setText(arg1+"");
				if(arg0.isBuffering()){
					textView.setVisibility(View.GONE);
					progress.setVisibility(View.GONE);
				}
			}
		});
		
		mVideoView.setOnCompletionListener(new MOnCompletionListener());
		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
		mVideoView.setMediaController(new MediaController(this));
	}
	

	private final class MOnCompletionListener implements OnCompletionListener{

		@Override
		public void onCompletion(MediaPlayer mPlayer) {
	        if(++index >= paths.size()) {
	            finish();
	            return;
	        }
			Toast.makeText(Play_Activity.this, "开始缓冲下一段...稍后", 1).show();
			mPlayer.getDuration();

			mVideoView.setVideoPath(paths.get(index));
			mVideoView.setOnCompletionListener(new MOnCompletionListener());
			mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
			mVideoView.setMediaController(new MediaController(Play_Activity.this));
			mVideoView.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer arg0) {
					
					
				}
			});
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
		super.onConfigurationChanged(newConfig);
	}
}
