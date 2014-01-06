package org.enrichla.thyme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ThymeContacts extends Activity {
	
	private static final String TAG = "ThymeContacts";
	
	private Context context;
	
	private TextView tvLoading;
	
	private ListView lvContacts;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter listItemAdapter;
	private int queryLength;
	
	private String[] arrFirstName;
	private String[] arrLastName;
	private String[] arrEmail;
	private String[] arrSite;
//	private int[] arrNumber;
	private String[] arrNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_contacts);
		
		this.context = this;
		findViews();
		
		listItem = new ArrayList<HashMap<String, Object>>();
        listItemAdapter = new SimpleAdapter(this,
        									listItem,
        									R.layout.thyme_listview,
        									new String[] {	"ItemFirstName",
        													"ItemLastName",
        													"ItemEmail",
        													"ItemSite",
        													"ItemNumber" },
        									new int[] {	R.id.lvFirstName,
        												R.id.lvLastName,
        												R.id.lvEmail,
        												R.id.lvSite,
        												R.id.lvNumber});
		lvContacts.setAdapter(listItemAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thyme_contacts, menu);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		new AsyncListTask().execute();
	}
	
	/**
	 * Find all the view widgets necessary for your UI
	 */
	private void findViews() {
    	tvLoading = (TextView) findViewById(R.id.contacts_loading);
    	lvContacts = (ListView) findViewById(R.id.list_contacts);
    }
	
	// Get data from Zoho Creator report
	private void postData() throws JSONException {
//			HttpClient httpclient = HttpClientBuilder.create().build();
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpGet httpget = new HttpGet("https://creator.zoho.com/api/json/blacky/view/Sirius_Report?authtoken=cedbc4971aed515dc6d665f95f89e095&scope=creatorapi&raw=true");
//			httpget.getParams().setParameter("authtoken", MURSHAW_TOKEN);
//			httpget.getParams().setParameter("scope", "creatorapi");
//			httpget.getParams().setParameter("raw", "true");
		
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
				final String responseString = sb.toString();
//					httpHandler.post(new Runnable() {
//						@Override
//						public void run() {
//							tvResponse.setText(responseString);
//						}
//					});
				JSONObject json = new JSONObject(responseString);
				String s = json.getJSONArray("Sirius").toString();
				Log.i("S DATA", s);
				JSONTokener jsonToken = new JSONTokener(s);
				Object obj;
				while (jsonToken.more()) {
					obj = jsonToken.nextValue();
					Log.i("DATA", obj.toString());
					if (obj instanceof JSONArray) {
						JSONArray result = (JSONArray) obj;
						parseJSON(result);
					} else {
						Log.i("DATA", "NOT JSON Array!");
					}
				}
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Client Protocol Exception:");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception:");
			e.printStackTrace();
		}
	}
	
	private void parseJSON(JSONArray ja) {
		queryLength = ja.length();
    	arrFirstName = new String[queryLength];
    	arrLastName = new String[queryLength];
    	arrEmail = new String[queryLength];
    	arrSite = new String[queryLength];
//    	arrNumber = new int[queryLength];
    	arrNumber = new String[queryLength];
    	try {
    		for (int i=0; i < ja.length(); i++) {
    			arrFirstName[i] = ja.getJSONObject(i).getString("First_Name");
    			arrLastName[i] = ja.getJSONObject(i).getString("Last_Name");
    			arrEmail[i] = ja.getJSONObject(i).getString("Email");
    			arrSite[i] = ja.getJSONObject(i).getString("Location");
//    			arrNumber[i] = ja.getJSONObject(i).getInt("Telephone");
    			arrNumber[i] = ja.getJSONObject(i).getString("Telephone");
    		}
    	} catch (JSONException je) {
    		Log.e(TAG, "Parse JSON Error:");
    		je.printStackTrace();
    	}
	}
	
	// AsyncTask pulling contact information
    private class AsyncListTask extends AsyncTask<Void, HashMap<String, Object>, Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			try {
    			postData();
    		} catch (JSONException e) {
    			Log.e(TAG, "JSON Exception Error:");
    			e.printStackTrace();
    		}
			for (int i = 0; i < queryLength; i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemFirstName", arrFirstName[i]);
				map.put("ItemLastName", arrLastName[i]);
				map.put("ItemEmail", arrEmail[i]);
				map.put("ItemSite", arrSite[i]);
				map.put("ItemNumber", arrNumber[i]);
				publishProgress(map);
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(HashMap<String, Object>... map) {
			listItem.add(map[0]);
			listItemAdapter.setViewBinder(new ViewBinder() {    
	            
	            @Override
				public boolean setViewValue(View view, Object data, String textRepresentation) {    
	                if (view instanceof ImageView  && data instanceof Bitmap) {    
	                    ImageView iv = (ImageView) view;
	                    iv.setImageBitmap((Bitmap) data);    
	                    return true;    
	                } else {
	                	return false;
	                }
	            }    
	        });
			listItemAdapter.notifyDataSetChanged();

			lvContacts.setOnItemClickListener(new OnItemClickListener() {
				//String bImage = "";
//				String bName = "";
//				String bDepart = "";
//				int bFor = 0;
//				int bAgainst = 0;
//				Bitmap bImage;
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, final long id) {
					HashMap<String, Object> itemAtPosition = listItem.get(position);
//					
//					chosenPosition = position;
//					
//					bName = itemAtPosition.get("ItemName").toString();
//					setTitle("¿ï¨ú¤F" + bName);
//					//bDepart = itemAtPosition.get("ItemDepart").toString();
//					bFor = (Integer) itemAtPosition.get("ItemFor");
//					bAgainst = (Integer) itemAtPosition.get("ItemAgainst");
//					bImage = (Bitmap) itemAtPosition.get("ItemImage");
//					Intent newAct = new Intent();
//					newAct.setClass(ListProfessor.this, Induvidual.class);
//					Bundle bData = new Bundle();
//					
//					bData.putString("bName", bName);
//					//bData.putString("bDepart", bDepart);
//					bData.putString("bDepart", depts);
//					bData.putInt("bFor", bFor);
//					bData.putInt("bAgainst", bAgainst);
//					//bData.putString("dept", "cs");
//					Log.i(TAG, "bDepart: " + bDepart);
//					newAct.putExtras(bData);
//					newAct.putExtra("bImage", bImage);
//					startActivityForResult(newAct, INDIVIDUAL_REQUEST);
					
					Toast.makeText(context, itemAtPosition.get("ItemNumber").toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			tvLoading.setVisibility(View.GONE);
		}
    }

}
