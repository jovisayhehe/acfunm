package tv.avfun;


import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.widget.MediaController;

import java.util.ArrayList;

import tv.ac.fun.R;
import tv.avfun.entity.VideoSegment;
import tv.avfun.view.VideoView;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class PlayActivity extends Activity{
	private VideoView mVideoView;
	private TextView textView;
	private ProgressBar progress;
	private ArrayList<VideoSegment> parts;
	private String displayName;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.videoview);
		MobclickAgent.onEvent(this, "into_play");
		parts = (ArrayList<VideoSegment>) getIntent().getSerializableExtra("parts");
		displayName = getIntent().getStringExtra("displayName");
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setVideoParts(parts);
		mVideoView.setFileName(displayName);
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
//		mVideoView.setOnErrorListener(errListener);
//		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
		mVideoView.setMediaController(new MediaController(this));
	}
	
	private final class MOnCompletionListener implements OnCompletionListener{

		@Override
		public void onCompletion(MediaPlayer mPlayer) {
		    
//	        if(parts == null || ++index >= parts.size() || isError) {
	            finish();
//	            return;
//	        }
//			Toast.makeText(PlayActivity.this, "开始缓冲下一段...稍后", 1).show();
//			mVideoView.setVideoPath(parts.get(index).url);
		}
		
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
		super.onConfigurationChanged(newConfig);
	}
	public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
