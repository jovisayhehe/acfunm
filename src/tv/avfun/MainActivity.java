package tv.avfun;

import static com.actionbarsherlock.app.ActionBar.NAVIGATION_MODE_TABS;
import tv.avfun.fragment.HomeFragment;
import tv.avfun.fragment.PlayTime;
import tv.avfun.fragment.User_HomeFragment;



import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends SherlockFragmentActivity implements OnPageChangeListener,  TabListener{
	ViewPager mPager;
	private SearchView mSearchView;
	public static int width;
	public static int height;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickAgent.onError(this);
        UmengUpdateAgent.update(this);
        getWindow().setSoftInputMode(
        		WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(5);
        mPager.setOnPageChangeListener(this);
        mPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(NAVIGATION_MODE_TABS);
        ab.addTab(ab.newTab().setText(R.string.nav).setTabListener(this));
        ab.addTab(ab.newTab().setText(R.string.time).setTabListener(this));
        ab.addTab(ab.newTab().setText(R.string.userhome).setTabListener(this));
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
        
    	mSearchView = new SearchView(this);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo localSearchableInfo = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(localSearchableInfo);
        
        menu.add("Search")
            .setIcon(R.drawable.action_search)
            .setActionView(mSearchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
//        int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View v = (View)mSearchView.findViewById(R.id.abs__search_plate);
        v.setBackgroundResource(R.drawable.edit_text_holo_light);          

        return super.onCreateOptionsMenu(menu);
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
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		mPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		getSupportActionBar().setSelectedNavigationItem(arg0);
	}
	
    public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
        	switch (position) {
			case 0:
				return HomeFragment.newInstance();
			case 1:
				return PlayTime.newInstance();
			case 2:
				return User_HomeFragment.newInstance();
			default:
				break;
			}
			return null;
            
        }
    }
}
