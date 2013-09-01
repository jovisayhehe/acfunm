package tv.danmaku.media.list;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.TrackInfo;
import io.vov.vitamio.Metadata;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import tv.danmaku.media.AbsMediaPlayer;
import tv.danmaku.media.DebugLog;
import tv.danmaku.media.PlayIndex;
import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.view.SurfaceHolder;

public class DefMediaListPlayer extends AbsMediaPlayer implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener {

    public static final String     TAG = DefMediaListPlayer.class.getName();

    private String                 mMetaListUrl;
    private DefMediaSegmentPlayer  mSegmentPlayer;
    private VideoSegmentListLoader mListLoader;

    private PlayIndex              mPlayIndex;
    private long                   mTotalDuration;

    private SurfaceHolder          mSurfaceHolder;

    boolean                        mListPlayerPrepared;
    boolean                        mIsMediaSwitchEnd;

    protected DefMediaListPlayer(Context context, VideoPart part) {
        mListLoader = new VideoSegmentListLoader(part);
        mContext = context;
    }
    private Map<String, String> mHeaders;
    
    public void setHeaders(Map<String, String> headers){
        mHeaders = headers;
    }
    
//    public void setHeaders(Map<String, String> headers){
//        StringBuffer headerBuffer = null;
//        if (headers != null) {
//            headerBuffer = new StringBuffer();
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                headerBuffer.append(entry.getKey()).append(":").append(entry.getValue()).append("\r\n");
//            }
//        }
//        setHeaders(headerBuffer == null? null : headerBuffer.toString());
//    }
    private DefMediaSegmentPlayer createItemPlayer() {
        DefMediaSegmentPlayer itemPlayer = new DefMediaSegmentPlayer(mContext);
        itemPlayer.setOnBufferingUpdateListener(this);
        itemPlayer.setOnCompletionListener(this);
        itemPlayer.setOnErrorListener(this);
        itemPlayer.setOnInfoListener(this);
        itemPlayer.setOnPreparedListener(this);
        itemPlayer.setOnSeekCompleteListener(this);
        itemPlayer.setOnVideoSizeChangedListener(this);
        // itemPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        itemPlayer.setScreenOnWhilePlaying(true);
        itemPlayer.setVideoQuality(MediaPlayer.VIDEOQUALITY_MEDIUM);
        SurfaceHolder holder = mSurfaceHolder;
        if (holder != null)
            itemPlayer.setDisplay(holder);

        return itemPlayer;
    }

    @Override
    public long getCurrentPosition() {
        if (mSegmentPlayer == null)
            return 0;

        try {
            return mSegmentPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return 0;
        }
    }
    private int mOrder;
    @Override
    public int getDuration() {
        if (mPlayIndex == null)
            return 0;
        
        if(mTotalDuration == 0 || (mOrder < mSegmentPlayer.getOrder() && mTotalDuration <= mPlayIndex.getTotalDuration())){
            mPlayIndex.mSegmentList.get(mSegmentPlayer.getOrder()).duration = mSegmentPlayer.getDuration();
            mTotalDuration = mPlayIndex.getTotalDuration();
            mOrder = mSegmentPlayer.getOrder();
        }
        return (int) mTotalDuration;
    }

    @Override
    public int getVideoHeight() {
        if (mSegmentPlayer == null)
            return 0;
        return mSegmentPlayer.getVideoHeight();
    }

    @Override
    public int getVideoWidth() {
        if (mSegmentPlayer == null)
            return 0;
        return mSegmentPlayer.getVideoWidth();
    }

