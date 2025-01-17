package org.rfcx.guardian.utility.asset;

import android.content.Context;

import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RfcxClassifierFileUtils {

    private static final SimpleDateFormat dirDateTimeFormat_MonthOnly = new SimpleDateFormat("yyyy-MM", Locale.US);
    private static final String classifierFileType = "tflite";
    private final String logTag;
    private String appRole = "Utils";

    public RfcxClassifierFileUtils(Context context, String appRole) {
        this.logTag = RfcxLog.generateLogTag(appRole, "RfcxClassifierFileUtils");
        this.appRole = appRole;
        initializeClassifierDirectories(context);
    }

    public static void initializeClassifierDirectories(Context context) {

        FileUtils.initializeDirectoryRecursively(classifierCacheDir(context), false);
        FileUtils.initializeDirectoryRecursively(classifierLibraryDir(context), false);
        FileUtils.initializeDirectoryRecursively(classifierActiveDir(context), false);
    }

    public static String classifierCacheDir(Context context) {
        return context.getFilesDir().toString() + "/classifiers/cache";
    }

    public static String classifierLibraryDir(Context context) {
        return context.getFilesDir().toString() + "/classifiers/library";
    }

    public static String classifierActiveDir(Context context) {
        return context.getFilesDir().toString() + "/classifiers/active";
    }

    public static String getClassifierFileLocation_Cache(Context context, long timestamp) {
        return classifierCacheDir(context) + "/" + timestamp + "." + classifierFileType;
    }

    public static String getClassifierFileLocation_Library(Context context, long timestamp) {
        return classifierLibraryDir(context) + "/" + dirDateTimeFormat_MonthOnly.format(new Date(timestamp)) + "/" + timestamp + "." + classifierFileType;
    }

    public static String getClassifierFileLocation_Active(Context context, long timestamp) {
        return classifierActiveDir(context) + "/" + timestamp + "." + classifierFileType;
    }


}
