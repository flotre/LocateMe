package com.fam.locateme;
import android.content.*;
import android.os.*;
import android.telephony.*;

public class BatteryLevelReceiver extends BroadcastReceiver
{
	private final static String m_tel_number = "0687141873";
	
		@Override
		public void onReceive(Context context, Intent intent) {
			// this is where we deal with the data sent from the battery.

			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			float batteryPct = level / (float)scale;

			String message = "battery:"+batteryPct+" % ("+level+","+scale+")";

			sendSMS(m_tel_number, message);
		}

	private void sendSMS(String phoneNumber,String message)
	{
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNumber, null, message, null, null);
	}
}
