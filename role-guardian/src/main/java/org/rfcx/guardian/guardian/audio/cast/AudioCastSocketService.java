package org.rfcx.guardian.guardian.audio.cast;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

public class AudioCastSocketService extends Service {

	public static final String SERVICE_NAME = "AudioCastSocket";

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "AudioCastSocketService");
	
	private RfcxGuardian app;
	
	private boolean runFlag = false;
	private AudioCastSocketSvc audioCastSocketSvc;

	private static final long minPushCycleDurationMs = 667;
	private static final int ifSendFailsThenExtendLoopByAFactorOf = 4;
	private static final int maxSendFailureThreshold = 24;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.audioCastSocketSvc = new AudioCastSocketSvc();
		app = (RfcxGuardian) getApplication();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.v(logTag, "Starting service: "+logTag);
		this.runFlag = true;
		app.rfcxSvc.setRunState(SERVICE_NAME, true);
		try {
			this.audioCastSocketSvc.start();
		} catch (IllegalThreadStateException e) {
			RfcxLog.logExc(logTag, e);
		}
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.rfcxSvc.setRunState(SERVICE_NAME, false);
		this.audioCastSocketSvc.interrupt();
		this.audioCastSocketSvc = null;
	}
	
	
	private class AudioCastSocketSvc extends Thread {
		
		public AudioCastSocketSvc() { super("AudioCastSocketService-AudioCastSocketSvc"); }
		
		@Override
		public void run() {
			AudioCastSocketService audioCastSocketInstance = AudioCastSocketService.this;
			
			app = (RfcxGuardian) getApplication();

			if (app.audioCastUtils.isAudioCastEnablable(true, app.rfcxPrefs)) {
//
//				int currFailureThreshold = maxSendFailureThreshold +1;
//				long pingPushCycleDurationMs = Math.max(app.rfcxPrefs.getPrefAsLong(RfcxPrefs.Pref.COMPANION_TELEMETRY_PUSH_CYCLE), minPushCycleDurationMs);
//
//				while (audioCastSocketInstance.runFlag) {
//
//					try {
//
//						app.rfcxSvc.reportAsActive(SERVICE_NAME);
//
//						if (currFailureThreshold >= maxSendFailureThreshold) {
//							app.companionSocketUtils.socketUtils.stopServer();
//							app.companionSocketUtils.startServer();
//							currFailureThreshold = 0;
//							pingPushCycleDurationMs = Math.max(app.rfcxPrefs.getPrefAsLong(RfcxPrefs.Pref.COMPANION_TELEMETRY_PUSH_CYCLE), minPushCycleDurationMs);
//							Thread.sleep(pingPushCycleDurationMs);
//							app.companionSocketUtils.updatePingJson(false);
//						}
//
//						if (app.companionSocketUtils.sendSocketPing()) {
//							Thread.sleep(pingPushCycleDurationMs);
//							currFailureThreshold = 0;
//							app.companionSocketUtils.updatePingJson(true);
//						} else {
//							Thread.sleep(ifSendFailsThenExtendLoopByAFactorOf * pingPushCycleDurationMs );
//							currFailureThreshold++;
//						}
//
//
//					} catch (Exception e) {
//						RfcxLog.logExc(logTag, e);
//						app.rfcxSvc.setRunState(SERVICE_NAME, false);
//						audioCastSocketInstance.runFlag = false;
//					}
//				}
			} else {
//				app.companionSocketUtils.socketUtils.stopServer();
			}

			app.rfcxSvc.setRunState(SERVICE_NAME, false);
			audioCastSocketInstance.runFlag = false;
			Log.v(logTag, "Stopping service: "+logTag);
		}
	}

	
}
