package com.example.guitest;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.http.protocol.RequestContent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.guitest.Constants.*;

// TO DO:
// Consider reworking how I pull the xml data. Currently pretty hacky. Look into Cursors and built in xml parser
// Use a SAX parser or xmlPullParser rather than a DOM parser. way less memory usage.
// http://code.tutsplus.com/tutorials/android-sdk-build-a-simple-sax-parser--mobile-9041
// On a related note, rework the downloading of the title and tag data. Should download both at once and store tags for later.
// Store the title and tag arrays for later use so they they don't have to be downloaded again every time the user makes a change
// Use SharedPreferences to store the Agency, Route, and Stop info
// Download the schedule data for the user's stops and switch to using that when there's no connection
// Don't auto select first item in spinner
// http://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one

public class MainActivity extends ActionBarActivity {

	
	private String stop1Url;
	private String stop2Url;
	private String stop1Name;
	private String stop2Name;
	
	private String selectedAgencyName;
	private String selectedRouteName;
	private String selectedStopName;
	private String agencyTag;
	private String routeTag;
	private String stopTag;
	
	private boolean stop1Ready = false;
	private boolean stop2Ready = false;
	
	private ArrayList<String> agencyNameList;
	private ArrayList<String> routeNameList;
	private ArrayList<String> stopNameList;
	
	private ArrayList<String> agencyTagList;
	private ArrayList<String> routeTagList;
	private ArrayList<String> stopTagList;
	
	private TextView agencyView;
	private TextView routeView;
	private TextView stopView;
	

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		

		agencyView = (TextView) findViewById(R.id.selectAgencyButton);
		routeView = (TextView) findViewById(R.id.selectRouteButton);
		stopView = (TextView) findViewById(R.id.selectStopButton);

		setViewText(agencyView, AGENCY_VIEW_PROMPT);
		setViewText(routeView, ROUTE_VIEW_PROMPT);
		setViewText(stopView, STOP_VIEW_PROMPT);
		
		// To begin, populate the agencies list.
		populateList(AGENCIES);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		int selectionIndex;
		if (resultCode == RESULT_CANCELED) {
			Log.i("RESULT_CANCELLED", "User returned from listview without making a selection");
			return;
		}
		
