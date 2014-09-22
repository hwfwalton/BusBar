package com.example.servtest;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class BusService extends IntentService {
	private Intent intent;
	public BusService() {
		super("BusService");
	}

	@Override
	protected void onHandleIntent(Intent receivedIntent) {
		intent = new Intent(receivedIntent);
		Bundle timesBundle = new Bundle();
		String times = getTimes(receivedIntent.getStringExtra("STOP1"));
		timesBundle.putString(RunService.TIME1, times);
		
		times = getTimes(receivedIntent.getStringExtra("STOP2"));
		timesBundle.putString(RunService.TIME2, times);
		
		intent.putExtras(timesBundle);
		intent.setClass(this, RunService.class);
		startService(intent);
	}
	
	private String getTimes(String xmlURL) {
		String response = "";
		try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        URL myURL = new URL(xmlURL);
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
			Log.i("BUSSERVICE", "malformedURL");
			intent.setAction(RunService.ERROR);
			response = "No Connection";
			e.printStackTrace();
		}
		return response;
	}
}
