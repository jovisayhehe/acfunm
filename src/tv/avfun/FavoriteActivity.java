
package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Cookie;

import tv.ac.fun.R;
import tv.avfun.api.Channel;
import tv.avfun.api.ChannelApi;
import tv.avfun.api.MemberUtils;
import tv.avfun.app.AcApp;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.Favorite;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class FavoriteActivity extends BaseListActivity implements OnItemClickListener, DialogInterface.OnClickListener, Callback, OnScrollListener {

    private static final int FAILED = 400;
    protected static final int GUEST = 100;
    protected static final int USER = 102;
    private static final int MORE = 200;
    private List<Favorite> data = new ArrayList<Favorite>();
    private ListView       list;
    private ProgressBar    progressBar;
    private AssistAdaper   adaper;
    private Cookie[] cookies;
    private View timeOut;
    private Handler handler;
    private int indexPage = 1;
    private View mFootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(this);
        setContentView(R.layout.list_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("收藏夹");
        progressBar = (ProgressBar)findViewById(R.id.time_progress);
        progressBar.setVisibility(View.VISIBLE);
        timeOut = findViewById(R.id.time_out_text);
        list = (ListView) findViewById(android.R.id.list);
        list.setVisibility(View.GONE);
        list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
        list.setDividerHeight(2);
        mFootView = getLayoutInflater().inflate(R.layout.list_footerview, list, false);
//        mFootView.setVisibility(View.GONE);
        list.addFooterView(mFootView);
        adaper = new AssistAdaper(this, data);
        list.setAdapter(adaper);
        list.setOnItemClickListener(this);
        list.setOnScrollListener(this);
        cookies = AcApp.instance().getCookies();
        favouriteLocal = new DBService(FavoriteActivity.this).getFovs();
        if(cookies == null){
            data = favouriteLocal;
            handler.sendEmptyMessage(GUEST);
        }else
            loadData(indexPage);

    }

    List<Favorite> favouriteLocal;
    private boolean isLoading;
    private void loadData(final int pageNo) {

        new Thread() {

            public void run() {
                isLoading = true;
                final List<Favorite> favouriteOnline = MemberUtils.getFavouriteOnline(cookies,pageNo);
                if(favouriteOnline == null){
                    handler.sendEmptyMessage(FAILED);
                    if(pageNo == 1) data.addAll(favouriteLocal);
                    return;
                }
                // 同步到本地
                DBService db = new DBService(getApplicationContext());
                db.beginTransaction();
                for(Favorite fav : favouriteOnline){
                    db.addtoFav(fav);
                }
                db.endTransaction();
                if(pageNo == 1){
                    // 同步到服务器
                    new Thread(){
                        public void run() {
                            for(Favorite fav : favouriteLocal){
                                if(!favouriteOnline.contains(fav)){
                                    MemberUtils.addFavourite(fav.aid, cookies);
                                }
                            }
                        }
                    }.start();
                    
                    data.addAll(favouriteLocal);
                    handler.sendEmptyMessage(USER);
                }else
                    handler.sendEmptyMessage(MORE);
            }
        }.start();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getSupportMenuInflater().inflate(R.menu.menu_sync, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.sync) {
//
//            new AlertDialog.Builder(this).setTitle("与AC娘同步收藏品").setMessage("你有我有全都有啊")
//                    .setNegativeButton("算了", null).setPositiveButton("开始同步", this).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Favorite favorite = data.get(position);
        if (data.get(position).getTpye() == 0) {
            Intent intent = new Intent(FavoriteActivity.this, DetailActivity.class);
            Contents c = new Contents();
            c.setAid(favorite.aid);
            c.setTitle(favorite.title);
            c.setChannelId(favorite.channelid);
            intent.putExtra("from", 1);
            intent.putExtra("contents", c);
            startActivity(intent);

        } else {
            // TODO 改为contents
            Intent intent = new Intent(FavoriteActivity.this, WebViewActivity.class);
            intent.putExtra("modecode", ChannelActivity.modecode);
            intent.putExtra("aid", data.get(position).getAid());
            intent.putExtra("title", data.get(position).getTitle());
            intent.putExtra("channelId", data.get(position).getChannelid());
            startActivity(intent);
        }
    }

    private final class AssistAdaper extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Favorite> data;

        public AssistAdaper(Context context, List<Favorite> data) {
            this.mInflater = LayoutInflater.from(context);
            this.data = data;
        }

        public void setData(List<Favorite> data) {
            this.data = data;
        }

        @Override
        public int getCount() {

            return this.data.size();
        }

        @Override
        public Object getItem(int position) {

            return data.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ListViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.favorites_list_item, parent, false);
                holder = new ListViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.favrites_item_title);
                holder.channel = (TextView) convertView.findViewById(R.id.favrites_item_chann);
                holder.user = (TextView) convertView.findViewById(R.id.favrites_item_art);
                convertView.setTag(holder);
            } else {
                holder = (ListViewHolder) convertView.getTag();
            }
            holder.title.setText(data.get(position).getTitle());
            Channel channel = ChannelApi.channels.get(data.get(position).getChannelid());
            if (channel == null)
                // 不知名的id 暂且隐藏起来
                holder.channel.setVisibility(View.INVISIBLE);
            else {
                holder.channel.setText(channel.title);
                holder.channel.setVisibility(View.VISIBLE);
            }
            holder.user.setText("ac" + data.get(position).getAid());

            return convertView;
        }

    }

    static class ListViewHolder {

        TextView title;
        TextView channel;
        TextView user;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
//        if (which == DialogInterface.BUTTON_POSITIVE) {
//            dialog.dismiss();
//            startSync();
//        }

    }

    @Override
    public boolean handleMessage(Message msg) {
        progressBar.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
//        adaper.setData(data);
        adaper.notifyDataSetChanged();
        switch (msg.what) {
        case FAILED:
            Toast.makeText(getApplicationContext(),"连接失败！请检查网络后重试！",0).show();
            break;
        case GUEST:
            Toast.makeText(getApplicationContext(), "未登录！收藏夹为本地数据。请登录以同步收藏！", 1).show();
            isLoading = false;
            break;
        case USER:
            isLoading = false;
            Toast.makeText(getApplicationContext(), "你已登录！收藏夹为网站数据。收藏操作将会同步到网络！", 1).show();
            break;
        case MORE:
            isLoading = false;
            break;
        }
        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        
        if (view.getLastVisiblePosition() == (view.getCount() - 1) && !isLoading) {
            if(indexPage+1 < MemberUtils.totalPage)
                loadData(++indexPage);
            else if (mFootView!=null) {
                    ((TextView) mFootView.findViewById(R.id.list_footview_text)).setText(R.string.nomorecomments);
                    mFootView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
            }
                
        }
        
    }

