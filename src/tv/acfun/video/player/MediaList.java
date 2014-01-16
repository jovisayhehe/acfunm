package tv.acfun.video.player;

import java.util.ArrayList;
import java.util.List;

import tv.acfun.video.entity.VideoSegment;

import android.content.Context;

public final class MediaList {
    private List<MediaSegment> mSegmentList;

    private long mDuration;

    public MediaList() {
        mSegmentList = new ArrayList<MediaSegment>();
    }
    
    public void add(MediaSegment mediaSegment) {
        mediaSegment.mOrder = mSegmentList.size();
        mediaSegment.mStartTime = mDuration;
        mDuration += mediaSegment.mDuration;
        mSegmentList.add(mediaSegment);
    }

    public long getTotalDuration() {
        return mDuration;
    }

    public MediaSegment get(int index) {
        return mSegmentList.get(index);
    }

    public int size() {
        return mSegmentList.size();
    }

    public void clear() {
        mSegmentList.clear();
        mDuration = 0;
    }

    public MediaSegment getItemByTime(long position) {
        MediaSegment lowerItem = null;
        for (MediaSegment item : mSegmentList) {
            if (item.mStartTime > position)
                break;

            lowerItem = item;
        }

        return lowerItem;
    }

    public interface Resolver {
        void clearCache();

        void resolve(Context context) throws ResolveException;

        void resolveAsync(Context context);

        MediaList getMediaList() ;
        
        /**
         * Youku's resolution
         * <pre>
         * 
         * hd3|hd2|mp4|flv
         * ---------------
         *  3 | 2 | 1 | 0
         * </pre>
         * 
         * @param resolution
         *            video resolution
         * @throws ResolveException
         */
        MediaList getMediaList(int resolution);

        MediaSegment getMediaSegment(int segmentId);

        void setOnResolvedListener(OnResolvedListener l);
    }

    public interface OnResolvedListener{
        void onResolved(Resolver resolver);
    }
    /**
     * 转换为下载用的Segment
     * @return
     */
    public List<VideoSegment> toSegments(){
        List<VideoSegment> segments = new ArrayList<VideoSegment>(size());
        for(MediaSegment ms : mSegmentList){
            VideoSegment vs = new VideoSegment();
            vs.url = ms.mUrl;
            vs.stream = ms.mUrl;
            vs.duration = ms.mDuration;
            vs.num = ms.mOrder;
            vs.size = ms.mSize;
            segments.add(vs);
        }
        return segments;
    }

    public static MediaList createFromeSegments(List<VideoSegment> segments) {
        MediaList list = new MediaList();
        for(VideoSegment vs : segments){
            MediaSegment ms = new MediaSegment();
            ms.mUrl = vs.url;
            ms.mDuration = vs.duration;
            ms.mSize = vs.size;
            list.add(ms);
        }
        
        return list;
    }
}
