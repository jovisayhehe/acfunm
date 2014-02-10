/*
 * Copyright (C) 2014 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.player;

import io.vov.vitamio.MediaPlayer;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Yrom
 *
 */
public class MediaSegmentPlayer extends MediaPlayer implements IMediaSegmentPlayer{
    private MediaSegment mSegment;
    public MediaSegmentPlayer(Context ctx, boolean preferHWDecoder,MediaSegment segment) {
        super(ctx, preferHWDecoder);
        mSegment = segment;
    }

    public MediaSegmentPlayer(Context ctx, MediaSegment segment) {
        this(ctx,false,segment);
    }
    
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException {
        if(uri == null) uri = Uri.parse(mSegment.mUrl);
        super.setDataSource(context, uri, headers);
    }

    public long getAbsolutePosition(){
        return mSegment.mStartTime + super.getCurrentPosition();
    }
    
    @Override
    public long getDuration() {
        if(mSegment.mDuration >0 ) return mSegment.mDuration;
        return super.getDuration();
    }
    public boolean hasDataSource() {
        if (mSegment == null || TextUtils.isEmpty(mSegment.mUrl))
            return false;

        return true;
    }
    
    public boolean isSameMediaItem(MediaSegment mediaItem) {
        if (!hasDataSource())
            return false;

        if (mSegment.mOrder != mediaItem.mOrder)
            return false;

        return true;
    }

    public int getOrder() {
        return mSegment.mOrder;
    }
}
