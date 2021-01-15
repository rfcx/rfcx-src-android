package org.rfcx.guardian.admin;

import org.rfcx.guardian.admin.asset.AssetUtils;
import org.rfcx.guardian.admin.asset.ScheduledAssetCleanupService;
import org.rfcx.guardian.admin.device.android.capture.CameraCaptureDb;
import org.rfcx.guardian.admin.device.android.capture.CameraCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScheduledCameraCaptureService;
import org.rfcx.guardian.admin.device.android.control.ScheduledClockSyncService;
import org.rfcx.guardian.admin.device.android.ssh.SSHServerControlService;
import org.rfcx.guardian.admin.device.sentinel.SentinelCompassUtils;
import org.rfcx.guardian.admin.device.sentinel.SentinelSensorDb;
import org.rfcx.guardian.admin.device.sentinel.SentinelAccelUtils;
import org.rfcx.guardian.admin.sms.SmsDispatchCycleService;
import org.rfcx.guardian.admin.sms.SmsMessageDb;
import org.rfcx.guardian.admin.device.android.control.ADBStateSetService;
import org.rfcx.guardian.admin.sms.SmsDispatchService;
import org.rfcx.guardian.admin.device.android.control.WifiHotspotStateSetService;
import org.rfcx.guardian.admin.device.android.system.DeviceDataTransferDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSpaceDb;
import org.rfcx.guardian.admin.device.android.system.DeviceRebootDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSensorDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSystemDb;
import org.rfcx.guardian.admin.device.android.system.DeviceSystemService;
import org.rfcx.guardian.admin.device.android.system.DeviceUtils;
import org.rfcx.guardian.i2c.DeviceI2cUtils;
import org.rfcx.guardian.utility.device.capture.DeviceBattery;
import org.rfcx.guardian.utility.device.capture.DeviceCPU;
import org.rfcx.guardian.utility.device.capture.DeviceMobileNetwork;
import org.rfcx.guardian.utility.device.capture.DeviceMobilePhone;
import org.rfcx.guardian.utility.device.capture.DeviceNetworkStats;
import org.rfcx.guardian.utility.device.control.DeviceGPIOUtils;
import org.rfcx.guardian.utility.device.control.DeviceNetworkName;
import org.rfcx.guardian.utility.device.control.DeviceUARTUtils;
import org.rfcx.guardian.utility.device.control.DeviceWallpaper;
import org.rfcx.guardian.utility.device.hardware.DeviceHardware_OrangePi_3G_IOT;
import org.rfcx.guardian.utility.misc.DateTimeUtils;
import org.rfcx.guardian.utility.device.DeviceConnectivity;
import org.rfcx.guardian.utility.device.control.DeviceAirplaneMode;
import org.rfcx.guardian.utility.rfcx.RfcxGuardianIdentity;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;
import org.rfcx.guardian.utility.rfcx.RfcxRole;
import org.rfcx.guardian.utility.service.RfcxServiceHandler;

import org.rfcx.guardian.admin.device.android.capture.LogcatDb;
import org.rfcx.guardian.admin.device.android.capture.LogcatCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScreenShotDb;
import org.rfcx.guardian.admin.device.android.capture.ScreenShotCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScheduledLogcatCaptureService;
import org.rfcx.guardian.admin.device.android.capture.ScheduledScreenShotCaptureService;
import org.rfcx.guardian.admin.device.android.control.AirplaneModeToggleService;
import org.rfcx.guardian.admin.device.android.control.AirplaneModeEnableService;
import org.rfcx.guardian.admin.device.android.control.ScheduledRebootService;
import org.rfcx.guardian.admin.device.android.control.ClockSyncJobService;
import org.rfcx.guardian.admin.device.android.control.ForceRoleRelaunchService;
import org.rfcx.guardian.admin.device.android.control.RebootTriggerService;
import org.rfcx.guardian.admin.device.sentinel.DeviceSentinelService;
import org.rfcx.guardian.admin.device.sentinel.SentinelPowerDb;
import org.rfcx.guardian.admin.device.sentinel.SentinelPowerUtils;
import org.rfcx.guardian.admin.receiver.AirplaneModeReceiver;
import org.rfcx.guardian.admin.receiver.ConnectivityReceiver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

public class RfcxGuardian extends Application {
	
	public String version;
	
	public static final String APP_ROLE = "Admin";

	private static final String logTag = RfcxLog.generateLogTag(APP_ROLE, "RfcxGuardian");

	public RfcxGuardianIdentity rfcxGuardianIdentity = null;
	public RfcxPrefs rfcxPrefs = null;
	public RfcxServiceHandler rfcxServiceHandler = null;
	
