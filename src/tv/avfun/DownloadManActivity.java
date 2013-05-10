package tv.avfun;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DownloadManActivity extends SherlockActivity {
    private DownloadManager mDownloadMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadMan = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Query query = new Query();
       // query.setFilterById(ids);
        mDownloadMan.query(query);
    }
}
