package org.rfcx.guardian.admin.comms.sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.device.DeviceSmsUtils;
import org.rfcx.guardian.utility.misc.DateTimeUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

import java.util.List;

public class SmsDispatchService extends Service {

    public static final String SERVICE_NAME = "SmsDispatch";

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "SmsDispatchService");

    private RfcxGuardian app;

    private boolean runFlag = false;
    private SmsDispatch smsDispatch;

    private final long forcedPauseBetweenEachDispatch = 5000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.smsDispatch = new SmsDispatch();
        app = (RfcxGuardian) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //	Log.v(logTag, "Starting service: "+logTag);
        this.runFlag = true;
        app.rfcxSvc.setRunState(SERVICE_NAME, true);
        try {
            this.smsDispatch.start();
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
        this.smsDispatch.interrupt();
        this.smsDispatch = null;
    }


    private class SmsDispatch extends Thread {

        public SmsDispatch() {
            super("SmsDispatchService-SmsDispatch");
        }

        @Override
        public void run() {
            SmsDispatchService smsDispatchInstance = SmsDispatchService.this;

            app = (RfcxGuardian) getApplication();

            try {

                app.rfcxSvc.reportAsActive(SERVICE_NAME);

                String apiSmsAddress = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.API_SMS_ADDRESS);

                List<String[]> smsQueuedForDispatch = app.smsMessageDb.dbSmsQueued.getRowsInOrderOfTimestamp();

                for (String[] smsForDispatch : smsQueuedForDispatch) {

                    // only proceed with dispatch process if there is a valid queued sms message in the database
                    if (smsForDispatch[0] != null) {

                        long sendAtOrAfter = Long.parseLong(smsForDispatch[1]);
                        long rightNow = System.currentTimeMillis();

                        if (sendAtOrAfter <= rightNow) {

                            String msgId = smsForDispatch[4];
                            String msgAddress = smsForDispatch[2];
                            String msgBody = smsForDispatch[3];

                            DeviceSmsUtils.sendSmsMessage(msgAddress, msgBody);

                            app.smsMessageDb.dbSmsQueued.deleteSingleRowByMessageId(msgId);

                            if (!msgAddress.equalsIgnoreCase(apiSmsAddress)) {

                                app.smsMessageDb.dbSmsSent.insert(rightNow, msgAddress, msgBody, msgId);
                                Log.w(logTag, "SMS Sent (ID " + msgId + "): To " + msgAddress + " at " + DateTimeUtils.getDateTime(rightNow) + ": \"" + msgBody + "\"");

                            } else {

                                String concatSegId = msgBody.substring(0, 4) + "-" + msgBody.substring(4, 7);
                                Log.v(logTag, DateTimeUtils.getDateTime(rightNow) + " - Segment '" + concatSegId + "' sent by SMS (" + msgBody.length() + " chars)");
                                RfcxComm.updateQuery("guardian", "database_set_last_accessed_at", "segments|" + concatSegId, app.getResolver());
                            }

                            Thread.sleep(forcedPauseBetweenEachDispatch);
                        }
                    }
                }

            } catch (Exception e) {
                RfcxLog.logExc(logTag, e);

            } finally {
                app.rfcxSvc.setRunState(SERVICE_NAME, false);
                app.rfcxSvc.stopService(SERVICE_NAME, false);
                smsDispatchInstance.runFlag = false;
            }

        }
    }


}
