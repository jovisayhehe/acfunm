package tv.avfun;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import tv.avfun.R;
import tv.acfun.util.Parser;
import tv.acfun.util.Util;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActivityGroup {
    /** Called when the activity is first created. */
	private TextView home_txt;
	private TextView channel_txt;
	private TextView search_txt;
	private TextView download_txt;
	private TextView more_txt;
	private LinearLayout view = null;
	public static int playcode;
	public static ArrayList<ArrayList<HashMap<String, String>>> hotdata;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Util.fullScreen(this);
        setContentView(R.layout.main);
        if(!Util.isNetWorkActive(this)){
        	showDialog(998);
        }else{
        	 InitView();
             addActivity("home", HomeActivity.class,null);
             setEnaled(home_txt);
        }
    }

	public void InitView(){
    	view = (LinearLayout) findViewById(R.id.contentbody);
    	home_txt = (TextView) findViewById(R.id.main_home_txt);
    	channel_txt = (TextView) findViewById(R.id.main_channel_txt);
    	search_txt = (TextView) findViewById(R.id.main_search_txt);
    	download_txt = (TextView) findViewById(R.id.main_favorites_txt);
    	more_txt = (TextView) findViewById(R.id.main_more_txt);
    	LinearLayout startan = (LinearLayout) findViewById(R.id.start_an);
    	int acfaces[] = {R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d,R.drawable.e,R.drawable.f};
    	int n = (int)(Math.random()*5);
    	startan.setBackgroundResource(acfaces[n]);
    	ButtonListener listener = new ButtonListener();
    	
    	home_txt.setOnClickListener(listener);
    	channel_txt.setOnClickListener(listener);
    	search_txt.setOnClickListener(listener);
    	download_txt.setOnClickListener(listener);
    	more_txt.setOnClickListener(listener);
    	SharedPreferences sharedPreferences = getSharedPreferences(  
                "config", Activity.MODE_PRIVATE);
    	playcode = sharedPreferences.getInt("playcode", 0);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	showDialog(88);
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
    
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
	    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	showDialog(88);
	        return true;
	    }
		return super.dispatchKeyEvent(event);
	}
    
    private final class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.main_home_txt:
				setEnaled(home_txt);
				addActivity("home", HomeActivity.class,null);
				break;
			case R.id.main_channel_txt:
				setEnaled(channel_txt);
				addActivity("channel", ChannelActivity.class,null);
				break;
			case R.id.main_search_txt:
				setEnaled(search_txt);
				addActivity("search", SearchActivity.class,null);
				break;
			case R.id.main_favorites_txt:
				setEnaled(download_txt);
				addActivity("favorites", FavoritesActivity.class,null);
				break;
			case R.id.main_more_txt:
				setEnaled(more_txt);
				addActivity("search", MoreActivity.class,null);
				break;
			default:
				break;
			}
		}
    	
    }
	private void setEnaled(TextView tview) {
		tview.setEnabled(false);
		switch (tview.getId()) {
		case R.id.main_home_txt:
			tview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.homel, 0, 0);
			channel_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.channel, 0, 0);
			channel_txt.setEnabled(true);
			
			search_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search, 0, 0);
			search_txt.setEnabled(true);
			
			download_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_folder, 0, 0);
			download_txt.setEnabled(true);
			
			more_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.more, 0, 0);
			more_txt.setEnabled(true);
			break;
		case R.id.main_channel_txt:
			tview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.channell, 0, 0);
			home_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
			home_txt.setEnabled(true);
			
			search_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search, 0, 0);
			search_txt.setEnabled(true);
			
			download_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_folder, 0, 0);
			download_txt.setEnabled(true);
			
			more_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.more, 0, 0);
			more_txt.setEnabled(true);
			break;
		case R.id.main_search_txt:
			tview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.searchl, 0, 0);
			home_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
			home_txt.setEnabled(true);
			
			channel_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.channel, 0, 0);
			channel_txt.setEnabled(true);
			
			download_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_folder, 0, 0);
			download_txt.setEnabled(true);
			
			more_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.more, 0, 0);
			more_txt.setEnabled(true);
			break;
		case R.id.main_favorites_txt:
			tview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_folderl, 0, 0);
			home_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
			home_txt.setEnabled(true);
			
			channel_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.channel, 0, 0);
			channel_txt.setEnabled(true);
			
			search_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search, 0, 0);
			search_txt.setEnabled(true);
			
			more_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.more, 0, 0);
			more_txt.setEnabled(true);
			break;
		case R.id.main_more_txt:
			tview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.morel, 0, 0);
			home_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.home, 0, 0);
			home_txt.setEnabled(true);
			
			channel_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.channel, 0, 0);
			channel_txt.setEnabled(true);
			
			search_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.search, 0, 0);
			search_txt.setEnabled(true);
			
			download_txt.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.favorites_folder, 0, 0);
			download_txt.setEnabled(true);
			break;
		default:
			break;
		}
	}
	
	
	public void addActivity(String id,Class<?> clazz,ArrayList<String> value) {
		
		if(view!=null){
			view.removeAllViews();
		}
		
		Intent intent = new Intent(MainActivity.this, clazz)
		.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if(value!=null){
			intent.putStringArrayListExtra("info", value);
		}
		view.addView(getLocalActivityManager().startActivity(
				id,intent)
				.getDecorView());
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		if(id==998){
			ImageView img = new ImageView(MainActivity.this);
			img.setImageResource(R.drawable.neterror);
			return new AlertDialog.Builder(MainActivity.this)
					.setTitle("喂！再怎么也要准备好网络在来吧！").setView(img)
					.setPositiveButton("退出", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							finish();
						}
					}).create();
		}else{
			ImageView img = new ImageView(MainActivity.this);
			img.setImageResource(R.drawable.neterror);
			return new AlertDialog.Builder(MainActivity.this)
					.setTitle("确认退出吗?").setView(img)
					.setPositiveButton("退出", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

							finish();
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						
						}
					})
					
					.create();	
		}
	}
	
	
	public static void getHotdata(String address) throws IOException{
		
		hotdata = Parser.getHotdata(address);
	}
	
    
}