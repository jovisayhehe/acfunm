package tv.acfun.video.player;

public class MediaSegment {
    public String mUrl;
    public long mDuration;
    public long mSize;

    public int mOrder;
    public long mStartTime;

    public MediaSegment() {
    }

    public MediaSegment(String simpleUrl) {
        this();
        mUrl = simpleUrl;
    }

    public MediaSegment(String simpleUrl, long duration) {
        this(simpleUrl);
        mDuration = duration;
    }

    public long getEndTime() {
        return mStartTime + mDuration;
    }

    public long getRelativeTime(long absoluteTime) {
        long relativeTime = absoluteTime - mStartTime;
        relativeTime = Math.max(0, relativeTime);
        return relativeTime;
    }
}
