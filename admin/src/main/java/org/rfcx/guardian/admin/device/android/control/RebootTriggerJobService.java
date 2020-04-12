package org.rfcx.guardian.admin.device.android.control;

import org.rfcx.guardian.utility.device.AppProcessInfo;
import org.rfcx.guardian.utility.device.root.DeviceReboot;
import org.rfcx.guardian.utility.misc.ShellCommands;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxGarbageCollection;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import org.rfcx.guardian.admin.RfcxGuardian;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class RebootTriggerJobService extends Service {

	private static final String SERVICE_NAME = "RebootTrigger";

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, RebootTriggerJobService.class.getSimpleName());
	
	private RfcxGuardian app;
	
	private boolean runFlag = false;
	private RebootTriggerJob rebootTriggerJob;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.rebootTriggerJob = new RebootTriggerJob();
		app = (RfcxGuardian) getApplication();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.v(logTag, "Starting service: "+logTag);
		this.runFlag = true;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, true);
		try {
			this.rebootTriggerJob.start();
		} catch (IllegalThreadStateException e) {
			RfcxLog.logExc(logTag, e);
		}
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
		this.rebootTriggerJob.interrupt();
		this.rebootTriggerJob = null;
	}
	
	
	private class RebootTriggerJob extends Thread {
		
		public RebootTriggerJob() {
			super("RebootTriggerJobService-RebootTriggerJob");
		}
		
		@Override
		public void run() {
			RebootTriggerJobService rebootTriggerJobInstance = RebootTriggerJobService.this;
			
			app = (RfcxGuardian) getApplication();
			
			try {
				
				app.rfcxServiceHandler.reportAsActive(SERVICE_NAME);

				// Halting the Guardian role services
				Log.e(logTag, "Reboot: Requesting that Guardian role stop all services...");
				try {
					Cursor killGuardianSvcs = app.getApplicationContext().getContentResolver().query(
						RfcxComm.getUri("guardian", "control", "kill"),
						RfcxComm.getProjection("guardian", "control"),
						null, null, null);
					Log.v(logTag, killGuardianSvcs.toString());
					killGuardianSvcs.close();
				} catch (Exception e) {
					RfcxLog.logExc(logTag, e);
				}

				// Garbage Collection
				RfcxGarbageCollection.runAndroidGarbageCollection();

				// Triggering reboot request
				Log.e(logTag, "Reboot: Broadcasting ACTION_REBOOT Intent...");
				Intent actionReboot = new Intent(Intent.ACTION_REBOOT);
				actionReboot.putExtra("nowait", 1);
				actionReboot.putExtra("window", 1);
				sendBroadcast(actionReboot);

				Log.e(logTag, "System should be shutting down now...");
				app.rfcxServiceHandler.stopAllServices();


			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			} finally {
				rebootTriggerJobInstance.runFlag = false;
				app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
				app.rfcxServiceHandler.stopService(SERVICE_NAME);
			}
		}
	}

	
}
