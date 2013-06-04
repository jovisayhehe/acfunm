
package tv.avfun;

import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.umeng.analytics.MobclickAgent;

import tv.ac.fun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.db.DBService;
import tv.avfun.entity.Article;
import tv.avfun.view.SWebView;
import tv.avfun.view.SWebView.ScrollInterface;
import tv.avfun.view.SWebView.WContentHeight;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WebViewActivity extends SherlockActivity implements OnClickListener {

    private SWebView    mWebView;
    private TextView    btn;
    private Document    doc;
    private String      aid;
    private String      title;
    private int         channelid;
    private ProgressBar probar;
    private ProgressBar tprobar;
    private TextView    reloadtext;
    private Article     article;
    private boolean     isfavorite = false;
    private boolean     isbottom   = false;
    private int         modecode;
    Handler             handler    = new Handler() {

           @Override
           public void handleMessage(Message msg) {

               super.handleMessage(msg);
               switch (msg.what) {
               case 1:
                   tprobar.setVisibility(View.GONE);
                   mWebView.loadData(URLEncoder.encode(doc.html()).replaceAll("\\+", " "),
                           "text/html; charset=UTF-8", null);
                   break;
               case 2:
                   tprobar.setVisibility(View.GONE);
                   reloadtext.setVisibility(View.VISIBLE);
                   break;

               default:
                   break;
               }
           }

       };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        channelid = getIntent().getIntExtra("channelId", 0);
        title = getIntent().getStringExtra("title");
        modecode = getIntent().getIntExtra("modecode", 0);
        aid = getIntent().getStringExtra("aid");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        new DBService(this).addtoHis(aid, title, sdf.format(new Date()), 1, channelid);
        isfavorite = new DBService(this).isFaved(aid);
        btn = (TextView) findViewById(R.id.web_comment_btn);
        btn.setOnClickListener(this);

        probar = (ProgressBar) findViewById(R.id.web_progress);
        tprobar = (ProgressBar) findViewById(R.id.web_time_progress);
        reloadtext = (TextView) findViewById(R.id.web_time_out_text);
        reloadtext.setOnClickListener(this);
        mWebView = (SWebView) findViewById(R.id.web_webview);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setSupportZoom(true);

        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                probar.setProgress(progress);
                if (progress == 100) {
                    probar.setVisibility(View.GONE);
                }
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Pattern regex = Pattern.compile("(ac\\d{5,})");
                Matcher matcher = regex.matcher(url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (matcher.find()) {
                    String ac = matcher.group();
                    try {
                        intent.setData(Uri.parse("av://" + ac));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        // nothing
                    }
                }
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }

        });
        loaddata();

        mWebView.setOnCustomScroolChangeListener(new ScrollInterface() {

            @Override
            public void onSChanged(int l, int t, int oldl, int oldt) {

                if (!isbottom) {
                    if (mWebView.getContentHeight() * mWebView.getScale()
                            - (mWebView.getHeight() + mWebView.getScrollY()) < 100) {
                        mWebView.scrollTo(mWebView.getScrollX(),
                                (int) (mWebView.getContentHeight() * mWebView.getScale()));
                        btn.setVisibility(View.VISIBLE);
                        isbottom = true;
                    }
                }

            }
        });

        mWebView.setFisrtOndrawOverListener(new WContentHeight() {

            @Override
            public void onDrawOver(int h) {

                if (h <= MainActivity.height) {
                    btn.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        case R.id.menu_item_comment:
            gotoComment();
            break;
        case R.id.menu_item_fov_action_provider_action_bar:
            if (isfavorite) {
                new DBService(this).delFav(aid);
                isfavorite = false;
                item.setIcon(R.drawable.rating_favorite);
                Toast.makeText(this, "取消成功", Toast.LENGTH_SHORT).show();
            } else {
                new DBService(this).addtoFav(aid, title, 1, channelid);
                isfavorite = true;
                item.setIcon(R.drawable.rating_favorite_p);
                Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
            }

            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);
        MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(createShareIntent());
        if (isfavorite) {
            menu.findItem(R.id.menu_item_fov_action_provider_action_bar).setIcon(R.drawable.rating_favorite_p);
        }
        return true;
    }

    private Intent createShareIntent() {
        String shareurl = title + "http://www.acfun.tv/v/ac" + aid;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareurl);
        return shareIntent;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.web_comment_btn:
            gotoComment();
            break;
        case R.id.web_time_out_text:
            loaddata();
            break;
        default:
            break;
        }
    }

    private void gotoComment() {
        Intent intent = new Intent(WebViewActivity.this, CommentsActivity.class);
        intent.putExtra("aid", aid);
        startActivity(intent);
    }

    public void loaddata() {
        tprobar.setVisibility(View.VISIBLE);
        reloadtext.setVisibility(View.GONE);

        new Thread() {

            public void run() {

                try {
                    article = ApiParser.getArticle(aid);
                    if (modecode == 2) {
                        Intent intent = new Intent(WebViewActivity.this, ImagePagerActivity.class);
                        intent.putStringArrayListExtra("imgs", article.getImgUrls());
                        intent.putExtra("title", article.getTitle());
                        intent.putExtra("aid", article.getId());
                        intent.putExtra("channelId", channelid);
                        startActivity(intent);
                        WebViewActivity.this.finish();
                        return;
                    }
                    InputStream in = getAssets().open("usite.html");
                    doc = Jsoup.parse(in, "utf-8", "");

                    Element tdiv = doc.getElementById("title");
                    Element cdiv = doc.getElementById("content");
                    Element idiv = doc.getElementById("info");

                    tdiv.html(title);

                    idiv.append("<p align=\"center\">" + "投稿: " + article.getName() + "</p>");
                    Date date = new Date(article.getPosttime());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                    idiv.append("<p align=\"center\">" + sdf.format(date) + "</p>");

                    List<HashMap<String, String>> contents = article.getContents();
                    for (int i = 0; i < contents.size(); i++) {
                        String subtitle = contents.get(i).get("subtitle");
                        if (!subtitle.equals(title)) {
                            cdiv.append("<p align=\"center\">" + subtitle + "</p>");
                        }
                        cdiv.append("<hr>");
                        cdiv.append("<p/>");
                        String content = contents.get(i).get("content");

                        switch (modecode) {
                        case 0:
                            cdiv.append(content);
                            break;
                        case 1:
                            // Whitelist wl = new Whitelist();//过滤图片
                            // wl.addTags("img");
                            // cdiv.append(Jsoup.clean(content, wl));
                            cdiv.append(content).select("img").remove();
                            break;

                        default:
                            break;
                        }

                    }

                    handler.sendEmptyMessage(1);

                } catch (Exception e) {

                    e.printStackTrace();

                    handler.sendEmptyMessage(2);
                }

            }
        }.start();

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
