package org.rfcx.guardian.utility.device.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DeviceWifi {

    private static final String logTag = RfcxLog.generateLogTag("Utils", "DeviceWifi");
    private final WifiManager wifiManager;
    private String hotspotName;
    private String hotspotPassword;
    private boolean isVisible = true;

    public DeviceWifi(Context context) {
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private boolean isWifiEnabled() {
        if (this.wifiManager != null) {
            @SuppressLint("MissingPermission") int wifiState = this.wifiManager.getWifiState();
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_UNKNOWN:
                case WifiManager.WIFI_STATE_DISABLING:
                    return false;
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_ENABLING:
                    return true;
            }
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public void setPowerOn() {
        if (!isWifiEnabled()) {
            Log.v(logTag, "Activating Wifi Power");
            this.wifiManager.setWifiEnabled(true);
        }
    }

    @SuppressLint("MissingPermission")
    public void setPowerOff() {
        if (isWifiEnabled()) {
            Log.v(logTag, "Deactivating Wifi Power");
            this.wifiManager.setWifiEnabled(false);
        }
    }

    public boolean isHotspotEnabled() {
        try {
            Method wifiManagerMethods = this.wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            wifiManagerMethods.setAccessible(true);
            return (Boolean) wifiManagerMethods.invoke(this.wifiManager);
        } catch (NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            RfcxLog.logExc(logTag, e);
        }
        return false;
    }

    public void setHotspotConfig(String hotspotName, String hotspotPassword, boolean isVisible) {
        this.hotspotName = hotspotName;
        this.hotspotPassword = hotspotPassword;
        this.isVisible = isVisible;
    }

    public void setHotspotOn() {
        setHotspot(true);
    }

    public void setHotspotOff() {
        setHotspot(false);
    }

    public void setHotspot(boolean enableOrDisable) {

        Method[] wmMethods = this.wifiManager.getClass().getDeclaredMethods();   //Get all declared methods in WifiManager class
        boolean methodFound = false;
        for (Method method : wmMethods) {

            if (method.getName().equals("setWifiApEnabled")) {

                methodFound = true;

                WifiConfiguration wifiConfig = new WifiConfiguration();

                wifiConfig.SSID = this.hotspotName;
                wifiConfig.hiddenSSID = !this.isVisible;
                wifiConfig.status = WifiConfiguration.Status.ENABLED;

                if ((this.hotspotPassword != null) && !this.hotspotPassword.equalsIgnoreCase("")) {
                    wifiConfig.preSharedKey = this.hotspotPassword;
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                }

                try {
                    if (enableOrDisable) {
                        Log.v(logTag, "Activating Wifi Hotspot");
                    } else {
                        Log.v(logTag, "De-activating Wifi Hotspot");
                    }
                    boolean apStatus = (Boolean) method.invoke(wifiManager, wifiConfig, enableOrDisable);

                    for (Method isWifiApEnabledmethod : wmMethods) {
                        if (isWifiApEnabledmethod.getName().equals("isWifiApEnabled") && enableOrDisable) {
                            //	while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
                            for (Method thisMethod : wmMethods) {
                                if (thisMethod.getName().equals("getWifiApState")) {
                                    int apState = (Integer) thisMethod.invoke(wifiManager);
                                    Log.v(logTag, "Wifi Hotspot Network Name: '" + wifiConfig.SSID + "' (" + wifiConfig.preSharedKey + ")");
                                    break;
                                }
                            }
                            //	}
                        }
                    }

                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    RfcxLog.logExc(logTag, e);
                }
            }
        }
        if (!methodFound) {
            Log.e(logTag, "Failed to activate Wifi Hotspot (no method found)");
        }

    }

}
