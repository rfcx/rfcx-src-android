package org.rfcx.guardian.updater.api;

import java.util.List;

import org.json.JSONObject;
import org.rfcx.guardian.updater.RfcxGuardian;
import org.rfcx.guardian.utility.datetime.DateTimeUtils;
import org.rfcx.guardian.utility.install.InstallUtils;
import org.rfcx.guardian.utility.misc.StringUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import android.content.Context;
import android.util.Log;

public class ApiCheckVersionUtils {

	public ApiCheckVersionUtils(Context context) {
		this.context = context;
		this.app = (RfcxGuardian) context.getApplicationContext();
	}

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "ApiCheckVersionUtils");

	private Context context;
	private RfcxGuardian app;

	public static final long minimumAllowedIntervalBetweenCheckIns = 30; // in minutes
	public long lastCheckInTriggered = 0;
	public long lastCheckInTime = System.currentTimeMillis();

	private static final int installationBatteryCutoffPercentage = 50;

	public boolean apiCheckVersionFollowUp(String targetRole, List<JSONObject> jsonList) {
		
		this.lastCheckInTime = System.currentTimeMillis();

		String focusRole = null;
		String focusVersion = null;
		String focusReleaseDate = null;
		String focusSha1 = null;
		String focusUrl = null;
		int focusVersionValue = 0;
		
		try {
		
			for (JSONObject jsonListItem : jsonList) {
				if (jsonListItem.getString("role").equals(targetRole.toLowerCase())) {
					focusRole = jsonListItem.getString("role");
					focusVersion = jsonListItem.getString("version");
					focusReleaseDate = jsonListItem.getString("released");
					focusSha1 = jsonListItem.getString("sha1");
					focusUrl= jsonListItem.getString("url");
					focusVersionValue = RfcxRole.getRoleVersionValue(focusVersion);
					break;
				}
			}

			String installedVersion = RfcxRole.getRoleVersionByName(targetRole, RfcxGuardian.APP_ROLE, context);
			int installedVersionValue = InstallUtils.calculateVersionValue(installedVersion);
			
			if (	(	(focusVersion != null) && (installedVersion == null))
				||	(!installedVersion.equals(focusVersion) && (installedVersionValue < focusVersionValue))
				) {

				app.installUtils.setInstallConfig(focusRole, focusVersion, focusReleaseDate, focusUrl, focusSha1, focusVersionValue);
				
				if (isBatteryChargeSufficientForDownloadAndInstall()) {
					Log.d(logTag, "Update required. Latest release version ("+focusVersion+") detected and download triggered.");
					app.rfcxServiceHandler.triggerService("DownloadFile", true);
				} else {
					Log.i(logTag, "Update required, but will not be triggered due to low battery level"
							+" (current: "+app.deviceBattery.getBatteryChargePercentage(app.getApplicationContext(), null)+"%, required: "+installationBatteryCutoffPercentage+"%)."
							);
				}
				return true;
			} else if (!installedVersion.equals(focusVersion) && (installedVersionValue > focusVersionValue)) {
				Log.d(logTag,"RFCx "+ StringUtils.capitalizeFirstChar(focusRole) +": No Update. Installed version ("+installedVersion+") is newer than the latest release version ("+focusVersion+").");
			} else {
				Log.d(logTag,"RFCx "+ StringUtils.capitalizeFirstChar(focusRole) +": No Update. Installed version ("+installedVersion+") is already up-to-date.");
			}
		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}
		return false;
	}

	

	
	private boolean isCheckInAllowed(boolean printLoggingFeedbackIfNotAllowed) {
		if (app != null) {
			if (app.deviceConnectivity.isConnected()) {
				long timeElapsedSinceLastCheckIn = System.currentTimeMillis() - this.lastCheckInTriggered;
				if (timeElapsedSinceLastCheckIn > (minimumAllowedIntervalBetweenCheckIns * (60 * 1000))) {
					this.lastCheckInTriggered = System.currentTimeMillis();
					return true;
				} else if (printLoggingFeedbackIfNotAllowed) {
					Log.e(logTag, "Update CheckIn blocked b/c minimum allowed interval has not yet elapsed"
									+" - Elapsed: " + DateTimeUtils.milliSecondDurationAsReadableString(timeElapsedSinceLastCheckIn)
									+" - Required: " + minimumAllowedIntervalBetweenCheckIns + " minutes");
				}
			} else {
				Log.e(logTag, "Update CheckIn blocked b/c there is no internet connectivity.");
			}
		}
		return false;
	}

	public void attemptToTriggerCheckIn(boolean forceRequest, boolean printLoggingFeedbackIfNotAllowed) {
		if (forceRequest || isCheckInAllowed(printLoggingFeedbackIfNotAllowed)) {
			app.rfcxServiceHandler.triggerService("ApiCheckVersion", false);
		}
	}
	
	private boolean isBatteryChargeSufficientForDownloadAndInstall() {
		return (app.deviceBattery.getBatteryChargePercentage(context, null) >= installationBatteryCutoffPercentage);
	}
	
}
