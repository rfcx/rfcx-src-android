package org.rfcx.guardian.guardian.companion

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.rfcx.guardian.guardian.RfcxGuardian
import org.rfcx.guardian.utility.rfcx.RfcxLog
import org.rfcx.guardian.utility.rfcx.RfcxPrefs

class OldWifiCommunicationUtils(private val context: Context) {

    private val app = context.applicationContext as RfcxGuardian
    private val logTag = RfcxLog.generateLogTag(
        RfcxGuardian.APP_ROLE,
        "OldWifiCommunicationUtils"
    )

    fun getCurrentConfigurationAsJson(): JSONArray {
        val configurationJsonArray = JSONArray()
        try {
            val configurationJson = JSONObject()
            val sampleRate = app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CAPTURE_SAMPLE_RATE)
            val bitrate = app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_STREAM_BITRATE)
            val fileFormat = app.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.AUDIO_STREAM_CODEC)
            val duration = app.rfcxPrefs.getPrefAsInt(RfcxPrefs.Pref.AUDIO_CYCLE_DURATION)

            configurationJson.let {
                it.put("sample_rate", sampleRate)
                it.put("bitrate", bitrate)
                it.put("file_format", fileFormat)
                it.put("duration", duration)
            }

            configurationJsonArray.put(configurationJson)
        } catch (e: Exception) {
            RfcxLog.logExc(logTag, e)
        } finally {
            return configurationJsonArray
        }
    }

    fun getDiagnosticAsJson(): JSONArray {
        val diagnosticJsonArray = JSONArray()
        try {
            val diagnosticJson = JSONObject()
            val battery = app.deviceBattery.getBatteryChargePercentage(this.context, null)

            diagnosticJson.let {
                it.put("battery_percentage", battery)
            }

            diagnosticJsonArray.put(diagnosticJson)
        } catch (e: Exception) {
            RfcxLog.logExc(logTag, e)
        } finally {
            return diagnosticJsonArray
        }
    }

    fun getPrefsChangesAsJson(): JSONArray {
        val jsonArray = JSONArray()
        val jsonObject = JSONObject()
        jsonObject.put("result", "success")
        jsonArray.put(jsonObject)
        return  jsonArray
    }

    fun getAudioBufferAsJson(): JSONArray? {
        if (app.audioCaptureUtils.isAudioChanged) {
            val jsonArray = JSONArray()
            val jsonObject = JSONObject()
            val audioBufferPair = app.audioCaptureUtils.audioBuffer
            jsonObject.put("buffer", Base64.encodeToString(audioBufferPair.first, Base64.NO_WRAP))
            jsonObject.put("read_size", audioBufferPair.second)
            jsonArray.put(jsonObject)
            return  jsonArray
        }
        return JSONArray()
    }
}