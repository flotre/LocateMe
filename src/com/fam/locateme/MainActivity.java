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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);		
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
		super.onDestroy();
	}
}
