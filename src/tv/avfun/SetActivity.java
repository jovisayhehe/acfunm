package tv.avfun;

import tv.avfun.R;
import tv.acfun.util.Util;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class SetActivity extends Activity {
	private RadioGroup radiog;
	private RadioButton radiod;
	private RadioButton radioe;
	private RadioButton radiof;
	private SharedPreferences settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.set_layout);
		radiog = (RadioGroup) this.findViewById(R.id.set_radio_g);
		radiod = (RadioButton) this.findViewById(R.id.set_radio_dif);
		radioe = (RadioButton) this.findViewById(R.id.set_radio_ex);
		radiof = (RadioButton) this.findViewById(R.id.set_radio_flash);
		
		if(Util.isSmallertfroyo()){
			radiof.setVisibility(View.GONE);
		}
		switch (MainActivity.playcode) {
		case 0:
			radiod.setChecked(true);
			break;
		case 1:
			radioe.setChecked(true);
			break;
		case 2:
			radiof.setChecked(true);
			break;
		default:
			break;
		}
		
		Button r_but = (Button) this.findViewById(R.id.set_return_btn);
		r_but.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SetActivity.this.finish();
			}
		});
		
		
		settings = getSharedPreferences("config", 0);
		
		radiog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.set_radio_dif:
					settings.edit().putInt("playcode", 0).commit();
					MainActivity.playcode=0;
					break;
				case R.id.set_radio_ex:
					settings.edit().putInt("playcode", 1).commit();
					MainActivity.playcode=1;
					break;
				case R.id.set_radio_flash:
					settings.edit().putInt("playcode", 2).commit();
					MainActivity.playcode=2;
					break;
				default:
					break;
				}
			}
		});
		
	}

}