	public ScreenShotDb screenShotDb = null;
	public CameraCaptureDb cameraCaptureDb = null;
	public LogcatDb logcatDb = null;
	public SentinelPowerDb sentinelPowerDb = null;
	public SentinelSensorDb sentinelSensorDb = null;
	public DeviceSystemDb deviceSystemDb = null;
    public DeviceSensorDb deviceSensorDb = null;
    public DeviceRebootDb rebootDb = null;
    public DeviceDataTransferDb deviceDataTransferDb = null;
    public DeviceSpaceDb deviceSpaceDb = null;
    public SmsMessageDb smsMessageDb = null;

	public DeviceConnectivity deviceConnectivity = new DeviceConnectivity(APP_ROLE);
	public DeviceAirplaneMode deviceAirplaneMode = new DeviceAirplaneMode(APP_ROLE);

	// Android Device Handlers
    public DeviceBattery deviceBattery = new DeviceBattery(APP_ROLE);
    public DeviceNetworkStats deviceNetworkStats = new DeviceNetworkStats(APP_ROLE);
    public DeviceCPU deviceCPU = new DeviceCPU(APP_ROLE);
    public DeviceUtils deviceUtils = null;
	public DeviceMobilePhone deviceMobilePhone = null;
	public DeviceMobileNetwork deviceMobileNetwork = new DeviceMobileNetwork(APP_ROLE);
	public AssetUtils assetUtils = null;

	public DeviceI2cUtils deviceI2cUtils = new DeviceI2cUtils(APP_ROLE);
	public SentinelPowerUtils sentinelPowerUtils = null;
	public SentinelCompassUtils sentinelCompassUtils = null;
	public SentinelAccelUtils sentinelAccelUtils = null;

	public DeviceGPIOUtils deviceGPIOUtils = new DeviceGPIOUtils(APP_ROLE);
	public DeviceUARTUtils deviceUARTUtils = new DeviceUARTUtils(APP_ROLE);

	// Receivers
	private final BroadcastReceiver connectivityReceiver = new ConnectivityReceiver();
	private final BroadcastReceiver airplaneModeReceiver = new AirplaneModeReceiver();
	
	public String[] RfcxCoreServices = 
			new String[] {
				DeviceSystemService.SERVICE_NAME,
				DeviceSentinelService.SERVICE_NAME,
				SmsDispatchCycleService.SERVICE_NAME
			};

	@Override
	public void onCreate() {

		super.onCreate();

		this.rfcxGuardianIdentity = new RfcxGuardianIdentity(this, APP_ROLE);
		this.rfcxPrefs = new RfcxPrefs(this, APP_ROLE);
		this.rfcxServiceHandler = new RfcxServiceHandler(this, APP_ROLE);

		this.version = RfcxRole.getRoleVersion(this, logTag);
		RfcxRole.writeVersionToFile(this, logTag, this.version);

		this.registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		this.registerReceiver(airplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

		setDbHandlers();
		setServiceHandlers();

		DeviceNetworkName.setName("rfcx-"+this.rfcxGuardianIdentity.getGuid(), this);
		this.deviceUtils = new DeviceUtils(this);
		this.sentinelPowerUtils = new SentinelPowerUtils(this);
		this.sentinelCompassUtils = new SentinelCompassUtils(this);
		this.sentinelAccelUtils = new SentinelAccelUtils(this);
		this.assetUtils = new AssetUtils(this);

		// Hardware-specific hacks and modifications
		runHardwareSpecificModifications();

		// Initialize I2C Handler
		this.deviceI2cUtils.initializeOrReInitialize();

		// Android-Build-specific hacks and modifications
		// This is not necessary if this app role is running as "system"
		// DateTimeUtils.resetDateTimeReadWritePermissions(this);

		initializeRoleServices();

		DateTimeUtils.setSystemTimezone(this.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_SYSTEM_TIMEZONE), this);

	}
	
	public void onTerminate() {
		super.onTerminate();

		this.unregisterReceiver(connectivityReceiver);
		this.unregisterReceiver(airplaneModeReceiver);
	}
	
	public void appResume() { }
	
	public void appPause() { }

	public ContentResolver getResolver() {
		return this.getApplicationContext().getContentResolver();
	}

	public boolean isGuardianRegistered() {
		return (this.rfcxGuardianIdentity.getAuthToken() != null);
	}
	
