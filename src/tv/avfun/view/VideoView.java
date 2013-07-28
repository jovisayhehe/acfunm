
package tv.avfun.view;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.MediaController.MediaPlayerControl;

import java.io.IOException;
import java.util.List;

import tv.avfun.entity.VideoSegment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.yixia.vitamio.library.R;

public class VideoView extends SurfaceView implements SurfaceHolder.Callback, MediaPlayerControl {

    public static final int           VIDEO_LAYOUT_ORIGIN       = 0;
    public static final int           VIDEO_LAYOUT_SCALE        = 1;
    public static final int           VIDEO_LAYOUT_STRETCH      = 2;
    public static final int           VIDEO_LAYOUT_ZOOM         = 3;
    
    private static final String       TAG  = "ac.VideoView";
    private static final int          STATE_ERROR               = -1;
    private static final int          STATE_IDLE                = 0;
    private static final int          STATE_PREPARING           = 1;
    private static final int          STATE_PREPARED            = 2;
    private static final int          STATE_PLAYING             = 3;
    private static final int          STATE_PAUSED              = 4;
    private static final int          STATE_PLAYBACK_COMPLETED  = 5;
    private static final int          STATE_SUSPEND             = 6;
    private static final int          STATE_RESUME              = 7;
    private static final int          STATE_SUSPEND_UNSUPPORTED = 8;
    
