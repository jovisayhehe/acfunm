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
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.Vitamio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.controller.DMSiteType;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.IOUtils;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;
import tv.ac.fun.R;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaController;
import tv.acfun.video.player.MediaController.MediaPlayerControl;
import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.MediaSegmentPlayer;
import tv.acfun.video.player.VideoView;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.ResolverType;
import tv.acfun.video.util.download.DownloadEntry;
import tv.acfun.video.util.net.Connectivity;
import tv.acfun.video.util.net.DanmakusRequest;
import tv.acfun.video.util.net.NetWorkUtil;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.umeng.analytics.MobclickAgent;

/**
 * @author Yrom TODO :
 */
public class PlayerActivity extends ActionBarActivity implements OnClickListener, MediaPlayerControl, Callback {
    private static final String EXTRA_VIDEO = "video";
    private static final String TAG = "PlayerActivity";
    private static final int SEEK_COMPLETE = 10;
    private static final int SYNC = 11;
    private static final int HIDE_TEXT = 12;
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
                    mDMView.start(((MediaSegmentPlayer) mp).getAbsolutePosition());
                else
                    mDMView.start();
            }
            mp.start();
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
                mDMView.resume();
                mHandler.sendEmptyMessageDelayed(SYNC, 500);
                mBufferingIndicator.setVisibility(View.GONE);
            }
            return false;
        }
    };

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
                } else {
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(which == DialogInterface.BUTTON_POSITIVE){
                                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://pan.baidu.com/s/1c03RAUG"));
                                startActivity(intent);
                            }
                            finish();
                            
                        }
                    };
                    new AlertDialog.Builder(PlayerActivity.this)
                        .setTitle("你的设备需要下载解码器")
                        .setMessage(R.string.cpu_not_support)
                        .setNegativeButton("取消", listener)
                        .setPositiveButton("好", listener)
                        .show();
                    
                }
            }

          }.execute();
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
        mDMView = (DanmakuSurfaceView) findViewById(R.id.danmakus);
        mDMView.enableDanmakuDrawingCache(mEnabledDrawingCache);
        mDMView.setCallback(mDMCallback);
        View holder = findViewById(R.id.holder);
        holder.setOnClickListener(this);
        mMediaController = (MediaController)new MediaController(this);
        mMediaController.setAnchorView(holder);
        mMediaController.setMediaPlayer(this);
        mMediaController.setInstantSeeking(false);
        String name = mVideo.name == null?"":mVideo.name;
        mMediaController.setFileName(name);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setOnBufferingUpdateListener(onBuffering);
        boolean chroma565 = AcApp.getBoolean(getString(R.string.key_chroma_565), false);
        if(chroma565) mVideoView.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
        
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
    DanmakuSurfaceView.Callback mDMCallback = new DanmakuSurfaceView.Callback() {
        @Override
        public void prepared() {
            Log.i("Play", "dm prepared");
            mProgressText.post(new Runnable() {
                @Override
                public void run() {
                    mProgressText.setText(mProgressText.getText()
                            +"\n"
                            + getString(R.string.danmakus_loaded)
                            +"\n"
                            +getString(R.string.enable_danmaku_drawing_cache,mEnabledDrawingCache));
                    mHandler.sendEmptyMessage(SYNC);
                    hideTextDelayed();
                }
            });
        }

        @Override
        public void updateTimer(DanmakuTimer timer) {
            mTimer = timer;
        }

        @Override
        public void error(Throwable error) {
            Log.e("Play", "error", error);
            mProgressText.post(new Runnable() {
                @Override
                public void run() {
                    mProgressText.setText(mProgressText.getText() + "\n"+getString(R.string.danmakus_load_failed));
                    hideTextDelayed();
                }
            });
        }
    };
    private MediaController mMediaController;
    private Handler mHandler;
    private MediaList mList;
    Response.Listener<String> dmListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            mProgressText.setText(mProgressText.getText() + "\n"+ getString(R.string.danmakus_downloaded));
            try {
                DMSiteType type = DMSiteType.ACFUN;
                ILoader loader = type.getLoader();
                loader.loadData(response);
                BaseDanmakuParser parser = type.getParser().load(loader.getDataSource());
                mDMView.prepare(parser);
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
        StringRequest request = new DanmakusRequest(mVideo.commentId,save,dmListener , err);
        Connectivity.addRequest(request);
    }

    private boolean loadDownloadedDanmakus(DownloadEntry entry) {
        
        File dmFile = new File(entry.destination, entry.part.commentId + ".json");
        if (!dmFile.exists()) return false;
        try {
            DMSiteType type = DMSiteType.ACFUN;
            ILoader loader = type.getLoader();
            loader.load(new FileInputStream(dmFile));
            BaseDanmakuParser parser = type.getParser().load(loader.getDataSource());
            mDMView.prepare(parser);
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
        String sourceType = mVideo.type;
        ResolverType type = null;
        try {
            type = ResolverType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (type == null) {
            mProgressText.setText(mProgressText.getText() + "\n" + getString(R.string.source_type_not_support_yet));
            Toast.makeText(this, getString(R.string.source_type_not_support_yet), Toast.LENGTH_SHORT).show();
            return;
        }
        mResolver = (BaseResolver) type.getResolver(mVideo.sourceId);
        int resolution = Integer.parseInt(AcApp.getString(getString(R.string.key_resolution_mode), "1"));
        mResolver.setResolution(resolution);
        mResolver.setOnResolvedListener(OnResolved);
        mResolver.resolveAsync(getApplicationContext());
        mProgressText.setText(mProgressText.getText() + "\n"+getString(R.string.video_segments_paring));
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        
        menu.add(Menu.NONE, 0x1, Menu.NONE, "关闭弹幕");
        menu.add(Menu.NONE, 0x2, Menu.NONE, "发送弹幕");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case 0x1:
            mDMView.stop();
            break;
        case 0x2:
            
            break;
        }
        
        return super.onContextItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        if (mMediaController != null) 
            toggleMediaControlsVisiblity();
    }

    @Override
    public void start() {
        mVideoView.start();
        if(isDMShow()){
            mDMView.resume();
            mHandler.sendEmptyMessageDelayed(SYNC,200);
        }
    }

    @Override
    public void pause() {
        mVideoView.pause();
        mHandler.removeMessages(SYNC);
        mDMView.pause();
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
        Log.i(TAG, "seek to "+ pos);
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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case SEEK_COMPLETE:
            if(isPlaying()){
                mDMView.start(getCurrentPosition());
                mHandler.sendEmptyMessageDelayed(SYNC, 1500);
            }else
                mHandler.sendEmptyMessageDelayed(SEEK_COMPLETE, 100);
            break;
        case SYNC:
            if (isPlaying()){
                if(mTimer != null){
                    long cur = getCurrentPosition();
                    long d = Math.abs(cur -mTimer.currMillisecond );
                    if(d > 1000){
                        mDMView.start(cur);
                        Log.i(TAG, String.format("timer sync::%d",d));
                    }
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
            mDMView.start(getCurrentPosition());
            mHandler.sendEmptyMessageDelayed(SYNC, 200);
        }
    }
}
