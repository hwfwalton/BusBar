package com.example.servtest;
// TOD
// Use Broadcast Receiver to update on wake. 
// http://androidexample.com/Screen_Wake_Sleep_Event_Listner_Service_-_Android_Example/index.php?view=article_discription&aid=91&aaid=115 
// Make custom notification window
// Use shared preferences to store and load my xmlurls rather than hardcoding them
// http://developer.android.com/guide/topics/resources/string-resource.html
// http://stackoverflow.com/questions/13558550/can-i-get-data-from-shared-preferences-inside-a-service
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    private String stop1URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=unitrans&r=A&s=22072";
    private String stop2URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=unitrans&r=A&s=22072";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    
    // Send an intent to BusService to start it
    public void startService(View view) {    	
    	// Define the intent to be sent to MyReceiver by the alarm manager
		Bundle urlBundle = new Bundle();
		urlBundle.putString(RunService.STOP1, stop1URL);
		urlBundle.putString(RunService.STOP2, stop2URL);
		
    	Intent startIntent = new Intent(this, RunService.class);
    	startIntent.putExtras(urlBundle);
    	startIntent.setAction(RunService.START);

    	startService(startIntent);
    }
    
    // Stop Alarm Manager and clear the notification
    public void stopService(View view) {
    	// Send an intent to the receiver that's passed along to BusService to clear the notification.
		Bundle times = new Bundle();
    	times.putString("TIMES", "ENDING");

    	Intent stopIntent = new Intent(this, RunService.class);
    	stopIntent.setAction("STOP_SERVICE");
    	stopIntent.putExtras(times);
    	stopService(stopIntent);
		Toast.makeText(this, "Service Stopped and Notification Cleared", Toast.LENGTH_SHORT).show();
    }
}