package tv.acfun.video;

import tv.acfun.video.fragment.CategoriesFragment;
import tv.acfun.video.fragment.HomeFragment;
import tv.acfun.video.util.CommonUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

public class MainActivity extends ActionBarActivity implements TabListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int mActivatedPosition = CommonUtil.INVALID_POSITION;
    private int mCurrentPosition;
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            mCurrentPosition = mActivatedPosition = savedInstanceState.getInt(STATE_ACTIVATED_POSITION);
        }else{
            mCurrentPosition = 1;
        }
        mViewPager.setCurrentItem(mCurrentPosition, false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
            case 0:
                fragment = new Fragment();
                break;
            case 1:
                fragment = new HomeFragment();
                break;
            case 2:
            default:
                fragment = new CategoriesFragment();
                break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return getString(R.string.title_user_center);
            case 1:
                return getString(R.string.title_home);
            case 2:
                return getString(R.string.title_categories);
            }
            return null;
        }
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        mCurrentPosition = mActivatedPosition = arg0.getPosition();
        mViewPager.setCurrentItem(mCurrentPosition);
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mActivatedPosition != CommonUtil.INVALID_POSITION)
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
    }
}
