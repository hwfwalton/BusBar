package com.example.guitest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import static com.example.guitest.Constants.*;

public class ScreenReceiver extends BroadcastReceiver {
	private String stop1Url, stop2Url;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		// If this was a START_SERVICE intent, pull the new urls from the intent before proceeding
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.i("SCREEN ON:", "ACTION_SCREEN_ON intent caught by broadcast receiver");
			sendBusUpdateIntents(context);
		}
	}
	
	private void sendBusUpdateIntents(Context context) {
		Intent stop1Intent = new Intent(context, UpdateBusTimesService.class);
		Intent stop2Intent = new Intent(context, UpdateBusTimesService.class);
		
		stop1Intent.setAction(STOP1_UPDATE_INTENT);
		stop2Intent.setAction(STOP2_UPDATE_INTENT);
		
		stop1Intent.putExtra(STOP_URL_EXTRA, stop1Url);
		stop2Intent.putExtra(STOP_URL_EXTRA, stop2Url);

		context.startService(stop1Intent);
		context.startService(stop2Intent);
	}
	
	// Used to set and update the urls the receiver sends to the UpdateBusTimesService. Called by BackgroundSerivce.
	public void setUrls(String stop1Url, String stop2Url, Context context) {
		this.stop1Url = stop1Url;
		this.stop2Url = stop2Url;
		
		sendBusUpdateIntents(context);
	}
}
