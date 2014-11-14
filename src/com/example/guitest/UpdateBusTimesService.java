package com.example.guitest;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import static com.example.guitest.Constants.*;

public class UpdateBusTimesService extends IntentService {
	private String stopUrl, stopTimes;
	private boolean connectionError = false;

	public UpdateBusTimesService() {
		super("UpdateBusTimesService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("UPDATE_TIMES_SERVICE: ", "Intent Received in UpdateBusTimesService");
		
		stopUrl = intent.getStringExtra(STOP_URL_EXTRA);
		stopTimes = getBusTimes(stopUrl);
		
		if (connectionError) { intent.setAction(STOP_UPDATE_ERROR); }
		
		intent.putExtra(STOP_TIMES_EXTRA, stopTimes);
		intent.setClass(getApplicationContext(), BackgroundService.class);
		startService(intent);
		
	}
	
	private String getBusTimes(String url) {
		String response = "";
		try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        URL myURL = new URL(url);
	        Document doc = builder.parse(myURL.openStream());
	        NodeList predictionsList = doc.getElementsByTagName("prediction"); // get all the prediction nodes

	        // Parse out the prediction times, unless there are none
	        if (predictionsList.getLength() != 0) {
		        for (int i = 0; i < predictionsList.getLength(); i++) {
		        	Element e = (Element) predictionsList.item(i);
		        	response += e.getAttribute("minutes") + ", ";
		        }
	        } else {
	        	response = "No busses";
	        }
	        Log.i("BUS_SERVICE", "Successfully pulled times: " + response);

		} catch (Exception e) {
			Log.i("BUS_SERVICE", "malformedURL");
			connectionError = true;
			response = "No Connection";
			e.printStackTrace();
		}
		return response;
	}
}
