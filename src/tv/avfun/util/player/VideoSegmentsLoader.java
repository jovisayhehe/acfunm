package tv.avfun.util.player;

import tv.ac.fun.BuildConfig;
import tv.avfun.api.ApiParser;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoPart;
import android.os.Handler;
import android.util.Log;

public final class VideoSegmentsLoader{
    public static final int LOAD_OK = 200;
    public static final int LOAD_ERROR = 400;
    private static final String TAG = VideoSegmentsLoader.class.getSimpleName();
    
    public static void loadVideoSegments(final VideoPart part, final Handler handler){
        new Thread(){
            public void run() {
                if(!part.isDownloading && !part.isDownloaded){
                    if(BuildConfig.DEBUG)
                        Log.i(TAG, "parsing parts for " + part.vtype + part.vid);
                    
                    ApiParser.parseVideoParts(part,AcApp.getParseMode());
                }
                if(part.segments != null && !part.segments.isEmpty())
                    handler.sendEmptyMessage(LOAD_OK);
                else
                    handler.sendEmptyMessage(LOAD_ERROR);
            }
            
        }
        .start();
        
        
    }

}
