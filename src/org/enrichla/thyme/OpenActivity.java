package org.enrichla.thyme;

import org.enrichla.thyme.ThymeContacts.Criteria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class OpenActivity extends Activity implements View.OnClickListener {
	
	Button btnContacts, btnRoles, btnGroups, btnFindGPS;
	Intent iContacts, iFindGPS;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_open);
		
		findViews();
		setListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open, menu);
		return true;
	}
	
	private void findViews() {
		btnContacts = (Button) findViewById(R.id.btnContacts);
		btnRoles = (Button) findViewById(R.id.btnRoles);
		btnGroups = (Button) findViewById(R.id.btnGroups);
		btnFindGPS = (Button) findViewById(R.id.btnFindMe);
	}
	
	private void setListeners() {
		btnContacts.setOnClickListener(this);
		btnRoles.setOnClickListener(this);
		btnGroups.setOnClickListener(this);
		btnFindGPS.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		Bundle bData = new Bundle();
		switch (v.getId()) {
		case R.id.btnContacts:
			iContacts = new Intent().setClass(this, ThymeContacts.class);
			bData.putSerializable("criteria", Criteria.NAMES);
			iContacts.putExtras(bData);
			startActivity(iContacts);
			break;
		case R.id.btnRoles:
			iContacts = new Intent().setClass(this, ThymeContacts.class);
			bData.putSerializable("criteria", Criteria.ROLES);
			iContacts.putExtras(bData);
			startActivity(iContacts);
			break;
		case R.id.btnGroups:
			iContacts = new Intent().setClass(this, ThymeContacts.class);
			bData.putSerializable("criteria", Criteria.GROUPS);
			iContacts.putExtras(bData);
			startActivity(iContacts);
			break;
		case R.id.btnFindMe:
			iFindGPS = new Intent().setClass(this, ThymeActivity.class);
			startActivity(iFindGPS);
			break;
		default:
			break;
		}
	}

}
