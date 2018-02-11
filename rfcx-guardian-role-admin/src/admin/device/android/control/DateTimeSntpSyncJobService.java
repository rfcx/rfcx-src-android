package admin.device.android.control;

import org.rfcx.guardian.utility.SntpClient;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import admin.RfcxGuardian;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class DateTimeSntpSyncJobService extends Service {

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, DateTimeSntpSyncJobService.class);
	
	private static final String SERVICE_NAME = "DateTimeSntpSyncJob";
	
	private RfcxGuardian app;
	
	private boolean runFlag = false;
	private DateTimeSntpSyncJob dateTimeSntpSyncJob;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
		
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.dateTimeSntpSyncJob = new DateTimeSntpSyncJob();
		app = (RfcxGuardian) getApplication();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.v(logTag, "Starting service: "+logTag);
		this.runFlag = true;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, true);
		try {
			this.dateTimeSntpSyncJob.start();
		} catch (IllegalThreadStateException e) {
			RfcxLog.logExc(logTag, e);
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
		this.dateTimeSntpSyncJob.interrupt();
		this.dateTimeSntpSyncJob = null;
	}
	
	
	private class DateTimeSntpSyncJob extends Thread {
		
		public DateTimeSntpSyncJob() {
			super("DateTimeSntpSyncJobService-DateTimeSntpSyncJob");
		}
		
		@Override
		public void run() {
			DateTimeSntpSyncJobService dateTimeSntpSyncJobInstance = DateTimeSntpSyncJobService.this;
			
			app = (RfcxGuardian) getApplication();
			
			try {
				
				app.rfcxServiceHandler.reportAsActive(SERVICE_NAME);

				SntpClient sntpClient = new SntpClient();
				if (sntpClient.requestTime("time.apple.com", 15000)) {
					long nowSntp = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
					long nowSystem = System.currentTimeMillis();
					Log.v(logTag, "SNTP CHECK: "+nowSntp+" - "+nowSystem+" ("+(nowSystem-nowSntp)+")");
				 }
				
//				(new ShellCommands(app.APP_ROLE, app.getApplicationContext())).triggerRebootAsRoot();
					
			} catch (Exception e) {
				RfcxLog.logExc(logTag, e);
			} finally {
				dateTimeSntpSyncJobInstance.runFlag = false;
				app.rfcxServiceHandler.setRunState(SERVICE_NAME, false);
				app.rfcxServiceHandler.stopService(SERVICE_NAME);
			}
		}
	}

	
}
