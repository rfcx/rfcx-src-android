package org.rfcx.rfcx_src_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.rfcx.src_audio.AudioCaptureService;
import org.rfcx.src_api.ApiCommService;
import org.rfcx.src_arduino.ArduinoService;
import org.rfcx.src_device.DeviceStatsService;

public class BootReceiver extends BroadcastReceiver {
	
	private static final String TAG = BootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (RfcxSource.verboseLog()) { Log.d(TAG, "onReceive()"); }	
		
		if (org.rfcx.src_arduino.ArduinoState.isArduinoEnabled()) {
			context.startService(new Intent(context, ArduinoService.class));
		}
		
		if (org.rfcx.src_audio.AudioState.isAudioEnabled()) {
			context.startService(new Intent(context, AudioCaptureService.class));
		}
		
		if (org.rfcx.src_device.DeviceStatsService.areDeviceStatsEnabled()) {
			context.startService(new Intent(context, DeviceStatsService.class));
		}
		
		if (org.rfcx.src_api.ApiTransmit.isApiTransmitEnabled()) {
			context.startService(new Intent(context, ApiCommService.class));
		}
		
	}

}
