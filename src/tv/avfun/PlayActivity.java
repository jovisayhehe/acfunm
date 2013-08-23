package tv.avfun;


import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import tv.ac.fun.R;
import tv.avfun.entity.VideoPart;
import tv.avfun.view.DanmakuView;
import tv.avfun.view.DanmakuView.OnPreparedListenr;
import tv.danmaku.media.AbsMediaPlayer;
import tv.danmaku.media.AbsMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.media.AbsMediaPlayer.OnCompletionListener;
import tv.danmaku.media.AbsMediaPlayer.OnInfoListener;
import tv.danmaku.media.list.VideoView;
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
	private VideoPart parts;
    private DanmakuView danmaku;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
			return;
		boolean showDanmaku = getIntent().getBooleanExtra("danmaku_mode", false);
		if(showDanmaku){
		    setContentView(R.layout.activity_play);
		}else
		    setContentView(R.layout.videoview);
		MobclickAgent.onEventBegin(this, "into_play");
		Object obj = getIntent().getExtras().getSerializable("item");
        if(obj == null) throw new IllegalArgumentException("what does the video item you want to play?");
        parts = (VideoPart) obj;
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		
		textView = (TextView) findViewById(R.id.video_proess_text);
		progress = (ProgressBar) findViewById(R.id.video_time_progress);
		
		danmaku = (DanmakuView) findViewById(R.id.danmaku);
		
		mVideoView.setOnCompletionListener(new MOnCompletionListener());
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.setOnInfoListener(new OnInfoListener() {
            
            @Override
            public boolean onInfo(AbsMediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                    mp.pause();
                    textView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    if(danmaku != null){
                        danmaku.pause();
                    }
                }
                else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
                    mp.start();
                    progress.setVisibility(View.GONE);
                    if(danmaku != null){
                        danmaku.resume();
                    }
                } //else if(what == MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED) {
                    // TODO 
                //}
                return true;
            }
        });
		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
            
            @Override
            public void onBufferingUpdate(AbsMediaPlayer arg0, int arg1) {
                textView.setText(arg1+"");
                if(arg0.isBuffering() || arg1 >= 90){
                    textView.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }
            }
        });
		if(danmaku != null){
		    textView.setText("弹幕加载中....");
            String url = "http://comment.acfun.tv/"+parts.danmakuId+".json";
            danmaku.setDataSource(url);
            danmaku.prepareAsync();
            danmaku.attachVideoView(mVideoView);
            danmaku.setOnPreparedListenr(new OnPreparedListenr() {
                
                @Override
                public void onPrepared(DanmakuView view) {
                    textView.setVisibility(View.VISIBLE);
                    int size = view.danmakusSize();
                    textView.setText(textView.getText()+"\n弹幕加载完毕...[共"+size+"条]\n加载视频中...");
                    mVideoView.setVideoPart(parts);
                }
            });
            danmaku.setOnErrorListener(new DanmakuView.OnErrorListener() {
                
                @Override
                public void onError(DanmakuView view, int what, int extra) {
                    if(what == 1 && extra == 1){
                        textView.setText(textView.getText()+"\n暂无弹幕！\n加载视频中...");
                        view.release();
                        view.setVisibility(View.GONE);
                        mVideoView.setVideoPart(parts);
                    }
                    
                }
            });
        }else
            mVideoView.setVideoPart(parts);
	}
	
	private final class MOnCompletionListener implements  OnCompletionListener{

		@Override
		public void onCompletion(AbsMediaPlayer mPlayer) {
		    finish();
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
    @Override
    protected void onDestroy() {
        if (mVideoView != null){
            mVideoView.release(true);
            mVideoView = null;
        }
        if(danmaku!=null){
            danmaku.release();
            danmaku = null;
        }
        super.onDestroy();
        MobclickAgent.onEventEnd(this, "into_play");
    }
    
}
