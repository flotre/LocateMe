package com.fam.locateme;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.telephony.*;


public class MainActivity extends Activity
{
    IntentFilter intentFilter;
	Button btnSendSMS;
    EditText txtPhoneNo;
    EditText txtMessage;
    
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
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(intentReceiver);
        super.onDestroy();
	}
	
	public void TestButton_onClick(View view)
	{
		Intent intent = new Intent();
		intent.setAction("CON-UPDATE");
		
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct =100* level / (float)scale;

		String message = "battery:"+batteryPct+" % ("+level+","+scale+")";
		
		
		intent.putExtra("info", message);
		
		
		this.sendBroadcast(intent);
	}
}
