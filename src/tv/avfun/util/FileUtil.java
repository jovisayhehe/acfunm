package tv.avfun.util;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtil {

    public static long getFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                size = size + getFolderSize(files[i]);
            else
                size = size + files[i].length();
        }
        return size;
    }

    /*** 格式化文件大小(xxx.xx B/KB/MB/GB) */
    public static String formetFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (size < _1KB) 
            fileSizeString = df.format((double) size) + "B";
        else if (size < _1MB) 
            fileSizeString = df.format((double) size / 1024) + "KB";
        else if (size < _1GB) 
            fileSizeString = df.format((double) size / 1048576) + "MB";
        else 
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        
        return fileSizeString;
    }

    public static final long _1KB = 1024;
    public static final long _1MB = _1KB * _1KB;
    public static final long _1GB = _1KB * _1MB;
}
