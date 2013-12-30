/*
 * Copyright (C) 2013 YROM.NET
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

package tv.acfun.video.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * {
      "videoId": 779819,
      "sourceId": "119196848",
      "type": "sina",
      "commentId": "119196848"
    }
 * @author Yrom
 *
 */
public class VideoPart implements Parcelable{
    public long videoId;
    public String sourceId;
    public String type;
    public String commentId;
    
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(videoId);
        dest.writeString(sourceId);
        dest.writeString(type);
        dest.writeString(commentId);
        
    }
    public static final Parcelable.Creator<VideoPart> CREATOR = new Creator<VideoPart>() {
        @Override
        public VideoPart[] newArray(int size) {
            
            return new VideoPart[size];
        }
        
        @Override
        public VideoPart createFromParcel(Parcel source) {
            VideoPart part = new VideoPart();
            part.videoId = source.readLong();
            part.sourceId = source.readString();
            part.type = source.readString();
            part.commentId = source.readString();
            return part;
        }
    };
}
