package tv.avfun.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import tv.avfun.entity.VideoInfo;


public class Downloader {
    public static VideoInfo getDownloadedVideo(String aid, String vid){
        VideoInfo info = new VideoInfo();
        if(!hasDownload(aid, vid)) return null;
        info.vid = vid;
        File[] files = AcApp.getDownloadPath(aid, vid).listFiles();
        info.files = new ArrayList<String>();
        for(int i=0;i<files.length;i++){
            info.files.add(files[i].getAbsolutePath());
        }
        return info;
    }
    public static boolean hasDownload(String aid, String vid){
        
        File path = AcApp.getDownloadPath(aid, vid);
        File[] list = path.listFiles();
        return list != null && list.length >0;
    }
    
}
