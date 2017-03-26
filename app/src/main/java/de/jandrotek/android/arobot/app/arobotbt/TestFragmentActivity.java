package de.jandrotek.android.arobot.app.arobotbt;

import android.app.Activity;
import android.os.Bundle;

public class TestFragmentActivity extends Activity {
	public AsciiViewFragment myFragment;
	@Override
	protected void onCreate(Bundle arg0){
		super.onCreate(arg0);
		setContentView(R.layout.activity_for_tests);
	}

}
