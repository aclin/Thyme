package org.enrichla.thyme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.enrichla.thyme.network.ThymeNetwork;
import org.enrichla.thyme.util.Entry;
import org.enrichla.thyme.util.SimpleCustomAdapter;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ThymeContacts extends Activity implements ThymeNetwork, View.OnClickListener, TextView.OnEditorActionListener {
	
	private static final String TAG = "ThymeContacts";
	
	private static final String TOKEN = "925a3531d67c8358bbe1903c4649af1a";
	
	private static final String EQUALS = "%3D";
	private static final String QUOTE = "%22";
	private static final String OR = "%7C%7C";
	private static final String AND = "%26%26";
	
	private static final String OP_CONTAINS = "26";
	private static final String OP_STARTS_WITH = "24";
	
	public enum Criteria {
		NAMES(""), ROLES("Role"), GROUPS("Group");
		
		private String name;
		
		private Criteria(String c) {
			this.name = c;
		}
		
		public String getName() {
			return this.name;
		}
	};
	
	private Context context;
	
	public Criteria mCriteria;
	
	private Button btnSearch;
	private TextView tvLoading;
	private LinearLayout llSearch;
	private EditText etSearchFirstName;
	private EditText etSearchLastName;
	private EditText etSearchCriteria;
	private TextView tvCriteria;
	
	private ListView lvContacts;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleCustomAdapter listItemAdapter;
	private int queryLength;
	
	private ArrayList<Entry> entries;
	private String[] arrFirstName;
	private String[] arrLastName;
	private String[] arrEmail;
	private String[] arrSite;
//	private int[] arrNumber;
	private String[] arrNumber;
	
	private String queryFName;
	private String queryLName;
	private String query;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_contacts);
		
		this.context = this;
		findViews();
    	setListeners();
		
		Bundle bData = this.getIntent().getExtras();
		mCriteria = (Criteria) bData.getSerializable("criteria");
		
		switch (mCriteria) {
		case NAMES:
			llSearch.addView(getLayoutInflater().inflate(R.layout.linearlayout_search_name, null));
			etSearchFirstName = (EditText) findViewById(R.id.etSearchFirstName);
	    	etSearchLastName = (EditText) findViewById(R.id.etSearchLastName);
			etSearchFirstName.setOnEditorActionListener(this);
			etSearchLastName.setOnEditorActionListener(this);
			break;
		default:
			llSearch.addView(getLayoutInflater().inflate(R.layout.linearlayout_search_other, null));
			etSearchCriteria = (EditText) findViewById(R.id.etSearchCriteria);
			etSearchCriteria.setOnEditorActionListener(this);
			tvCriteria = (TextView) findViewById(R.id.tvSearchCriteria);
			tvCriteria.setText(mCriteria.getName());
			break;
		}
		
		entries = new ArrayList<Entry>();
		listItem = new ArrayList<HashMap<String, Object>>();
        listItemAdapter = new SimpleCustomAdapter(this,
        									listItem,
        									R.layout.thyme_listview,
        									new String[] {	"ItemFirstName",
        													"ItemLastName",
        													"ItemEmail",
        													"ItemSite",
        													"ItemRole",
        													"ItemMobile" },
        									new int[] {	R.id.lvFirstName,
        												R.id.lvLastName,
        												R.id.lvEmail,
        												R.id.lvSite,
        												R.id.lvRole,
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
		
		//new AsyncListTask().execute();
	}
	
	/**
	 * Find all the view widgets necessary for your UI
	 */
	private void findViews() {
    	tvLoading = (TextView) findViewById(R.id.contacts_loading);
    	btnSearch = (Button) findViewById(R.id.btnSearch);
    	lvContacts = (ListView) findViewById(R.id.list_contacts);
    	llSearch = (LinearLayout) findViewById(R.id.llSearchCriteria);
    	
//    	etSearchFirstName.setOnEditorActionListener(new OnEditorActionListener() {
//    	    @Override
//    	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//    	        boolean handled = false;
//    	        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//    	        	doSearch();
//    	            handled = true;
//    	        }
//    	        return handled;
//    	    }
//    	});
//    	
//    	etSearchLastName.setOnEditorActionListener(new OnEditorActionListener() {
//    	    @Override
//    	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//    	        boolean handled = false;
//    	        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//    	        	doSearch();
//    	            handled = true;
//    	        }
//    	        return handled;
//    	    }
//    	});
    }
	
	private void setListeners() {
		btnSearch.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSearch:
			doSearch();
			break;
		}
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			doSearch();
			handled = true;
		}
		return handled;
	}
	
	public void doSearch() {
//		String[] queryFirstName = etSearchFirstName.getText().toString().split(" ");
//		String[] queryLastName = etSearchLastName.getText().toString().split(" ");
//		Log.i(TAG, "etSearchFirstName: " + etSearchFirstName.getText().toString());
//		Log.i(TAG, "etSearchLastName: " + etSearchFirstName.getText().toString());
//		Log.i(TAG, "queryFirstName: "+ queryFirstName[0]);
//		Log.i(TAG, "queryLastName: "+ queryLastName[0]);
//		if (queryFirstName.length == 0) {
//			
//		}
		listItem.clear();
		entries.clear();
		tvLoading.setVisibility(View.VISIBLE);
		
		String tmp;
		switch (mCriteria) {
		case NAMES:
			queryFName = etSearchFirstName.getText().toString().trim();
			queryLName = etSearchLastName.getText().toString().trim();
			
			tmp = queryFName.replaceAll("\\s+", "");
			if (tmp.isEmpty())
				queryFName = tmp;
			else
				queryFName = queryFName.replaceAll("\\s+", "+");
			
			tmp = queryLName.replaceAll("\\s+", "");
			if (tmp.isEmpty())
				queryLName = tmp;
			else
				queryLName = queryLName.replaceAll("\\s+", "+");
			
			Log.i(TAG, "queryFName: "+ queryFName);
			Log.i(TAG, "queryLName: "+ queryLName);
			break;
		default:
			query = etSearchCriteria.getText().toString().trim();
			
			tmp = query.replaceAll("\\s+", "");
			if (tmp.isEmpty())
				query = tmp;
			else
				query = query.replaceAll("\\s+", "+");
			
			Log.i(TAG, "query: " + query);
			break;
		}
		
		new AsyncListTask().execute();
	}
	
	@Override
	public JSONObject postData(String... data) throws JSONException {
		return null;
	}
	
	// Get data from Zoho Creator report
	public JSONObject getData() throws JSONException {
//			HttpClient httpclient = HttpClientBuilder.create().build();
		HttpClient httpclient = new DefaultHttpClient();
		
//		HttpGet httpget = new HttpGet("https://creator.zoho.com/api/json/blacky/view/Sirius_Report?authtoken=cedbc4971aed515dc6d665f95f89e095&scope=creatorapi&raw=true");
//		HttpGet httpget = new HttpGet("https://creator.zoho.com/api/json/thyme/view/humans");
//		httpget.getParams().setParameter("authtoken", TOKEN);
//		httpget.getParams().setParameter("scope", "creatorapi");
//		httpget.getParams().setParameter("raw", "true");
		
		String zview = "Humans";
		
		String uri = "https://creator.zoho.com/api/json/thyme/view/" + zview + "?" +
						"authtoken=" + TOKEN +
						"&scope=creatorapi&raw=true";
		
		switch (mCriteria) {
		case NAMES:
			if (!queryFName.isEmpty()) {
				uri += "&First_Name=" + queryFName + "&First_Name_op=" + OP_STARTS_WITH;
			}
			if (!queryLName.isEmpty()) {
				uri += "&Last_Name=" + queryLName + "&Last_Name_op=" + OP_STARTS_WITH;
			}
			break;
		case ROLES:
			if (!query.isEmpty())
				uri += "&Role=" + query + "&Role_op=" + OP_CONTAINS;
			break;
		case GROUPS:
			if (!query.isEmpty())
				uri += "&Group=" + query + "&Group_op=" + OP_CONTAINS;
			break;
		}
		
		/*for (int i = 0; i < query.length-1; i++) {
			uri += "First_Name" + EQUALS +
					QUOTE + query[i] + QUOTE +
					OR +
					"Last_Name" + EQUALS 
					+ QUOTE + query[i] + QUOTE + OR;
		}
		uri += "First_Name" + EQUALS +
				QUOTE + query[query.length-1] + QUOTE +
				OR +
				"Last_Name" + EQUALS +
				QUOTE + query[query.length-1] + QUOTE + ")";*/
		
		Log.i(TAG, uri);
		HttpGet httpget = new HttpGet(uri);
		
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
//					httpHandler.post(new Runnable() {
//						@Override
//						public void run() {
//							tvResponse.setText(responseString);
//						}
//					});
				json = new JSONObject(responseString);
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Client Protocol Exception:");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "IO Exception:");
			e.printStackTrace();
		} finally {
			
		}
		
		return json;
	}
	
	public void parseJSON(JSONArray ja) {
		queryLength = ja.length();
    	try {
    		for (int i=0; i < ja.length(); i++) {
    			Entry person = new Entry();
        		person.fname = ja.getJSONObject(i).getString("First_Name");
        		person.lname = ja.getJSONObject(i).getString("Last_Name");
    			person.email = ja.getJSONObject(i).getString("Email");
    			String s = ja.getJSONObject(i).getString("Site");
    			person.site = s.substring(1, s.length()-1);
    			String r = ja.getJSONObject(i).getString("Role");
    			person.role = r.substring(1, r.length()-1);
    			person.mobile = ja.getJSONObject(i).getString("Mobile");
    			entries.add(person);
    			
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
    			JSONObject json = getData();
    			if (json != null) {
    				String resultKey = "Human";
					JSONTokener token = new JSONTokener(json.getJSONArray(resultKey).toString());
					Object obj;
					while (token.more()) {
						obj = token.nextValue();
						Log.i("DATA", obj.toString());
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
    		}
			Collections.sort(entries, Entry.COMPARE_FNAME);
			for (Entry e : entries) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemFirstName", e.fname);
				map.put("ItemLastName", e.lname);
				map.put("ItemEmail", e.email);
				map.put("ItemSite", e.site);
				map.put("ItemRole", e.role);
				map.put("ItemMobile", e.mobile);
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
					
					Toast.makeText(context, itemAtPosition.get("ItemMobile").toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			tvLoading.setVisibility(View.GONE);
		}
    }

}
