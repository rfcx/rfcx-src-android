package org.rfcx.guardian.admin.comms.swm.api

import android.util.Log
import org.rfcx.guardian.admin.comms.swm.data.*
import java.text.SimpleDateFormat
import java.util.*

class SwmApi(private val connection: SwmConnection) {

    enum class Command { TD, MT, SL, RT, DT, PO, CS, GS, RS }

    private val datetimeCompactFormatter =
        SimpleDateFormat("yyyyMMddHHmmss").also { it.timeZone = TimeZone.getTimeZone("GMT") }

    private fun resetDb() {
        /*
         * There is not response back from this command
         */
        connection.execute(Command.RS.name, "dbinit")
    }

    fun transmitData(msgStr: String, priority: Int = 2): String? {
        val fullMsg = if (priority == 1) {
            "HD=43200,$msgStr" // plus 12 hours
        } else {
            "HD=10800,$msgStr" // plus 3 hours
        }
        val results = connection.execute(Command.TD.name, fullMsg)
        /*
         * DBXTOHIVEFULL is the case when the queued database is full
         * The possible case is the expired messages not being removed from the database
         * It happens if the Tile cannot communicate with the satellite
         */
        if (results.contains("DBXTOHIVEFULL")) {
            resetDb()
        }
        val regex = "OK,(-?[0-9]+)".toRegex()
        val firstMatchResult = results.mapNotNull { regex.find(it) }.firstOrNull()
        return firstMatchResult?.let { match ->
            val (messageId) = match.destructured
            return messageId
        }
    }

    fun getUnsentMessages(): List<SwmUnsentMsg>? {
        val results = connection.execute(Command.MT.name, "L=U", 10)
        val unsentMessages = arrayListOf<SwmUnsentMsg>()
        if (results.isEmpty()) return null
        results.forEach { payload ->
            "(-?[a-z0-9]+),(-?[0-9]+),(-?[0-9]+)".toRegex().find(payload)?.let { result ->
                val (message, id, timestamp) = result.destructured
                unsentMessages.add(SwmUnsentMsg(message, id, timestamp))
            } ?: return null
        }
        return unsentMessages
    }

    fun getNumberOfUnsentMessages(): Int {
        return connection.execute(Command.MT.name, "C=U", 3).firstOrNull()?.let { payload ->
            return "(-?[0-9]+)".toRegex().find(payload)?.let { result ->
                val (count) = result.destructured
                Log.d("SwmCommand", "MT= $count")
                return count.toInt()
            } ?: -1
        } ?: -1
    }

    fun powerOff(): Boolean {
        return connection.execute(Command.PO.name, "").firstOrNull()?.let { payload ->
            return payload.contains("OK")
        } ?: false
    }

    fun getRTSatellite(): SwmRTResponse? {
        val results = connection.executeWithoutTimeout(Command.RT.name, "@")
        val regex =
            "RSSI=(-?[0-9]+),SNR=(-?[0-9]+),FDEV=(-?[0-9]+),TS=([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}),DI=(0x[0-9]+)".toRegex()
        val firstMatchResult = results.mapNotNull { regex.find(it) }.firstOrNull()
        return firstMatchResult?.let { match ->
            val (rssi, snr, fdev, time, satId) = match.destructured
            return SwmRTResponse(rssi.toInt(), snr.toInt(), fdev.toInt(), time, satId)
        }
    }

    fun getRTBackground(): SwmRTBackgroundResponse? {
        // Set the background rate to 1s and wait 1.5s to get a result
        val result =
            connection.execute(Command.RT.name, "2", 2).filter { !it.contains("OK") }.firstOrNull()
                ?.let { payload ->
                    Log.d("RfcxSwmCommand", "RT Res=$payload")
                    "RSSI=(-?[0-9]+)".toRegex().find(payload)?.let { match ->
                        val (rssi) = match.destructured
                        SwmRTBackgroundResponse(rssi = rssi.toInt())
                    }
                }
        Log.d("RfcxSwmCommand", "RT RSSI=${result?.rssi}")
        // Set the rate back to off
        connection.executeWithoutTimeout(Command.RT.name, "0")
        return result
    }

    fun getDateTime(): SwmDTResponse? {
        val datetime = connection.executeWithoutTimeout(Command.DT.name, "@")
            .firstOrNull()?.let {
                val match = "^([0-9]{14}),V$".toRegex().find(it) ?: return null
                datetimeCompactFormatter.parse(match.groupValues[1])
            } ?: return null
        return SwmDTResponse(datetime.time)
    }

    fun getSwarmDeviceId(): String? {
        return connection.executeWithoutTimeout(Command.CS.name, "")
            .firstOrNull()?.let {
                val match = "DI=0x([a-fA-F0-9]+)".toRegex().find(it) ?: return null
                val (deviceId) = match.destructured
                deviceId.lowercase()
            } ?: return null
    }

    fun getGPSConnection(): SwmGSResponse? {
        return connection.executeWithoutTimeout(Command.GS.name, "@")
            .firstOrNull()?.let {
                val match = "([0-9]+),([0-9]+),([0-9]+),([0-9]+),([A-Z0-9]+)".toRegex().find(it)
                    ?: return null
                val (hdop, vdop, gnss, unused, type) = match.destructured
                return SwmGSResponse(hdop.toInt(), vdop.toInt(), gnss.toInt(), type)
            } ?: return null
    }

    fun sleep(): Boolean {
        return connection.execute(Command.SL.name, "S=10800").firstOrNull()?.let { payload ->
            return payload.contains("OK")
        } ?: false
    }
}
