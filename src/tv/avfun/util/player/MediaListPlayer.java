package tv.avfun.util.player;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import tv.avfun.entity.VideoPart;
import tv.avfun.entity.VideoSegment;

public class MediaListPlayer implements Callback {
    private VideoPart mPart;
    private Handler mHandler;
    private PlayList mPlayList;
    public MediaListPlayer() {
        mHandler = new Handler(this);
    }
    
    public void setVideoPart(VideoPart part){
        mPart = part;
        if(part.segments == null || part.segments.isEmpty()){
            VideoSegmentsLoader.loadVideoSegments(mPart, mHandler);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        
        switch (msg.what) {
        case VideoSegmentsLoader.LOAD_OK:
            mPlayList = new PlayList(mPart.segments);
            break;

        default:
            // TODO : error
            break;
        }
        
        return true;
    }
    
    
    static class PlayList{
        
        ArrayList<VideoSegment> mSegments;
        
        PlayList(ArrayList<VideoSegment> segments) {
            mSegments = segments;
        }

        public long getTotalDuration(){
            long total = 0;
            for(VideoSegment s : mSegments){
                total += s.duration;
            }
            return total;
        }
        
        public int getStartTime(int index){
            int startTime = 0;
            for (int i = 0; i < index; ++i) {
                if (index >= mSegments.size())
                    break;
                VideoSegment segment = mSegments.get(i);
                startTime += segment.duration;
            }

            return startTime;
        }
        
        public int getIndexByTime(long msec){
            long endTime = 0;
            for(int i= 0;i<mSegments.size();i++){
                endTime += mSegments.get(i).duration;
                if(endTime > msec){
                    return i;
                }
            }
            return -1;
        }
        
    }
}