	public void initializeRoleServices() {
		
		if (!this.rfcxServiceHandler.hasRun("OnLaunchServiceSequence")) {
			
			String[] runOnceOnlyOnLaunch = new String[] {
					ServiceMonitor.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:02:00").getTimeInMillis() // waits two minutes before running
							+ "|" + ServiceMonitor.SERVICE_MONITOR_CYCLE_DURATION
							,
					ScheduledAssetCleanupService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:03:00").getTimeInMillis() // waits three minutes before running
							+ "|" + ( ScheduledAssetCleanupService.ASSET_CLEANUP_CYCLE_DURATION_MINUTES * 60 * 1000 )
							,
					ScheduledScreenShotCaptureService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:00:45").getTimeInMillis() // waits forty five seconds before running
							+ "|" + ( this.rfcxPrefs.getPrefAsLong(RfcxPrefs.Pref.ADMIN_SCREENSHOT_CAPTURE_CYCLE) * 60 * 1000 )
							,
					ScheduledLogcatCaptureService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:03:00").getTimeInMillis() // waits three minutes before running
							+ "|" + ( this.rfcxPrefs.getPrefAsLong(RfcxPrefs.Pref.ADMIN_LOG_CAPTURE_CYCLE) * 60 * 1000 )
							,
					ScheduledCameraCaptureService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:04:00").getTimeInMillis() // waits four minutes before running
							+ "|" + ( this.rfcxPrefs.getPrefAsLong(RfcxPrefs.Pref.ADMIN_CAMERA_CAPTURE_CYCLE) * 60 * 1000 )
							,
					ADBStateSetService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:00:10").getTimeInMillis() // waits ten seconds before running
							+ "|" + "norepeat"
							,
					WifiHotspotStateSetService.SERVICE_NAME
							+ "|" + DateTimeUtils.nowPlusThisLong("00:00:15").getTimeInMillis() // waits fifteen seconds before running
							+ "|" + "norepeat"
							,
					ScheduledRebootService.SERVICE_NAME
							+ "|" + DateTimeUtils.nextOccurrenceOf(this.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.REBOOT_FORCED_DAILY_AT)).getTimeInMillis()
							+ "|" + "norepeat"
			};
			
			String[] onLaunchServices = new String[ RfcxCoreServices.length + runOnceOnlyOnLaunch.length ];
			System.arraycopy(RfcxCoreServices, 0, onLaunchServices, 0, RfcxCoreServices.length);
			System.arraycopy(runOnceOnlyOnLaunch, 0, onLaunchServices, RfcxCoreServices.length, runOnceOnlyOnLaunch.length);
			this.rfcxServiceHandler.triggerServiceSequence( "OnLaunchServiceSequence", onLaunchServices, false, 0);
		}
	}
	
	private void setDbHandlers() {
		
		this.sentinelPowerDb = new SentinelPowerDb(this, this.version);
		this.sentinelSensorDb = new SentinelSensorDb(this, this.version);
		this.screenShotDb = new ScreenShotDb(this, this.version);
		this.cameraCaptureDb = new CameraCaptureDb(this, this.version);
		this.logcatDb = new LogcatDb(this, this.version);
		this.deviceSystemDb = new DeviceSystemDb(this, this.version);
        this.deviceSensorDb = new DeviceSensorDb(this, this.version);
        this.rebootDb = new DeviceRebootDb(this, this.version);
        this.deviceDataTransferDb = new DeviceDataTransferDb(this, this.version);
        this.deviceSpaceDb = new DeviceSpaceDb(this, this.version);
        this.smsMessageDb = new SmsMessageDb(this, this.version);
		this.deviceMobilePhone = new DeviceMobilePhone(this);
	}

	private void setServiceHandlers() {
		this.rfcxServiceHandler.addService( ServiceMonitor.SERVICE_NAME, ServiceMonitor.class);
		this.rfcxServiceHandler.addService( ScheduledAssetCleanupService.SERVICE_NAME, ScheduledAssetCleanupService.class);

		this.rfcxServiceHandler.addService( AirplaneModeToggleService.SERVICE_NAME, AirplaneModeToggleService.class);
		this.rfcxServiceHandler.addService( AirplaneModeEnableService.SERVICE_NAME, AirplaneModeEnableService.class);

		this.rfcxServiceHandler.addService( WifiHotspotStateSetService.SERVICE_NAME, WifiHotspotStateSetService.class);
		this.rfcxServiceHandler.addService( ADBStateSetService.SERVICE_NAME, ADBStateSetService.class);

        this.rfcxServiceHandler.addService( SmsDispatchService.SERVICE_NAME, SmsDispatchService.class);
		this.rfcxServiceHandler.addService( SmsDispatchCycleService.SERVICE_NAME, SmsDispatchCycleService.class);

		this.rfcxServiceHandler.addService( ClockSyncJobService.SERVICE_NAME, ClockSyncJobService.class);
		this.rfcxServiceHandler.addService( ScheduledClockSyncService.SERVICE_NAME, ScheduledClockSyncService.class);

		this.rfcxServiceHandler.addService( ForceRoleRelaunchService.SERVICE_NAME, ForceRoleRelaunchService.class);

		this.rfcxServiceHandler.addService( RebootTriggerService.SERVICE_NAME, RebootTriggerService.class);
		this.rfcxServiceHandler.addService( ScheduledRebootService.SERVICE_NAME, ScheduledRebootService.class);

		this.rfcxServiceHandler.addService( DeviceSystemService.SERVICE_NAME, DeviceSystemService.class);
		this.rfcxServiceHandler.addService( DeviceSentinelService.SERVICE_NAME, DeviceSentinelService.class);

		this.rfcxServiceHandler.addService( ScreenShotCaptureService.SERVICE_NAME, ScreenShotCaptureService.class);
		this.rfcxServiceHandler.addService( ScheduledScreenShotCaptureService.SERVICE_NAME, ScheduledScreenShotCaptureService.class);

		this.rfcxServiceHandler.addService( LogcatCaptureService.SERVICE_NAME, LogcatCaptureService.class);
		this.rfcxServiceHandler.addService( ScheduledLogcatCaptureService.SERVICE_NAME, ScheduledLogcatCaptureService.class);

		this.rfcxServiceHandler.addService( CameraCaptureService.SERVICE_NAME, CameraCaptureService.class);
		this.rfcxServiceHandler.addService( ScheduledCameraCaptureService.SERVICE_NAME, ScheduledCameraCaptureService.class);

		this.rfcxServiceHandler.addService("SSHServerControl", SSHServerControlService.class);

	}

	public void onPrefReSync(String prefKey) {

		if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_ENABLE_WIFI)) {
			rfcxServiceHandler.triggerService( WifiHotspotStateSetService.SERVICE_NAME, false);
			rfcxServiceHandler.triggerService( ADBStateSetService.SERVICE_NAME, false);

		} else if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_ENABLE_TCP_ADB)) {
			rfcxServiceHandler.triggerService( ADBStateSetService.SERVICE_NAME, false);

		} else if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_SYSTEM_TIMEZONE)) {
			DateTimeUtils.setSystemTimezone(this.rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_SYSTEM_TIMEZONE), this);

		} else if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.REBOOT_FORCED_DAILY_AT)) {
			Log.e(logTag, "Pref ReSync: ADD CODE FOR FORCING RESET OF SCHEDULED REBOOT");

		} else if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_ENABLE_SSH_SERVER)) {
			rfcxServiceHandler.triggerService("SSHServerControl", false);

		} else if (prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_ENABLE_GEOPOSITION_CAPTURE) || prefKey.equalsIgnoreCase(RfcxPrefs.Pref.ADMIN_GEOPOSITION_CAPTURE_CYCLE)) {
			rfcxServiceHandler.triggerService( DeviceSystemService.SERVICE_NAME, true);
		}
	}

	private void runHardwareSpecificModifications() {

		if (DeviceHardware_OrangePi_3G_IOT.isDevice_OrangePi_3G_IOT()) {

			// Disable Sensor Listeners for sensors the don't exist on the OrangePi 3G-IoT
			this.deviceUtils.disableSensorListener("accel"); // accelerometer
			this.deviceUtils.disableSensorListener("light");  // light meter

			// Set Desktop Wallpaper to empty black
			DeviceWallpaper.setWallpaper(this, R.drawable.black);

			// Rename Device Hardware with /system/build.prop.
			// Only occurs once, on initial launch, and requires reboot if changes are made.
			DeviceHardware_OrangePi_3G_IOT.checkSetDeviceHardwareIdentification(this);

			// Sets I2C interface
			this.deviceI2cUtils.setInterface(DeviceHardware_OrangePi_3G_IOT.DEVICE_I2C_INTERFACE);

			// Sets GPIO interface
			this.deviceGPIOUtils.setGpioHandlerFilepath(DeviceHardware_OrangePi_3G_IOT.DEVICE_GPIO_HANDLER_FILEPATH);
			this.deviceGPIOUtils.setPinsByName(DeviceHardware_OrangePi_3G_IOT.DEVICE_GPIO_PINMAP);

			// Sets UART interface
			this.deviceUARTUtils.setInterface(DeviceHardware_OrangePi_3G_IOT.DEVICE_UART_INTERFACE);

		}

	}
    
}
