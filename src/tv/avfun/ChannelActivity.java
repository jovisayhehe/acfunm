
package tv.avfun;


import java.util.List;

import tv.avfun.api.Channel;
import tv.avfun.api.ChannelApi;
import tv.avfun.app.AcApp;
import tv.avfun.fragment.ChannelContentFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TabPageIndicator;

public class ChannelActivity extends SherlockFragmentActivity implements OnPageChangeListener, TabListener {

    private int               gdposition;
    private ActionBar         ab;
    ViewPager                 mPager;
    private List<Channel>     apis;
    public static boolean     isarticle;
    private static final int  MODEID   = 500;
    private static final int  MIX      = 501;
    private static final int  PIC      = 502;
    private static final int  NOPIC    = 503;
    public static int         modecode = 0;
    private SubMenu           subMenu1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gdposition = getIntent().getIntExtra("position", -1);
        isarticle = getIntent().getBooleanExtra("isarticle", false);
        if (isarticle) {
            modecode = AcApp.getConfig().getInt("view_mode", 0);
        }
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(5);
        mPager.setOnPageChangeListener(this);
        initTab(gdposition);

        mPager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

    }

    private void initTab(int pos) {

        apis = ChannelApi.getApi(pos);
        // 标题栏
        getSupportActionBar().setTitle(apis.get(0).getTitle());
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

        if (isarticle) {
            subMenu1 = menu.addSubMenu("阅读模式");
            subMenu1.add(1, ChannelActivity.MIX, 1, "图文").setIcon(R.drawable.mode_mix);
            subMenu1.add(1, ChannelActivity.NOPIC, 1, "文本").setIcon(R.drawable.mode_article);
            subMenu1.add(1, ChannelActivity.PIC, 1, "漫画").setIcon(R.drawable.mode_picture);
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
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        case ChannelActivity.MIX:
            modecode = 0;
            subMenu1.setIcon(R.drawable.mode_mix);
            Toast.makeText(this, "图文模式", Toast.LENGTH_SHORT).show();
            AcApp.putInt("view_mode", 0);
            break;
        case ChannelActivity.NOPIC:
            modecode = 1;
            subMenu1.setIcon(R.drawable.mode_article);
            Toast.makeText(this, "文本模式", Toast.LENGTH_SHORT).show();
            AcApp.putInt("view_mode", 1);
            break;
        case ChannelActivity.PIC:
            modecode = 2;
            subMenu1.setIcon(R.drawable.mode_picture);
            Toast.makeText(this, "漫画模式", Toast.LENGTH_SHORT).show();
            AcApp.putInt("view_mode", 2);
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        this.ab.setSelectedNavigationItem(tab.getPosition());
        mPager.setCurrentItem(tab.getPosition(),true);
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

    private final class MyFragmentAdapter extends FragmentPagerAdapter {

        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return apis.size();
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return apis.get(position).title;
        }
        @Override
        public Fragment getItem(int position) {
            return ChannelContentFragment.newInstance(apis.get(position), isarticle);

        }
    }

}
