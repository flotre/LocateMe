package com.fam.locateme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.location.*;
import java.text.*;
import java.util.*;
import android.os.*;
import android.util.*;
import android.content.*;
import android.app.*;



public class SmsReceiver extends BroadcastReceiver
{
    String mReceiver_tel_number;
	LocationManager mLoc_manager;
	LocationListener  mLoc_listener;
	LocationListener  mLoc_listener_gps;
	Boolean mIsNeededLocUpdate = false;
	long mLocUpdateDuration_ms = 5000;
	long mLocUpdateStartTime;
	Timer m_timerLocation = null;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// get the sms message passed in
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		long minTime_ms = 5000;
		long minDistance = 5;
		
        
		if( bundle != null)
		{
			// retrieve sms message received
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for( int i=0; i<msgs.length; i++)
			{
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				if( i== 0)
				{
					mReceiver_tel_number = msgs[i].getOriginatingAddress(); 
				}
				
				str += msgs[i].getMessageBody().toString();
			}
			
			// test message content
			if( str.startsWith("#wru#") )
			{
				// request one location update
				minTime_ms = 0;
				minDistance = 0;
				mIsNeededLocUpdate = true;
				mLocUpdateDuration_ms = 0;
				mLocUpdateStartTime = SystemClock.elapsedRealtime();
							
				// message only for this application
				this.abortBroadcast();
			}
			else if( str.startsWith("#pwru#") )
			{
				// get option
				if( str.contains(":") )
				{
					String fields[] = str.split(":");
					if( str.contains(",") )
					{
						String options[] = fields[1].split(",");
						if( options.length == 3 )
						{
							// good options : #PWRU#:period,distance,totalTime
							minTime_ms = 1000*Long.parseLong(options[0]);
							minDistance =  Long.parseLong(options[1]);
							mLocUpdateDuration_ms = 1000*Long.parseLong(options[2]);
							mIsNeededLocUpdate = true;
							mLocUpdateStartTime = SystemClock.elapsedRealtime();
						}
					}
				}
					
				// message only for this application
				this.abortBroadcast();
			}
			else if(str.startsWith("#bat#"))
			{
				IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = context.registerReceiver(null, ifilter);
				
				int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

				float batteryPct = 100*level / (float)scale;
				
				String message = "battery: "+batteryPct+"% ("+level+","+scale+")";

				toolbox.sendSMS(mReceiver_tel_number, message);
				
				// message only for this application
				this.abortBroadcast();
			}
			
			// location update
			if( mIsNeededLocUpdate == true )
			{
				mIsNeededLocUpdate = false;
				m_timerLocation = null;
                Log.d(toolbox.TAG,"add loc update:"+mLocUpdateStartTime);
                   
				// location request
				mLoc_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
               
                //test available location service
                Boolean loc_gps = mLoc_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                Boolean loc_net = mLoc_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    
				if (loc_gps)
				{
					// GPS position
					mLoc_listener_gps = new gpsLocationListener();
					mLoc_manager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						minTime_ms,
						minDistance,
						mLoc_listener_gps);
				}
				
				if (loc_net)
				{
					// network position
					mLoc_listener = new networkLocationListener();
					mLoc_manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						minTime_ms,
						minDistance,
						mLoc_listener);
				}
				
				// no service available
				if(!loc_net && !loc_gps)
				{
					toolbox.sendSMS(mReceiver_tel_number,"no location available");
					
					//disable connection
					toolbox.setInternetConnection(context,false);
				}
				else
				{
					//enable connection
					toolbox.setInternetConnection(context,true);
					toolbox.addTimeout(context,60*5);
				}
			}
		}
	}

	
	
	
	// location listener for gps
	private class networkLocationListener implements LocationListener
	{
		public void onLocationChanged(Location loc)
		{
			if(loc != null)
			{
				// check time
                long timeElapsed = SystemClock.elapsedRealtime() - mLocUpdateStartTime;
                Log.d(toolbox.TAG,"send loc:"+timeElapsed);
                
                String message = "net: http://maps.google.com/maps?q="+
                    loc.getLatitude()+","+loc.getLongitude();

                toolbox.sendSMS(mReceiver_tel_number, message);
				
                	
				if( timeElapsed >= mLocUpdateDuration_ms )
				{
					mLoc_manager.removeUpdates(mLoc_listener);
                    Log.d(toolbox.TAG,"remove net loc listener");
				}
			}
		}
		public void onProviderDisabled(String provider){}
		public void onProviderEnabled(String provider){}
		public void onStatusChanged(String provider,int status,Bundle extra){}
	}
	
	// location listener for gps
	private class gpsLocationListener implements LocationListener
	{
		public void onLocationChanged(Location loc)
		{
			if(loc != null)
			{
				String message = "gps: http://maps.google.com/maps?q="+
					loc.getLatitude()+","+loc.getLongitude();
				
				toolbox.sendSMS(mReceiver_tel_number, message);

				long timeElapsed = SystemClock.elapsedRealtime() - mLocUpdateStartTime;
				if( timeElapsed >= mLocUpdateDuration_ms )
				{
					mLoc_manager.removeUpdates(mLoc_listener_gps);
                    Log.d(toolbox.TAG,"remove gps loc listener");
				}
			}
		}
		public void onProviderDisabled(String provider){}
		public void onProviderEnabled(String provider){}
		public void onStatusChanged(String provider,int status,Bundle extra){}
	}
}
