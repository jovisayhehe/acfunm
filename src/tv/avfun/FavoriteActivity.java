
package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.ac.fun.R;
import tv.avfun.api.Channel;
import tv.avfun.api.ChannelApi;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.Favorite;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class FavoriteActivity extends BaseListActivity implements OnItemClickListener, DialogInterface.OnClickListener {

    private List<Favorite> data = new ArrayList<Favorite>();
    private ListView       list;
    private ProgressBar    progressBar;
    private AssistAdaper   adaper;
    private AlertDialog    dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("收藏夹");
        progressBar = (ProgressBar)findViewById(R.id.time_progress);
        list = (ListView) findViewById(android.R.id.list);
        list.setVisibility(View.INVISIBLE);
        list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
        list.setDividerHeight(2);
        adaper = new AssistAdaper(this, data);
        list.setAdapter(adaper);
        list.setOnItemClickListener(this);

        new Thread() {

            public void run() {

                data = new DBService(FavoriteActivity.this).getFovs();

                runOnUiThread(new Runnable() {

                    public void run() {
                        list.setVisibility(View.VISIBLE);
                        adaper.setData(data);
                        adaper.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sync) {

            new AlertDialog.Builder(this).setTitle("与AC娘同步收藏品").setMessage("你有我有全都有啊")
                    .setNegativeButton("算了", null).setPositiveButton("开始同步", this).show();
            return true;
        }

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
        if (which == DialogInterface.BUTTON_POSITIVE) {
            dialog.dismiss();
            startSync();
        }

    }

    private void startSync() {
        View syncView = getLayoutInflater().inflate(R.layout.dialog_sync, null);
        dialog = new AlertDialog.Builder(this).setView(syncView).setCancelable(false).show();
        new SyncFavouriteTask(syncView).execute();
    }
    
    private class SyncFavouriteTask extends AsyncTask<Void, Integer, Boolean>{
        
        private ProgressBar pb;
        private TextView text;
        private TextView progress;
        public SyncFavouriteTask(View syncView) {
            pb = (ProgressBar) syncView.findViewById(R.id.pb);
            text = (TextView) syncView.findViewById(R.id.status);
            progress = (TextView) syncView.findViewById(R.id.progress);
        }
        @Override
        protected void onPreExecute() {
            pb.setIndeterminate(true);
            text.setText("...");
            progress.setText("...");
            pb.setMax(10);
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            // 连接 收藏夹
            try {
                for(int i=0;i<10;i++){
                    publishProgress(i+1);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 解析
            // 与本地数据库比对
            // 同步不同点（增删），并发布进度
            // 完成同步，提示消失
            return true;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            pb.setIndeterminate(false);
            pb.setProgress(values[0].intValue());
            text.setText(String.format("同步第 %d 个", values[0].intValue()));
            progress.setText(values[0]+"/10");
        }
        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if(result){
                Toast.makeText(getApplicationContext(), "同步完成", 0).show();
            }
        }
        
    }

}
