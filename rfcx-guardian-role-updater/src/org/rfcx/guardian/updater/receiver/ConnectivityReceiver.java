package org.rfcx.guardian.updater.receiver;

import java.util.Calendar;

import org.rfcx.guardian.updater.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class ConnectivityReceiver extends BroadcastReceiver {

	private static final String TAG = "Rfcx-"+RfcxGuardian.APP_ROLE+"-"+ConnectivityReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
        RfcxGuardian app = (RfcxGuardian) context.getApplicationContext();
		app.isConnected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (app.isConnected) {
			app.lastConnectedAt = Calendar.getInstance().getTimeInMillis();
			// added to ensure that multiple checkins don't occur at each connectivity reception
			if (app.lastApiCheckTriggeredAt < (app.lastConnectedAt-2000)) {
				if (app.apiCore.allowTriggerCheckIn()) {
					app.triggerService("ApiCheckVersion",true);
				}
			}
		} else {
			app.lastDisconnectedAt = Calendar.getInstance().getTimeInMillis();
		}
	}

	
	
	
	
}