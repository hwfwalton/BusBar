package com.example.guitest;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import static com.example.guitest.Constants.*;

// TODO
// send intent to broadcast receiver on first run of this service. Attach the urls to that intent rather than using the seturls function.
// I'll need to add an additional intentfilter to the receiver so it can receive my own intents as well as the screen on intents

public class BackgroundService extends Service {
	private ScreenReceiver myReceiver;
	private NotificationManager nManager;
	private NotificationCompat.Builder nBuilder;
	
	private String stop1Url;
	private String stop2Url;
	private String stop1Name;
	private String stop2Name;
	private String stop1Times;
	private String stop2Times;
	private String notificationTitle = "LOADING TIMES";
	private String notificationContent = "LOADING NAMES";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	// At creation, sets up the notification manager and receiver and then starts the service
	@Override
	public void onCreate() {
		super.onCreate();
		
    	nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		screenFilter.addAction(Intent.ACTION_SCREEN_ON);
		myReceiver = new ScreenReceiver();
		
		nBuilder = 
				new NotificationCompat.Builder(this)
		    		.setPriority(NotificationCompat.PRIORITY_MAX) // PRIORITY_MIN
		    		.setSmallIcon(R.drawable.ic_stat_bus)
		    		.setOngoing(true)
		    		.setOnlyAlertOnce(true)
		    		.setContentTitle(notificationTitle)
		    		.setContentText(notificationContent);
		
		nManager.notify(NOTIFICATIONID, nBuilder.build());
		
		registerReceiver(myReceiver, screenFilter);
	}
	
	// Receives Intents sent by NotificationUpdateService or MainActivity
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		switch (intent.getAction()) {
		case START_SERVICE:
			Log.i("BackgroundService", "doing initial setup");
			stop1Name = intent.getStringExtra(STOP1_NAME);
			stop2Name = intent.getStringExtra(STOP2_NAME);
			stop1Url = intent.getStringExtra(STOP1_URL_EXTRA);
			stop2Url = intent.getStringExtra(STOP2_URL_EXTRA);
			myReceiver.setUrls(stop1Url, stop2Url, getApplicationContext());
			break;
		case STOP1_UPDATE_INTENT:
			Log.i("BACKGROUND_SERVICE", "Updating Stop 1");
			stop1Times = intent.getStringExtra(STOP_TIMES_EXTRA);
			updateNotification();
			break;
		case STOP2_UPDATE_INTENT:
			Log.i("BACKGROUND_SERVICE", "Updating Stop 2");
			stop2Times = intent.getStringExtra(STOP_TIMES_EXTRA);
			updateNotification();
			break;
		case STOP_UPDATE_ERROR:
			Log.i("BACKGROUND_SERVICE", "Error fetching bus times");
			Toast.makeText(getApplicationContext(), "Error fetching bus times" , Toast.LENGTH_SHORT).show();
			break;
		
		}
		return START_STICKY;
	}
	
	@Override
    public void onDestroy() {
		unregisterReceiver(myReceiver);
		nManager.cancel(NOTIFICATIONID);
		Toast.makeText(getApplicationContext(), "Ending Service" , Toast.LENGTH_SHORT).show();
	}
	
	private void updateNotification() {
		notificationTitle = stop1Times + "||" + stop2Times;
		notificationContent = stop1Name + ", " + stop2Name;
		
		nBuilder.setContentTitle(notificationTitle);
		nBuilder.setContentText(notificationContent);
		
		nManager.notify(NOTIFICATIONID, nBuilder.build());
	}

}
