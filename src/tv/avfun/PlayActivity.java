
package tv.avfun;

import master.flame.danmaku.controller.AcDanmakuPlayer;
import master.flame.danmaku.controller.AcDanmakuPlayer.OnPreparedListener;
import master.flame.danmaku.ui.widget.DanmakuView;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import tv.ac.fun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.avfun.view.MediaController;
import tv.avfun.view.VideoView;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class PlayActivity extends Activity {

    private VideoView mVideoView;
    private TextView textView;
    private ProgressBar progress;
    private VideoPart parts;
    private DanmakuView danmaku;
    private MediaController mController;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
            return;
        boolean showDanmaku = getIntent().getBooleanExtra("danmaku_mode", false);
        if (showDanmaku) {
            MobclickAgent.onEvent(this, "view_danmaku");
            setContentView(R.layout.activity_play);
        } else
            setContentView(R.layout.videoview);
        MobclickAgent.onEventBegin(this, "into_play");
        Object obj = getIntent().getExtras().getSerializable("item");
        if (obj == null)
            throw new IllegalArgumentException("what does the video item you want to play?");
        parts = (VideoPart) obj;
        mVideoView = (VideoView) findViewById(R.id.surface_view);

        textView = (TextView) findViewById(R.id.video_proess_text);
        progress = (ProgressBar) findViewById(R.id.video_time_progress);

        danmaku = (DanmakuView) findViewById(R.id.danmaku);

        mVideoView.setOnCompletionListener(new MOnCompletionListener());
        mController = (MediaController) findViewById(R.id.media_controller);
        mController.setVisibility(View.GONE);
        // TODO OnMenuClickListener
        // mController.setOnMenuClickListener(new
        // MediaController.OnMenuClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // // TODO Auto-generated method stub
        // // show settings
        // }
        // });
        mVideoView.setMediaController(mController);
        // mVideoView.setOnTouchListener(new View.OnTouchListener() {
        //
        // @Override
        // public boolean onTouch(View v, MotionEvent event) {
        // return mController.mGestureDetector.onTouchEvent(event);
        // }
        // });
        mVideoView.setOnInfoListener(new OnInfoListener() {

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mp.pause();
                    textView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    if (danmaku != null) {
                        danmaku.pause();
                    }
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mp.start();
                    progress.setVisibility(View.GONE);
                    if (danmaku != null) {
                        danmaku.start();
                    }
                } else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
                    AcApp.showToast("拖不动...");
                }
                return true;
            }
        });
        mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
                textView.setText(arg1 + "");
                if (arg0.isBuffering() || arg1 >= 90) {
                    textView.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }
            }
        });
        if (danmaku != null) {
            textView.setText("弹幕加载中....");
            String url = "http://comment.acfun.tv/" + parts.danmakuId + ".json";
            danmaku.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(AcDanmakuPlayer player) {
                    textView.setVisibility(View.VISIBLE);
                    int size = player.danmakusSize();
                    textView.setText(textView.getText() + "\n弹幕加载完毕...[共" + size + "条]\n视频加载中...");
                    loadVideo();
                }
            });
            danmaku.setOnErrorListener(new AcDanmakuPlayer.OnErrorListener() {

                @Override
                public void onError(AcDanmakuPlayer player, int what, int extra) {
                    if (what == AcDanmakuPlayer.ERROR_DATA_SOURCE) {
                        textView.setText(textView.getText() + "\nΣ( ﾟдﾟ)暂无弹幕！\n视频加载中...");
                    } else if( what == 1){
                        textView.setText(textView.getText() + "\n弹幕加载失败！\n视频加载中...");
                    }
                    player.release();
                    danmaku.setVisibility(View.GONE);
                    loadVideo();

                }
            });
            danmaku.setDanmakuPath(url);
        } else
            loadVideo();

    }

    private void loadVideo() {
        new LoadVideoSegmentTask().execute(parts);
    }

    private class LoadVideoSegmentTask extends AsyncTask<VideoPart, Void, Boolean> {

        private VideoPart parts;

        @Override
        protected Boolean doInBackground(VideoPart... params) {
            parts = params[0];
            ApiParser.parseVideoParts(parts, AcApp.getParseMode());
            return parts.segments != null && parts.segments.size() > 0;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mVideoView.setVideoSegments(parts.segments, getExternalCacheDir().getAbsolutePath());
                mVideoView.setVideoName(parts.subtitle);
                mController.setVisibility(View.VISIBLE);
                mController.attachDanmakuView(danmaku);
            } else {
                AcApp.showToast("解析视频地址失败");
                finish();
            }
        }
    }

    private final class MOnCompletionListener implements OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mPlayer) {
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
        // if (mVideoView != null){
        // mVideoView.release(true);
        // mVideoView = null;
        // }
//        try {
//            if (danmaku != null) {
//                danmaku.release();
//                danmaku = null;
//            }
//        } catch (Exception e) {}
        super.onDestroy();
        MobclickAgent.onEventEnd(this, "into_play");
    }

}
