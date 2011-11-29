package tv.acfun;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MoreActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_layout);
		TextView titel = (TextView) findViewById(R.id.title_text);
		titel.setText("更多");
	}

}
