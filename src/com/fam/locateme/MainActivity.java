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
import android.support.v4.content.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.*;


public class MainActivity extends Activity
{
    public static final String TAG = "locateme";
    IntentFilter intentFilter;
	private static Queue<String> m_consoleQueue=new ConcurrentLinkedQueue<String>();
	
    
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context Context, Intent intent)
        {
            // display content on a new line
            myLog(intent.getExtras().getString("info"));   
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
		
		//empty the console queue
		String message=m_consoleQueue.poll();

		while(null != message)
		{
			consoleAdd(message);
			message=m_consoleQueue.poll();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(intentReceiver);
        super.onDestroy();
	}
	
	public void TestButton_onClick(View view)
	{
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct =100* level / (float)scale;

		String message = "battery:"+batteryPct+" % ("+level+","+scale+")";
		
		myLog(message);
	}
	
	private void consoleAdd(String message)
	{
		// display content on a new line
		TextView console = (TextView)findViewById(R.id.console);
		console.append(message);   
	} 
	
	// ecrit un message dans la console
	public void conWrite(String message)
	{
		Intent intent = new Intent();
		intent.setAction("CON-UPDATE");
		intent.putExtra("info", message);
		this.sendBroadcast(intent);
	}
	
	public static void myLog(String message)
	{
		//m_console.append("\n"+SystemClock.elapsedRealtime()+":"+message);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.FRANCE);
		
		m_consoleQueue.add("\n"+sdf.format(new Date(System.currentTimeMillis()))+":"+message);
	}
	
	
	// send a sms
	public static void sendSMS(String phoneNumber,String message)
	{
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
}
