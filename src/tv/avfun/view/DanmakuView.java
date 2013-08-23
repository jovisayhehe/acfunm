package tv.avfun.view;

import master.flame.danmaku.controller.DrawHelper;
import master.flame.danmaku.controller.DrawTask;
import master.flame.danmaku.controller.IDrawTask;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.parser.IDataSource;
import tv.danmaku.media.AbsMediaPlayer;
import tv.danmaku.media.AbsMediaPlayer.OnSeekCompleteListener;
import tv.danmaku.media.list.VideoView;
import tv.danmaku.media.list.VideoView.OnStartListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DanmakuView extends SurfaceView implements SurfaceHolder.Callback {
    

    public static final int ERROR = 1;
    public static final int OK = 2;
    private SurfaceHolder mSurfaceHolder;
    private DanmakuTimer timer;
    private HandlerThread mDrawThread;
    private DrawHandler handler;
    private boolean mEnableMultiThread;
    private DrawTask drawTask;
    private VideoView mPlayer;
    private IDataSource<?> mDataSource;
    public DanmakuView(Context context) {
        this(context, null);
    }
    
    public DanmakuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        if (timer == null) {
            timer = new DanmakuTimer();
        }
        
    }
    private String mUri;
    public void setDataSource(String url){
        mUri = url;
    }
    public void prepareAsync(){
        new Thread(){

            public void run() {
                ILoader loader = DanmakuLoaderFactory.create("acfun");
                try {
                    loader.load(mUri);
                    mDataSource = loader.getDataSource();
                    mHandler.sendEmptyMessage(OK);
                } catch (IllegalDataException e) {
                    e.printStackTrace();
                    mHandler.obtainMessage(ERROR, 1, 1).sendToTarget();
                }
            }
            
        }.start();
    }
    public void attachVideoView(VideoView player){
        this.mPlayer = player;
        mPlayer.setOnStartListner(new OnStartListener() {
            
            @Override
            public void onStart(VideoView player) {
                resume();
            }
        });
        mPlayer.setOnPauseListener(new VideoView.OnPauseListener() {
            
            @Override
            public void onPause(VideoView player) {
                pause();
            }
        });
//        mPlayer.setOnPreparedListener(new AbsMediaPlayer.OnPreparedListener() {
//            
//            @Override
//            public void onPrepared(AbsMediaPlayer mp) {
//                prepareAsync();
//            }
//        });
        mPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
            
            @Override
            public void onSeekComplete(AbsMediaPlayer mp) {
                handler.removeMessages(DrawHandler.START);
                handler.removeMessages(DrawHandler.UPDATE);
                handler.sendEmptyMessage(DrawHandler.SEEK);
                
            }
        });
    }
    public void resume() {
        if (handler != null && mDrawThread != null && handler.isStop())
            handler.sendEmptyMessage(DrawHandler.RESUME);
        else {
            restart();
        }
    }
    
    public void pause() {
        if (handler != null)
            handler.quit();
    }
    
    public long getCurrentPosition(){
        if(mPlayer != null)
            return mPlayer.getCurrentPosition();
        return 0;
    }
    
    public void start() {
        if(mDrawThread == null){
            mDrawThread = new HandlerThread("draw thread");
            mDrawThread.start();
        }
        if(handler == null)
            handler = new DrawHandler(mDrawThread.getLooper());
        handler.sendEmptyMessage(DrawHandler.START);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
//        release();
    }
    
    public void release() {
        stop();
        if (drawTask != null) {
            drawTask.quit();
            drawTask = null;
        }
        if(mPlayer != null){
            mPlayer = null;
        }
        
    }
    
    public void seekBy(Long deltaMs) {
        if (handler != null) {
            handler.obtainMessage(DrawHandler.SEEK, deltaMs).sendToTarget();
        }
    }
    public int danmakusSize(){
        if(drawTask == null){
            drawTask = newTask(timer, getContext(), getWidth(), getHeight(),
                    new IDrawTask.TaskListener() {
                        @Override
                        public void ready() {
                            //nothing
                        }
                    });
        }
        return drawTask.getDanmakuSize();
    }
    void drawDanmakus() {
//        long stime = System.currentTimeMillis();
        if(mSurfaceHolder != null){
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                DrawHelper.clearCanvas(canvas);
                drawTask.draw(canvas);
    
//                long dtime = System.currentTimeMillis() - stime;
//                String fps = String.format("fps %.2f", 1000 / (float) dtime);
//                DrawHelper.drawText(canvas, fps);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

    }
    public void restart() {
        stop();
        start();
    }
    public void stop() {
        if (handler != null) {
            handler.quit();
            handler = null;
        }
        if (mDrawThread != null) {
            mDrawThread.quit();
            mDrawThread = null;
        }
        
    }

    public void enableMultiThread(boolean enableMultiThread) {
        mEnableMultiThread = enableMultiThread;
    }
    OnPreparedListenr mOnPreparedListenr;
    public void setOnPreparedListenr(OnPreparedListenr l ){
        mOnPreparedListenr = l;
    }
    public interface OnPreparedListenr{
        void onPrepared(DanmakuView view);
    }
    OnErrorListener mOnErrorListener;
    public void setOnErrorListener(OnErrorListener l ){
        mOnErrorListener = l;
    }
    public interface OnErrorListener{
        void onError(DanmakuView view,int what,int extra);
    }
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case ERROR:
                if(mOnErrorListener!=null)
                    mOnErrorListener.onError(DanmakuView.this, msg.arg1, msg.arg2);
                break;
            case OK:
                    if(mOnPreparedListenr!= null)
                    mOnPreparedListenr.onPrepared(DanmakuView.this);
                break;
            }
        }
    };
    public class DrawHandler extends Handler {
        static final int ERROR = 0;

        static final int START = 1;

        static final int UPDATE = 2;

        static final int RESUME = 3;

        static final int SEEK = 4;
        static final int OK = 5;
        
        static final String TAG = "DrawHandler";
        long pausedPostion = 0;

        boolean quitFlag;

        private long mTimeBase;

        public DrawHandler(Looper looper) {
            super(looper);
        }

        public void quit() {
            quitFlag = true;
        }

        public boolean isStop() {
            return quitFlag;
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case START:
                    pausedPostion = 0;
                case RESUME:
                    quitFlag = false;
                    mTimeBase = System.currentTimeMillis() - pausedPostion;
                    timer.update(pausedPostion);
                    startDrawingWhenReady(new Runnable() {

                        @Override
                        public void run() {
                            sendEmptyMessage(UPDATE);
                        }
                    });
                    break;
                case SEEK:
                    long seekPos = System.currentTimeMillis() - mTimeBase;
                    seekPos = getCurrentPosition();
                    mTimeBase -= (System.currentTimeMillis() - mTimeBase - seekPos);
                    drawTask.seek(seekPos);
//                    timer.update(seekPos);
                    startDrawingWhenReady(new Runnable() {

                        @Override
                        public void run() {
                            sendEmptyMessage(UPDATE);
                        }
                    });
                    break;
                case UPDATE:
                    long d = timer.update(System.currentTimeMillis() - mTimeBase);
                    if (d == 0) {
                        if (!quitFlag)
                            sendEmptyMessageDelayed(UPDATE, 15);
                        return;
                    }
                    if (d < 15) {
                        if (d < 10) {
                            try {
                                Thread.sleep(15 - d);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    drawDanmakus();
                    if (!quitFlag)
                        sendEmptyMessage(UPDATE);
                    else {
                        pausedPostion = System.currentTimeMillis() - mTimeBase;
                        Log.i(TAG, "stop draw: current = " + pausedPostion);
                    }
                    break;
            }
        }

        private void startDrawingWhenReady(final Runnable runnable) {
            if (drawTask == null) {
                drawTask = newTask(timer, getContext(), getWidth(), getHeight(),
                        new IDrawTask.TaskListener() {
                            @Override
                            public void ready() {
                                Log.i(TAG, "start drawing multiThread enabled:" + mEnableMultiThread);
                                runnable.run();
                            }
                        });

            } else {
                runnable.run();
            }
        }

    }

//    private IDrawTask createTask(boolean useMultiThread, DanmakuTimer timer, Context context, int width, int height, IDrawTask.TaskListener taskListener) {
//        return useMultiThread ? new CachingDrawTask(timer, context, width, height, taskListener) : new DrawTask(timer, context, width, height, taskListener);
//    }
    private DrawTask newTask(DanmakuTimer timer, Context context, int width, int height, IDrawTask.TaskListener taskListener){
        return new DrawTask(timer, context, mDataSource, width, height, taskListener);
    }
}
