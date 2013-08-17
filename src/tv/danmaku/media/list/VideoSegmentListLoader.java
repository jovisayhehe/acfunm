package tv.danmaku.media.list;

import tv.ac.fun.BuildConfig;
import tv.avfun.api.ApiParser;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import tv.danmaku.media.PlayIndex;
import android.util.Log;


public class VideoSegmentListLoader{

    private static final String TAG = "VideoSegmentListLoader";
    private VideoPart part;
    private PlayIndex mIndex;

    public VideoSegmentListLoader(VideoPart part) {
        this.part = part;
    }
    
    public boolean loadIndex(boolean b) {
        if(!part.isDownloading && !part.isDownloaded || b){
            if(BuildConfig.DEBUG)
                Log.i(TAG, "parsing parts for " + part.vtype + part.vid);
            ApiParser.parseVideoParts(part,AcApp.getParseMode());
        }
        
        return part.segments != null && !part.segments.isEmpty();
    }

    public PlayIndex getPlayIndex() {
        if(mIndex == null && part.segments != null && !part.segments.isEmpty())
            mIndex = new PlayIndex(part);
        return mIndex;
    }

   
}
