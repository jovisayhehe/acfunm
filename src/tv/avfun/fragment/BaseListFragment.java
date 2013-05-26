package tv.avfun.fragment;

import tv.ac.fun.R;
import tv.avfun.util.NetWorkUtil;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class BaseListFragment extends SherlockListFragment {

    private boolean isRefreshing = false;
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.refresh:
            if (!this.isRefreshing) {
                startRefresh();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * 开始刷新
     */
    public void startRefresh() {
        if (NetWorkUtil.isNetworkAvailable(getActivity())){
            if(!this.isRefreshing) {
                this.isRefreshing = true;
                onRefresh();
            }
        }else{
            showNetWorkError();
        }
    }
    /**
     * 显示连接错误消息
     */
    protected void showNetWorkError() {
    }

    /**
     * 刷新数据。由子类来实现。
     * @param b
     */
    protected void onRefresh() {

    }
    /**
     * 到顶部
     */
    public void scrollToTop() {
        try {
            getListView().smoothScrollToPosition(0);
            return;
        } catch (IllegalStateException e) {}
    }
    /**
     * 刷新完成！
     */
    public void onRefreshCompleted(){
        this.isRefreshing = false;
    }
}