//    private void startSync() {
//        View syncView = getLayoutInflater().inflate(R.layout.dialog_sync, null);
//        dialog = new AlertDialog.Builder(this).setView(syncView).setCancelable(false).show();
//        new SyncFavouriteTask(syncView).execute();
//    }
    
//    private class SyncFavouriteTask extends AsyncTask<Void, Favorite, Boolean>{
//        
//        private TextView text;
//        private List<Favorite> favsOnline;
//        public SyncFavouriteTask(View syncView) {
//            text = (TextView) syncView.findViewById(R.id.status);
//        }
//        @Override
//        protected void onPreExecute() {
//            text.setText("...");
//        }
//        
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // 连接 收藏夹
//            
//            if(cookies == null){
//                return false;
//            }
//                
//            favsOnline = MemberUtils.getFavouriteOnline(cookies);
//            // 与本地数据库比对
//            // 同步不同点（增删），并发布进度
//            // FIXME: 开启事务，批量增删
//            for(Favorite local: data){
//                if(!favsOnline.contains(local)){
//                    MemberUtils.addFavourite(local.aid, cookies);
//                    publishProgress(local);
//                }
//            }
//            for(Favorite online : favsOnline){
//                try {
//                    if(data.contains(online)){
//                        continue;
//                    }
//                    new DBService(FavoriteActivity.this).addtoFav(online);
//                    publishProgress(online);
//                    Log.i("sysnc", "fav - "+online.aid+", "+online.title+", "+online.type);
//                } catch (Exception e) {
//                }
//            }
//
//            // 完成同步，提示消失
//            return true;
//        }
//        @Override
//        protected void onProgressUpdate(Favorite... values) {
//            text.setText(String.format("同步: %s", values[0].title));
//            data.add(values[0]);
//            adaper.notifyDataSetChanged();
//            
//        }
//        @Override
//        protected void onPostExecute(Boolean result) {
//            dialog.dismiss();
//            if(result){
//                Toast.makeText(getApplicationContext(), "同步完成", 0).show();
//            }else{
//                Toast.makeText(getApplicationContext(), "同步失败！请检查是否已登录.", 0).show();
//            }
//        }
//        
//    }

}
