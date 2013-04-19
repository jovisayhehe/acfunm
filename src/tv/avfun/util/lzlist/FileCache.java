package tv.avfun.util.lzlist;

import java.io.File;

import tv.avfun.AcApp;
/*import android.content.Context;*/

public final class FileCache {
    private static File cacheDir;
    static {
        AcApp app = AcApp.instance();
        if(AcApp.isExternalStorageAvailable())
            cacheDir = app.getExternalCacheDir();
        else cacheDir = app.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    /*public FileCache(Context context){
        //Find the dir to save cached images
        
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Android/data/tv.avfun/cache/imgcache");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }*/
    
    public static File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;
        
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