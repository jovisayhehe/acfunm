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

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;

/**
 * @author Yrom
 * 
 */
public class MediaSegmentPlayerSys extends MediaPlayer implements IMediaSegmentPlayer {
    private MediaSegment mSegment;

    public MediaSegmentPlayerSys(MediaSegment segment) {
        mSegment = segment;
    }

    @Override
    public long getAbsolutePosition() {
        return mSegment.mStartTime + super.getCurrentPosition();
    }
    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException,
            IllegalStateException {
        if(uri == null) uri = Uri.parse(mSegment.mUrl);
        super.setDataSource(context, uri);
    }
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException {
        if(uri == null) uri = Uri.parse(mSegment.mUrl);
        super.setDataSource(context, uri, headers);
    }

    @Override
    public int getDuration() {
        if (mSegment.mDuration > 0) return (int) mSegment.mDuration;
        return super.getDuration();
    }

    @Override
    public boolean hasDataSource() {
        if (mSegment == null || TextUtils.isEmpty(mSegment.mUrl)) return false;
        return true;
    }

    @Override
    public boolean isSameMediaItem(MediaSegment mediaItem) {
        if (!hasDataSource()) return false;
        if (mSegment.mOrder != mediaItem.mOrder) return false;
        return true;
    }

    @Override
    public int getOrder() {
        return mSegment.mOrder;
    }
}
