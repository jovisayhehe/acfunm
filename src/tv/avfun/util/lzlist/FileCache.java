package tv.avfun.util.lzlist;

import java.io.File;

import tv.avfun.app.AcApp;
import tv.avfun.util.FileUtil;
/*import android.content.Context;*/

public final class FileCache {
    private static File cacheDir;
    static {
        AcApp app = AcApp.instance();
        if(AcApp.isExternalStorageAvailable())
            cacheDir = AcApp.getExternalCacheDir(AcApp.IMAGE);
        else cacheDir = app.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    
    public static File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
        
    }
    /*** 获取缓存文件夹大小 */
    public static String getCacheSize(){
        long size = FileUtil.getFolderSize(cacheDir);
        if(size == 0) return null;
        return FileUtil.formatFileSize(size);
    }
    public static boolean clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return false;
        boolean b = false;
        for(File f:files)
            b |= f.delete();
        return b;
    }

}