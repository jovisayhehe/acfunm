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
		addPreferencesFromResource(R.xml.preferences);
		// share
		findPreference("share").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享~");  
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content)); 
                startActivity(Intent.createChooser(intent, "分享给好友"));
                return true;
            }
        });
		// about
		findPreference("about").setSummary("v"+app.getVersionName());
		// comment
		findPreference("comment").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id="+getPackageName()));
                    startActivity(intent);
                }catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "你的好意我已心领=。=", Toast.LENGTH_SHORT).show();
                    feedBack();
                }
                return true;
            }
        });
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
		// mail
		findPreference("feedback").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				feedBack();
				return false;
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
		// 匿名版
		findPreference("hfun").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try{
                    ComponentName cmp = new ComponentName("acfunh.yoooo.org", "acfunh.yoooo.org.MainActivity");
                    if(getPackageManager().getActivityInfo(cmp, 0)!= null){
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.addCategory("android.intent.category.LAUNCHER");
                        intent.setComponent(cmp);
                        startActivity(intent);
                    }
                    
                } catch (Exception e) {
                    if(BuildConfig.DEBUG) Log.e("Setting", "打开不能",e);
                    try{
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=acfunh.yoooo.org"));
                        startActivity(intent);
                    }
                    catch (Exception ex) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.acfunh)));
                        startActivity(intent);
                    
                    }
                }
                return true;
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
