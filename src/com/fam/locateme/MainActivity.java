package com.fam.locateme;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.telephony.SmsManager;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;


public class MainActivity extends Activity
{
    IntentFilter intentFilter;
    
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context Context, Intent intent)
        {
            // display content on a new line
            TextView console = (TextView)findViewById(R.id.console);
            console.append("\n"+intent.getExtras().getString("info"));
            
        } 
    };
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		intentFilter = new IntentFilter();
		intentFilter.addAction("CON-UPDATE");
		registerReceiver(intentReceiver, intentFilter);
    }
	
	
	@Override
	protected void onResume()
	{
		//register the receiver
		//registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(intentReceiver);
        super.onDestroy();
	}
}
