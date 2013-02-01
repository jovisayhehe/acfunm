package tv.avfun.entity;

import java.util.ArrayList;

import tv.acfundanmaku.defines.Defines;

public class VideoListItem {
	
	public int APIId = 0;
	public long ClipDurationCount = 0;
	public String Danmaku = "";
	public String DanmakuId = "";
	public ArrayList<ClipInfo> FLV = new ArrayList<ClipInfo>();
	public String FormatFLV = "novideo";
	public String FormatMP4 = "novideo";
	public ArrayList<ClipInfo> MP4 = new ArrayList<ClipInfo>();
	public String Type = "";
	public String Vid = "";
	public String VideoName = "";
	
	public String getFormat(int ParseMode)
	{
		switch (ParseMode)
		{
			case Defines.PARSE_MP4:
				return FormatMP4;
			case Defines.PARSE_FLV:
				return FormatFLV;
		}
		
		return "novideo";
	}

	public int getAPIId() {
		return APIId;
	}

	public void setAPIId(int aPIId) {
		APIId = aPIId;
	}

	public long getClipDurationCount() {
		return ClipDurationCount;
	}

	public void setClipDurationCount(long clipDurationCount) {
		ClipDurationCount = clipDurationCount;
	}

	public String getDanmaku() {
		return Danmaku;
	}

	public void setDanmaku(String danmaku) {
		Danmaku = danmaku;
	}

	public String getDanmakuId() {
		return DanmakuId;
	}

	public void setDanmakuId(String danmakuId) {
		DanmakuId = danmakuId;
	}

	public ArrayList<ClipInfo> getFLV() {
		return FLV;
	}

	public void setFLV(ArrayList<ClipInfo> fLV) {
		FLV = fLV;
	}

	public String getFormatFLV() {
		return FormatFLV;
	}

	public void setFormatFLV(String formatFLV) {
		FormatFLV = formatFLV;
	}

	public String getFormatMP4() {
		return FormatMP4;
	}

	public void setFormatMP4(String formatMP4) {
		FormatMP4 = formatMP4;
	}

	public ArrayList<ClipInfo> getMP4() {
		return MP4;
	}

	public void setMP4(ArrayList<ClipInfo> mP4) {
		MP4 = mP4;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getVid() {
		return Vid;
	}

	public void setVid(String vid) {
		Vid = vid;
	}

	public String getVideoName() {
		return VideoName;
	}

	public void setVideoName(String videoName) {
		VideoName = videoName;
	}
	
	
}
