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

				String message = "battery low";

				MainActivity.sendSMS(m_tel_number, message);
			
				MainActivity.myLog(message);
			
		}
}
