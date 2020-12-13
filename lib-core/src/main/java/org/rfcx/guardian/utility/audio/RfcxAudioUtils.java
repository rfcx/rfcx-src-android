package org.rfcx.guardian.utility.audio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

public class RfcxAudioUtils {

	private static final String logTag = RfcxLog.generateLogTag("Utils", "RfcxAudioUtils");

	private static final SimpleDateFormat dirDateFormat = new SimpleDateFormat("yyyy-MM/yyyy-MM-dd/HH", Locale.US);
	private static final SimpleDateFormat fileDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSZZZ", Locale.US);
	
	public static final int AUDIO_SAMPLE_SIZE = 16;
	public static final int AUDIO_CHANNEL_COUNT = 1;
	
	public static void initializeAudioDirectories(Context context) {
		FileUtils.initializeDirectoryRecursively(audioSdCardDir(), true);
		FileUtils.initializeDirectoryRecursively(audioCaptureDir(context), false);
		FileUtils.initializeDirectoryRecursively(audioEncodeDir(context), false);
		FileUtils.initializeDirectoryRecursively(audioQueueDir(context), false);
		FileUtils.initializeDirectoryRecursively(audioStashDir(context), false);
	}

	private static String audioSdCardDir() {
		return Environment.getExternalStorageDirectory().toString() + "/rfcx/audio";
	}
	
	public static String audioCaptureDir(Context context) {
		return context.getFilesDir().toString() + "/audio/capture";
	}

	public static String audioEncodeDir(Context context) {
		return context.getFilesDir().toString() + "/audio/encode";
	}

	private static String audioQueueDir(Context context) {
		return context.getFilesDir().toString() + "/audio/final";
	}

	private static String audioStashDir(Context context) {
		return context.getFilesDir().toString() + "/audio/stash";
	}
	
	public static String getAudioFileLocation_Capture(Context context, long timestamp, String fileExtension) {
		return audioCaptureDir(context) + "/" + timestamp + "." + fileExtension;
	}
	
	public static String getAudioFileLocation_PreEncode(Context context, long timestamp, String fileExtension) {
		return audioEncodeDir(context) + "/" + timestamp + "." + fileExtension;
	}
	
	public static String getAudioFileLocation_PostEncode(Context context, long timestamp, String audioCodec) {
		return audioEncodeDir(context) + "/_" + timestamp + "." + getFileExtension(audioCodec);
	}

	public static String getAudioFileLocation_Queue(String rfcxDeviceId, Context context, long timestamp, String audioCodec) {
		return audioQueueDir(context) + "/" + dirDateFormat.format(new Date(timestamp)) + "/" + rfcxDeviceId + "_" + fileDateTimeFormat.format(new Date(timestamp)) + "." + getFileExtension(audioCodec) + ".gz";
	}

	public static String getAudioFileLocation_Stash(String rfcxDeviceId, Context context, long timestamp, String audioCodec) {
		return audioStashDir(context) + "/" + dirDateFormat.format(new Date(timestamp)) + "/" + rfcxDeviceId + "_" + fileDateTimeFormat.format(new Date(timestamp)) + "." + getFileExtension(audioCodec) + ".gz";
	}

	public static String getAudioFileLocation_ExternalStorage(String rfcxDeviceId, long timestamp, String audioCodec) {
		return audioSdCardDir() + "/" + dirDateFormat.format(new Date(timestamp)) + "/" + rfcxDeviceId + "_" + fileDateTimeFormat.format(new Date(timestamp)) + "." + getFileExtension(audioCodec) + ".gz";
	}
	
	public static String getFileExtension(String audioCodecOrFileExtension) {

		if 	(		audioCodecOrFileExtension.equalsIgnoreCase("opus")
				||	audioCodecOrFileExtension.equalsIgnoreCase("flac")
			) {
			
			return audioCodecOrFileExtension;
			
		} else {
			
			return "wav";
			
		}
	}
	
	public static boolean isEncodedWithVbr(String audioCodecOrFileExtension) {
		return (	audioCodecOrFileExtension.equalsIgnoreCase("opus")
				||	audioCodecOrFileExtension.equalsIgnoreCase("flac")
				);
	}
	
}
