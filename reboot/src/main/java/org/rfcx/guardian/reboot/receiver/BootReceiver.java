package reboot.receiver;

import rfcx.utility.rfcx.RfcxLog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import reboot.RfcxGuardian;

public class BootReceiver extends BroadcastReceiver {

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, BootReceiver.class);
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.v(logTag, "Rfcx-"+RfcxGuardian.APP_ROLE+" BootReceiver Launched...");
		
		// initializing rfcx application
		RfcxGuardian app = (RfcxGuardian) context.getApplicationContext();

	}

}
