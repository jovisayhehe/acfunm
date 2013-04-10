package tv.avfun;

import tv.avfun.fragment.HomeFragment;
import tv.avfun.fragment.UserHomeFragment;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

    public static int   width;
    public static int   height;
    private SlidingMenu menu;
    private SearchView  mSearchView;
    private ActionBar   bar;

    public boolean isNetworkAvailable() {
        NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return (info != null) && (info.isConnected());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 得到界面宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        // 设置左边的menu
        // TODO 左边为大的分类：如视频、文章、专题、图区、、、
        setBehindContentView(R.layout.menu_frame);
        
        // above view
        setContentView(R.layout.content_frame);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, HomeFragment.newInstance()).commit();
        // setTitle(R.string.app_name);

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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.menu_frame_right, UserHomeFragment.newInstance()).commit();
        // 启用home
        bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
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
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM
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

}
