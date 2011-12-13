package tv.acfun;

import tv.acfun.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.about_layout);
		TextView vtv = (TextView) this.findViewById(R.id.about_version);
		TextView mtv = (TextView) this.findViewById(R.id.about_mail);
		PackageManager manager = this.getPackageManager();
        PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			vtv.setText("acfun android客户端\r\n版本："+info.versionName) ;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
		Button r_but = (Button) this.findViewById(R.id.about_return_btn);
		r_but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AboutActivity.this.finish();
			}
		});
		
		mtv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 Intent intent=new Intent(android.content.Intent.ACTION_SEND);
		         intent.setType("plain/text");
		         intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[]{"jovisayhehe@gmail.com"});
		         startActivity(intent);
			}
		});
	}

}
