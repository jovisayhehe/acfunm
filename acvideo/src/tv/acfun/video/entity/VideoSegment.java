
package tv.acfun.video.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 视频分段
 * 
 * @author Yrom
 * 
 */
public class VideoSegment implements Parcelable {

    /**
     * 在视频分段列表中的索引
     */
    public int    num      = 0;
    /**
     * 分段持续时间 ms
     */
    public long    duration = 0;
    /**
     * 播放的url，可能是远程的，或者本地的
     */
    public String url      = null;
    /**
     * 用于下载的url（XXX 有些视频貌似播放和下载的不是同一个文件）
     */
    public String stream   = null;
    /**
     * -1，表示未读到有效数据。需要重新获取
     */
    public long   size     = -1;
    
    public String fileName = null;
    public String etag     = null;
    @Override
    public String toString() {
        return "Segment [num=" + num + ", url=" + url +"]";
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(num);
        dest.writeLong(duration);
        dest.writeString(url);
        dest.writeString(stream);
        dest.writeLong(size);
        dest.writeString(fileName);
        dest.writeString(etag);
    }
    public static final Parcelable.Creator<VideoSegment> CREATOR = new Creator<VideoSegment>() {
        @Override
        public VideoSegment[] newArray(int size) {
            
            return new VideoSegment[size];
        }
        
        @Override
        public VideoSegment createFromParcel(Parcel source) {
            VideoSegment s = new VideoSegment();
            s.num = source.readInt();
            s.duration = source.readLong();
            s.url = source.readString();
            s.stream = source.readString();
            s.size = source.readLong();
            s.fileName = source.readString();
            s.etag = source.readString();
            return s;
        }
    };
}
