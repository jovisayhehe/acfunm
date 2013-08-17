package tv.danmaku.media;

import java.util.ArrayList;

import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;
import android.text.TextUtils;

public class PlayIndex{
    public ArrayList<VideoSegment> mSegmentList;
    public long mPseudoBitrate;

    
    @SuppressWarnings("unchecked")
    public PlayIndex(VideoPart part) {
        mSegmentList = (ArrayList<VideoSegment>) part.segments.clone();
        
    }
    public VideoSegment getFirstSegment() {
        if (mSegmentList.size() >= 1)
            return mSegmentList.get(0);

        return null;
    }

    public String getFirstSegmentUrl() {
        VideoSegment segment = getFirstSegment();
        if (segment == null || TextUtils.isEmpty(segment.url))
            return null;

        return segment.url;
    }

    public VideoSegment getSingleSegment() {
        if (mSegmentList.size() == 1)
            return mSegmentList.get(0);

        return null;
    }

    public String getSingleSegmentUrl() {
        VideoSegment segment = getSingleSegment();
        if (segment == null || TextUtils.isEmpty(segment.url))
            return null;

        return segment.url;
    }

    public long getBitrate() {
        long totalSize = 0;
        long totalDuration = 0;
        for (VideoSegment seg : mSegmentList) {
            totalSize += seg.size;
            totalDuration += seg.duration;
        }

        if (totalDuration <= 0 || totalSize <= 0)
            return mPseudoBitrate;

        return totalSize * 8 / (totalDuration / 1000);
    }

    public long getTotalDuration() {
        long totalDuration = 0;
        for (VideoSegment seg : mSegmentList) {
            totalDuration += seg.duration;
        }

        return totalDuration;
    }

    public int getOrderByTime(long msec) {
        int order = 0;

        boolean found = false;
        long endTime = 0;
        for (VideoSegment item : mSegmentList) {
            endTime += item.duration;
            if (msec < endTime) {
                found = true;
                break;
            }

            order += 1;
        }

        if (!found)
            return -1;

        return order;
    }

    public int getEndTime(int order) {
        return getStartTime(order + 1);
    }

    public int getStartTime(int order) {
        int startTime = 0;
        for (int i = 0; i < order; ++i) {
            if (order >= mSegmentList.size())
                break;

            VideoSegment segment = mSegmentList.get(i);
            startTime += segment.duration;
        }

        return startTime;
    }

}
