
package tv.acfun.video.util.download;

import tv.acfun.video.entity.VideoPart;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 单个下载项 一个视频分p对应于一个下载项
 * 
 * @author Yrom
 * 
 */
public class DownloadEntry implements Parcelable {
    public String aid = "";
    public String title = "";
    public String destination = "";
    /**
     * 欲下载的视频分p
     */
    public VideoPart part;

    public DownloadEntry() {}

    public DownloadEntry(String aid, String title, VideoPart part) {
        this.aid = aid;
        this.title = title;
        this.part = part;
    }

    public static final Parcelable.Creator<DownloadEntry> CREATOR = new Creator<DownloadEntry>() {
        @Override
        public DownloadEntry[] newArray(int size) {
            return new DownloadEntry[size];
        }

        @Override
        public DownloadEntry createFromParcel(Parcel source) {
            DownloadEntry entry = new DownloadEntry();
            entry.part = source.readParcelable(getClass().getClassLoader());
            entry.aid = source.readString();
            entry.title = source.readString();
            entry.destination = source.readString();
            return entry;
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(part, flags);
        dest.writeString(aid);
        dest.writeString(title);
        dest.writeString(destination);
    }
}
