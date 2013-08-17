
package tv.avfun;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import tv.avfun.fragment.BaseFragment;
import tv.avfun.fragment.HomeChannelListFragment;
import tv.avfun.fragment.PlayTime;
import tv.avfun.fragment.UserHomeFragment;
import tv.avfun.view.SlideNavItemView;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends SlidingFragmentActivity implements OnOpenListener, OnCloseListener {

    public static int             width;
    public static int             height;
    private SlidingMenu           menu;
    private BaseFragment          mContent;
    private ActionBar             bar;
    private FragmentManager       mFragmentMan;
    private int                   navId = R.id.slide_nav_home;
    private BaseFragment          nextContent;
    private SlideNavItemView      mNavItem;
    // 用于存放Fragment实例
    private Map<String, BaseFragment> instances;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUmeng();
        // 得到界面宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        instances = new HashMap<String, BaseFragment>();
        // 初始Fragment
        mContent = new HomeChannelListFragment();

        instances.put("home", mContent);

        BaseFragment time = PlayTime.newInstance();
        instances.put("play_time", time);

        mFragmentMan = getSupportFragmentManager();

        // 初始化导航
        initNav();

        // above view
        setContentView(R.layout.content_frame);

        mFragmentMan.beginTransaction().replace(R.id.content_frame, mContent).commit();
        // 启用home
        bar = getSupportActionBar();
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_transparent));
        bar.setDisplayHomeAsUpEnabled(true);
        // TODO
        // bar.setDisplayUseLogoEnabled(true);
        forceShowActionBarOverflowMenu();

    }
    @Override
    protected void onStart() {
        super.onStart();
        
        boolean isFirstRun = AcApp.getConfig().getBoolean("first_run", true);
        if (isFirstRun) {
            startActivity(new Intent(this, OverlayActivity.class));;
        }
    }

    private void initUmeng() {
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        MobclickAgent.setAutoLocation(false);
        MobclickAgent.onError(this);
        UmengUpdateAgent.setUpdateListener(null); 
        UmengUpdateAgent.update(this);
        UMFeedbackService.enableNewReplyNotification(this, NotificationType.AlertDialog);
    }

    private void initNav() {
        // 设置左边的menu
        // TODO 左边为大的分类：如视频、文章、专题、图区、、、
        setBehindContentView(R.layout.slide_nav_list);
        initNavItems();
        setSlideNavHint(this.navId);

        // 设置左右的Sliding Menu属性
        menu = getSlidingMenu();
        menu.setMode(SlidingMenu.LEFT_RIGHT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowDrawable(R.drawable.slidingmenu_shadow);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setSecondaryMenuOffsetRes(R.dimen.slidingmenu_offset_2);
        menu.setFadeDegree(0.35f);
        menu.setSecondaryMenu(R.layout.menu_frame_right);
        menu.setSecondaryShadowDrawable(R.drawable.slidingmenu_shadow_right);
        menu.setOnOpenListener(this);
        menu.setOnCloseListener(this);
        mFragmentMan.beginTransaction().replace(R.id.menu_frame_right, UserHomeFragment.newInstance()).commit();
    }

    @Override
    public void onOpen() {
        this.bar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onClose() {
        this.bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        AcApp.addSearchView(this, menu);
        
        menu.add(Menu.NONE, android.R.id.button1, Menu.NONE, "设置")
        .setIcon(R.drawable.ic_action_settings).setIntent(new Intent(getApplicationContext(), SettingsActivity.class))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); 
        menu.add(Menu.NONE, android.R.id.button2, Menu.NONE, "用户中心")
        .setIcon(R.drawable.ic_about)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); 
        menu.add(Menu.NONE, android.R.id.button3, Menu.NONE, "下载管理")
        .setIcon(R.drawable.av_download).setIntent(new Intent(getApplicationContext(), DownloadManActivity.class))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        return super.onCreateOptionsMenu(menu);

    }
    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggle();
            return true;
        case android.R.id.button2:
            showSecondaryMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Deprecated
