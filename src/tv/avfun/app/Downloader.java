
package tv.avfun.app;

import java.util.List;

import tv.avfun.db.DBService;
import tv.avfun.entity.VideoInfo.VideoItem;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
@TargetApi(9)
public class Downloader {

    public static boolean isDownloaded(Context context,String vid){
        List<Long> downloadIds = new DBService(context).getDownloadIds(vid);
        if(downloadIds == null || downloadIds.isEmpty()) return false;
        String whereClause = null;
        
        String[] whereArgs = null; 
        if(downloadIds.size()>0) {
            whereArgs = getWhereArgsForIds(downloadIds);
            whereClause = getWhereClauseForIds(downloadIds);
        }
        Cursor query = context.getContentResolver().query(DownloadService.CONTENT_URI, null, 
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
        Cursor query = context.getContentResolver().query(DownloadService.CONTENT_URI, null, 
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
    public static VideoItem getItemByVid(Context context, String vid){
        return new DBService(context).getDownloadedItemById(vid);
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
