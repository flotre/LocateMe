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


public class SmsReceiver extends BroadcastReceiver
{
	private static final String tag = "locateme";
    String mReceiver_tel_number;
	LocationManager mLoc_manager;
	LocationListener  mLoc_listener;
	LocationListener  mLoc_listener_gps;
	Boolean mIsNeededLocUpdate = false;
	long mLocUpdateDuration_ms = 5000;
	long mLocUpdateStartTime;

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
				minTime_ms = 5000;
				minDistance = 5;
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
			
			// location update
			if( mIsNeededLocUpdate == true )
			{
				mIsNeededLocUpdate = false;
                Log.d(tag,"add loc update:"+mLocUpdateStartTime);
                
                
				// location request
				mLoc_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
               
                //test available location service
                Location loc_gps = mLoc_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location loc_net = mLoc_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(loc_gps != null || loc_net != null)
                {
                    Location loc;
                    if(loc_gps != null)
                    {
                        loc = loc_gps;
                    }
                    else
                    {
                        loc= loc_net;
                    }

                    // test loc accuracy in meter
                    if( loc.getAccuracy() < 10.0)
                    {
                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        Date date = new Date(loc.getTime());
                        String strDate = format.format(date);
                        String message = "http://maps.google.com/maps?q="+
                            loc.getLatitude()+","+loc.getLongitude()+"  "+strDate;

                        sendSMS(mReceiver_tel_number, message);
                    }
                    else if(loc_net != null)
					{
						// network position
						mLoc_listener = new networkLocationListener();
						mLoc_manager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							minTime_ms,
							minDistance,
							mLoc_listener);
                    }
                    else if(loc_gps!= null)
                    {

						// GPS position
						mLoc_listener_gps = new gpsLocationListener();
						mLoc_manager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							minTime_ms,
							minDistance,
							mLoc_listener_gps);
                    }
                    else
                    {
                        Log.e(tag,"no location available");
                    }
                }
			}
		}
	}

	private void sendSMS(String phoneNumber,String message)
	{
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNumber, null, message, null, null);
		
	}
	
	// location listener for gps
	private class networkLocationListener implements LocationListener
	{
		
		public void onLocationChanged(Location loc)
		{
			if(loc != null)
			{
				String message = "net: http://maps.google.com/maps?q="+
						loc.getLatitude()+","+loc.getLongitude();
				
				sendSMS(mReceiver_tel_number, message);
			
				long timeElapsed = SystemClock.elapsedRealtime() - mLocUpdateStartTime;
                Log.d(tag,"send loc:"+timeElapsed);
				if( timeElapsed >= mLocUpdateDuration_ms )
				{
					mLoc_manager.removeUpdates(mLoc_listener);
                    Log.d(tag,"remove net loc listener");
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
				
				sendSMS(mReceiver_tel_number, message);

				long timeElapsed = SystemClock.elapsedRealtime() - mLocUpdateStartTime;
				if( timeElapsed >= mLocUpdateDuration_ms )
				{
					mLoc_manager.removeUpdates(mLoc_listener_gps);
                    Log.d(tag,"remove gps loc listener");
				}
			}
		}
		public void onProviderDisabled(String provider){}
		public void onProviderEnabled(String provider){}
		public void onStatusChanged(String provider,int status,Bundle extra){}
	}
}
