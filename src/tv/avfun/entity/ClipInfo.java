package tv.avfun.entity;

import tv.acfundanmaku.defines.Defines;

public class ClipInfo {
	public long DownloadId = -1;
	public String DownloadPath = "";
	public long DownloadSize = 0;
	public int DownloadStatus = Defines.DOWNLOAD_EMPTY;
	public long Duration = 0;
	public String Url = "";
	
	public ClipInfo (long duration)
	{
		Duration = duration;
	}
	
	public ClipInfo (String url)
	{
		Url = url;
	}
	
	public ClipInfo (String url, long duration)
	{
		Url = url;
		Duration = duration;
	}
	
	public ClipInfo (String url, long duration, String path, int status, long size)
	{
		Url = url;
		Duration = duration;
		DownloadPath = path;
		DownloadStatus = status;
		DownloadSize = size;
	}

	public long getDownloadId() {
		return DownloadId;
	}

	public void setDownloadId(long downloadId) {
		DownloadId = downloadId;
	}

	public String getDownloadPath() {
		return DownloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		DownloadPath = downloadPath;
	}

	public long getDownloadSize() {
		return DownloadSize;
	}

	public void setDownloadSize(long downloadSize) {
		DownloadSize = downloadSize;
	}

	public int getDownloadStatus() {
		return DownloadStatus;
	}

	public void setDownloadStatus(int downloadStatus) {
		DownloadStatus = downloadStatus;
	}

	public long getDuration() {
		return Duration;
	}

	public void setDuration(long duration) {
		Duration = duration;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}
	
	
}
