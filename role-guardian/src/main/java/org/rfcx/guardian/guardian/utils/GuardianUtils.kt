package org.rfcx.guardian.guardian.utils

import android.content.Context
import android.net.ConnectivityManager
import org.rfcx.guardian.guardian.RfcxGuardian
import java.io.File
import java.io.FileOutputStream

object GuardianUtils {

    fun isGuardianRegistered(context: Context): Boolean {
        val path = context.filesDir.toString() + "/txt/"
        val tokenFile = File(path + "token")
        return tokenFile.exists()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
