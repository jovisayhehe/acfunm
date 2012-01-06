package tv.avfun;

import tv.avfun.R;
import tv.acfun.util.Util;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
	
	private WebView webview;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreent(this);
		setContentView(R.layout.webview_layout);
		path = getIntent().getStringExtra("path");
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);     
		webview.setWebChromeClient(new WebChromeClient());
		webview.getSettings().setPluginState(PluginState.ON);
		webview.loadUrl(path);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		webview.loadUrl("about:blank");
	}
	
	
	
	
}
