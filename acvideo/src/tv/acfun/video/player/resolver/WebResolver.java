
package tv.acfun.video.player.resolver;

import tv.acfun.video.player.MediaList;
import android.content.Context;

public class WebResolver extends BaseResolver{
    public WebResolver(String vid) {
        super(vid);
    }

    @Override
    public void resolve(Context context) {
    }

    @Override
    public void resolveAsync(Context context) {
        mHandler.sendEmptyMessage(ARG_ERROR);
    }

    @Override
    public MediaList getMediaList(int resolution) {
        return null;
    }

}