//    public void switchContent(Fragment fragment, int navId) {
//        if (mContent != fragment) {
//            mContent = fragment;
//            mFragmentMan.beginTransaction().setCustomAnimations(android.R.anim.fade_in, R.anim.slide_out)
//                    .replace(R.id.content_frame, fragment).commit();
//            setSlideNavHint(navId);
//        }
//        menu.showContent();
//    }

    public void switchContent(BaseFragment from, BaseFragment to, int navId) {
        if (mContent != to) {
            mContent = to;
            FragmentTransaction transaction = mFragmentMan.beginTransaction().setCustomAnimations(
                    android.R.anim.fade_in, R.anim.slide_out);
            if (!to.isAdded()) {
                transaction.hide(from).add(R.id.content_frame, to).commit();
            } else {
                transaction.hide(from).show(to).commit();
            }
            setSlideNavHint(navId);
        }
        menu.showContent();
    }

//    public void switchContent(Fragment fragment) {
//        if (mContent != fragment) {
//            mContent = fragment;
//            mFragmentMan.beginTransaction().setCustomAnimations(android.R.anim.fade_in, R.anim.slide_out)
//                    .replace(R.id.content_frame, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .addToBackStack("mContent").commit();
//        }
//    }

    /*
     * 数组记录nav id
     */
    private int[] navIds = { R.id.slide_nav_home, R.id.slide_nav_bangumi };
    private SparseArray<SlideNavItemView> mNavItems;

    private void initNavItems() {

        mNavItems = new SparseArray<SlideNavItemView>();
        for (int navId : navIds) {
            SlideNavItemView item = ((SlideNavItemView) findViewById(navId));
            mNavItems.put(navId, item);
        }
    }

    public void setSlideNavHint(int navId) {
        if (navId != this.navId) {
            // 将原item 的hint 设为隐藏
            mNavItem.setHintEnabled(false);
            this.navId = navId;
        }
        mNavItem = mNavItems.get(this.navId);
        mNavItem.setHintEnabled(true);

    }

    public void slideNavItemClicked(View view) {
        SlideNavItemView navItem = (SlideNavItemView) view;
        int id = navItem.getId();
        switch (id) {
        case R.id.slide_nav_home:
//            bar.setTitle("主页");
            nextContent = instances.get("home");
            nextContent.onSwitch(bar);
            break;
        case R.id.slide_nav_bangumi:
            nextContent = instances.get("play_time");
            if (nextContent == null) {
                nextContent = PlayTime.newInstance();
                instances.put("play_time", nextContent);
            }else
                nextContent.onSwitch(bar);
            break;
        case R.id.slide_nav_article:
            // TODO 做成Fragment
            /*
             * nextContent = instances.get("article"); if(nextContent==null){
             * nextContent = PlayTime.newInstance();
             * instances.put("play_time",nextContent); }
             */
            Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
            intent.putExtra("position", 5);
            intent.putExtra("isarticle", true);
            startActivity(intent);
            toggle();
            return;
        }
        switchContent(mContent, nextContent, id);

    }

    private boolean isFirst = true;

    @Override
    public void onBackPressed() {
        if (this.navId != R.id.slide_nav_home) {
            nextContent = instances.get("home");
//            bar.setTitle("主页");
            bar.setDisplayShowTitleEnabled(false);
            switchContent(mContent, nextContent, R.id.slide_nav_home);
            return;
        } else if (this.navId == R.id.slide_nav_home) {
            if (isFirst) {
                Toast.makeText(getApplicationContext(), "( ⊙o⊙ ) 真的要退出吗？", 0).show();
                isFirst = false;
                return;
            } else {
                // 退出
// TODO               stopService(new Intent(this, DownloadService.class));
            }
        }
        super.onBackPressed();
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
