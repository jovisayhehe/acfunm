package tv.avfun.util.player;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.utils.FileUtils;
import io.vov.vitamio.utils.Log;

import java.io.IOException;

import tv.avfun.entity.VideoSegment;
import android.content.Context;

public class MediaSegmentPlayer extends MediaPlayer {
    
    public MediaSegmentPlayer(Context ctx) {
        super(ctx);
    }

    public MediaSegmentPlayer(Context ctx, boolean preferHWDecoder) {
        super(ctx, preferHWDecoder);
    }
    
    private int mIndex;
    private long mStartTime;
    private VideoSegment mSegment;
    
    
    
    @Override
    public long getCurrentPosition() {
        if(mSegment == null)
            return -1;
        return this.mStartTime + super.getCurrentPosition();
    }
    
    
    public int getIndex(){
        return mIndex;
    }
    
    public VideoSegment getSegment(){
        return mSegment;
    }
    
    public long getStartTime(){
        return mStartTime;
    }
    
    public final void setSegment(int index, long startTime, VideoSegment segment,String headers) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
        this.mIndex = index;
        this.mSegment = segment;
        this.mStartTime = startTime;
        
        Log.i("set segment[%d] %d(%d): %s ", index, startTime,segment.duration,segment.url);
        
        if (segment.url.startsWith("file://")) {
          setDataSource(FileUtils.getPath(segment.url));
          return;
        }
        _setDataSource(segment.url, headers);
    }
}
