package org.enrichla.thyme.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ThymeNetwork {
	String TOKEN = "925a3531d67c8358bbe1903c4649af1a";
	
	public JSONObject postData(String... data) throws JSONException;
	public JSONObject getData() throws JSONException;
	public void parseJSON(JSONArray ja);
}
