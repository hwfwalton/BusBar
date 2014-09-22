package com.example.servtest;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
//import android.widget.Toast;

public class RunService extends Service {
	
	public static final String UPDATE = "UPDATE_TIMES";
	public static final String START = "START_SERVICE";
	public static final String STOP1 = "STOP1";
	public static final String STOP2 = "STOP2";
	public static final String TIME1 = "TIME1";
	public static final String TIME2 = "TIME2";
	public static final String ERROR = "NO CONNECTION";
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	private BroadcastReceiver aReceiver;
	private IntentFilter screenFilter;
	private NotificationManager nManager;
	private NotificationCompat.Builder nBuilder;
	private Bundle urlBundle;
	private String updateTime;
	private int nID = 100;
    private Boolean justCreated;
    private String stop1URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=unitrans&r=A&s=22072";
    private String stop2URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=unitrans&r=A&s=22258";
	
    @Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
    	// Fetch system services we're going to need
    	alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    	nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
		// Setup the screen_on receiver and register it to the context. By registering it to this service rather than
		// MainActvity, the receiver stays alive even if the kills the application.
		screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    	aReceiver = new AlarmReceiver();
    	registerReceiver(aReceiver, screenFilter);
    	
		// Build and send the persistent notification
    	nBuilder = 
			new NotificationCompat.Builder(this)
	    		.setPriority(NotificationCompat.PRIORITY_MAX) // PRIORITY_MIN
	    		.setSmallIcon(R.drawable.ic_stat_bus)
	    		.setOngoing(true)
	    		.setWhen(0)
	    		.setOnlyAlertOnce(true)
	    		.setContentTitle("STARTING TEXT")
	    		.setContentText("BLA");

    	// Get the NotificationManager service and create the AsyncNextBusTask, though do not execute it
		nManager.notify(nID, nBuilder.build());	
    	
		urlBundle = new Bundle();
		urlBundle.putString("STOP1", stop1URL);
		urlBundle.putString("STOP2", stop2URL);
		
		Intent startIntent = new Intent(this, AlarmReceiver.class);
		startIntent.putExtras(urlBundle);
    	startIntent.setAction("UPDATE_TIMES");
    	
    	// Initialize the PendingIntent and start the repeating alarm
    	alarmIntent = PendingIntent.getBroadcast(this, 0, startIntent, 0);
    	alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 
    			(long)0, (long)60000, alarmIntent); 

		justCreated = true;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!justCreated){
			//Toast.makeText(this, intent.getAction(), Toast.LENGTH_SHORT).show();
			String iAction = intent.getAction();
			
			if (iAction.equals(RunService.UPDATE)) {	
				// Set the new notification text and update it
				String title = "GS: " + intent.getStringExtra(RunService.TIME1);
				title = padSpaces(title, 20);
				title += "SI: " + intent.getStringExtra(RunService.TIME2);
				nBuilder.setContentTitle(title);
				
				Calendar c = Calendar.getInstance();
				updateTime = c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE); 
				nBuilder.setContentText("Last Updated " + updateTime);
				
				nManager.notify(nID, nBuilder.build());
			} else if (iAction.equals(RunService.ERROR)) {	
				nBuilder.setContentText("Last Updated " + updateTime + "(no connection)");
				nManager.notify(nID, nBuilder.build());
			} else if (iAction.equals(Intent.ACTION_SCREEN_ON)) {	
				urlBundle = new Bundle();
				urlBundle.putString("STOP1", stop1URL);
				urlBundle.putString("STOP2", stop2URL);
				
				intent.putExtras(urlBundle);
				intent.setAction(RunService.UPDATE);
				intent.setClass(this, BusService.class);
				startService(intent);
			}

		} else {
			justCreated = false;
		}	
		return START_STICKY;
	}
	
	@Override
    public void onDestroy() {    	
    	// Unregister the screen_on receiver, cancel the repeating alarm, and clear the notification
		unregisterReceiver(aReceiver);
    	alarmMgr.cancel(alarmIntent);
		nManager.cancel(nID);
	}
	
	public String padSpaces(String str, int size) {
		String ret = str;
		int sLen = str.length();
				  
		if (sLen > size) {
			ret = str.substring(0, size);
		} else { 
			for(int i = 0; i < (size - sLen); i++) {
				ret += " ";
			}
		}
		return ret;
	}
}
