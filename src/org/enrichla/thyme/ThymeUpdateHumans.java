package org.enrichla.thyme;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ThymeUpdateHumans extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_update_humans);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thyme_update_humans, menu);
		return true;
	}

}
