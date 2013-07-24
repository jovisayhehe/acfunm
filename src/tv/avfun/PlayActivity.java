package tv.avfun;


import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import java.util.ArrayList;

import tv.ac.fun.R;
import tv.avfun.api.net.UserAgent;
import tv.avfun.entity.VideoSegment;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class PlayActivity extends Activity{
	private VideoView mVideoView;
	private TextView textView;
	private ProgressBar progress;
	private ArrayList<VideoSegment> parts;
	private String displayName;
	private int index =0;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.videoview);
		parts = (ArrayList<VideoSegment>) getIntent().getSerializableExtra("parts");
		displayName = getIntent().getStringExtra("displayName");
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setVideoPath(parts.get(index).url);
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
		mVideoView.setOnErrorListener(errListener);
		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
		mVideoView.setMediaController(new MediaController(this));
	}
	
	private OnErrorListener errListener = new OnErrorListener() {
        
        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            isError = true;
            return false;
        }
    };
    private boolean isError;
	private final class MOnCompletionListener implements OnCompletionListener{

		@Override
		public void onCompletion(MediaPlayer mPlayer) {
		    
	        if(parts == null || ++index >= parts.size() || isError) {
	            finish();
	            return;
	        }
			Toast.makeText(PlayActivity.this, "开始缓冲下一段...稍后", 1).show();
			mVideoView.setVideoPath(parts.get(index).url);
//			mVideoView.setOnCompletionListener(new MOnCompletionListener());
//			mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
//			mVideoView.setMediaController(new MediaController(PlayActivity.this));
//			mVideoView.setOnPreparedListener(new OnPreparedListener() {
//				
//				@Override
//				public void onPrepared(MediaPlayer arg0) {
//					
//					
//				}
//			});
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
