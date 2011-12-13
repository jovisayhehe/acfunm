package tv.acfun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_layout);
		TextView titel = (TextView) findViewById(R.id.title_text);
		titel.setText("更多");
		
		Button his_btn = (Button) findViewById(R.id.more_his_btn);
		Button set_btn = (Button) findViewById(R.id.more_set_btn);
		Button abt_btn = (Button) findViewById(R.id.more_about_btn);
		
		
		his_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MoreActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
		});
		
		set_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MoreActivity.this, SetActivity.class);
				startActivity(intent);
			}
		});
		
		abt_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MoreActivity.this, AboutActivity.class);
				startActivity(intent);
			}
		});
	}

}
