package org.enrichla.thyme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.enrichla.thyme.network.ThymeNetwork;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ThymeUpdateHumans extends Activity implements ThymeNetwork, View.OnClickListener, OnItemSelectedListener {

	public static final String TAG = "ThymeUpdateHumans";
	
	private Context context;
	
	private Spinner[] spinRoles = new Spinner[10];
	private String[] arrRoles;
	private int[] roleID;
	
	private Button btnSendUpdateFormHumans;
	private Button btnAddRole, btnRemoveRole;
	private Button btnAddGroup;
	private EditText etUpdateFirstName, etUpdateLastName, etUpdateEmail;
	private LinearLayout llUpdateRoleSection;
	
	private int roleCount;
	private int groupCount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_update_humans);
		
		this.context = this;
		findViews();
		setListeners();
		setIDs();
		roleCount = 0;
		
		new AsyncListTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thyme_update_humans, menu);
		return true;
	}
	
	private void findViews() {
//		spinRoles = (Spinner) findViewById(R.id.spinUpdateRole);
		btnAddRole = (Button) findViewById(R.id.btnAddRole);
		btnRemoveRole = (Button) findViewById(R.id.btnRemoveRole);
		btnAddGroup = (Button) findViewById(R.id.btnAddGroup);
		llUpdateRoleSection = (LinearLayout) findViewById(R.id.llUpdateRoleSection);
		
		etUpdateFirstName = (EditText) findViewById(R.id.etUpdateFirstName);
		etUpdateLastName = (EditText) findViewById(R.id.etUpdateLastName);
		etUpdateEmail = (EditText) findViewById(R.id.etUpdateEmail);
		btnSendUpdateFormHumans = (Button) findViewById(R.id.btnSendUpdateFormHumans);
	}
	
	private void setListeners() {
		btnAddRole.setOnClickListener(this);
		btnRemoveRole.setOnClickListener(this);
		btnAddGroup.setOnClickListener(this);
		btnSendUpdateFormHumans.setOnClickListener(this);
	}
	
	private void setIDs() {
		roleID = new int[10];
		roleID[0] = R.id.roleSpinner1;
		roleID[1] = R.id.roleSpinner2;
		roleID[2] = R.id.roleSpinner3;
		roleID[3] = R.id.roleSpinner4;
		roleID[4] = R.id.roleSpinner5;
		roleID[5] = R.id.roleSpinner6;
		roleID[6] = R.id.roleSpinner7;
		roleID[7] = R.id.roleSpinner8;
		roleID[8] = R.id.roleSpinner9;
		roleID[9] = R.id.roleSpinner10;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAddRole:
			if (roleCount < 10) {	// No more than 10 roles
				LinearLayout newLL = new LinearLayout(this);	// Create a new horizontal linear layout
				TextView newTV = new TextView(this);			// Create a new textview
				Spinner newSpinner = new Spinner(this);			// Create a new spinner
				LinearLayout.LayoutParams paramsLL = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				LinearLayout.LayoutParams paramsSpinner = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
				paramsText.weight = 1;
				paramsSpinner.weight = 3;
				newLL.setOrientation(LinearLayout.HORIZONTAL);
				newTV.setText("Role");
				newTV.setGravity(Gravity.RIGHT);
				newSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrRoles));
				newSpinner.setId(roleID[roleCount]);		// Set the ID of the new spinner so we can get the value later
				spinRoles[roleCount] = newSpinner;
				newSpinner.setOnItemSelectedListener(this);
				newLL.addView(newTV, paramsText);			// Add the textview to the new linear layout
				newLL.addView(newSpinner, paramsSpinner);	// Add the spinner to the new linear layout
				llUpdateRoleSection.addView(newLL, paramsLL);	// Add the linear layout to the filler linear layout
				roleCount++;	// Increment roleCount by 1
			}
			break;
		case R.id.btnRemoveRole:
			if (roleCount > 0) {
				llUpdateRoleSection.removeViewAt(roleCount-1);
				roleCount--;	// Decrement roleCount by 1
			}
			break;
		case R.id.btnAddGroup:
			break;
		case R.id.btnSendUpdateFormHumans:
			launchProgressDialog();
			break;
		default:
			break;
		}
	}
	
	private void launchProgressDialog() {
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Update", "Sending new entry...");
		progressDialog.setCancelable(true);
//		final String rd = roleData;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String response = "";
				try {
					HashMap<String, Boolean> hmRoles = new HashMap<String, Boolean>();
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < roleCount; i++) {
						hmRoles.put((String)spinRoles[i].getSelectedItem(), true);
						Set<String> set = hmRoles.keySet();
						for (String s : set) {
							sb.append(s);
							sb.append(',');
						}
						sb.deleteCharAt(sb.length()-1);
					}
					JSONObject json;
					if (roleCount == 0)
						json = postData(etUpdateFirstName.getText().toString(), etUpdateLastName.getText().toString(), etUpdateEmail.getText().toString());
					else
						json = postData(etUpdateFirstName.getText().toString(), etUpdateLastName.getText().toString(), etUpdateEmail.getText().toString(), sb.toString());
	    			if (json != null) {
	    				response = json.getJSONArray("formname").getJSONArray(1).getJSONObject(1).getString("Status");
	    				
					} else {
						// Some manner of server error since there was no response
					}
	    		} catch (JSONException e) {
	    			Log.e(TAG, "JSON Exception Error:");
	    			e.printStackTrace();
	    		} finally {
	    			if (response.isEmpty()) {
	    				// Connection was not made or something
	    			} else if (response.equals("Success")) {
	    				// Remark success
	    			} else if (response.equals("Failure")) {
	    				// Remark failure
	    			}
	    		}
				progressDialog.dismiss();
			}
		}).start();
	}
	
	// Post data to Zoho Creator report
	public JSONObject postData(String... data) throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		
		String zview = "Human";
