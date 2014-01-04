package com.fam.locateme;
import android.content.*;
import android.net.*;
import java.lang.reflect.*;
import android.telephony.*;
import android.net.wifi.*;
import android.util.*;
import android.app.*;
import android.os.*;

public class toolbox
{
	public static final String TAG = "locateme";
	public static final String STOP_CONNECTION = "locateme.stopconnection";
	
	public static void _setMobileDataEnabled(Context context, boolean enabled)
	{
		// data connection
		try
		{
			final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final Class conmanClass = Class.forName(conman.getClass().getName());
			final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			setMobileDataEnabledMethod.setAccessible(true);

			setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		}
		catch (Exception e)
		{
			Log.e(TAG,Log.getStackTraceString(e));
		}
		
		// wifi
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		wifiManager.setWifiEnabled(enabled);
		
		Log.d(toolbox.TAG,"connection : "+enabled);
	}
	
	public static void setMobileDataEnabled(Context context, boolean enable)
	{
		ConnectivityManager cm =
			(ConnectivityManager) context
			.getSystemService(Activity.CONNECTIVITY_SERVICE);
		Method m = getMethodFromClass(cm, "setMobileDataEnabled");
		runMethodofClass(cm, m, enable);
	}

	private static Method getMethodFromClass(Object obj, String methodName)
	{
		final String TAG = "getMethodFromClass";
		Class<?> whichClass = null;
		try {
			whichClass = Class.forName(obj.getClass().getName());
		} catch (ClassNotFoundException e2) {
			Log.d(TAG, "class not found");
		}
		Method method = null;
		try {
			//method = whichClass.getDeclaredMethod(methodName);
			Method[] methods = whichClass.getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().contains(methodName)) {
					method = m;
				}
			}
		} catch (SecurityException e2) {
			Log.d(TAG, "SecurityException for " + methodName);
		}
		return method;
	}

	private static Object runMethodofClass(Object obj, Method method, Object... argv)
	{
		Object result = null;
		if (method == null) return result;
		method.setAccessible(true);
		try {
			result = method.invoke(obj, argv);
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "IllegalArgumentException for " + method.getName());
		} catch (IllegalAccessException e) {
			Log.d(TAG, "IllegalAccessException for " + method.getName());
		} catch (InvocationTargetException e) {
			Log.d(TAG, "InvocationTargetException for " + method.getName()
				  + "; Reason: " + e.getLocalizedMessage());
		}
		return result;
	}
	
	// send a sms
	public static void sendSMS(String phoneNumber,String message)
	{
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
	// add timeout for location update
	public static void addTimeout(Context context, long timeout)
	{
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent broadcastIntent = new Intent(STOP_CONNECTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeout*1000, pendingIntent);

		Log.d(toolbox.TAG,"add timeout");
	}
	
}
