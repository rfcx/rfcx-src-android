package org.rfcx.guardian.admin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.device.control.DeviceAirplaneMode;
import org.rfcx.guardian.utility.misc.DateTimeUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class AirplaneModeReceiver extends BroadcastReceiver {

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "AirplaneModeReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(logTag,
                (new StringBuilder())
                        .append("AirplaneMode ")
                        .append(DeviceAirplaneMode.isEnabled(context) ? "Enabled" : "Disabled")
                        .append(" at ")
                        .append(DateTimeUtils.getDateTime(System.currentTimeMillis()))
                        .toString()
        );


    }

}
