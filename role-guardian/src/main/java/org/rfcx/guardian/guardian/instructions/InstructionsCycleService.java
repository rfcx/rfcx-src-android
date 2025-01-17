package org.rfcx.guardian.guardian.instructions;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class InstructionsCycleService extends Service {

    public static final String SERVICE_NAME = "InstructionsCycle";
    public static final long CYCLE_DURATION = 5000;
    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "InstructionsCycleService");
    private RfcxGuardian app;
    private boolean runFlag = false;
    private InstructionsCycleSvc instructionsCycleSvc;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instructionsCycleSvc = new InstructionsCycleSvc();
        app = (RfcxGuardian) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.v(logTag, "Starting service: " + logTag);
        this.runFlag = true;
        app.rfcxSvc.setRunState(SERVICE_NAME, true);
        try {
            this.instructionsCycleSvc.start();
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
        this.instructionsCycleSvc.interrupt();
        this.instructionsCycleSvc = null;
    }


    private class InstructionsCycleSvc extends Thread {

        public InstructionsCycleSvc() {
            super("InstructionsCycleService-InstructionsCycleSvc");
        }

        @Override
        public void run() {
            InstructionsCycleService instructionsCycleInstance = InstructionsCycleService.this;

            app = (RfcxGuardian) getApplication();

            while (instructionsCycleInstance.runFlag) {

                try {

                    app.rfcxSvc.reportAsActive(SERVICE_NAME);

                    if (app.instructionsDb.dbQueued.getCount() > 0) {

                        app.rfcxSvc.triggerService(InstructionsExecutionService.SERVICE_NAME, false);

                    }

                    Thread.sleep(CYCLE_DURATION);

                } catch (Exception e) {
                    RfcxLog.logExc(logTag, e);
                    app.rfcxSvc.setRunState(SERVICE_NAME, false);
                    instructionsCycleInstance.runFlag = false;
                }
            }

            app.rfcxSvc.setRunState(SERVICE_NAME, false);
            instructionsCycleInstance.runFlag = false;
            Log.v(logTag, "Stopping service: " + logTag);
        }
    }


}
