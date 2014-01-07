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

/**
 * @author Yrom
 *
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener {
    public static void start(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
    private static String kEY_CLEAR_CACHE;
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        kEY_CLEAR_CACHE = getString(R.string.key_clear_cache);
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
        }
        return false;
    }
}
