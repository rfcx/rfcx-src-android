package org.rfcx.guardian.guardian.api.methods.ping;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.guardian.api.methods.checkin.ApiCheckInJsonUtils;
import org.rfcx.guardian.utility.device.hardware.DeviceHardwareUtils;
import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.io.IOException;
import java.util.Locale;

public class ApiPingUtils {

	public ApiPingUtils(Context context) {

		this.app = (RfcxGuardian) context.getApplicationContext();

	}

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "ApiPingUtils");

	private RfcxGuardian app;

	public boolean sendPing(boolean includeAllExtraFields, String[] includeExtraFields, String forceProtocol) {

		String[] apiProtocols = app.rfcxPrefs.getDefaultPrefValueAsString("api_protocol_escalation_order").split(",");
		if (forceProtocol.equalsIgnoreCase("all")) {
			apiProtocols = app.rfcxPrefs.getPrefAsString("api_protocol_escalation_order").split(",");
			Log.v(logTag, "Allowed Ping protocols (in order): " + TextUtils.join(", ", apiProtocols).toUpperCase(Locale.US));
		} else if (ArrayUtils.doesStringArrayContainString(apiProtocols,forceProtocol)) {
			apiProtocols = new String[] { forceProtocol };
		}

		boolean isPublished = false;

		try {

			String pingJson = app.apiPingJsonUtils.buildPingJson(includeAllExtraFields, includeExtraFields);

			for (String apiProtocol : apiProtocols) {

				Log.v(logTag, "Attempting Ping publication via "+apiProtocol.toUpperCase(Locale.US)+" protocol...");

				if (	(	apiProtocol.equalsIgnoreCase("mqtt")
						&& 	app.apiMqttUtils.sendMqttPing(pingJson)
						)
					||	(	apiProtocol.equalsIgnoreCase("rest")
						&&  app.apiRestUtils.sendRestPing(pingJson)
						)
					||	(	apiProtocol.equalsIgnoreCase("sms")
						&& 	app.apiSmsUtils.sendSmsPing(pingJson)
						)
					||	(	apiProtocol.equalsIgnoreCase("sbd")
						&& 	app.apiSbdUtils.sendSbdPing(pingJson)
						)
				) {
					isPublished = true;
					Log.v(logTag, "Ping has been published via "+apiProtocol.toUpperCase(Locale.US)+".");
					break;
				}
			}

		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}

		if (!isPublished) { Log.e(logTag, "Ping failed to publish via protocol(s): "+TextUtils.join(", ", apiProtocols).toUpperCase(Locale.US)); }

		return isPublished;
	}

	public boolean sendPing(boolean includeAllExtraFields, String[] includeExtraFields) {
		return sendPing(includeAllExtraFields, includeExtraFields, "all");
	}

	public boolean sendPing() {
		return sendPing(true, new String[]{}, "all");
	}

}
