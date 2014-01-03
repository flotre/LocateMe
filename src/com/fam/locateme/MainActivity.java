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
import android.net.*;
import java.lang.reflect.*;


public class MainActivity extends Activity
{
    IntentFilter intentFilter;
	private static Queue<String> m_consoleQueue=new ConcurrentLinkedQueue<String>();
	private static boolean state = false;
	
    
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent)
        {
			String action = intent.getAction();

			if(action.equals("CON-UPDATE")){
				// display content on a new line
				myLog(intent.getExtras().getString("info")); 
			}
			else if(action.equals(toolbox.STOP_CONNECTION)){
				//disable connection
				toolbox.setMobileDataEnabled(context,false);
				myLog("action stop connection");
			}
        } 
    };
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		intentFilter = new IntentFilter();
		intentFilter.addAction(toolbox.STOP_CONNECTION);
		registerReceiver(intentReceiver, intentFilter);
		
		intentFilter = new IntentFilter();
		intentFilter.addAction("CON-UPDATE");
		registerReceiver(intentReceiver, intentFilter);
		
		//disable connection
		toolbox.setMobileDataEnabled(this,false);
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
		
		{
			toolbox.setMobileDataEnabled(this,true);
			
			toolbox.addTimeout(this,10);
		}
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
	
	
	
	
}
