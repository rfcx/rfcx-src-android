package org.rfcx.guardian.guardian.manager

import android.content.Context
import android.content.SharedPreferences
import java.util.*

class PreferenceManager(context: Context){

    private var sharedPreferences: SharedPreferences

    companion object {
        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenceManager(context).also { INSTANCE = it }
            }

        private const val PREFERENCES_NAME = "Rfcx.Guardian"
        private const val PREFIX = "org.rfcx.guardian:"

        const val ID_TOKEN = "${PREFIX}ID_TOKEN"
        const val ACCESS_TOKEN = "${PREFIX}ACCESS_TOKEN"
        const val REFRESH_TOKEN = "${PREFIX}REFRESH_TOKEN"
        const val USER_GUID = "${PREFIX}USER_GUID"
        const val EMAIL = "${PREFIX}EMAIL"
        const val NICKNAME = "${PREFIX}NICKNAME"
        const val ROLES = "${PREFIX}ROLES"
        const val ACCESSIBLE_SITES = "${PREFIX}ACCESSIBLE_SITES"
        const val DEFAULT_SITE = "${PREFIX}SITE"
        const val HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP = "${PREFIX}HAS_SUBSCRIBED_TO_DEFAULT_SITE"
        const val SHOULD_RECEIVE_EVENT_NOTIFICATIONS = "${PREFIX}SHOULD_RECEIVE_EVENT_NOTIFICATIONS"
        const val SELECTED_GUARDIAN_GROUP = "${PREFIX}SELECTED_GUARDIAN_GROUP"
        const val GUARDIAN_GROUPS_LAST_UPDATED = "${PREFIX}GUARDIAN_GROUPS_LAST_UPDATED"
        const val ENABLE_LOCATION_TRACKING = "${PREFIX}ENABLE_LOCATION_TRACKING"
    }

    init {
        sharedPreferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getString(key: String, defValue: String): String {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

//    fun <E> getObject(key: String, objClass: Class<E>): E? {
//        val objectJson = sharedPreferences.getString(key, null) ?: return null
//        return GsonProvider.getInstance().gson.fromJson(objectJson, objClass)
//    }

//    fun putObject(key: String, value: Any) {
//        sharedPreferences.edit().putString(key, GsonProvider.getInstance().gson.toJson(value)).apply()
//    }

    fun getDate(key: String): Date? {
        val secondsSinceEpoch = sharedPreferences.getLong(key, 0L)
        if (secondsSinceEpoch == 0L) {
            return null
        }
        return Date(secondsSinceEpoch)
    }

    fun putDate(key: String, date: Date) {
        sharedPreferences.edit().putLong(key, date.time).apply()
    }

    fun getLong(key: String, defValue: Long): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    fun putLong(key: String, long: Long) {
        sharedPreferences.edit().putLong(key, long).apply()
    }

    fun getStringSet(key: String): Set<String> {
        return sharedPreferences.getStringSet(key, setOf()) ?: setOf()
    }

    fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}