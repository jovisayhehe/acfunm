
package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.api.ApiParser;
import tv.avfun.app.AcApp;
import tv.avfun.entity.VideoInfo.VideoItem;
import tv.avfun.util.FileUtil;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.umeng.analytics.MobclickAgent;

public class SectionActivity extends SherlockActivity implements OnClickListener {

    private String            aid;
    private String            vid;
    private String            vtype;
    private ProgressBar       progressBar;
    private TextView          time_outtext;
    private ListView          list;
    private SectionAdapter    adapter;
    private List<String> data        = new ArrayList<String>();
    private int               playmode    = 0;
    private static final int  PARSE_OK    = 1;
    private static final int  PARSE_ERROR = 2;
    private static final String TAG = SectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        Object obj = intent.getExtras().getSerializable("item");
        if(obj == null) throw new IllegalArgumentException("what does the video item you want to play?");
        item = (VideoItem) obj;
        ab.setTitle(item.subtitle);
        vid = item.vid;
        vtype = item.vtype;
        playmode = AcApp.getConfig().getInt("playmode", 0);
        list = (ListView) findViewById(android.R.id.list);
        progressBar = (ProgressBar) findViewById(R.id.time_progress);
        time_outtext = (TextView) findViewById(R.id.time_out_text);
        time_outtext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                getdatas();
            }
        });
        list.setVisibility(View.INVISIBLE);
        list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
        list.setDividerHeight(2);
        adapter = new SectionAdapter(this, data);
        list.setAdapter(adapter);
        getdatas();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private Handler handler = new Handler() {

            public void handleMessage(android.os.Message msg) {
                progressBar.setVisibility(View.GONE);
                switch (msg.what) {
                case PARSE_OK:
                    list.setVisibility(View.VISIBLE);
                    if (data.size() > 1) {
                        Intent intent = new Intent(SectionActivity.this, PlayActivity.class);
                        intent.putStringArrayListExtra("paths", (ArrayList<String>) data);
                        startActivity(intent);
                    } else {
                        if (playmode == 0) {
                            Intent intent = new Intent(SectionActivity.this, PlayActivity.class);
                            intent.putExtra("path", data.get(0));
                            startActivity(intent);
                        } else {
                            Intent it = new Intent(Intent.ACTION_VIEW);
                            String url = data.get(0);
                            Uri uri = Uri.parse(url);
                            String ext = FileUtil.getUrlExt(url);
                            String mimetype = null;
                            if(".flv".equals(ext)){
                                mimetype = "video/x-flv";
                            }else if(".f4v".equals(ext)){
                                mimetype = "video/x-f4v";
                            }else if(".mp4".equals(ext)){
                                mimetype = "video/mp4";
                            }else if(".hlv".equals(ext)){
                                mimetype = "video/x-flv"; // XXX: mimetype of hlv???
                            }
                            it.setDataAndType(uri, mimetype);
                            startActivity(it);
                        }
                    }
                    SectionActivity.this.finish();
                    break;
                case PARSE_ERROR:
                    list.setVisibility(View.GONE);
                    time_outtext.setEnabled(false);
                    time_outtext.setVisibility(View.VISIBLE);
                    time_outtext.setText(R.string.noparser);
                default:
                    break;
                }
            }
        };
    private VideoItem item;

    public void getdatas() {
        time_outtext.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread() {

            public void run() {
                try {
                    //data = (ArrayList<String>) ApiParser.ParserVideopath(vtype, vid);
                    if(!item.isdownloaded){
                        if(BuildConfig.DEBUG)
                            Log.i(TAG, "parsing parts for"+ vid);
                        int parseMode = 1;
                        if(AcApp.getConfig().getBoolean("isHD", false))
                            parseMode = 2;
                        ApiParser.parseVideoParts(item,parseMode);
                    }
                    data = item.urlList;
                    if (data != null && data.size() > 0) {
                        handler.obtainMessage(PARSE_OK, data).sendToTarget();
                    }else{
                        handler.obtainMessage(PARSE_ERROR).sendToTarget();
                    }
                } catch (Exception e) {
                    handler.obtainMessage(PARSE_ERROR).sendToTarget();
                    if(BuildConfig.DEBUG)
                        e.printStackTrace();
                }
            }
        }.start();

    }

    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private final class SectionAdapter extends BaseAdapter {

        private List<String> data;

        public SectionAdapter(Context context, List<String> data) {
            this.data = data;
        }

        public void setData(List<String> data) {
            this.data = data;
        }

        @Override
        public int getCount() {

            return this.data.size();
        }

        @Override
        public Object getItem(int position) {

            return this.data.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = new TextView(SectionActivity.this);

            textView.setText(String.valueOf(position + 1));
            textView.setTextSize(24);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(12, 12, 12, 12);
            textView.setTextColor(Color.BLACK);
            textView.setTag(data.get(position));
            textView.setOnClickListener(SectionActivity.this);
            textView.setBackgroundResource(R.drawable.selectable_background);
            convertView = textView;
            return convertView;
        }

    }

    @Override
    public void onClick(View v) {

        String flvpath = (String) v.getTag();

        if (playmode != 0) {
            try {
                Intent it = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(flvpath);
                it.setDataAndType(uri, "video/flv");
                startActivity(it);
                return;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "没有可用的外部播放器！", 0).show();
            }
        }
        Intent intent = new Intent(SectionActivity.this, PlayActivity.class);
        intent.putExtra("path", flvpath);
        startActivity(intent);
    }
}
