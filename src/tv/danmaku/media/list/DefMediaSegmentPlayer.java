package tv.danmaku.media.list;

import io.vov.vitamio.MediaPlayer;

import java.io.IOException;
import java.util.Map;

import tv.avfun.entity.VideoSegment;
import tv.danmaku.media.DebugLog;
import android.content.Context;
public class DefMediaSegmentPlayer extends MediaPlayer {
    public DefMediaSegmentPlayer(Context ctx) {
        super(ctx);
    }
    public DefMediaSegmentPlayer(Context ctx, boolean hw){
        super(ctx, hw);
    }
    public static String TAG = DefMediaSegmentPlayer.class.getName();
    private int mOrder = -1;
    private VideoSegment mSegment;
    private int mStartTime;

    final public int getOrder() {
        return mOrder;
    }

    final public int getStartTime() {
        return mStartTime;
    }

    final public VideoSegment getSegment() {
        return mSegment;
    }

    final public void setSegment(int order, int startTime, VideoSegment segment, Map<String, String> headers)
            throws IllegalArgumentException, IllegalStateException, IOException {
        mOrder = order;
        mStartTime = startTime;
        mSegment = segment;

        DebugLog.dfmt(TAG, "set item [%d] %d(%d) %s", order, startTime,
                segment.duration, segment.url);
        super.setDataSource(segment.url, headers);
    }

    final public boolean hasSegment() {
        return mSegment != null;
    }

    @Override
    public long getCurrentPosition() {
        if (mSegment == null)
            return -1;

        long currentPosition = mStartTime + super.getCurrentPosition();
        return currentPosition;
    }
}