		selectionIndex = data.getIntExtra(USER_SELECTION_INDEX_EXTRA, 0);
		setTag(requestCode, selectionIndex);
		
	}
	
	public void buttonPress(View v) {
		switch(v.getId()) {
			case R.id.setStop1Button:
				if (checkTags()) { 
					setUserStop(USERSTOP1);
					stop1Name = selectedStopName;
					stop1Ready = true;
					Toast.makeText(getApplicationContext(), "Stop 1 Set!" , Toast.LENGTH_SHORT).show();
				}
				else { Toast.makeText(getApplicationContext(), "Verify you've properly selected all criteria." , Toast.LENGTH_SHORT).show(); }
				break;
			case R.id.setStop2Button:
				if (checkTags()) {
					setUserStop(USERSTOP2);
					stop2Name = selectedStopName;
					stop2Ready = true;
					Toast.makeText(getApplicationContext(), "Stop 2 Set!" , Toast.LENGTH_SHORT).show();
				}
				else { Toast.makeText(getApplicationContext(), "Verify you've properly selected all criteria." , Toast.LENGTH_SHORT).show(); }
				break;
			case R.id.startServiceButton:
				// Break early if either stop hasn't been set
				if (!(stop1Ready && stop2Ready)) {
					Toast.makeText(getApplicationContext(), "One of your stops is not set." , Toast.LENGTH_SHORT).show();
					break; }
				
				// Create the intent to start our service and attach the urls and names of the stops
				Intent startServiceIntent = new Intent(this, BackgroundService.class);
				startServiceIntent.putExtra(STOP1_NAME, stop1Name);
				startServiceIntent.putExtra(STOP2_NAME, stop2Name);
				startServiceIntent.putExtra(STOP1_URL_EXTRA, stop1Url);
				startServiceIntent.putExtra(STOP2_URL_EXTRA, stop2Url);
				
				startServiceIntent.setAction(START_SERVICE);
				startService(startServiceIntent);
				Log.i("MainActivity", "intent sent from Main to BackgroundService");
				break;
			case R.id.stopServiceButton:
				Intent stopServiceIntent = new Intent(this, BackgroundService.class);
				stopService(stopServiceIntent);
				break;
				
			case R.id.selectAgencyButton:
				if (!listReady(agencyView)) { break; }
				goToListView(AGENCIES);
				break;
			case R.id.selectRouteButton:
				if (!listReady(routeView)) { break; }
				goToListView(ROUTES);
				break;
			case R.id.selectStopButton:
				if (!listReady(stopView)) { break; }
				goToListView(STOPS);
				break;
		}
	}

	//=====================================================================================//
	// Tag manipulation functions. Used to set, verify, or reset the component tags.
	
	// Sets the specified component name and tag variable to the value at the given index in the name and tag lists
	// For AGENCIES and ROUTES, also then populates the next list based on the user's selection.
	private void setTag(int componentIndex, int tagIndex) {
		switch(componentIndex) {
		case AGENCIES: agencyTag = agencyTagList.get(tagIndex);
			selectedAgencyName = agencyNameList.get(tagIndex);
			setViewText(agencyView, selectedAgencyName);
			resetTags(false, true, true);
			populateList(ROUTES);
			break;
		case ROUTES: routeTag = routeTagList.get(tagIndex);
			selectedRouteName = routeNameList.get(tagIndex);
			setViewText(routeView, selectedRouteName);
			resetTags(false, false, true);
			populateList(STOPS);
			break;
		case STOPS: stopTag = stopTagList.get(tagIndex);
			selectedStopName = stopNameList.get(tagIndex);
			setViewText(stopView, selectedStopName);
			break;
		}
	}
	
	// Used to check that all the tags are set before setting a stop tag to give to the service
	private boolean checkTags() {
		boolean checkVal = true;
		
		if (agencyTag == null) { checkVal = false; }
		else if (routeTag == null) { checkVal = false; }
		else if (stopTag == null) { checkVal = false; }
		
		return checkVal;
	}
	
	// Used to reset subsequent selections if a user changes an earlier tag after setting the later tags.
	private void resetTags(boolean resetAgency, boolean resetRoute, boolean resetStop) {
		if (resetAgency) {
			agencyTag = null;
			setViewText(agencyView, AGENCY_VIEW_PROMPT);
		}
		if (resetRoute) {
			routeTag = null;
			setViewText(routeView, ROUTE_VIEW_PROMPT);
			setViewLock(routeView, LOCK_VIEW);
		}
		if (resetStop) {
			stopTag = null;
			setViewText(stopView, STOP_VIEW_PROMPT);
			setViewLock(stopView, LOCK_VIEW);
		}
	}
	
	// Sets the stop urls used by the service to look up the predictions. Called by buttonPress
	private void setUserStop(boolean setStop1)  {
		String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a="
					+agencyTag+"&r="+routeTag+"&s="+stopTag;
		
		if (setStop1) { stop1Url = url; }
		else { stop2Url = url; }
	}
	
	//=====================================================================================//
	// Navigation and UI functions. Used to prep, check,  or modify UI elements.

	// Looks up the name and tag lists for the given component (agency, route, or stop)
	private void populateList(int componentIndex) {
		String xmlURL, tagName;
		AsyncNextBusTask nextBus = new AsyncNextBusTask(componentIndex);
		
		switch(componentIndex) {
			case AGENCIES:
		        xmlURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList";
		        tagName = "agency";
		        
				nextBus.execute(xmlURL, tagName);
				break;
			case ROUTES:
				xmlURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a="+agencyTag;
		        tagName = "route";
		        
		        nextBus.execute(xmlURL, tagName);
				break;
			case STOPS:
				xmlURL = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a="+agencyTag+"&r="+routeTag;
				tagName = "stop";
				
				nextBus.execute(xmlURL, tagName);
				break;
		}
		
	}

	// Sends an intent to ListViewActivity with a string arrayList as an extra. The user makes a selection from this list
	// which is then used to set the correct tag using the public setTag (called by ListViewActivity)
	private void goToListView(int componentIndex) {
		Intent intent = new Intent(this, ListViewActivity.class);
		switch(componentIndex) {
		case AGENCIES: intent.putExtra(ARRAYLIST_EXTRA, agencyNameList);
			intent.setAction(CHOOSE_AGENCY_LIST);
			break;
		case ROUTES:			
			intent.putExtra(ARRAYLIST_EXTRA, routeNameList);
			intent.setAction(CHOOSE_ROUTE_LIST);
			break;
		case STOPS: intent.putExtra(ARRAYLIST_EXTRA, stopNameList);
			intent.setAction(CHOOSE_STOP_LIST);
			break;
		}
		
		startActivityForResult(intent, componentIndex);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	// Enables or disables the specified TextView. Called by resetTags and AsyncNextBusTask
	private void setViewLock(TextView selectedView, boolean lockView) {
		selectedView.setEnabled(lockView);
		selectedView.setClickable(lockView);
	}

	// Changes the specified view's text to the given String
	private void setViewText(TextView selectedView, String viewText) {
		selectedView.setText(viewText);
	}
	
	// Checks if a textView has been set to 'enabled', indicating that its list has been retrieved and is ready to use
	private boolean listReady(TextView selectedView) {
		if (selectedView.isEnabled()) { return true; }
		else return false;
	}
	
	//=====================================================================================//
	
	private class AsyncNextBusTask extends AsyncTask<String, Void, Void>  {
		int componentIndex;
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> tagList = new ArrayList<String>();
		
		public AsyncNextBusTask(int componentIndex) {
			this.componentIndex = componentIndex;
		}
		
    	@Override
    	protected Void doInBackground(String... params) {
    		String xmlURL = params[0];
    		String tagName = params[1];
    		try {
    	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	        DocumentBuilder builder = factory.newDocumentBuilder();
    	        URL myURL = new URL(xmlURL);
    	        Document doc = builder.parse(myURL.openStream());
    	        NodeList nodeList = doc.getElementsByTagName(tagName);
    	        
    	        for (int i = 0; i < nodeList.getLength(); i++) {
    	        	Element e = (Element) nodeList.item(i);
    	        	String newElement = e.getAttribute(TITLE);
    	        	// This is awful. I need to switch to using a proper xml parser to avoid this :\
    	        	// It checks if the attribute for a given node is zero length as if it is, that means it doesn't have that attribute
    	        	if (!(newElement.length()==0)) {
    	        		Log.i("Adding ELement", "Adding to "+tagName+" array element with title l"+newElement+"l");
    	        		nameList.add(newElement);
    	        		tagList.add(e.getAttribute(TAG));
    	        	}
    	        }

    		} catch (Exception e) {
    			//response = "failed";
    			e.printStackTrace();
    		}
    		return null;
    	}

    	@Override
    	protected void onPostExecute(Void v) {
			switch(componentIndex) {
    			case AGENCIES:
    				agencyNameList = nameList;
    				agencyTagList = tagList;
    				setViewLock(agencyView, UNLOCK_VIEW);
    				break;
    			case ROUTES:
    				routeNameList = nameList;
    				routeTagList = tagList;
    				setViewLock(routeView, UNLOCK_VIEW);
    				break;
    			case STOPS:
    				stopNameList = nameList;
    				stopTagList = tagList;
    				setViewLock(stopView, UNLOCK_VIEW);
    				break;
			}
			Log.i("ASYNC_NEXTBUS", "Tag and name lists retrieved for component: " + componentIndex);
    	}
	}
}