    private int                       mCurrentState             = STATE_IDLE;
    private int                       mTargetState              = STATE_IDLE;
    private int                       mVideoLayout              = VIDEO_LAYOUT_SCALE;
    private int                       mCurrentIndex;
    private List<VideoSegment>        mParts;
    private SurfaceHolder             mSurfaceHolder;
    private MediaPlayer               mMediaPlayer;
    private Uri                       mUri;
    private String                    mHeader;
    private long                      mDuration;
    private int                       mVideoWidth;
    private int                       mVideoHeight;
    private int                       mSurfaceWidth;
    private int                       mSurfaceHeight;
    private MediaController           mMediaController;
    private OnInfoListener            mOnInfoListener;
    private OnErrorListener           mOnErrorListener;
    private OnPreparedListener        mOnPreparedListener;
    private OnTimedTextListener       mOnTimedTextListener;
    private OnCompletionListener      mOnCompletionListener;
    private OnSeekCompleteListener    mOnSeekCompleteListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int                       mCurrentBufferPercentage;
    private long                      mSeekWhenPrepared;
    private boolean                   mCanPause                 = true;
    private boolean                   mCanSeekBack              = true;
    private boolean                   mCanSeekForward           = true;
    private Context                   mContext;
    private float mVideoAspectRatio;
    private float mAspectRatio;

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        setListener();
    }
    private AlertDialog loadingDialog;
    private OnPreparedListener mPreparedListener;
    private OnVideoSizeChangedListener mSizeChangedListener;
    private OnCompletionListener mCompletionListener;
    private OnErrorListener mErrorListener;
    private OnBufferingUpdateListener mBufferingUpdateListener;
    private OnInfoListener mInfoListener;
    private OnSeekCompleteListener mSeekCompleteListener;
    private View loadingView;
    private void setListener() {
        mPreparedListener = new OnPreparedListener() {

            public void onPrepared(MediaPlayer mp) {
//                Log.d(TAG, "onPrepared");
                mCurrentState = STATE_PREPARED;
                mTargetState = STATE_PLAYING;

                if (mOnPreparedListener != null)
                    mOnPreparedListener.onPrepared(mMediaPlayer);
                if (mMediaController != null)
                    mMediaController.setEnabled(true);
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                mVideoAspectRatio = mp.getVideoAspectRatio();
                if(mParts.get(mCurrentIndex).duration == 0 ){
                    mParts.get(mCurrentIndex).duration = mp.getDuration();
                    resetTotalDuration();
                }
                long seekToPosition = mSeekWhenPrepared;

                if (seekToPosition != 0)
                    seekTo(seekToPosition);
                if (mVideoWidth != 0 && mVideoHeight != 0) {
                    setVideoLayout(mVideoLayout, mAspectRatio);
                    if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        if (mTargetState == STATE_PLAYING) {
                            start();
                            if (mMediaController != null)
                                mMediaController.show();
                        } else if (!isPlaying() && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                            if (mMediaController != null)
                                mMediaController.show(0);
                        }
                    }
                } else if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        };
        mSizeChangedListener = new OnVideoSizeChangedListener() {
            
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                Log.d(TAG, String.format("onVideoSizeChanged: (%dx%d)", width, height));
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                mVideoAspectRatio = mp.getVideoAspectRatio();
                if (mVideoWidth != 0 && mVideoHeight != 0)
                    setVideoLayout(mVideoLayout, mAspectRatio);
            }
        };
        mCompletionListener = new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                if(tv.ac.fun.BuildConfig.DEBUG) Log.d(TAG, "Completion index="+mCurrentIndex);
                if(mCurrentIndex>=mParts.size()-1){
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null)
                        mMediaController.hide();
                    if (mOnCompletionListener != null)
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                }else{
                    mCurrentIndex++;
                    openVideo(mCurrentIndex);
                    
                }
            }
        };
        mBufferingUpdateListener = new OnBufferingUpdateListener() {

            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mCurrentBufferPercentage = percent;
                if (mOnBufferingUpdateListener != null)
                    mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
            }
        };
        mSeekCompleteListener = new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if(tv.ac.fun.BuildConfig.DEBUG)Log.d(TAG, "onSeekComplete");
                if (mOnSeekCompleteListener != null)
                    mOnSeekCompleteListener.onSeekComplete(mp);
            }
        };
        mInfoListener = new OnInfoListener() {


            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if(tv.ac.fun.BuildConfig.DEBUG) Log.i(TAG, String.format("onInfo: (%d, %d)", what, extra));
                if (mOnInfoListener != null) {
                    mOnInfoListener.onInfo(mp, what, extra);
                } else if (mMediaPlayer != null) {
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
                        mMediaPlayer.pause();
                        if(loadingDialog == null)
                            loadingDialog = new AlertDialog.Builder(mContext).setView(loadingView).setCancelable(false).show();
                        else if(!loadingDialog.isShowing())
                            loadingDialog.show();
                    }
                    else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
                        if(loadingDialog != null && loadingDialog.isShowing()){
                            loadingDialog.dismiss();
                        }
                        mMediaPlayer.start();
                    }
                    
                }

                return true;
            }
        };
        mErrorListener = new OnErrorListener() {

            public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                if(tv.ac.fun.BuildConfig.DEBUG) Log.e(TAG, String.format("Error: %d, %d", framework_err, impl_err));
                mCurrentState = STATE_ERROR;
                mTargetState = STATE_ERROR;
                if (mMediaController != null)
                    mMediaController.hide();

                if (mOnErrorListener != null) {
                    if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
                        return true;
                }

                if (getWindowToken() != null) {
                    int message = framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? R.string.VideoView_error_text_invalid_progressive_playback
                            : R.string.VideoView_error_text_unknown;

                    new AlertDialog.Builder(mContext).setTitle(R.string.VideoView_error_title).setMessage(message)
                            .setPositiveButton(R.string.VideoView_error_button, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (mOnCompletionListener != null)
                                        mOnCompletionListener.onCompletion(mMediaPlayer);
                                }
                            }).setCancelable(false).show();
                }
                return true;
            }
        };
    }
    private void resetTotalDuration() {
        mDuration = 0;
        for(int i=0;i<=mCurrentIndex;i++){
            mDuration += mParts.get(i).duration;
        }
        
    }
    public VideoView(Context context) {
        this(context, null);
    }
    private void initView(Context context) {
        mContext = context;
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().setFormat(PixelFormat.RGBA_8888);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        loadingView = LayoutInflater.from(context).inflate(tv.ac.fun.R.layout.loading, null);
    }


    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }
    public void setVideoLayout(int layout, float aspectRatio) {
        LayoutParams lp = getLayoutParams();
        DisplayMetrics disp = mContext.getResources().getDisplayMetrics();
        int windowWidth = disp.widthPixels, windowHeight = disp.heightPixels;
        float windowRatio = windowWidth / (float) windowHeight;
        float videoRatio = aspectRatio <= 0.01f ? mVideoAspectRatio : aspectRatio;
        mSurfaceHeight = mVideoHeight;
        mSurfaceWidth = mVideoWidth;
        if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth && mSurfaceHeight < windowHeight) {
            lp.width = (int) (mSurfaceHeight * videoRatio);
            lp.height = mSurfaceHeight;
        } else if (layout == VIDEO_LAYOUT_ZOOM) {
            lp.width = windowRatio > videoRatio ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = windowRatio < videoRatio ? windowHeight : (int) (windowWidth / videoRatio);
        } else {
            boolean full = layout == VIDEO_LAYOUT_STRETCH;
            lp.width = (full || windowRatio < videoRatio) ? windowWidth : (int) (videoRatio * windowHeight);
            lp.height = (full || windowRatio > videoRatio) ? windowHeight : (int) (windowWidth / videoRatio);
        }
        setLayoutParams(lp);
        getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
        if(tv.ac.fun.BuildConfig.DEBUG)
            Log.d(TAG, String.format("VIDEO: %dx%dx%f, Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f", mVideoWidth, mVideoHeight,
                mVideoAspectRatio, mSurfaceWidth, mSurfaceHeight, lp.width, lp.height, windowWidth, windowHeight,
                windowRatio));
        mVideoLayout = layout;
        mAspectRatio = aspectRatio;
    }
    
    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            if(tv.ac.fun.BuildConfig.DEBUG)
                Log.i(TAG, "seek to "+ msec+", CurrentIndex="+mCurrentIndex);
            long currentTotal = getCurrentPartTotalDuration();
            if(msec<= mMediaPlayer.getDuration())
            {
                mMediaPlayer.seekTo(msec);
            }else if(msec < currentTotal){
                mMediaPlayer.seekTo(mMediaPlayer.getDuration() - currentTotal + msec);
            }else{
                long total = 0;
                for (int i = 0; i < mParts.size(); i++) {
                    total += mParts.get(i).duration;
                    if(total>msec){
                        if(tv.ac.fun.BuildConfig.DEBUG)
                            Log.i(TAG,String.format("index=%d,total=%d",i,total));
                        if(mCurrentIndex != i){
                            mCurrentIndex = i;
                            mSeekWhenPrepared = mParts.get(i).duration - total + msec;
                        }
                        break;
                    }
                }
                if(mSeekWhenPrepared > 0){
                    if(tv.ac.fun.BuildConfig.DEBUG)
                        Log.i(TAG, "seek to index :" + mCurrentIndex);
                    if(loadingDialog != null) loadingDialog.show();
                    openVideo(mCurrentIndex);
                }
                
            }
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
        
    }
    private long getCurrentPartTotalDuration(){
        long total = 0;
        for(int i=0;i<= mCurrentIndex;i++){
            total += mParts.get(i).duration;
        }
        return total;
    }
    private long getTotalPartDuration(){
        long total = 0;
        for(int i=0;i< mParts.size();i++){
            total += mParts.get(i).duration;
        }
        return total;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        mSurfaceWidth = w;
        mSurfaceHeight = h;
        boolean isValidState = (mTargetState == STATE_PLAYING);
        boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
        if (mMediaPlayer != null && isValidState && hasValidSize) {
            if (mSeekWhenPrepared != 0)
                seekTo(mSeekWhenPrepared);
            start();
            if (mMediaController != null) {
                if (mMediaController.isShowing())
                    mMediaController.hide();
                mMediaController.show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (mCurrentIndex != 0 && mMediaPlayer != null && mCurrentState == STATE_SUSPEND && mTargetState == STATE_RESUME) {
            mMediaPlayer.setDisplay(mSurfaceHolder);
            resume();
        } else {
            mCurrentIndex = 0;
            openVideo();
        }
    }

    public void resume() {
        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            mCurrentIndex = 0;
            openVideo();
        }
        
    }
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        if (mMediaController != null)
            mMediaController.hide();
        if (mCurrentState != STATE_SUSPEND)
            release(true);
    }
    private void openVideo(int index){
        if(mParts == null || mParts.isEmpty()||index >= mParts.size()|| mSurfaceHolder == null || !Vitamio.isInitialized(mContext)){
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        
        release(false);
        
        try {
//            if(index == 0){
//                mDuration = -1;
//            }
            mCurrentBufferPercentage = 0;
            mUri = Uri.parse(mParts.get(index).url);
            mMediaPlayer = new MediaPlayer(mContext);
            mMediaPlayer.setOnPreparedListener(mPreparedListener );
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
            mMediaPlayer.setDataSource(mContext, mUri,mHeader==null?"":mHeader);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            if(index==0) attachMediaController();
        }catch (IOException ex) {
            Log.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        
    }
    private void openVideo() {
        openVideo(0);
    }
    
    
    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());

            if (mUri != null) {
                if (TextUtils.isEmpty(mFileName)) {
                    List<String> paths = mUri.getPathSegments();
                    String name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
                    mMediaController.setFileName(name);
                } else
                    mMediaController.setFileName(mFileName);
            }
        }
    }
    public List<VideoSegment> getVideoParts() {
        return mParts;
    }
    
    public void setVideoParts(List<VideoSegment> parts) {
        mParts = parts;
        if (mDuration > 0)
            mDuration = 0;
        mDuration = getTotalPartDuration();
    }
    
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }
    private String mFileName;
    
    public void setFileName(String fileName){
        mFileName = fileName;
    }

    public void setOnPreparedListener(OnPreparedListener l) {
      mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
      mOnCompletionListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
      mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
      mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
      mOnSeekCompleteListener = l;
    }

    public void setOnTimedTextListener(OnTimedTextListener l) {
      mOnTimedTextListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
      mOnInfoListener = l;
    }
    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return mDuration;
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = getTotalPartDuration();
        return mDuration;
    }
    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            long current = 0;
            for (int i = 0; i < mCurrentIndex; i++) {
                current += mParts.get(i).duration;
            }
            current += mCurrentBufferPercentage * mMediaPlayer.getDuration();
            int percentage = (int) (current * 100 / mDuration);
            return percentage;
        }
        return 0;
    }
    @Override
    public boolean canPause() {
        return mCanPause;
    }
    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }
    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }
    @Override
    public long getCurrentPosition() {
        long playedPosition = 0;
        for(int i=0;i<mCurrentIndex;i++){
            playedPosition+= mParts.get(i).duration;
        }
        if (isInPlaybackState()){
            long current = mMediaPlayer.getCurrentPosition();
            return playedPosition + current;
        }
        return playedPosition+mSeekWhenPrepared;
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null)
            mMediaController.hide();
        mMediaController = controller;
        attachMediaController();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null)
            toggleMediaControlsVisiblity();
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null)
            toggleMediaControlsVisiblity();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }
    public void setVideoQuality(int quality) {
        if(mMediaPlayer != null)
            mMediaPlayer.setVideoQuality(quality);
    }

    public boolean isValid() {
        return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
    }
}
