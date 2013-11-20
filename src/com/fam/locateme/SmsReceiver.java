package com.fam.locateme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.location.*;



public class SmsReceiver extends BroadcastReceiver
{
	String receiver_tel_number;
	LocationManager loc_manager;
	LocationListener  loc_listener;

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
				loc_listener = new MyLocationListener();			
				loc_manager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						5000,
						10,
						loc_listener);
			
				// message only for this application
				this.abortBroadcast();
			}
			
			
			
			// display message
			Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
			
			// Filter message
			
			// send to other activity
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction("SMS_RECEIVED_ACTION");
			broadcastIntent.putExtra("sms",str);
			context.sendBroadcast(broadcastIntent);
		}
	}

	
	
	private class MyLocationListener implements LocationListener
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
	
}
