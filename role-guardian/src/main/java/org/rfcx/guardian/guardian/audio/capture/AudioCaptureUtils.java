package org.rfcx.guardian.guardian.audio.capture;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.audio.wav.WavUtils;
import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.asset.RfcxAssetCleanup;
import org.rfcx.guardian.utility.asset.RfcxAudioFileUtils;
import org.rfcx.guardian.utility.device.capture.DeviceStorage;
import org.rfcx.guardian.utility.misc.DateTimeUtils;
import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class AudioCaptureUtils {

	public AudioCaptureUtils(Context context) {
		this.app = (RfcxGuardian) context.getApplicationContext();
		RfcxAudioFileUtils.initializeAudioDirectories(context);
	}

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "AudioCaptureUtils");

	private final RfcxGuardian app;

	public String[] samplingRatioStrArr = new String[] {};
	public int[] samplingRatioArr = new int[] {};
	public int samplingRatioIteration = 0;

	public long[] queueCaptureTimeStamp = new long[] { 0, 0 };
	public int[] queueCaptureSampleRate = new int[] { 0, 0 };

	private boolean isAudioCaptureHardwareSupported = false;
	private static final int requiredFreeDiskSpaceForAudioCapture = 32;

	private static AudioCaptureWavRecorder wavRecorderForCompanion = null;

	public static AudioCaptureWavRecorder initializeWavRecorder(String captureDir, long timestamp, int sampleRate) throws Exception {
		wavRecorderForCompanion = AudioCaptureWavRecorder.getInstance(sampleRate);;
		wavRecorderForCompanion.setOutputFile(getCaptureFilePath(captureDir, timestamp, "wav"));
		wavRecorderForCompanion.prepareRecorder();
		return wavRecorderForCompanion;
	}

	public void updateSamplingRatioIteration() {
		this.samplingRatioStrArr = TextUtils.split(app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_SAMPLING_RATIO), ":");
		this.samplingRatioArr = new int[] { Integer.parseInt(this.samplingRatioStrArr[0]), Integer.parseInt(this.samplingRatioStrArr[1]) };
		if (this.samplingRatioIteration > this.samplingRatioArr[1]) { this.samplingRatioIteration = 0; }
		this.samplingRatioIteration++;
	}


	private boolean doesHardwareSupportCaptureSampleRate() {

		if (!this.isAudioCaptureHardwareSupported) {

			int[] defaultSampleRateOptions = new int[]{ 8000, 12000, 16000, 24000, 48000 };
			int originalSampleRate = app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE);
			int verifiedOrUpdatedSampleRate = originalSampleRate;

			for (int i = 0; i < defaultSampleRateOptions.length; i++) {
				if ((defaultSampleRateOptions[i] >= originalSampleRate)
						&& (AudioRecord.getMinBufferSize(defaultSampleRateOptions[i], AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT) > 0)
				) {

					verifiedOrUpdatedSampleRate = defaultSampleRateOptions[i];
					this.isAudioCaptureHardwareSupported = true;
					break;

				} else if (i == (defaultSampleRateOptions.length - 1)) {
					this.isAudioCaptureHardwareSupported = false;
					Log.e(logTag, "Failed to verify hardware support for any of the provided audio sample rates...");
				}
			}

			if (verifiedOrUpdatedSampleRate != originalSampleRate) {
				app.setSharedPref(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE, "" + verifiedOrUpdatedSampleRate);
				Log.e(logTag, "Audio capture sample rate of " + originalSampleRate + " Hz not supported. Sample rate updated to " + verifiedOrUpdatedSampleRate + " Hz.");
				this.isAudioCaptureHardwareSupported = true;
			}

			if (this.isAudioCaptureHardwareSupported) {
				Log.v(logTag, "Hardware support verified for audio capture sample rate of " + verifiedOrUpdatedSampleRate + " Hz.");
			}
		}

		return this.isAudioCaptureHardwareSupported;
	}

	private boolean isBatteryChargeSufficientForCapture() {
		int batteryChargeCutoff = this.app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CUTOFF_INTERNAL_BATTERY);
		int batteryCharge = this.app.deviceBattery.getBatteryChargePercentage(app.getApplicationContext(), null);
		boolean isBatteryChargeSufficient = (batteryCharge >= batteryChargeCutoff);
		if (isBatteryChargeSufficient && (batteryChargeCutoff == 100)) {
			isBatteryChargeSufficient = this.app.deviceBattery.isBatteryCharged(app.getApplicationContext(), null);
			if (!isBatteryChargeSufficient) { Log.d(logTag, "Battery is at 100% but is not yet fully charged."); }
		}
		return isBatteryChargeSufficient;
	}

	private boolean limitBasedOnBatteryLevel() {
		return (!isBatteryChargeSufficientForCapture() && this.app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_CUTOFFS_INTERNAL_BATTERY));
	}

	private boolean isAudioCaptureLimitedBySentinelBatteryLastValue = false;
	private long isAudioCaptureLimitedBySentinelBatteryLastValueSetAt = 0;
	private static final long isAudioCaptureLimitedBySentinelBatteryCacheExpiresAfter = 6666;

	private boolean limitBasedOnSentinelBatteryLevel() {

		if (this.app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_CUTOFFS_SENTINEL_BATTERY)) {
			try {
				if (!(Math.abs(DateTimeUtils.timeStampDifferenceFromNowInMilliSeconds(this.isAudioCaptureLimitedBySentinelBatteryLastValueSetAt)) <= isAudioCaptureLimitedBySentinelBatteryCacheExpiresAfter)) {
					Log.w(logTag, "Updating value for limitBasedOnSentinelBatteryLevel based on Admin role status via ContentProvider...");
					JSONArray jsonArray = RfcxComm.getQuery("admin", "status", "*", app.getResolver());
					if (jsonArray.length() > 0) {
						JSONObject jsonObj = jsonArray.getJSONObject(0);
						if (jsonObj.has("audio_capture")) {
							JSONObject apiCheckInObj = jsonObj.getJSONObject("audio_capture");
							this.isAudioCaptureLimitedBySentinelBatteryLastValue = apiCheckInObj.has("is_allowed") && !apiCheckInObj.getBoolean("is_allowed");
							this.isAudioCaptureLimitedBySentinelBatteryLastValueSetAt = System.currentTimeMillis();
						}
					}
				}
				return this.isAudioCaptureLimitedBySentinelBatteryLastValue;
			} catch (JSONException e) {
				RfcxLog.logExc(logTag, e);
				return false;
			}
		}
		return false;
	}



	private boolean limitBasedOnInternalStorage() {
		return (DeviceStorage.getInternalDiskFreeMegaBytes() <= requiredFreeDiskSpaceForAudioCapture);
	}




	private boolean isCaptureAllowedAtThisTimeOfDay() {
		for (String offHoursRange : TextUtils.split(app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_SCHEDULE_OFF_HOURS), ",")) {
			String[] offHours = TextUtils.split(offHoursRange, "-");
			if (DateTimeUtils.isTimeStampWithinTimeRange(new Date(), offHours[0], offHours[1])) {
				return false;
			}
		}
		return true;
	}

	private boolean limitBasedOnTimeOfDay() {
		return (!isCaptureAllowedAtThisTimeOfDay() && this.app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_CUTOFFS_SCHEDULE_OFF_HOURS));
	}

	private boolean limitBasedOnCaptureSamplingRatio() {
		return (!isCaptureAllowedAtThisSamplingRatioIteration() && this.app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_CUTOFFS_SAMPLING_RATIO));
	}

	private boolean isCaptureAllowedAtThisSamplingRatioIteration() {
		return (this.samplingRatioIteration == 1);
	}

	public String audioCaptureStatusAsJsonObjStr() {

		String statusObjStr = "{\"is_allowed\":true,\"is_disabled\":false}";

        try {
			// Now add real data
			boolean isAllowed = isAudioCaptureAllowed(false, false);
			boolean isDisabled = isAudioCaptureDisabled(false);
			JSONObject statusObj = new JSONObject();
			statusObj.put("is_allowed", isAllowed);
			statusObj.put("is_disabled", isDisabled);
			statusObjStr = statusObj.toString();
        } catch (Exception e) {
            RfcxLog.logExc(logTag, e);
        }

        return statusObjStr;
	}


	private boolean isCaptureAllowedLastValue = true;
	private boolean isCaptureDisabledLastValue = false;
	private long isCaptureAllowedLastValueSetAt = 0;
	private long isCaptureDisabledLastValueSetAt = 0;
	private static final long captureStatusCacheExpiresAfter = 3333;

	public boolean isAudioCaptureAllowed(boolean includeSentinel, boolean printFeedbackInLog) {

		// we set this to true, and cycle through conditions that might make it false
		// we then return the resulting true/false value
		boolean isAudioCaptureAllowedUnderKnownConditions = true;

		if  ((Math.abs(DateTimeUtils.timeStampDifferenceFromNowInMilliSeconds(this.isCaptureAllowedLastValueSetAt)) <= captureStatusCacheExpiresAfter)) {

			isAudioCaptureAllowedUnderKnownConditions = this.isCaptureAllowedLastValue;

		} else {

			StringBuilder msgNoCapture = new StringBuilder();

			if (limitBasedOnBatteryLevel()) {
				msgNoCapture.append("Low Battery level")
						.append(" (current: ").append(this.app.deviceBattery.getBatteryChargePercentage(this.app.getApplicationContext(), null)).append("%,")
						.append(" required: ").append(this.app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CUTOFF_INTERNAL_BATTERY)).append("%).");
				isAudioCaptureAllowedUnderKnownConditions = false;

			} else if (includeSentinel && limitBasedOnSentinelBatteryLevel()) {
				msgNoCapture.append("Low Sentinel Battery level")
						.append(" (required: ").append(this.app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CUTOFF_SENTINEL_BATTERY)).append("%).");
				isAudioCaptureAllowedUnderKnownConditions = false;

			} else if (limitBasedOnInternalStorage()) {
				msgNoCapture.append("a lack of sufficient free internal disk storage.")
						.append(" (current: ").append(DeviceStorage.getInternalDiskFreeMegaBytes()).append("MB)")
						.append(" (required: ").append(requiredFreeDiskSpaceForAudioCapture).append("MB).");
				isAudioCaptureAllowedUnderKnownConditions = false;

			} else if (!doesHardwareSupportCaptureSampleRate()) {
				msgNoCapture.append("lack of hardware support for capture sample rate: ")
						.append(app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE)).append(" Hz.");
				isAudioCaptureAllowedUnderKnownConditions = false;

			}

			if (!isAudioCaptureAllowedUnderKnownConditions) {
				if (printFeedbackInLog) {
					Log.d(logTag, msgNoCapture
							.insert(0, DateTimeUtils.getDateTime() + " - AudioCapture not allowed due to ")
							.append(" Waiting ").append(app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CYCLE_DURATION)).append(" seconds before next attempt.")
							.toString());
				}
			}

			this.isCaptureAllowedLastValue = isAudioCaptureAllowedUnderKnownConditions;
			this.isCaptureAllowedLastValueSetAt = System.currentTimeMillis();
		}

		return isAudioCaptureAllowedUnderKnownConditions;
	}

	public boolean isAudioCaptureDisabled(boolean printFeedbackInLog) {

		// we set this to false, and cycle through conditions that might make it true
		// we then return the resulting true/false value
		boolean isAudioCaptureDisabledRightNow = false;

		if  ((Math.abs(DateTimeUtils.timeStampDifferenceFromNowInMilliSeconds(this.isCaptureDisabledLastValueSetAt)) <= captureStatusCacheExpiresAfter)) {

			isAudioCaptureDisabledRightNow = this.isCaptureDisabledLastValue;

		} else {

			StringBuilder msgNoCapture = new StringBuilder();

			if (!this.app.rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_AUDIO_CAPTURE)) {
				msgNoCapture.append("it being explicitly disabled ('" + RfcxPrefs.Pref.ENABLE_AUDIO_CAPTURE.toLowerCase() + "' is set to false).");
				isAudioCaptureDisabledRightNow = true;

			} else if (limitBasedOnTimeOfDay()) {
				msgNoCapture.append("current time of day/night")
						.append(" (off hours: '").append(app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_SCHEDULE_OFF_HOURS)).append("'.");
				isAudioCaptureDisabledRightNow = true;

			} else if (limitBasedOnCaptureSamplingRatio()) {
				msgNoCapture.append("a sampling ratio definition. ")
						.append("Ratio is '").append(app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_SAMPLING_RATIO)).append("'. ")
						.append("Currently on iteration ").append(this.samplingRatioIteration).append(" of ").append(this.samplingRatioArr[0] + this.samplingRatioArr[1]).append(".");
				isAudioCaptureDisabledRightNow = true;

			} else if (!app.isGuardianRegistered()) {
				msgNoCapture.append("the Guardian not having been registered.");
				isAudioCaptureDisabledRightNow = true;

			}

			if (isAudioCaptureDisabledRightNow) {
				if (printFeedbackInLog) {
					Log.d(logTag, msgNoCapture
							.insert(0, DateTimeUtils.getDateTime() + " - AudioCapture paused due to ")
							.append(" Waiting ").append(app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CYCLE_DURATION)).append(" seconds before next attempt.")
							.toString());
				}
			}

			this.isCaptureDisabledLastValue = isAudioCaptureDisabledRightNow;
			this.isCaptureDisabledLastValueSetAt = System.currentTimeMillis();
		}

		return isAudioCaptureDisabledRightNow;
	}

	public boolean updateCaptureQueue(long timeStamp, int sampleRate) {

		queueCaptureTimeStamp[0] = queueCaptureTimeStamp[1];
		queueCaptureTimeStamp[1] = timeStamp;

		queueCaptureSampleRate[0] = queueCaptureSampleRate[1];
		queueCaptureSampleRate[1] = sampleRate;

		return (queueCaptureTimeStamp[0] > 0);
	}

	public static String getCaptureFilePath(String captureDir, long timestamp, String fileExtension) {
		return captureDir + "/" + timestamp + "." + fileExtension;
	}

	public static boolean reLocateAudioCaptureFile(Context context, boolean isToBeEncoded, boolean isToBeClassified, long timestamp, int sampleRate, String fileExt) {
		boolean isEncodeFileMoved = true;
		boolean isClassifyFileMoved = true;
		File captureFile = new File(getCaptureFilePath(RfcxAudioFileUtils.audioCaptureDir(context),timestamp,fileExt));
		if (captureFile.exists()) {
			try {

				if (isToBeEncoded) {
					File preEncodeFile = new File(RfcxAudioFileUtils.getAudioFileLocation_PreEncode(context, timestamp, fileExt, sampleRate, "g10"));
					WavUtils.copyWavFile(captureFile.getAbsolutePath(), preEncodeFile.getAbsolutePath(), sampleRate);
					isEncodeFileMoved = preEncodeFile.exists();
				}

				if (isToBeClassified) {
					File preClassifyFile = new File(RfcxAudioFileUtils.getAudioFileLocation_PreClassify(context, timestamp, fileExt, sampleRate, "g10"));
					WavUtils.copyWavFile(captureFile.getAbsolutePath(), preClassifyFile.getAbsolutePath(), sampleRate);
					isClassifyFileMoved = preClassifyFile.exists();
				}

				if (isEncodeFileMoved && isClassifyFileMoved) {
					FileUtils.delete(captureFile);
					return true;
				}

			} catch (IOException e) {
				RfcxLog.logExc(logTag, e);
			}
		}
		return false;
	}


	public static File checkOrCreateReSampledWav(Context context, String purpose, String inputFilePath, long timestamp, String fileExt, int inputSampleRate, int outputSampleRate, double outputGain) throws IOException {

		String outputFilePath = RfcxAudioFileUtils.getAudioFileLocation_PreEncode(context, timestamp, fileExt, outputSampleRate, "g"+Math.round(outputGain*10));

		if (purpose.equalsIgnoreCase("classify")) {
			outputFilePath = RfcxAudioFileUtils.getAudioFileLocation_PreClassify(context, timestamp, fileExt, outputSampleRate, "g"+Math.round(outputGain*10));
		}

		if (FileUtils.exists(outputFilePath)) {

			Log.d(logTag, "Already exists: "+ RfcxAssetCleanup.conciseFilePath(outputFilePath, RfcxGuardian.APP_ROLE));
			return (new File(outputFilePath));

		} else if (!FileUtils.exists(inputFilePath)) {

			Log.e(logTag, "Input file does not exist: "+ RfcxAssetCleanup.conciseFilePath(inputFilePath, RfcxGuardian.APP_ROLE));
			return null;

		} else {

			WavUtils.resampleWavWithGain(inputFilePath, outputFilePath, inputSampleRate, outputSampleRate, outputGain);
			return (new File(outputFilePath));
		}

	}

	public static int getAudioFileDurationInMilliseconds(File audioFileObj, int fallbackDuration, boolean printResultsToLog) {
		int audioDuration = fallbackDuration;

		try {
			long startTime = System.currentTimeMillis();
			MediaPlayer mediaPlayer = new MediaPlayer();
			FileInputStream fileInputStream = new FileInputStream(audioFileObj);
			mediaPlayer.setDataSource(fileInputStream.getFD());
			mediaPlayer.prepare();
			audioDuration = mediaPlayer.getDuration();
			mediaPlayer.release();
			fileInputStream.close();
			if (printResultsToLog) {
				Log.v(logTag, "Measured Audio Duration: "+audioDuration+" ms, "
						+ RfcxAssetCleanup.conciseFilePath(audioFileObj.getAbsolutePath(), RfcxGuardian.APP_ROLE)
						+ " (" + (System.currentTimeMillis() - startTime) + " ms to process the measurement)"
				);
			}

		} catch (Exception e) {
			RfcxLog.logExc(logTag, e);
		}
		return audioDuration;
	}



	public Pair<byte[], Integer> getAudioBuffer() {
		return wavRecorderForCompanion.getAudioBuffer();
	}

	public Boolean isAudioChanged() {
		return wavRecorderForCompanion.isAudioChanged();
	}

}
