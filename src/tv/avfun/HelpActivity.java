
package tv.avfun;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

public class HelpActivity extends SherlockPreferenceActivity {

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getListView().setFooterDividersEnabled(false);
        addPreferencesFromResource(R.xml.help_pref);

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
        // findPreference("about").setSummary("v"+app.getVersionName());
        // update
        Preference update = findPreference("update");
        update.setSummary("v" + AcApp.instance().getVersionName());
        update.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                UmengUpdateAgent.setUpdateAutoPopup(false);
                UmengUpdateAgent.setUpdateOnlyWifi(true);
                UmengUpdateAgent.setUpdateListener(updateListener);
                UmengUpdateAgent.update(HelpActivity.this);
                return false;
            }
        });
        // comment
        findPreference("comment").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "你的好意我已心领=。=", Toast.LENGTH_SHORT).show();
                    feedBack();
                }
                return true;
            }
        });
        // 匿名版
        findPreference("hfun").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    ComponentName cmp = new ComponentName("acfunh.yoooo.org", "acfunh.yoooo.org.MainActivity");
                    if (getPackageManager().getActivityInfo(cmp, 0) != null) {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.addCategory("android.intent.category.LAUNCHER");
                        intent.setComponent(cmp);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    if (BuildConfig.DEBUG)
                        Log.e("Setting", "打开不能", e);
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=acfunh.yoooo.org"));
                        startActivity(intent);
                    } catch (Exception ex) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.acfunh)));
                        startActivity(intent);

                    }
                }
                return true;
            }
        });
        mFaqView = new WebView(getApplicationContext());
        mFaqView.setWebViewClient(new WebViewClient());
        mFaqView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = mFaqView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NORMAL);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUserAgentString(UserAgent.MY_UA);
        dialog = new AlertDialog.Builder(HelpActivity.this).setView(mFaqView).setCancelable(true)
                .setNegativeButton("OK", null).create();
        findPreference("faq").setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mFaqView.loadUrl(getString(R.string.faq_url));
                dialog.show();
                return false;
            }
        });
        findPreference("licence").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mFaqView.loadUrl(getString(R.string.licence_url));
                dialog.show();
                return false;
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
    }

    private WebView             mFaqView;
    private UmengUpdateListener updateListener = new UmengUpdateListener() {

                                                   @Override
                                                   public void onUpdateReturned(int updateStatus,
                                                           UpdateResponse updateInfo) {
                                                       switch (updateStatus) {
                                                       case 0: // has update
                                                           UmengUpdateAgent.showUpdateDialog(HelpActivity.this,
                                                                   updateInfo);
                                                           break;
                                                       case 1: // has no update
                                                           Toast.makeText(HelpActivity.this, "已是最新版",
                                                                   Toast.LENGTH_SHORT).show();
                                                           break;
                                                       case 2: // none wifi
                                                           Toast.makeText(HelpActivity.this, "没有wifi连接， 只在wifi下更新",
                                                                   Toast.LENGTH_SHORT).show();
                                                           break;
                                                       case 3: // time out
                                                           Toast.makeText(HelpActivity.this, "超时", Toast.LENGTH_SHORT)
                                                                   .show();
                                                           break;
                                                       }

                                                   }
                                               };
    private AlertDialog         dialog;

    private void feedBack() {
        UMFeedbackService.openUmengFeedbackSDK(this);
        UMFeedbackService.setGoBackButtonVisible();
    }

    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
