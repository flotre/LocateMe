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



public class SmsReceiver extends BroadcastReceiver
{
	String receiver_tel_number;
	LocationManager loc_manager;
	LocationListener  loc_listener;
	LocationListener  loc_listener_gps;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// get the sms message passed in
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		
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
					receiver_tel_number = msgs[i].getOriginatingAddress(); 
				}
				
				str += msgs[i].getMessageBody().toString();
			}
			
			// test message content
			if( str.startsWith("#WRU#") )
			{
				// location request
				loc_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

				Location loc = loc_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if(loc != null)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = new Date(loc.getTime());
					String strDate = format.format(date);
					String message = "http://maps.google.com/maps?q="+
						loc.getLatitude()+","+loc.getLongitude()+"  "+strDate;

					SmsManager manager = SmsManager.getDefault();
					manager.sendTextMessage(receiver_tel_number, null, message, null, null);
				}			
				// message only for this application
				this.abortBroadcast();
			}
			else if( str.startsWith("#PWRU#") )
			{
				// get option
				if( str.contains(":") )
				{
					String fields[] = str.split(":");
					if( str.contains(",") )
					{
						String options[] = fields[1].split(",");
						if( options.length == 2 )
						{
							// good options
						}
					}
				}
				// location request
				loc_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				
				// network position
				loc_listener = new networkLocationListener();
				loc_manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						5000,
						10,
						loc_listener);
				
				// GPS position
				loc_listener_gps = new gpsLocationListener();
				loc_manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					5000,
					5,
					loc_listener_gps);
				
				
				// message only for this application
				this.abortBroadcast();
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
				String message = "http://maps.google.com/maps?q="+
						loc.getLatitude()+","+loc.getLongitude();
				
				SmsManager manager = SmsManager.getDefault();
				manager.sendTextMessage(receiver_tel_number, null, message, null, null);
			
				loc_manager.removeUpdates(loc_listener);
			}
		}	
		
		
		public void onProviderDisabled(String provider)
		{
		}
		public void onProviderEnabled(String provider)
		{
		}
		public void onStatusChanged(String provider,int status,Bundle extra)
		{
		}
	}
	
	// location listener for gps
	private class gpsLocationListener implements LocationListener
	{

		public void onLocationChanged(Location loc)
		{
			if(loc != null)
			{
				String message = "http://maps.google.com/maps?q="+
					loc.getLatitude()+","+loc.getLongitude();

				SmsManager manager = SmsManager.getDefault();
				manager.sendTextMessage(receiver_tel_number, null, message, null, null);

				loc_manager.removeUpdates(loc_listener_gps);
			}
		}	


		public void onProviderDisabled(String provider)
		{
		}
		public void onProviderEnabled(String provider)
		{
		}
		public void onStatusChanged(String provider,int status,Bundle extra)
		{
		}
	}
}
