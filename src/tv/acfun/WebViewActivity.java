package tv.acfun;

import tv.acfun.util.Util;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

public class WebViewActivity extends Activity {
	
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreent(this);
		setContentView(R.layout.webview_layout);
		
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);     
		webview.setWebChromeClient(new WebChromeClient());
		webview.getSettings().setPluginState(PluginState.ON);
		webview.loadUrl("http://www.acfun.tv/newflvplayer/playert.swf");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		webview.loadUrl("about:blank");
	}
	
	
	
	
}
