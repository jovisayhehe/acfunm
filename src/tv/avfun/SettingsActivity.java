package tv.avfun;

import tv.avfun.app.AcApp;
import tv.avfun.util.lzlist.FileCache;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;

@SuppressWarnings("deprecation")
public class SettingsActivity  extends SherlockPreferenceActivity{
	private CheckBoxPreference cbpf;
    private AcApp app;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (AcApp) getApplicationContext();
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    getListView().setFooterDividersEnabled(false);
		addPreferencesFromResource(R.xml.preferences);

		// cache
		final Preference imgCache = findPreference("clear_imgcache");
		String size = FileCache.getCacheSize();
		imgCache.setSummary(size==null?"暂无缓存":size);
		imgCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				preference.setEnabled(false);
				if(FileCache.clear()){
				    Toast.makeText(getApplicationContext(), "清除完毕", 0).show();
				    imgCache.setSummary("暂无缓存");
				}
				
				return true;
			}
		});

		// ex play
		cbpf = (CheckBoxPreference) findPreference("ex_palyer");
		cbpf.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				if(cbpf.isChecked()){
				    AcApp.putInt("playmode", 1);
				}else{
				    AcApp.putInt("playmode", 0);
				}
				
				return false;
			}
		});

	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void feedBack() {
        UMFeedbackService.openUmengFeedbackSDK(SettingsActivity.this);
        UMFeedbackService.setGoBackButtonVisible();
    }
}
