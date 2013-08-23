package tv.avfun.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Locale;

import tv.avfun.app.AcApp;

import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

public class FileUtil {
    public static Uri getLocalFileUri(File file){
        return Uri.fromFile(file);
    }
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
    public static String formatFileSize(long size) {
        if(size <=0) return "0B";
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (size < _1KB)
            fileSizeString = df.format((double) size) + "B";
        else if (size < _1MB)
            fileSizeString = df.format((double) size / _1KB) + "KB";
        else if (size < _1GB)
            fileSizeString = df.format((double) size / _1MB) + "MB";
        else
            fileSizeString = df.format((double) size / _1GB) + "GB";

        return fileSizeString;
    }

    /**
     * 显示SD卡剩余空间
     * 
     * @return SD卡不存在则返回null
     */
    public static String showFileAvailable() {
        long availableSize = getExternalAvailable();
        if (availableSize > 0)
            return formatFileSize(availableSize);
        return null;
    }

    /**
     * 获得SD卡剩余空间
     * 
     * @return SD卡未挂载则返回-1
     */
    public static long getExternalAvailable() {
        if (AcApp.isExternalStorageAvailable()) {
            StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            return availCount * blockSize;
        } else
            return -1;

    }
    /**
     *   "/" ~ "?"之间的".xxx"
     * @param url
     * @return
     */
    public static String getUrlExt(String url){
        
        if (!TextUtils.isEmpty(url)) {
//            int start = url.lastIndexOf('.');
            int start = url.lastIndexOf('/');
            int end = url.lastIndexOf('?');
            end = end <= start ? url.length() : end;
            String ext = "";
            if (start > 0 && start < url.length() - 1) {
                try{
                ext = url.substring(start, end).toLowerCase();
                
                return ext.substring(ext.lastIndexOf('.'));
                }catch (StringIndexOutOfBoundsException e) {
                   Log.e("Util", "when get url ext : "+url,e);
                }
            }
            
        }
        return "flv";
    }
    
    public static String guessVideoMimetype(String ext){
        String mimetype = null;
        if(".flv".equals(ext)){
            mimetype = "video/x-flv";
        }else if(".f4v".equals(ext)){
            mimetype = "video/x-f4v";
        }else if(".mp4".equals(ext)){
            mimetype = "video/mp4";
        }else mimetype = "video/*";
/*        else if(".hlv".equals(ext)){
            mimetype = "video/x-f4v"; // XXX: mimetype of hlv???
        }*/
        return mimetype;
    }
    public static final long _1KB = 1024;
    public static final long _1MB = _1KB * _1KB;
    public static final long _1GB = _1KB * _1MB;
    /**
     * @param type the http header, content-type
     * @return
     */
    public static String getMimeType(String type) {
        if (type == null) {
            return null;
        }

        type = type.trim().toLowerCase(Locale.US);

        final int semicolonIndex = type.indexOf(';');
        if (semicolonIndex != -1) {
            type = type.substring(0, semicolonIndex);
        }
        return type;
    }
    public static String getName(String url) {
        if (!TextUtils.isEmpty(url)) {
          int start = url.lastIndexOf('/');
          int end = url.lastIndexOf('?');
          end = end <= start ? url.length() : end;
          String name = "";
          if (start > 0 && start < url.length() - 1) {
              try{
              name = url.substring(start, end).toLowerCase();
              return name;
              }catch (StringIndexOutOfBoundsException e) {
                 Log.e("Util", "when get url name : "+url,e);
              }
          }
        }
        return String.valueOf(url.hashCode())+".jpg";
    }
}
