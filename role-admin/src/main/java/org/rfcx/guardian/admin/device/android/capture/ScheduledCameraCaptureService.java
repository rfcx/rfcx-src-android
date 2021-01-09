package org.rfcx.guardian.admin.device.android.capture;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.service.RfcxServiceHandler;

public class ScheduledCameraCaptureService extends IntentService {

	private static final String SERVICE_NAME = "ScheduledCameraCapture";

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "ScheduledCameraCaptureService");

	public ScheduledCameraCaptureService() {
		super(logTag);
	}
	
	@Override
	protected void onHandleIntent(Intent inputIntent) {
		Intent intent = new Intent(RfcxServiceHandler.intentServiceTags(false, RfcxGuardian.APP_ROLE, SERVICE_NAME));
		sendBroadcast(intent, RfcxServiceHandler.intentServiceTags(true, RfcxGuardian.APP_ROLE, SERVICE_NAME));;
		
		RfcxGuardian app = (RfcxGuardian) getApplication();

		if (app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ADMIN_ENABLE_CAMERA_CAPTURE)) {
			app.rfcxServiceHandler.triggerService("CameraCapture", true);
		} else {
			Log.i(logTag, "Scheduled Photo/Video Capture is currently disabled in preferences.");
		}
		
	}
	
	
}