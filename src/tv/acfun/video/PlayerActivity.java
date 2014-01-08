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

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import master.flame.danmaku.controller.DMSiteType;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaController;
import tv.acfun.video.player.MediaController.MediaPlayerControl;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.MediaSegmentPlayer;
import tv.acfun.video.player.VideoView;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.ResolverType;
import tv.acfun.video.util.net.Connectivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
    private BaseResolver sResolver;
    private VideoPart mVideo;
    OnResolvedListener OnResolved = new OnResolvedListener() {
        @Override
        public void onResolved(Resolver resolver) {
            if (resolver.getMediaList() != null && resolver.getMediaList().size()>0) {
                mProgressText.setText(mProgressText.getText() 
                        + getString(R.string.video_segments_parsing_success, resolver.getMediaList().size() )
                        +"\n" 
                        + getString(R.string.danmakus_buffering));
                addDanmakusRequest();
            } else {
                mProgressText.setText(mProgressText.getText() + getString(R.string.failed));
                // TODO: show retry
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) return;
        mHandler = new Handler(this);
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
        if (sResolver != null && sResolver.getMediaList() != null)  return;
        resolveVideos();
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
        mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
        mProgressText = (TextView) findViewById(R.id.progress_text);
        mVideoView.setOnPreparedListener(onPrepared);
        mVideoView.setOnSeekCompleteListener(onSeekComplete);
        mDMView = (DanmakuSurfaceView) findViewById(R.id.danmakus);
        mDMView.enableDanmakuDrawingCache(mEnabledDrawingCache);
        mDMView.setCallback(mDMCallback);
        mDMView.setOnClickListener(this);
        mMediaController = new MediaController(this);
        mMediaController.setAnchorView(mDMView);
        mMediaController.setMediaPlayer(this);
        mMediaController.setInstantSeeking(false);
        mVideoView.setMediaController(mMediaController);
    }
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
            // TODO Auto-generated method stub
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

    private void addDanmakusRequest() {
        String url = "http://comment.acfun.tv/" + mVideo.commentId + ".json";
        // TODO: acfun lock danmakus
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
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
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressText.setText(mProgressText.getText() +"\n"+getString(R.string.danmakus_download_failed));
                hideTextDelayed();
                startPlay();
            }
        }) {
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                parsed = new String(response.data, Charset.defaultCharset());
                return Response.success(parsed, Connectivity.newCache(response, 60));
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null || headers.equals(Collections.EMPTY_MAP)) {
                    headers = new HashMap<String, String>();
                }
                headers.put("User-Agent", BaseResolver.UA_DEFAULT);
                return headers;
            }
        };
        Connectivity.addRequest(request);
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
        mVideoView.setMediaList(sResolver.getMediaList());
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
        sResolver = (BaseResolver) type.getResolver(mVideo.sourceId);
        int resolution = Integer.parseInt(AcApp.getString(getString(R.string.key_resolution_mode), "1"));
        sResolver.setResolution(resolution);
        sResolver.setOnResolvedListener(OnResolved);
        sResolver.resolveAsync(getApplicationContext());
        mProgressText.setText(mProgressText.getText() + "\n"+getString(R.string.video_segments_paring));
    }

    @Override
    public void onClick(View v) {
        if (mMediaController != null) 
            toggleMediaControlsVisiblity();
    }

    @Override
    public void start() {
        mVideoView.start();
        mDMView.resume();
        mHandler.sendEmptyMessageDelayed(SYNC,200);
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
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                mDMView.start(getCurrentPosition());
                mHandler.sendEmptyMessageDelayed(SYNC, 1500);
            }
            break;
        case HIDE_TEXT:
            mProgressText.startAnimation(mTextAnimation);
            break;
        default:
            break;
        }
        return false;
    }
}
