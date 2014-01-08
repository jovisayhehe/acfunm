/*
 * Copyright (C) 2014 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video;

import tv.acfun.video.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

/**
 * @author Yrom
 *
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {
    public static void start(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
    private static String KEY_FEED_BACK;
    private static String kEY_CLEAR_CACHE;
    private static String KEY_UPDATE;
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        kEY_CLEAR_CACHE = getString(R.string.key_clear_cache);
        KEY_FEED_BACK = getString(R.string.key_feedback);
        KEY_UPDATE = getString(R.string.key_update);
        findPreference(KEY_FEED_BACK).setOnPreferenceClickListener(this);
        Preference update = findPreference(KEY_UPDATE);
        update.setSummary("v"+AcApp.instance().getVersionName());
        update.setOnPreferenceClickListener(this);
        
        setCache();
        
    }
    private void setCache() {
        Preference cache = findPreference(kEY_CLEAR_CACHE);
        String size = "SD卡未挂载";
        if(AcApp.isExternalStorageAvailable()){
            size = FileUtil.getFormatFolderSize(getExternalCacheDir());
            cache.setOnPreferenceClickListener(this);
        }else{
            cache.setEnabled(false);
        }
        cache.setSummary(size);
    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (kEY_CLEAR_CACHE.equals(preference.getKey())) {
            preference.setEnabled(false);
            if (FileUtil.deleteFiles(getExternalCacheDir()))
                preference.setSummary("清除完毕");
            else
                Toast.makeText(getApplicationContext(), "清除失败", 0).show();
            return true;
        }else if (KEY_FEED_BACK.equals(preference.getKey())) {
            startActivity(new Intent(this, ConversationActivity.class));
        }else if (KEY_UPDATE.equals(preference.getKey())) {
            preference.setEnabled(false);
            update();
        }
        return false;
    }
    
    private void update() {
        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                switch (updateStatus) {
                case 0: // has update
                    UmengUpdateAgent.showUpdateDialog(SettingsActivity.this, updateInfo);
                    break;
                case 1: // has no update
                    Toast.makeText(SettingsActivity.this, "已是最新版", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // none wifi
                    Toast.makeText(SettingsActivity.this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 3: // time out
                    Toast.makeText(SettingsActivity.this, "超时", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

        });
    }
    protected void onDestroy() {
        super.onDestroy();
        UmengUpdateAgent.setUpdateListener(null);
        UmengUpdateAgent.setDownloadListener(null);
        UmengUpdateAgent.setDialogListener(null);
        UmengUpdateAgent.setUpdateAutoPopup(true);
    }
}
