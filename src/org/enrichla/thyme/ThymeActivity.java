package org.enrichla.thyme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ThymeActivity extends Activity {
	
	private static final String TAG = "ThymeActivity";
	
	private static final int UPDATE_LOCATION = 0x01;
	
	private static final int TWO_SECONDS = 1000 * 2;
    private static final int TEN_METERS = 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	private static final String MURSHAW_TOKEN = "cedbc4971aed515dc6d665f95f89e095";
	private static final String mock = "5237 Rosemead Blvd, San Gabriel, CA";
//	private static final String mocks[] = {"fox", "dog", "cat"};
	
	private Context context;
	
	private Handler mLocHandler, httpHandler;
	private Location mLoc;
	private LocationManager mLocationManager;
	private Geocoder gc;
	private double mLat, mLng;
	private double lat, lng;
	
	private TextView tvLocation, tvMock, tvLoading;
	private TextView tvResponse;
	private ProgressBar mProgress;
	
	private ListView lvContacts;
	private ArrayList<HashMap<String, Object>> listItem;
	private SimpleAdapter listItemAdapter;
	private int queryLength;
	
	private JSONObject mJson;
	private ArrayList<String> addresses;
	private TreeMap<Double, HashMap<String, Object>> tree;
	
	private String[] arrFirstName;
	private String[] arrLastName;
	private String[] arrEmail;
	private String[] arrSite;
//	private int[] arrNumber;
	private String[] arrNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thyme_main);
		
		this.context = this;
		findViews();
		
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		httpHandler = new Handler();
//		mLocHandler = new Handler() {
//            @Override
//			public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case UPDATE_LOCATION:
//                    	tvLocation.setText((String) msg.obj);
//                        break;
//                }
//            }
//        };
        
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
		getMenuInflater().inflate(R.menu.thyme, menu);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		initLocationTrack();
//		new GeocodeTask().execute(mock);
		new HttpRequestTask().execute();
//		new AsyncListTask().execute();

		
		
