package tv.avfun;

import java.util.HashMap;
import java.util.Map;

import tv.avfun.app.AcApp;
import tv.avfun.fragment.HomeChannelListFragment;
import tv.avfun.fragment.PlayTime;
import tv.avfun.fragment.UserHomeFragment;
import tv.avfun.view.SlideNavItemView;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static int   width;
    public static int   height;
    private SlidingMenu menu;
    private SearchView  mSearchView;
    private Fragment    mContent;
    private ActionBar   bar;
    private FragmentManager mFragmentMan;
    private int navId = R.id.slide_nav_home;
    private Fragment nextContent;
    private SlideNavItemView mNavItem;
    // 用于存放Fragment实例
    private Map<String,Fragment> instances;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initUmeng();
        // 得到界面宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        
        instances = new HashMap<String, Fragment>();
        // 初始Fragment
        mContent = new HomeChannelListFragment();
        
        instances.put("home", mContent);
        
        Fragment time = PlayTime.newInstance();
        instances.put("play_time",time);
        
        mFragmentMan = getSupportFragmentManager();
        
        //初始化导航
        initNav();
        
        // above view
        setContentView(R.layout.content_frame);
        
        mFragmentMan.beginTransaction().replace(R.id.content_frame, mContent).commit();
        // 启用home
        bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        
       
    }
    @Override
    protected void onStart() {
        super.onStart();
        boolean isFirstRun = AcApp.getConfig().getBoolean("first_run", true);
        if(isFirstRun){
            showOverlays();
        }
    }
    private void showOverlays() {
        overlays = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.overlays, null);
        overlays.setOnClickListener(new View.OnClickListener() {
            int count = overlays.getChildCount();
            int i = 2;
            @Override
            public void onClick(View v) {
                if(i<count)
                    overlays.getChildAt(i++).setVisibility(View.VISIBLE);
                else{
                    hideOverlays();
                    AcApp.putBoolean("first_run", false);
                }
            }
        });
        OnClickListener l = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                hideOverlays();
                AcApp.putBoolean("first_run", false);
            }
        };
        overlays.findViewById(R.id.close).setOnClickListener(l);
        overlays.findViewById(R.id.ok).setOnClickListener(l);
        ((FrameLayout)findViewById(R.id.content_frame)).addView(overlays);
        overlays.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));
    }
    private void hideOverlays() {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        anim.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                overlays.setVisibility(View.GONE);
            }
        });
        overlays.startAnimation(anim);
    }
    private void initUmeng() {
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        MobclickAgent.setAutoLocation(false);
        MobclickAgent.onError(this);
        UmengUpdateAgent.update(this);
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
        
        mFragmentMan.beginTransaction()
                .replace(R.id.menu_frame_right, UserHomeFragment.newInstance()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mSearchView = new SearchView(this);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        mSearchView.setSearchableInfo(info);

        menu.add("Search")
                .setIcon(R.drawable.action_search)
                .setActionView(mSearchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        View v = mSearchView.findViewById(R.id.abs__search_plate);
        v.setBackgroundResource(R.drawable.edit_text_holo_light);
        
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Deprecated
    public void switchContent(Fragment fragment, int navId) {
        if(mContent != fragment) {
            mContent = fragment;
            mFragmentMan.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.content_frame, fragment)
                .commit();
            setSlideNavHint(navId);
        }
        menu.showContent();
    }

    public void switchContent(Fragment from, Fragment to, int navId) {
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
    
    public void switchContent(Fragment fragment){
        if(mContent != fragment) {
            mContent = fragment;
            mFragmentMan.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, R.anim.slide_out)
                .replace(R.id.content_frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("mContent")
                .commit();
        }
    }
    /*
     * 数组记录nav id
     */
    private int[] navIds = {R.id.slide_nav_home,R.id.slide_nav_bangumi};
    private SparseArray<SlideNavItemView> mNavItems;
    private ViewGroup overlays;
    private void initNavItems(){
        
        mNavItems = new SparseArray<SlideNavItemView>();
        for(int navId : navIds){
            SlideNavItemView item = ((SlideNavItemView)findViewById(navId));
            mNavItems.put(navId,item);
        }
    }
    public void setSlideNavHint(int navId){
        if(navId != this.navId ){
            // 将原item 的hint 设为隐藏
            mNavItem.setHintEnabled(false);
            this.navId = navId;
        }
        mNavItem = mNavItems.get(this.navId);
        mNavItem.setHintEnabled(true);
        
    }
    public void slideNavItemClicked(View view){
        SlideNavItemView navItem= (SlideNavItemView)view;
        int id = navItem.getId();
        switch (id) {
        case R.id.slide_nav_home:
            bar.setTitle("主页");
            nextContent = instances.get("home");
            break;
        case R.id.slide_nav_bangumi:
            nextContent = instances.get("play_time");
            bar.setTitle("番组列表");
            if(nextContent==null){
                nextContent = PlayTime.newInstance();
                instances.put("play_time",nextContent);
            }
            break;
        case R.id.slide_nav_article:
            // TODO 做成Fragment
            /*nextContent = instances.get("article");
            if(nextContent==null){
                nextContent = PlayTime.newInstance();
                instances.put("play_time",nextContent);
            }*/
            Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
            intent.putExtra("position", 6);
            intent.putExtra("isarticle", true);
            startActivity(intent);
            toggle();
            return;
        }
        switchContent(mContent, nextContent,id);
        
    }
    private boolean isFirst = true;
    @Override
    public void onBackPressed() {
        if(this.navId != R.id.slide_nav_home){
            nextContent = instances.get("home");
            bar.setTitle("主页");
            switchContent(mContent, nextContent,R.id.slide_nav_home);
            return;
        } else if(this.navId == R.id.slide_nav_home){
            if(isFirst){
                Toast.makeText(getApplicationContext(), "( ⊙o⊙ ) 真的要退出吗？", 0).show();
                isFirst = false;
                return;
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
