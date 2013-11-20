package com.fam.locateme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver
{

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
				str += "sms from " + msgs[i].getOriginatingAddress();
				str += " :";
				str += msgs[i].getMessageBody().toString();
				str += "\n";
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


}
