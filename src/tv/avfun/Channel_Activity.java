package tv.avfun;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS;

import java.util.ArrayList;

import tv.avfun.api.ChannelApi;
import tv.avfun.fragment.Channel_Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.umeng.analytics.MobclickAgent;

public class Channel_Activity extends SherlockFragmentActivity implements OnPageChangeListener,TabListener{
	private int gdposition;
	public static boolean isarticle;
	private ActionBar ab;
	ViewPager mPager;
	ArrayList<String[]> apis;
	String[] titles;
	String[] urls;
	private static final int MODEID = 500;
	private static final int MIX = 501;
	private static final int PIC = 502;
	private static final int NOPIC = 503;
	private SharedPreferences sharedata;
	public static int modecode = 0;
	private SubMenu subMenu1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		   gdposition = getIntent().getIntExtra("position", -1);
		   isarticle = getIntent().getBooleanExtra("isarticle", false);
	        if(isarticle){
	        	sharedata = getSharedPreferences("viewmode", 0);
	        	modecode = sharedata.getInt("mode", 0);
	        }
		   ab = getSupportActionBar();
		   ab.setDisplayHomeAsUpEnabled(true);
		   mPager = (ViewPager)findViewById(R.id.pager);
		   mPager.setOffscreenPageLimit(5);
	        mPager.setOnPageChangeListener(this);
		    initTab(gdposition);
		    getSupportActionBar().setTitle(titles[0]);
	        mPager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));
	        
	}
	
	private void initTab(int pos){
		
		apis = ChannelApi.getApi(pos);
		titles = apis.get(0);
		urls = apis.get(1);
		
		for(int i=0;i<titles.length;i++){
			ab.addTab(ab.newTab().setText(titles[i]).setTabListener(this));
		}
		if(titles.length<=1){
			ab.removeAllTabs();
		}else{
			ab.setNavigationMode(NAVIGATION_MODE_TABS);
		}

	}
	
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		

	if(isarticle){
        subMenu1 = menu.addSubMenu("阅读模式");
        subMenu1.add(1, Channel_Activity.MIX, 1, "图文").setIcon(R.drawable.mode_mix);
        subMenu1.add(1, Channel_Activity.NOPIC, 1, "文本").setIcon(R.drawable.mode_article);
        subMenu1.add(1, Channel_Activity.PIC, 1, "漫画").setIcon(R.drawable.mode_picture);
        MenuItem subMenu1Item = subMenu1.getItem();
		switch (modecode) {
		case 0:
			subMenu1.setIcon(R.drawable.mode_mix);
		        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			break;
		case 1:
			subMenu1.setIcon(R.drawable.mode_article);
		        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			
			break;
		case 2:
			subMenu1.setIcon(R.drawable.mode_picture);
		        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
			
			break;

		default:
			break;
		}
	     
	}
      
		return super.onCreateOptionsMenu(menu);
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		sharedata = getSharedPreferences("viewmode", 0);
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case Channel_Activity.MIX:
			modecode = 0;
			subMenu1.setIcon(R.drawable.mode_mix);
			Toast.makeText(this, "图文模式", Toast.LENGTH_SHORT).show();
			sharedata.edit().putInt("mode", 0).commit();
			break;
		case Channel_Activity.NOPIC:
			modecode = 1;
			subMenu1.setIcon(R.drawable.mode_article);
			Toast.makeText(this, "文本模式", Toast.LENGTH_SHORT).show();
			sharedata.edit().putInt("mode", 1).commit();;
			break;
		case Channel_Activity.PIC:
			modecode = 2;
			subMenu1.setIcon(R.drawable.mode_picture);
			Toast.makeText(this, "漫画模式", Toast.LENGTH_SHORT).show();
			sharedata.edit().putInt("mode", 2).commit();;
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		
		mPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
		
	}

	@Override
	public void onPageSelected(int arg0) {
		
		getSupportActionBar().setSelectedNavigationItem(arg0);
	}
	
    private final class MyFragmentAdapter extends FragmentStatePagerAdapter {
        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public Fragment getItem(int position) {
        	
        	return Channel_Fragment.newInstance(urls[position]);
            
        }
    }


}
