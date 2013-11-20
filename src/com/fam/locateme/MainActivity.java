package com.fam.locateme;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.telephony.SmsManager;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;


public class MainActivity extends Activity
{
	IntentFilter intentFilter;
	
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// display sms received in textview
			TextView SMSes = (TextView) findViewById(R.id.console);
			SMSes.setText(SMSes.getText() + intent.getExtras().getString("sms"));
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		// intent to filter for sms messages received
		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);
    }
	
	private void SendSms(String phone_number, String message)
	{
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phone_number, null, message, null, null);
	}
	
	@Override
	protected void onResume()
	{
		//register the receiver
		//registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		// unregister the receiver
		unregisterReceiver(intentReceiver);
		super.onDestroy();
	}
}