//		Geocoder geo = new Geocoder(this);
//		try {
//			List<Address> list = geo.getFromLocationName(mock, 1);
//			Address add = list.get(0);
//			double lat = add.getLatitude();
//			double lng = add.getLongitude();
//			tvMock.setText("Latitude: " + lat + "\nLongitude: " + lng);
//		} catch (IOException e) {
//			Log.d(TAG, "More problems");
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		mLocationManager.removeUpdates(locListener);
	}

	/**
	 * Find all the view widgets necessary for your UI
	 */
	private void findViews() {
//    	tvLocation = (TextView) findViewById(R.id.gpsInfo);
//    	tvMock = (TextView) findViewById(R.id.mockInfo);
//    	tvResponse = (TextView) findViewById(R.id.test);
    	tvLoading = (TextView) findViewById(R.id.loading);
    	lvContacts = (ListView) findViewById(R.id.list_contacts);
    	mProgress = (ProgressBar) findViewById(R.id.pb_gps);
    }
	
	// Get data from Zoho Creator report
	private JSONObject postData() throws JSONException {
//		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpGet httpget = new HttpGet("https://creator.zoho.com/api/json/blacky/view/Sirius_Report?authtoken=cedbc4971aed515dc6d665f95f89e095&scope=creatorapi&raw=true");
//		httpget.getParams().setParameter("authtoken", MURSHAW_TOKEN);
//		httpget.getParams().setParameter("scope", "creatorapi");
//		httpget.getParams().setParameter("raw", "true");
		
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
//				httpHandler.post(new Runnable() {
//					@Override
//					public void run() {
//						tvResponse.setText(responseString);
//					}
//				});
				
				json = new JSONObject(responseString);
//				String s = json.getJSONArray("Sirius").toString();
//				Log.i("S DATA", s);
//				JSONTokener jsonToken = new JSONTokener(s);
//				Object obj;
//				while (jsonToken.more()) {
//					obj = jsonToken.nextValue();
//					Log.i("DATA", obj.toString());
//					if (obj instanceof JSONArray) {
//						JSONArray result = (JSONArray) obj;
//						parseJSON(result);
//					} else {
//						Log.i("DATA", "NOT JSON Array!");
//					}
//				}
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
	
	private ArrayList<String> parseAddress(JSONObject json) {
		ArrayList<String> add = new ArrayList<String>();
		try {
			JSONTokener token = new JSONTokener(json.getJSONArray("Sirius").toString());
			while (token.more()) {
				Object obj = token.nextValue();
				if (obj instanceof JSONArray) {
					queryLength = ((JSONArray) obj).length();
					for (int i=0; i < queryLength; i++) {
						add.add(((JSONArray) obj).getJSONObject(i).getString("Address")
		    					+ " "
		    					+ ((JSONArray) obj).getJSONObject(i).getString("City")
		    					+ " "
		    					+ ((JSONArray) obj).getJSONObject(i).getString("State1")
		    					+ " "
		    					+ ((JSONArray) obj).getJSONObject(i).getString("Zip"));
					}
				}
			}
		} catch (JSONException je) {
    		Log.e(TAG, "Parse JSON Error:");
    		je.printStackTrace();
    	}
		
		return add;
	}
	
	// Haversine formula to calculate distances between 2 locations given lat/long coordinates
	private double haversine(double lat1, double lng1, double lat2, double lng2) {
		// Radius of Earth: 6371km
		lat1 = Math.toRadians(lat1);
		lng1 = Math.toRadians(lng1);
		lat2 = Math.toRadians(lat2);
		lng2 = Math.toRadians(lng2);
		return 2 * 6371 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat2 - lat1)/2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin((lng2 - lng1)/2), 2)));
	}
	
	private void initLocationTrack() {
		Location gpsLocation = null;
		Location networkLocation = null;
		gpsLocation = requestUpdatesFromProvider(LocationManager.GPS_PROVIDER);
		networkLocation = requestUpdatesFromProvider(LocationManager.NETWORK_PROVIDER);
		
		// If both providers return last known locations, compare the two and use the better
        // one to update the UI.  If only one provider returns a location, use it.
        if (gpsLocation != null && networkLocation != null) {
        	mLoc = getBetterLocation(gpsLocation, networkLocation);
            updateUILocation(mLoc);
        } else if (gpsLocation != null) {
        	mLoc = gpsLocation;
            updateUILocation(mLoc);
        } else if (networkLocation != null) {
        	mLoc = networkLocation;
            updateUILocation(mLoc);
        }
	}
	
	private void updateUILocation(Location location) {
    	// We're sending the update to a handler which then updates the UI with the new
    	// location.
		mLat = location.getLatitude();
		mLng = location.getLongitude();
//    	Message.obtain(mLocHandler,
//    			UPDATE_LOCATION,
//    			"Latitude: " + mLat + "\nLongitude: " + mLng).sendToTarget();
    }
	
	/** Determines whether one Location reading is better than the current Location fix.
	 * Code taken from
	 * http://developer.android.com/guide/topics/location/obtaining-user-location.html
	 *
	 * @param newLocation  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new
	 *        one
	 * @return The better Location object based on recency and accuracy.
	 */
	protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return newLocation;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved.
		if (isSignificantlyNewer) {
			return newLocation;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return currentBestLocation;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return newLocation;
		} else if (isNewer && !isLessAccurate) {
			return newLocation;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return newLocation;
		}
		return currentBestLocation;
	}
	
	/** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
	
	private Location requestUpdatesFromProvider(final String provider) {
        Location location = null;
        if (mLocationManager.isProviderEnabled(provider)) {
            mLocationManager.requestLocationUpdates(provider, TWO_SECONDS, TEN_METERS, locListener);
            location = mLocationManager.getLastKnownLocation(provider);
        }
        return location;
    }
	
	private final LocationListener locListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // A new location update is received.  Do something useful with it.  Update the UI with
            // the location update.
            updateUILocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    
    private class HttpRequestTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
    			mJson = postData();
//    			addresses = parseAddress(json);
//    			Log.i(TAG, "Addresses: " + addresses.toString());
    		} catch (JSONException e) {
    			Log.e(TAG, "JSON Exception Error:");
    			e.printStackTrace();
    		}
			
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void unused) {
			new GeocodeTask().execute();
		}
    }
    
    // AsyncTask pulling contact information
    private class AsyncListTask extends AsyncTask<Void, HashMap<String, Object>, Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			try {
				JSONObject json = postData();
				if (json != null) {
					JSONTokener token = new JSONTokener(json.getJSONArray("Sirius").toString());
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
    
    // AsyncTask for geocoding site addresses
    private class GeocodeTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			gc = new Geocoder(context);
			tree = new TreeMap<Double, HashMap<String, Object>>();
			try {
				if (Geocoder.isPresent()) {
					JSONTokener token = new JSONTokener(mJson.getJSONArray("Sirius").toString());
					while (token.more()) {
						Object obj = token.nextValue();
						if (obj instanceof JSONArray) {
							JSONArray ja = (JSONArray) obj;
							queryLength = ja.length();
							for (int i=0; i < queryLength; i++) {
								String address = ja.getJSONObject(i).getString("Address")
												+ " "
												+ ja.getJSONObject(i).getString("City")
												+ " "
												+ ja.getJSONObject(i).getString("State1")
												+ " "
												+ ja.getJSONObject(i).getString("Zip");
								
								List<Address> place = gc.getFromLocationName(address, 1);
								Address a = place.get(0);
								
								HashMap<String, Object> hmap = new HashMap<String, Object>();
								hmap.put("ItemFirstName", ja.getJSONObject(i).getString("First_Name"));
								hmap.put("ItemLastName", ja.getJSONObject(i).getString("Last_Name"));
								hmap.put("ItemEmail", ja.getJSONObject(i).getString("Email"));
								hmap.put("ItemSite", ja.getJSONObject(i).getString("Location"));
								hmap.put("ItemNumber", ja.getJSONObject(i).getString("Telephone"));
								tree.put(haversine(mLat, mLng, a.getLatitude(), a.getLongitude()), hmap);
							}
						}
					}
				} else {
					Log.d(TAG, "Geocoder not present");
				}
			} catch (IOException e) {
				Log.d(TAG, "IOException with GeocodeTask");
				e.printStackTrace();
			} catch (JSONException je) {
	    		Log.e(TAG, "Parse JSON Error:");
	    		je.printStackTrace();
	    	}
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void unused) {
//			tvMock.setText("Latitude: " + mLat + "\nLongitude: " + mLng);
//			Log.i(TAG + ".TREE", tree.toString());
			
			tvLoading.setVisibility(View.GONE);
			mProgress.setVisibility(View.GONE);
			
			for (Double d : tree.keySet()) {
				listItem.add(tree.get(d));
			}
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
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, final long id) {
					HashMap<String, Object> itemAtPosition = listItem.get(position);
					Toast.makeText(context, itemAtPosition.get("ItemNumber").toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
    }
}
