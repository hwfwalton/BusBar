package com.example.servtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent receivedIntent) {
		String iAction = receivedIntent.getAction();
		Intent intent = new Intent(receivedIntent);

		if (iAction.equals(Intent.ACTION_SCREEN_ON)) {
			intent.setClass(context, RunService.class);
			context.startService(intent);

		} else if (iAction.equals(RunService.UPDATE)) {
	    	intent.setClass(context, BusService.class);
			context.startService(intent);
		}
	}
}
