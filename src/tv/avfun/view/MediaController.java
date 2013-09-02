/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
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

package tv.avfun.view;

import master.flame.danmaku.ui.widget.DanmakuView;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.OutlineTextView;
import tv.ac.fun.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MediaController extends FrameLayout {

    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private MediaPlayerControl mPlayer;
    private Context mContext;
    private PopupWindow mWindow;
    private int mAnimStyle;
    private View mAnchor;
    private View mRoot;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private TextView mFileName;
    private OutlineTextView mInfoView;
    private String mTitle;
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = true;
    private boolean mFromXml = false;
    private ImageButton mPauseButton;
    private AudioManager mAM;
    private OnShownListener mShownListener;
    private OnHiddenListener mHiddenListener;
    private View mMenuButton;
    private int mVideoLayout;
    private int mMaxVolume;
    private TextView mInfo;
    private ImageButton mLayoutButton;
    private int mSurfaceYDisplayRange;
    private ImageView mOperationImage;
    private View mOperation;
    private View mOperationPecent;
    private boolean mEnableBrightnessGesture = true;
    private boolean mIsFirstBrightnessGesture;
    private int mWidth;
    private View mControllerView;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
            case FADE_OUT:
                hide();
                mInfo.setVisibility(View.GONE);
                mOperation.setVisibility(View.GONE);
                break;
            case SHOW_PROGRESS:
                pos = setProgress();
                if (!mDragging && mShowing) {
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                    updatePausePlay();
                }
                break;
            }
        }
    };
    
    private View.OnClickListener mMenuListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO menu
            if(mOnMenuClickListener != null){
                mOnMenuClickListener.onClick(v);
                mPlayer.pause();
                if(mDanmaku!= null)
                    mDanmaku.pause();
            }
        }
    };
    private OnMenuClickListener mOnMenuClickListener;
    public void setOnMenuClickListener(OnMenuClickListener l){
        mOnMenuClickListener = l;
        if(l != null){
            mMenuButton.setVisibility(View.VISIBLE);
        }
    }
    private View.OnClickListener mDanmakuListener = new View.OnClickListener() {
        String mTag = "danmaku";
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if(tag == null){
                mDanmakuButton.setImageResource(R.drawable.danmaku_off);
                v.setTag(mTag);
                toggleDamaku(false);
            }else{
                mDanmakuButton.setImageResource(R.drawable.danmaku_on);
                v.setTag(null);
                toggleDamaku(true);
            }
        }
    };
    private View.OnClickListener mLockListener = new View.OnClickListener() {
        String mTag = "lock";
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if(tag == null){
                mLockButton.setImageResource(R.drawable.mediacontroller_lock);
                isLocked = true;
                v.setTag(mTag);
            }else{
                isLocked =false;
                mLockButton.setImageResource(R.drawable.mediacontroller_unlock);
                v.setTag(null);
            }
            toggleLock();
        }
    };
    private boolean isLocked;
    private View.OnClickListener mChangeLayoutListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            changeLayout();
        }
    };

    private View.OnClickListener mPauseListener = new View.OnClickListener() {

        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            show(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
            if (mInfoView != null) {
                mInfoView.setText("");
                mInfoView.setVisibility(View.VISIBLE);
            }
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;

            long newposition = (mDuration * progress) / 1000;
            String time = StringUtils.generateTime(newposition);
            if (mInstantSeeking){
                mPlayer.seekTo(newposition);
                if(mDanmaku!= null)mDanmaku.seekTo(newposition);
            }
            if (mInfoView != null)
                mInfoView.setText(time);
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking){
                long pos = (mDuration * bar.getProgress()) / 1000;
                mPlayer.seekTo(pos);
                if(mDanmaku!= null)mDanmaku.seekTo(pos);
            }
            if (mInfoView != null) {
                mInfoView.setText("");
                mInfoView.setVisibility(View.GONE);
            }
            show(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };
    private ImageButton mLockButton;
    private ImageButton mDanmakuButton;
    private View mButtons;

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
        makeControllerView();
    }

    private void toggleLock() {
        mButtons.setVisibility(isLocked ? View.GONE : View.VISIBLE);
        mProgress.setEnabled(!isLocked);
    }

    public MediaController(Context context) {
        super(context);
        if (!mFromXml && initController(context))
            initFloatingWindow();
    }

    private boolean initController(Context context) {
        mContext = context;
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(context, mGesutreListener);
        DisplayMetrics screen = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(screen);
        mWidth = screen.widthPixels;
        mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        return true;
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    private void initFloatingWindow() {
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }

    /**
     * Set the view that acts as the anchor for the control view. This can for
     * example be a VideoView, or your Activity's main view.
     * 
     * @param view
     *            The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        mAnchor = view;
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            mWindow.setWidth(LayoutParams.MATCH_PARENT);
            mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        }
        initControllerView(mRoot);
    }

    /**
     * Create the view that holds the widgets that control playback. Derived
     * classes can override this to create their own.
     * 
     * @return The controller view.
     */
    protected View makeControllerView() {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.mediacontroller, this);
    }

    private void initControllerView(View v) {
        mControllerView = v.findViewById(R.id.mediacontroller);
        mButtons = v.findViewById(R.id.mediacontroller_controls_buttons);
        mPauseButton = (ImageButton) v.findViewById(R.id.mediacontroller_play_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        mMenuButton = v.findViewById(R.id.video_menu);
        mMenuButton.setOnClickListener(mMenuListener);
        if(mOnMenuClickListener == null) mMenuButton.setVisibility(View.GONE);
        mLayoutButton = (ImageButton) v.findViewById(R.id.mediacontroller_screen_size);
        mLayoutButton.setOnClickListener(mChangeLayoutListener);
        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_seekbar);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
                seeker.setThumbOffset(1);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.mediacontroller_time_total);
        mCurrentTime = (TextView) v.findViewById(R.id.mediacontroller_time_current);
        mFileName = (TextView) v.findViewById(R.id.mediacontroller_file_name);
        mInfo = (TextView) v.findViewById(R.id.operation_info);
        mOperationImage = (ImageView) v.findViewById(R.id.operation_bg);
        mOperation = v.findViewById(R.id.operation_volume_brightness);
        mOperationPecent = v.findViewById(R.id.operation_percent);
        mLockButton = (ImageButton) v.findViewById(R.id.mediacontroller_lock);
        mLockButton.setOnClickListener(mLockListener);
        mDanmakuButton = (ImageButton) v.findViewById(R.id.mediacontroller_danmaku);
        mDanmakuButton.setOnClickListener(mDanmakuListener);
        if (mFileName != null)
            mFileName.setText(mTitle);
    }

    private void changeLayout() {
        mVideoLayout++;
        if (mVideoLayout == 4) {
            mVideoLayout = 0;
        }

        switch (mVideoLayout) {
        case 0:
            mVideoLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
            mLayoutButton.setImageResource(R.drawable.mediacontroller_sreen_size_100);
            break;
        case 1:
            mVideoLayout = VideoView.VIDEO_LAYOUT_SCALE;
            mLayoutButton.setImageResource(R.drawable.mediacontroller_screen_fit);
            break;
        case 2:
            mVideoLayout = VideoView.VIDEO_LAYOUT_STRETCH;
            mLayoutButton.setImageResource(R.drawable.mediacontroller_screen_size);
            break;
        case 3:
            mVideoLayout = VideoView.VIDEO_LAYOUT_ZOOM;
            mLayoutButton.setImageResource(R.drawable.mediacontroller_sreen_size_crop);
            break;
        }
        mPlayer.changeVideoLayout(mVideoLayout, 0);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }

    /**
     * Control the action when the seekbar dragged by user
     * 
     * @param seekWhenDragging
     *            True the media will seek periodically
     */
    public void setInstantSeeking(boolean seekWhenDragging) {
        mInstantSeeking = seekWhenDragging;
    }

    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Set the content of the file_name TextView
     * 
     * @param name
     */
    public void setFileName(String name) {
        mTitle = name;
        if (mFileName != null)
            mFileName.setText(mTitle);
    }

    /**
     * Set the View to hold some information when interact with the
     * MediaController
     * 
     * @param v
     */
    public void setInfoView(OutlineTextView v) {
        mInfoView = v;
    }

    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause())
                mPauseButton.setEnabled(false);
        } catch (IncompatibleClassChangeError ex) {}
    }

    /**
     * <p>
     * Change the animation style resource for this controller.
     * </p>
     * <p/>
     * <p>
     * If the controller is showing, calling this method will take effect only
     * the next time the controller is shown.
     * </p>
     * 
     * @param animationStyle
     *            animation style to use when the controller appears and
     *            disappears. Set to -1 for the default animation, 0 for no
     *            animation, or a resource identifier for an explicit animation.
     */
    public void setAnimationStyle(int animationStyle) {
        mAnimStyle = animationStyle;
    }

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     * 
     * @param timeout
     *            The timeout in milliseconds. Use 0 to show the controller
     *            until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            disableUnsupportedButtons();

            if (mFromXml) {
                // setVisibility(View.VISIBLE);
                mControllerView.setVisibility(View.VISIBLE);
            } else {
                showWindow();
            }
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }
        updatePausePlay();
        toggleLock();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
        }
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void hide() {
        if (mAnchor == null)
            return;

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if (mFromXml)
                    mControllerView.setVisibility(View.GONE);
                else
                    mWindow.dismiss();
            } catch (IllegalArgumentException ex) {
                Log.d("MediaController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    private long setProgress() {
        if (mPlayer == null || mDragging)
            return 0;

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mEndTime != null)
            mEndTime.setText(StringUtils.generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(StringUtils.generateTime(position));

        return position;
    }

    public GestureDetector mGestureDetector;
    int vol;
    private OnGestureListener mGesutreListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mShowing)
                show();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {}

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(Math.abs(distanceY) < 10 || Math.abs(distanceX)> 100) return true;
            if (!mEnableBrightnessGesture || e1.getRawX() > (mWidth / 2)) {
                vol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
                setVolume(vol + (int) ((distanceY / mSurfaceYDisplayRange) * mMaxVolume));
            } else if (mEnableBrightnessGesture)
                doBrightnessTouch(-distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {}

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return true;
        }
    };

    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setVolume(int vol) {
        if (vol > mMaxVolume) {
            vol = mMaxVolume;
        }
        if (vol < 0) {
            vol = 0;
        }
        Log.i("vol=%d", vol);
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        operate(R.drawable.video_volumn_bg, (float) vol / mMaxVolume);

    }

    private void doBrightnessTouch(float y_changed) {
        if (mIsFirstBrightnessGesture)
            initBrightnessTouch();
        float delta = y_changed / mSurfaceYDisplayRange * 0.1f;

        // Estimate and adjust Brightness
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.screenBrightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1);

        // Set Brightness
        ((Activity) mContext).getWindow().setAttributes(lp);
        operate(R.drawable.video_brightness_bg, lp.screenBrightness);
    }

    private void initBrightnessTouch() {
        float brightnesstemp = 0.01f;
        // Initialize the layoutParams screen brightness
        try {
            brightnesstemp = android.provider.Settings.System.getInt(mContext.getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.screenBrightness = brightnesstemp;
        ((Activity) mContext).getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    public void operate(int resId, float pecent) {
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
            if (mFromXml) {
                // setVisibility(View.VISIBLE);
            } else {
                showWindow();
            }
            mControllerView.setVisibility(View.GONE);
            mShowing = true;
            Log.i("show operation hide controller");
        }
        mOperationImage.setImageResource(resId);
        mOperation.setVisibility(View.VISIBLE);
        mInfo.setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = mOperationPecent.getLayoutParams();
        layoutParams.width = ((int) (pecent * findViewById(R.id.operation_full).getLayoutParams().width));
        Log.i("pecent = %f, width=%d", pecent, layoutParams.width);
        mOperationPecent.setLayoutParams(layoutParams);
        mHandler.removeMessages(FADE_OUT);
        mHandler.sendEmptyMessageDelayed(FADE_OUT, 500);
    }

    private void showWindow() {
        int[] location = new int[2];
        mAnchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + mAnchor.getWidth(), location[1]
                + mAnchor.getHeight());

        mWindow.setAnimationStyle(mAnimStyle);
        mWindow.showAtLocation(mAnchor, Gravity.NO_GRAVITY, anchorRect.left, anchorRect.bottom);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();
            return true;

        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            vol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
            setVolume(vol + 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            vol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
            setVolume(vol - 1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying())
            mPauseButton.setImageResource(R.drawable.mediacontroller_pause);
        else
            mPauseButton.setImageResource(R.drawable.mediacontroller_play);
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()){
            mPlayer.pause();
            if(mDanmaku!= null)mDanmaku.pause();
        }else{
            mPlayer.start();
            if(mDanmaku!= null)mDanmaku.start();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null)
            mPauseButton.setEnabled(enabled);
        if (mProgress != null)
            mProgress.setEnabled(enabled);
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    public interface OnShownListener {

        public void onShown();
    }

    public interface OnHiddenListener {

        public void onHidden();
    }

    public interface MediaPlayerControl {

        void start();

        void changeVideoLayout(int layout, float aspectRatio);

        void pause();

        long getDuration();

        long getCurrentPosition();

        void seekTo(long pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();
        
    }
    
    public interface OnMenuClickListener{
        void onClick(View v);
    }
    public void toggleDamaku(boolean show) {
        // TODO Auto-generated method stub
        if(show){
            mDanmaku.start();
            mDanmaku.seekTo(mPlayer.getCurrentPosition());
        }else
            mDanmaku.stop();
    }
    private DanmakuView mDanmaku;
    public void attachDanmakuView(DanmakuView danmaku){
        if(mDanmaku != null) mDanmaku.stop();
        mDanmaku = danmaku;
    }
}
