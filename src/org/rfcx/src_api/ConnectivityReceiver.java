package org.rfcx.src_api;

import org.rfcx.rfcx_src_android.RfcxSource;
import org.rfcx.src_device.AirplaneModeReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityReceiver extends BroadcastReceiver {

	private static final String TAG = ConnectivityReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (RfcxSource.verboseLog()) { Log.d(TAG, "onReceive()"); }
		Log.d(TAG, "connectivityreceiver: "+ intent.getAction());

	}

}
