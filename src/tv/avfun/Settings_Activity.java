package tv.avfun;

import tv.avfun.util.lzlist.FileCache;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.util.Log;

public class Settings_Activity  extends SherlockPreferenceActivity{
	private SharedPreferences sharedata;
	private CheckBoxPreference cbpf;
//	private ListPreference video_source_list;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
	    ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.preferences);
		getListView().setSelector(getResources().getDrawable(R.drawable.selectable_background));
		
		PackageManager manager = this.getPackageManager();
        PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			findPreference("version_name").setSummary(info.versionName);
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
		}
		findPreference("clear_imgcache").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				preference.setEnabled(false);
				FileCache fileCache = new FileCache(Settings_Activity.this);
				fileCache.clear();
				
				return false;
			}
		});
		findPreference("sendmail").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				UMFeedbackService.openUmengFeedbackSDK(Settings_Activity.this);
				return false;
			}
		});
		
		cbpf = (CheckBoxPreference) findPreference("ex_palyer");
		cbpf.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				sharedata = getSharedPreferences("playmode", 0);
				if(cbpf.isChecked()){
					sharedata.edit().putInt("mode", 1).commit();
				}else{
					sharedata.edit().putInt("mode", 0).commit();
				}
				
				return false;
			}
		});
		
		
//		video_source_list = (ListPreference) findPreference("video_source");
//		video_source_list.setDefaultValue(1);
//		video_source_list.setSummary(video_source_list.getEntry());
//		video_source_list.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//			
//			@Override
//			public boolean onPreferenceChange(Preference preference, Object newValue) {
//				
//				String value = newValue.toString();
//				sharedata = getSharedPreferences("video_source", 1);
//				if(value.equals("1")){
//					video_source_list.setValue("1");
//					video_source_list.setSummary("FLV");
//					sharedata.edit().putInt("source", 1).commit();
//
//				}else if(value.equals("2")){
//					video_source_list.setValue("2");
//					video_source_list.setSummary("MP4");
//					sharedata.edit().putInt("source", 2).commit();
//				}
//				return false;
//			}
//		});
		
		
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
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
