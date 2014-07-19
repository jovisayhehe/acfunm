/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.Vitamio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.android.DanmakuGlobalConfig;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;
import tv.ac.fun.R;
import tv.acfun.util.SystemBarConfig;
import tv.acfun.util.net.Connectivity;
import tv.acfun.util.net.NetWorkUtil;
import tv.acfun.video.api.DanmakuParser;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaController;
import tv.acfun.video.player.MediaController.MediaPlayerControl;
import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.MediaSegmentPlayer;
import tv.acfun.video.player.VideoView;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.WebResolver;
import tv.acfun.video.util.FileUtil;
import tv.acfun.video.util.download.DownloadEntry;
import tv.acfun.video.util.net.DanmakusRequest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.util.IOUtils;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Yrom 
 */
public class PlayerActivity extends ActionBarActivity implements OnClickListener, MediaPlayerControl, Callback {
    private static final String EXTRA_VIDEO = "video";
    private static final String TAG = "PlayerActivity";
    private static final int SEEK_COMPLETE = 10;
    private static final int SYNC = 11;
    private static final int HIDE_TEXT = 12;
    private static final int PREPARED = 13;
    private static final int RESUME = 14;
    private VideoView mVideoView;
    private View mBufferingIndicator;
    private TextView mProgressText;
    private TextView mBufferingMsg;
    private boolean mEnabledHW;
    private DanmakuSurfaceView mDMView;
    private boolean mEnabledDrawingCache;
    public static void start(Context context, VideoPart video) {
        Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO, video);
        context.startActivity(intent);
    }

    OnPreparedListener onPrepared = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mDMView.isPrepared()) {
                if (mp instanceof MediaSegmentPlayer)
                    mDMView.seekTo(((MediaSegmentPlayer) mp).getAbsolutePosition());
                else
                    mDMView.start();
            }
            mp.start();
            mBufferingIndicator.setVisibility(View.GONE);
        }
    };
    private Animation mTextAnimation;
    private BaseResolver mResolver;
    private VideoPart mVideo;
    OnResolvedListener OnResolved = new OnResolvedListener() {
        @Override
        public void onResolved(Resolver resolver) {
            mList = resolver.getMediaList();
            if (mList != null && mList.size()>0) {
                mProgressText.setText(mProgressText.getText() 
                        + getString(R.string.video_segments_parsing_success, resolver.getMediaList().size() )
                        +"\n" 
                        + getString(R.string.danmakus_buffering));
                addDanmakusRequest();
            } else {
                mProgressText.setText(mProgressText.getText() + getString(R.string.failed));
                mBufferingIndicator.setVisibility(View.GONE);
                // TODO: show retry
            }
        }
    };
    OnInfoListener onInfo = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                mp.pause();
                mDMView.pause();
                mHandler.removeMessages(SYNC);
                mBufferingMsg.setText("");
                mBufferingIndicator.setVisibility(View.VISIBLE);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                mp.start();
                mHandler.sendEmptyMessage(RESUME);
                mBufferingIndicator.setVisibility(View.GONE);
            }
            return false;
        }
    };
    private View mAds;
    OnCompletionListener onComplete = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            markComplete();
        }
    };
    private boolean mComplete;

    @TargetApi(19)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
            setTheme(R.style.AppTheme_NoActionBar_TranslucentDecor);
        }else{
            setTheme(R.style.AppTheme_NoActionBar_FullScreen);
        }
        super.onCreate(savedInstanceState);
        mHandler = new Handler(this);
        if (!Vitamio.isInitialized(this)){
            initLibs();
        }else{
            init();
        }
    }
    private void markComplete() {
        mComplete = true;
        mHandler.removeCallbacksAndMessages(null);
        mDMView.pause();
        mAds.setVisibility(View.VISIBLE);
    }
    private void init() {
        Parcelable extra = getIntent().getParcelableExtra(EXTRA_VIDEO);
        if (extra == null) {
            Toast.makeText(this, "出错了！EXTRA_VIDEO == null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mEnabledHW = AcApp.getBoolean(getString(R.string.key_hw_decode), false);
        mEnabledDrawingCache = AcApp.getBoolean(getString(R.string.key_dm_cache), true);
        mVideo = (VideoPart) extra;
        
        setContentView(R.layout.activity_player);
        initViews();
        initAnimation();
        if (mResolver != null && mResolver.getMediaList() != null && mResolver.getMediaList().size() > 0)  return;
        resolveVideos();
    }
    private void initLibs(){
        new AsyncTask<Object, Object, Boolean>() {
            protected ProgressDialog mPD;

            @Override
            protected void onPreExecute() {
                mPD = new ProgressDialog(PlayerActivity.this);
                mPD.setCancelable(false);
                mPD.setMessage(PlayerActivity.this.getString(R.string.vitamio_init_decoders));
                mPD.show();
            }

            @Override
            protected Boolean doInBackground(Object... params) {
                boolean init = false;
                if(Vitamio.getVitamioType() < 70){
                    String fileName = Environment.getExternalStorageDirectory()+"/codec.7z";
                    init = Vitamio.initialize(PlayerActivity.this,fileName);
                }else
                    init =  Vitamio.initialize(PlayerActivity.this, R.raw.libarm);
                return init;
            }

            @Override
            protected void onPostExecute(Boolean inited) {
                mPD.dismiss();
                if (inited) {
                    init();
                } else{
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(which == DialogInterface.BUTTON_POSITIVE){
                                String link = MobclickAgent.getConfigParams(getApplicationContext(), "codec_link");
                                if(TextUtils.isEmpty(link))
                                    link = "http://pan.baidu.com/s/1ENrlk";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(intent);
                                finish();
                            }else if(which ==DialogInterface.BUTTON_NEUTRAL){
                                String libUrl = "http://yrom.qiniudn.com/codec"+Vitamio.getVitamioType()+".7z";
                                try {
                                    URL url = new URL(libUrl);
                                    new DownloadTask().execute(url);
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                finish();
                            }
                            
                            
                        }
                    };
                    new AlertDialog.Builder(PlayerActivity.this)
                        .setTitle("你的设备需要下载解码器")
                        .setMessage(getString(R.string.cpu_not_support, Environment.getExternalStorageDirectory().getAbsolutePath()))
                        .setNegativeButton("取消", listener)
                        .setNeutralButton("黑洞", listener)
                        .setPositiveButton("网盘", listener)
                        .show();
                    
                }
            }

          }.execute();
    }
    
    class DownloadTask extends AsyncTask<URL, Boolean, Boolean>{
        private ProgressDialog mPD;
        @Override
        protected void onPreExecute() {
            mPD = new ProgressDialog(PlayerActivity.this);
            mPD.setCancelable(false);
            mPD.setMessage("开始下载....");
            mPD.show();
        }
        @Override
        protected void onProgressUpdate(Boolean... values) {
            if(values[0].booleanValue()){
                mPD.setMessage("下载完成，开始解压...."); 
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            mPD.dismiss();
            if(result.booleanValue()){
                Toast.makeText(getApplicationContext(), "解码器安装完毕", Toast.LENGTH_SHORT).show();
                init();
            }else{
                Toast.makeText(getApplicationContext(), "解码器安装失败！", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        @Override
        protected Boolean doInBackground(URL... params) {
            File file = new File(Environment.getExternalStorageDirectory()+"/codec.7z");
            URL parsedUrl = params[0];
            FileOutputStream out = null;
            InputStream in = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setUseCaches(false);
                connection.addRequestProperty("User-Agent", Connectivity.UA);
                out = new FileOutputStream(file);
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    in = connection.getInputStream();
                    FileUtil.copyStream(in, out);
                }
                boolean inited = false;
                if(file.length() == connection.getContentLength()){
                    publishProgress(Boolean.TRUE);
                    inited = Vitamio.initialize(PlayerActivity.this, file.getAbsolutePath());
                }
                connection.disconnect();
                return Boolean.valueOf(inited);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                IOUtils.close(in);
                IOUtils.close(out);
            }
            return Boolean.FALSE;
        }
        
    }
    private void initViews() {
        ViewStub stub = (ViewStub) findViewById(R.id.view_stub);
        stub.setLayoutResource(R.layout.video_view);
        mVideoView = (VideoView) stub.inflate();
        mVideoView.setPreferHWDecoder(mEnabledHW);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", BaseResolver.UA_DEFAULT);
        mVideoView.setVideoHeaders(headers);
        mBufferingIndicator = findViewById(R.id.buffering_indicator);
        mBufferingMsg = (TextView)mBufferingIndicator.findViewById(R.id.buffering_msg);
        mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
        mProgressText = (TextView) findViewById(R.id.progress_text);
        mVideoView.setOnPreparedListener(onPrepared);
        mVideoView.setOnSeekCompleteListener(onSeekComplete);
        mVideoView.setOnInfoListener(onInfo);
        mVideoView.setOnCompletionListener(onComplete);
        mDMView = (DanmakuSurfaceView) findViewById(R.id.danmakus);
        mDMView.enableDanmakuDrawingCache(mEnabledDrawingCache);
        // TODO : danmakus config
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        DanmakuGlobalConfig.DEFAULT
            .setMaximumVisibleSizeInScreen(100)
            .setScaleTextSize(displayMetrics.scaledDensity)
            /*.setDanmakuStyle(DanmakuGlobalConfig.DANMAKU_STYLE_STROKEN, 1.1f)*/;
        mDMView.setCallback(mDMCallback);
        View holder = findViewById(R.id.holder);
        holder.setOnClickListener(this);
        mMediaController = (MediaController)new MediaController(this);
        mMediaController.setAnchorView(holder);
        mMediaController.setMediaPlayer(this);
        mMediaController.setInstantSeeking(false);
        boolean transStatusBar = false;
        boolean transNavBar = false;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            transNavBar = true;
            transStatusBar = true;
        }
        SystemBarConfig config = new SystemBarConfig(this,transStatusBar,transNavBar);
        mMediaController.setSystemBarConfig(config);
        String name = mVideo.name == null?"":mVideo.name;
        mMediaController.setFileName(name);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setOnBufferingUpdateListener(onBuffering);
        boolean chroma565 = AcApp.getBoolean(getString(R.string.key_chroma_565), false);
        if(chroma565) mVideoView.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
        
        // ads
        mAds = findViewById(R.id.ads);
        findViewById(R.id.close).setOnClickListener(this);
        
    }
    MediaPlayer.OnBufferingUpdateListener onBuffering = new MediaPlayer.OnBufferingUpdateListener(){

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferingMsg.setText(percent+"%");
        }
        
    };
    
    MediaPlayer.OnSeekCompleteListener onSeekComplete = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            mHandler.sendEmptyMessageDelayed(SEEK_COMPLETE, 100);
        }
    };
    protected DanmakuTimer mTimer;
    DrawHandler.Callback mDMCallback = new DrawHandler.Callback() {
        @Override
        public void prepared() {
            mHandler.sendEmptyMessage(PREPARED);
        }

        @Override
        public void updateTimer(DanmakuTimer timer) {
            mTimer = timer;
        }

    };
    private MediaController mMediaController;
    private Handler mHandler;
    private MediaList mList;
    private DanmakuParser mParser;
    Response.Listener<String> dmListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            mProgressText.setText(mProgressText.getText() + "\n"+ getString(R.string.danmakus_downloaded));
            try {
                mParser = new DanmakuParser(getApplicationContext(), response);
                mDMView.prepare(mParser);
            } catch (Exception e) {
                Log.e(TAG, "解析失败", e);
                mProgressText.setText(mProgressText.getText() +"\n" +getString(R.string.parsing_failed));
            }
            startPlay();
        }

        
    };
    Response.ErrorListener err = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mProgressText.setText(mProgressText.getText() +"\n"+getString(R.string.danmakus_download_failed));
            hideTextDelayed();
            startPlay();
        }
    };
    private void addDanmakusRequest() {
        DownloadEntry entry = AcApp.getDownloadManager().getEntryByVid(mVideo.sourceId);
        if(entry != null && !NetWorkUtil.isNetworkAvailable(this)){
            if(loadDownloadedDanmakus(entry)) return;
        }
        // TODO: acfun lock danmakus
        String save =entry ==null?null: entry.destination;
        StringRequest request = new DanmakusRequest(getApplicationContext(), String.valueOf(mVideo.videoId),save,dmListener , err);
        Connectivity.addRequest(request);
    }

    private boolean loadDownloadedDanmakus(DownloadEntry entry) {
        
        File dmFile = new File(entry.destination, entry.part.videoId + ".json");
        if (!dmFile.exists()) return false;
        try {
            mParser = new DanmakuParser(getApplicationContext(),dmFile);
            mDMView.prepare(mParser);
        } catch (Exception e) {
            Log.e(TAG, "解析失败", e);
            mProgressText.setText(mProgressText.getText() + "\n" + getString(R.string.parsing_failed));
        }
        startPlay();
        return true;
    }

    private void hideTextDelayed() {
        mHandler.sendEmptyMessageDelayed(HIDE_TEXT, 2500);
    }

    private void startPlay() {
        mProgressText.setText(mProgressText.getText() 
                + "\n"
                +getString(R.string.video_buffering)
                +"\n"
                +getString(R.string.player_type,mEnabledHW?"HW":"SW")
                );

        mVideoView.setMediaList(mList);
        mVideoView.start();
    }

    private void initAnimation() {
        mTextAnimation = AnimationUtils.loadAnimation(mProgressText.getContext(), R.anim.fade_out);
        mTextAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mProgressText.setVisibility(View.GONE);
            }
        });
    }

    private void resolveVideos() {
        if(mVideo.isDownloaded){
            mList = MediaList.createFromeSegments(mVideo.segments);
            addDanmakusRequest();
            return;
        }
        mResolver = new WebResolver(mVideo.sourceId);
        int resolution = Integer.parseInt(AcApp.getString(getString(R.string.key_resolution_mode), "1"));
        mResolver.setResolution(resolution);
        mResolver.setOnResolvedListener(OnResolved);
        mResolver.resolveAsync(getApplicationContext());
        mProgressText.setText(mProgressText.getText() + "\n"+getString(R.string.video_segments_paring));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.close){
            mAds.setVisibility(View.GONE);
        }else if (mMediaController != null) 
            toggleMediaControlsVisiblity();
    }

    @Override
    public void start() {
        mAds.setVisibility(View.GONE);
        if(mComplete){
            restart();
        }else{
            mVideoView.start();
            if(isDMShow()){
                mDMView.resume();
                mHandler.sendEmptyMessageDelayed(SYNC,200);
            }
        }
    }

    private void restart() {
        mComplete = false;
        mVideoView.setMediaList(mList);
    }
    @Override
    public void pause() {
        mVideoView.pause();
        mHandler.removeMessages(SYNC);
        mDMView.pause();
        mAds.setVisibility(View.VISIBLE);
        
    }

    @Override
    public long getDuration() {
        return mVideoView.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    @Override
    public void seekTo(long pos) {
        mVideoView.seekTo(pos);
        mHandler.removeMessages(SYNC);
        mDMView.pause();
    }

    @Override
    public boolean isPlaying() {
        return mVideoView.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mVideoView.getBufferPercentage();
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void onStop() {
        super.onStop();
        if(mVideoView != null){
            mVideoView.pause();
        }
        if(mDMView != null)
            mDMView.pause();
        mHandler.removeCallbacksAndMessages(null);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null){
            mHandler.removeMessages(SYNC);
        }
        if(mVideoView != null){
            mVideoView.stopPlayback();
        }
        if(mDMView != null)
            mDMView.release();
    }
    long mLastTime;
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case PREPARED:
            mProgressText.setText(mProgressText.getText()
                    +"\n"
                    + getString(R.string.danmakus_loaded,mParser.size())
                    +"\n"
                    +getString(R.string.enable_danmaku_drawing_cache,mEnabledDrawingCache));
            startDM();
            hideTextDelayed();
            break;
        case RESUME:
            if(mDMView.isPrepared()){
                mDMView.resume();
                mHandler.sendEmptyMessageDelayed(SYNC, 500);
            }
            break;
        case SEEK_COMPLETE:
            if(isPlaying()){
                mDMView.seekTo(getCurrentPosition());
                mHandler.sendEmptyMessageDelayed(SYNC, 1500);
            }else
                mHandler.sendEmptyMessageDelayed(SEEK_COMPLETE, 100);
            break;
        case SYNC:
            if (isPlaying()){
                if(mTimer != null){
                    long cur = getCurrentPosition();
                    if(mLastTime == cur) {
                        mDMView.pause();
                    }
                    long d = cur -mTimer.currMillisecond ;
                    if(Math.abs(d) > 2000){
                        mDMView.seekTo(cur);
                    }else if(d < -500 && d >= -2000){
                         mDMView.pause();
                         mHandler.sendEmptyMessageDelayed(RESUME, -d);
                    }
                    mLastTime = getCurrentPosition();
                }
            }else{
                mDMView.pause();
            }
            mHandler.sendEmptyMessageDelayed(SYNC, 1500);
            break;
        case HIDE_TEXT:
            mProgressText.startAnimation(mTextAnimation);
            break;
        default:
            break;
        }
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean isDMShow() {
        return mDMView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void closeDM() {
        mDMView.pause();
        mDMView.setVisibility(View.GONE);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void startDM() {
        mDMView.setVisibility(View.VISIBLE);
        if(isPlaying()){
            if(mDMView.isPrepared()){
                mDMView.seekTo(getCurrentPosition());
            }else
                mDMView.start(getCurrentPosition());
            mHandler.sendEmptyMessageDelayed(SYNC, 200);
        }
    }
}
