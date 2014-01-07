package tv.acfun.video.player;

public interface IMediaSegmentPlayer {
    long getAbsolutePosition();
    boolean hasDataSource();
    boolean isSameMediaItem(MediaSegment mediaItem);
    int getOrder();
}
