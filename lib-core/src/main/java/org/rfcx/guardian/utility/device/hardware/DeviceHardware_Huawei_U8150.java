package org.rfcx.guardian.utility.device.hardware;

import android.content.Context;
import android.util.Log;

import org.rfcx.guardian.utility.device.root.DeviceReboot;
import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.misc.ShellCommands;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class DeviceHardware_Huawei_U8150 {

    public static final String DEVICE_NAME = "Huawei U8150";
    private static final String logTag = RfcxLog.generateLogTag("Utils", "DeviceHardware_Huawei_U8150");

    public DeviceHardware_Huawei_U8150(String appRole) {

    }

    public static boolean isDevice_Huawei_U8150() {
        return DeviceHardwareUtils.getName().equalsIgnoreCase(DEVICE_NAME);
    }

    public static void checkOrResetGPSFunctionality(Context context) {

        // constructing gps.conf files
        String gpsDotConfContents = (new StringBuilder())
                .append("NTP_SERVER=time.apple.com").append("\n")
                .append("XTRA_SERVER_1=http://xtra1.gpsonextra.net/xtra.bin").append("\n")
                .append("XTRA_SERVER_2=http://xtra2.gpsonextra.net/xtra.bin").append("\n")
                .append("XTRA_SERVER_3=http://xtra3.gpsonextra.net/xtra.bin").append("\n")
                .append("\n")
                .append("DEBUG_LEVEL = 3").append("\n")
                .append("INTERMEDIATE_POS=1").append("\n")
                .append("ACCURACY_THRES=250").append("\n")
                .append("\n")
                .append("SUPL_HOST=supl.google.com").append("\n")
                .append("SUPL_PORT=7275").append("\n")
//				.append("SUPL_SECURE_PORT=7275").append("\n")
//				.append("SUPL_NO_SECURE_PORT=3425").append("\n")
//				.append("REPORT_POSITION_USE_SUPL_REFLOC=1").append("\n")
                .append("\n")
                .append("ENABLE_WIPER=1").append("\n")
                .append("CURRENT_CARRIER=common").append("\n")
                .append("DEFAULT_SSL_ENABLE=FALSE").append("\n")
                .append("DEFAULT_USER_PLANE=TRUE").append("\n")
                .append("\n")
                .append("DEFAULT_AGPS_ENABLE=TRUE").append("\n")
                .append("AGPS=http://xtra1.gpsonextra.net/xtra.bin").append("\n")
                .append("\n")
//				.append("QOS_ACCURACY=50").append("\n")
//				.append("QOS_TIME_OUT_STANDALONE=60").append("\n")
//				.append("\n")

                .toString();

        String gpsDotConfCacheFilePath = context.getFilesDir().getAbsolutePath() + "/gps.conf";

        // writing gps.conf into place
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(gpsDotConfCacheFilePath));
            bufferedWriter.write(gpsDotConfContents);
            bufferedWriter.close();
            FileUtils.chmod(gpsDotConfCacheFilePath, "rw", "rw");
        } catch (Exception e) {
            RfcxLog.logExc(logTag, e);
        }

        String gpsDotConfFilePath = "/system/etc/gps.conf";

        if (!(new File(gpsDotConfFilePath)).exists()
                || !FileUtils.sha1Hash(gpsDotConfFilePath).equals(FileUtils.sha1Hash(gpsDotConfCacheFilePath))
        ) {

            String allCmds = "mount -o rw,remount /dev/block/mmcblk0p1 /system;\n"            // remounting partition
                    + "rm -rf " + gpsDotConfFilePath + ";\n"
                    + "mv " + gpsDotConfCacheFilePath + " " + gpsDotConfFilePath + ";\n"    // moving gps.conf file into place
                    + "echo 'ro.con g.xtra_support=true' >> /system/build.prop;\n"    // add a line to the build.prop file
                    ;
            ShellCommands.executeCommandAsRootAndIgnoreOutput(allCmds);

            // rebooting device
            DeviceReboot.triggerForcedRebootAsRoot();

        } else {
            Log.d(logTag, "GPS has already been activated on this device.");
        }

    }


}