    @Override
    public boolean isPlaying() {
        try {
            return mSegmentPlayer.isPlaying();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void start() throws IllegalStateException {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.pause();
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        loader = new AsyncLoader(this);
        loader.execute(mMetaListUrl);
    }

    private class AsyncLoader extends AsyncTask<String, Void, PlayIndex> {

        private WeakReference<DefMediaListPlayer> mWeakPlayer;

        public AsyncLoader(DefMediaListPlayer player) {
            mWeakPlayer = new WeakReference<DefMediaListPlayer>(player);
        }

        @Override
        protected PlayIndex doInBackground(String... params) {
            if (params.length <= 0)
                return null;

            DefMediaListPlayer player = mWeakPlayer.get();
            if (player == null)
                return null;

            if (!player.mListLoader.loadIndex(false))
                return null;
            return player.mListLoader.getPlayIndex();
        }

        @Override
        protected void onPostExecute(PlayIndex result) {
            DefMediaListPlayer player = mWeakPlayer.get();
            if (player == null)
                return;

            if (result == null) {
                if (player.mOnErrorListener != null) {
                    player.mOnErrorListener.onError(player, 1, 1);
                }
                return;
            }

            if (result.mSegmentList == null || result.mSegmentList.isEmpty()) {
                if (player.mOnErrorListener != null) {
                    player.mOnErrorListener.onError(player, 1, 1);
                }
                return;
            }

            try {
                DefMediaSegmentPlayer itemPlayer = player.createItemPlayer();
                mOrder = 0 ;
                itemPlayer.setSegment(mOrder, 0, result.mSegmentList.get(mOrder),mHeaders);

                player.mSegmentPlayer = itemPlayer;
                player.mPlayIndex = result;
                player.mTotalDuration = result.getTotalDuration();
                player.mSegmentPlayer.prepareAsync();
                return;

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (player.mOnErrorListener != null) {
                player.mOnErrorListener.onError(player, 1, 1);
            }
        }
    }

    @Override
    public void release() {
        if (mSegmentPlayer != null)
            mSegmentPlayer.release();
        if(loader != null && !loader.isCancelled())
            loader.cancel(true);
    }

    @Override
    public void reset() {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.reset();
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        DebugLog.e(TAG, "seek to " + msec);
        if (mPlayIndex == null)
            return;

        int order = mPlayIndex.getOrderByTime(msec);
        if (order < 0) {
            DebugLog.e(TAG, "seek to invalid segment " + order);
            return;
        }

        VideoSegment segment = mPlayIndex.mSegmentList.get(order);
        if (segment == null) {
            DebugLog.e(TAG, "seek to null segment " + order);
            return;
        }

        int startTime = mPlayIndex.getStartTime(order);
        long msecOffset = msec - startTime;
        DebugLog.e(TAG, String.format("seek to segment[%d:%d] %d", order, startTime, msecOffset));

        if (mSegmentPlayer != null) {
            int playerIndex = mSegmentPlayer.getOrder();
            if (order == playerIndex) {
                mSegmentPlayer.seekTo(msecOffset);
                return;
            }

            mSegmentPlayer.release();
        }
        mSeekWhenPrepared = msecOffset;
        mSegmentPlayer = createItemPlayer();
        try {
            mSegmentPlayer.setSegment(order, startTime, segment,mHeaders);
            mSegmentPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (mSegmentPlayer != null)
            mSegmentPlayer.setDisplay(holder);
    }


    private Context  mContext;

    private long mSeekWhenPrepared;

    private AsyncLoader loader;

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mSegmentPlayer == null)
            return;

        mSegmentPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public void enableLog(boolean enable) {
        // not support
    }

    @Override
    public boolean isBufferingEnd() {
        return mIsMediaSwitchEnd;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (mOnBufferingUpdateListener != null) {
//            DefMediaSegmentPlayer segmentPlayer = mSegmentPlayer;
//            if (segmentPlayer == null)
//                return;
//
//            int totalDuration = getDuration();
//            if (totalDuration <= 0)
//                return;
//
//            VideoSegment segment = segmentPlayer.getSegment();
//            if (segment == null || segment.duration <= 0)
//                return;
//
//            long bufferedTime = segment.duration * percent / 100;
//            int totalPercent = (int) ((segmentPlayer.getStartTime() + bufferedTime) * 100 / totalDuration);

            mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        DebugLog.v(TAG, "onCompletion");
        try {
            if (mPlayIndex != null && mSegmentPlayer != null) {
                int count = mPlayIndex.mSegmentList.size();
                int nextOrder = mSegmentPlayer.getOrder() + 1;
                if (nextOrder < count) {
                    mSegmentPlayer.release();
                    mIsMediaSwitchEnd = false;
//                    if (mOnInfoListener != null) {
//                        mOnInfoListener.onInfo(this, MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
//                    }

                    VideoSegment nextSegment = mPlayIndex.mSegmentList.get(nextOrder);
                    if (nextSegment != null) {
                        mSegmentPlayer = createItemPlayer();
                        int startTime = mPlayIndex.getStartTime(nextOrder);

                        mSegmentPlayer.setSegment(nextOrder, startTime, nextSegment,mHeaders);
                        mSegmentPlayer.prepareAsync();
                    }
                    return;
                }

            }

            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(this);
            }

            return;
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (mOnErrorListener != null) {
            mOnErrorListener.onError(this, 1, 1);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        DebugLog.v(TAG, "onError");
        if (mOnErrorListener != null) {
            return mOnErrorListener.onError(this, what, extra);
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (mOnInfoListener != null) {
            return mOnInfoListener.onInfo(this, what, extra);
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsMediaSwitchEnd = true;
        if (mListPlayerPrepared) {
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(this, MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
            }
            long seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0)
                mp.seekTo(seekToPosition);
            mSeekWhenPrepared = 0;
            mp.start();
        } else {
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(this);
            }
            mListPlayerPrepared = true;
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete(this);
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height);
        }
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException {
        // TODO Auto-generated method stub
    }

    @Override
    public float getVideoAspectRatio() {
        if (mSegmentPlayer == null)
            return 0;
        return mSegmentPlayer.getVideoAspectRatio();
    }

    @Override
    public Metadata getMetadata() {
        if (mSegmentPlayer == null)
            return null;
        return mSegmentPlayer.getMetadata();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setVideoQuality(int quality) {
        if (mSegmentPlayer == null)
            return;
        mSegmentPlayer.setVideoQuality(quality);
    }

    public void setVideoChroma(int chroma) {
        if (mSegmentPlayer != null)
            mSegmentPlayer.setVideoChroma(chroma);
    }

    public void setBufferSize(int bufSize) {
        if (mSegmentPlayer != null)
            mSegmentPlayer.setBufferSize(bufSize);
        
    }

    public boolean isBuffering() {
        if (mSegmentPlayer != null)
            return mSegmentPlayer.isBuffering();
        return false;
    }

    public TrackInfo[] getTrackInfo(String encoding) {
        if (mSegmentPlayer != null)
            return mSegmentPlayer.getTrackInfo(encoding);
        return null;
    }

    public int getAudioTrack() {
        if (mSegmentPlayer != null)
            return mSegmentPlayer.getAudioTrack();
        return 0;
    }

    public SparseArray<String> findTrackFromTrackInfo(int mediaTrackTypeAudio, TrackInfo[] trackInfo) {
        if (mSegmentPlayer != null)
            return mSegmentPlayer.findTrackFromTrackInfo(mediaTrackTypeAudio, trackInfo);
        return null;
    }

    public void selectTrack(int audioIndex) {
        if (mSegmentPlayer != null)
            mSegmentPlayer.selectTrack(audioIndex);
        
    }
    

}