//		https://creator.zoho.com/api/ashleyritarivers/json/thyme/form/Human/record/add/
		String uri = "https://creator.zoho.com/api/ashleyritarivers/json/thyme/form/Human/record/add/";//?" +
//				"authtoken=" + TOKEN +
//				"&scope=creatorapi" +
//				"&raw=true" +
//				"&First_Name=" + data[0] +
//				"&Last_Name=" + data[1] +
//				"&Email=" + data[2];
//		
//		if (data.length == 4) {
//			uri += "&Role=" + data[3];
//		}
		
		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("User-Agent", "Mozilla/5.0");
		 
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("authtoken", TOKEN));
		urlParameters.add(new BasicNameValuePair("scope", "creatorapi"));
		urlParameters.add(new BasicNameValuePair("raw", "true"));
		urlParameters.add(new BasicNameValuePair("First_Name", data[0]));
		urlParameters.add(new BasicNameValuePair("Last_Name", data[1]));
		urlParameters.add(new BasicNameValuePair("Email", data[2]));
		if (data.length == 4)
			urlParameters.add(new BasicNameValuePair("Role", data[3]));
 
		try {
			httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		HttpParams params = httppost.getParams();
//		params.setParameter("authtoken", TOKEN);
//		params.setParameter("scope", "creatorapi");
//		params.setParameter("raw", "true");
//		params.setParameter("First_Name", data[0]);
//		params.setParameter("Last_Name", data[1]);
//		params.setParameter("Email", data[2]);
//		if (data.length == 4)
//			params.setParameter("Role", data[3]);
//		httppost.setParams(params);
		
		JSONObject json = null;
		
		try {
			HttpResponse response = httpclient.execute(httppost);
			
			if(response != null) {
				InputStream is = response.getEntity().getContent();
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
				StringBuilder sb = new StringBuilder();
	
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				String responseString = sb.toString();
				Log.i(TAG, responseString);
				json = new JSONObject(responseString);
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Client Protocol Exception:");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception:");
			e.printStackTrace();
		}
		return json;
	}
	
	// Get data from Zoho Creator report
	public JSONObject getData() throws JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		
		String zview = "Roles";
		
		String uri = "https://creator.zoho.com/api/json/thyme/view/" + zview + "?" +
						"authtoken=" + TOKEN +
						"&scope=creatorapi" +
						"&raw=true";
		
		Log.i(TAG, uri);
		HttpGet httpget = new HttpGet(uri);
//		HttpParams params = httpget.getParams();
//		params.setParameter("authtoken", TOKEN);
//		params.setParameter("scope", "creatorapi");
//		params.setParameter("raw", "true");
//		httpget.setParams(params);
		
		JSONObject json = null;
		
		try {
			HttpResponse response = httpclient.execute(httpget);
			
			if(response != null) {
				InputStream is = response.getEntity().getContent();
	
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
				StringBuilder sb = new StringBuilder();
	
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				String responseString = sb.toString();
				json = new JSONObject(responseString);
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Client Protocol Exception:");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception:");
			e.printStackTrace();
		}
		return json;
	}
		
	@Override
	public void parseJSON(JSONArray ja) {
		ArrayList<String> roles = new ArrayList<String>();
		try {
    		for (int i=0; i < ja.length(); i++) {
    			roles.add(ja.getJSONObject(i).getString("Role"));
    		}
    		Collections.sort(roles);
    	} catch (JSONException je) {
    		Log.e(TAG, "Parse JSON Error:");
    		je.printStackTrace();
    	} finally {
    		arrRoles = roles.toArray(new String[roles.size()]);
    	}
	}

	// AsyncTask pulling contact information
    public class AsyncListTask extends AsyncTask<Void, HashMap<String, Object>, Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			try {
    			JSONObject json = getData();
    			if (json != null) {
    				String resultKey = "Role";
					JSONTokener token = new JSONTokener(json.getJSONArray(resultKey).toString());
					Object obj;
					while (token.more()) {
						obj = token.nextValue();
//						Log.i("DATA", obj.toString());
						if (obj instanceof JSONArray) {
							JSONArray result = (JSONArray) obj;
							parseJSON(result);
						} else {
							Log.i("DATA", "NOT JSON Array!");
						}
					}
				} else {
					// Some manner of server error since there was no response
				}
    		} catch (JSONException e) {
    			Log.e(TAG, "JSON Exception Error:");
    			e.printStackTrace();
    		} finally {
    			
    		}

			return null;
		}
		
		@Override
		protected void onProgressUpdate(HashMap<String, Object>... map) {
			
		}
		
		@Override
		protected void onPostExecute(Void unused) {
//			spinRoles.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, arrRoles));
		}
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Log.i(TAG, "Selected: " + parent.getItemAtPosition(pos));
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
    
}
