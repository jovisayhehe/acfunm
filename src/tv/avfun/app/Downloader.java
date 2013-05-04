
package tv.avfun.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.avfun.api.net.UserAgent;
import tv.avfun.db.DBService;
import tv.avfun.entity.VideoInfo.VideoItem;
import tv.avfun.util.ArrayUtil;
import tv.avfun.util.FileUtil;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

@TargetApi(9)
public class Downloader {

    private static DownloadManager downloadMan;
    /**
     * 
     * @param context
     * @param aid
     * @param item
     * @param downloadHandler
     * @return  补完downloadIDs的video item 
     */
    public static VideoItem enqueue(Context context, String aid, VideoItem item, DownloadHandler downloadHandler) {
        if(item == null || item.urlList == null || item.urlList.isEmpty()) throw new IllegalArgumentException("item 验证不通过");
        
        File file = AcApp.getDownloadPath(aid, item.vid);
        file.mkdirs();
        if (downloadMan == null)
            downloadMan = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        item.downloadIDs = new ArrayList<Long>();
        for (int i = 0; i < item.urlList.size(); i++) {
            
            String url = item.urlList.get(i);
            String filename = i + FileUtil.getUrlExt(url);
            if("tudou".equals(item.vtype)){ // tudou 视频地址比较神奇
                filename = i+".f4v";
            }
            Request request = new Request(Uri.parse(url));
            // TODO 让用户选择是否为wifi下载
            request.setAllowedNetworkTypes(Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setDescription(item.subtitle);
                
            int len = 10;
            if (item.subtitle.length() < len)
                len = item.subtitle.length();
            request.setTitle(item.subtitle.substring(0, len) + "_" + filename)
                    .addRequestHeader("User-Agent", UserAgent.DEFAULT)
                    .setDestinationInExternalPublicDir("Download/AcFun/Videos/" + aid + "/" + item.vid, filename);
            item.downloadIDs.add(downloadMan.enqueue(request));
            
        }
        new DBService(context).addDownload(aid, item);
        downloadHandler.obtainMessage(DownloadHandler.DOWNLOAD_START,item.vid).sendToTarget();
        return item;
    }
    public static abstract class DownloadHandler extends Handler {

        public static final int DOWNLOAD_SUCCESS = 1;
        public static final int DOWNLOAD_FAIL    = 2;
        public static final int DOWNLOAD_START   = 3;
        public static final int DOWNLOAD_STOP    = 3;

        @Override
        public abstract void handleMessage(Message msg);
    }
    public static void removeDownload(Context context,String vid) {
        if (downloadMan == null)
            downloadMan = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        List<Long> downloadIds = new DBService(context).getDownloadIds(vid);
        long[] array = ArrayUtil.toLongArray(downloadIds);
        
        if(array != null){
            int n = downloadMan.remove(array);
            if(n>0){
                Toast.makeText(context, "删除成功", 0).show();
            }
        }
        new DBService(context).removeDownload(vid);
    }
    public static boolean isDownloaded(Context context,String vid){
        List<Long> downloadIds = new DBService(context).getDownloadIds(vid);
        if(downloadIds == null || downloadIds.isEmpty()) return false;
        String whereClause = null;
        
        String[] whereArgs = null; 
        if(downloadIds.size()>0) {
            whereArgs = getWhereArgsForIds(downloadIds);
            whereClause = getWhereClauseForIds(downloadIds);
        }
        Cursor query = context.getContentResolver().query(DownloadService.CONTENT, null, 
                whereClause,whereArgs,null);
        boolean b = false;
        if(query!=null) {
            // 验证完整性
            if(query.getCount() == downloadIds.size()){
                b = true;
                while(query.moveToNext()){
                    int status = query.getInt(query.getColumnIndex("status"));
                    // 验证状态
                    b &= status == DownloadService.STATUS_SUCCESS;
                }
                
            }
            query.close();
        }
        return b;
        
    }
    public static boolean isDownloading(Context context,String vid){
        List<Long> downloadIds = new DBService(context).getDownloadIds(vid);
        if(downloadIds == null || downloadIds.isEmpty()) return false;
        String whereClause = null;
        
        String[] whereArgs = null; 
        if(downloadIds.size()>0) {
            whereArgs = getWhereArgsForIds(downloadIds);
            whereClause = getWhereClauseForIds(downloadIds);
        }
        Cursor query = context.getContentResolver().query(DownloadService.CONTENT, null, 
                whereClause,whereArgs,null);
        boolean b = false;
        if(query!=null) {
            if(query.getCount() == downloadIds.size()){
               while(query.moveToNext()){
                    int status = query.getInt(query.getColumnIndex("status"));
                    b |= status >= DownloadService.STATUS_PENDING && status <= DownloadService.STATUS_PAUSED;
                }
            }
            query.close();
        }
        return b;
    }
    
    
    public static String[] getWhereArgsForIds(List<Long> ids) {
        String[] whereArgs = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            whereArgs[i] = ids.get(i).toString();
        }
        return whereArgs;
    }
    
    public static String getWhereClauseForIds(List<Long> ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append("_id");
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

}
