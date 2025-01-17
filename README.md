# RFCx Guardian software

A collection of Android applications which, together, operate as autonomous Rainforest Connection guardians.

## Development environment

### Requirements

- [Android Studio](https://developer.android.com/studio) 3.2 or above
- (Optional: unless you need to recompile the native libs) Android SDK Tools: LLDB, CMake, NDK

### Getting started

#### Get the code

- Checkout the repo from GitHub
- Open in Android Studio
- Perform a Gradle sync (File -> Sync Project with Gradle) and accept licences/install SDK packages (if prompted by Android Studio)

#### Build Signed APKs (need Android Studio installed)

1. First need to prepare necessery files located in /gradle/signing/
   - rfcx-guardian-keystore.jks
   - rfcx-guardian-keystore-alias.txt
   - rfcx-guardian-keystore-password.txt
   - rfcx-platform-keystore.jks
   - rfcx-platform-keystore-alias.txt
   - rfcx-platform-keystore-password.txt
2. Use this command after prepared

   For specific role

   ```
   gradlew :role:assembleRelease  // gradlew :role-guardian:assembleRelease
   ```
   
   For all roles
   
   ```
   gradlew assembleRelease
   ```
   
3. Output will be in /{role}/build/outputs/apk/release/

#### Download apps from script

1. If your Environment variables do not have **ANDROID_SDK_ROOT**, create it and point to android-sdk root path
2. Run script with version **download-apk-install.sh 0.x.x** in /bin/

#### Build app APKs from script
Run **build-apk** script in /bin/apk/ with role as parameter
   ```
   build-apk.sh role  // build-apk.sh guardian
   ```

#### Install app APKs from script
Run **deploy-apk** script in /bin/apk/ with role as parameter
   ```
   deploy-apk.sh role  // deploy-apk.sh guardian
   ```

#### Publish app APKs to server from script
Run **publish-apk** script in /bin/apk/ with role and environment  as parameters
   ```
   publish-apk.sh role env // publish-apk.sh guardian staging
   ```

#### Run the guardian role from Android Studio (Android 4.4.2 recommended)

1. Clone this repo
2. Connect your phone with USB cable to your PC.
3. Open this project and wait for gradle syncing process.
4. You should be able to select your phone (or Orange Pi) for running the app (instead of an emulator).
4. Select **role-guardian** and press **Run** button to start building and installing process.
5. (If you are on Android 5 or above then...) The first time you run, you will need to quit the app, open the Android settings on your phone and enable all the permissions for the guardian app. And enable the Location Sharing(GPS). Then run the app again from Android Studio.
6. You can change environment of this guardian by enter to Preference menu at top right of the screen.
   - Leave it if you want this guardian to be on production.
   - If you want this guardian to be on staging, add **staging-** in front of the old ones on *api_rest_host* and *api_mqtt_host*.
6. Login (make sure your user account has guardianCreator role). Once logined, the browser will redirect to the app automatically.
7. Press Register button to register the guardian
8. If registration success, guid will show up on the screen and other UIs will show up.
9. Also, login to the *(production/staging)*Console/Dashboard -> Sites -> RFCx Lab -> check that you can see your guardian (you will see the first 4 chars of the guid)
10. Open "Logcat" in Android Studio and observe the logs for "Rfcx". You should see the Audio Capture service begin immediately, and after 90 seconds you should see the Checkin service upload the audio to the server. At this point you can log into the RFCx console and listen to the latest audio from your guardian.

#### Run the guardian role from APK (Android 4.4.2 recommended)

1. You can download latest release guardian APK from [here](https://github.com/rfcx/rfcx-guardian-android/releases)
2. Download [ADB driver](https://adb.clockworkmod.com/), if you do not have it installed.
3. Connect your phone with USB cable to your PC.
4. Enter this command in your terminal
   ```
   adb install path/to/apk/file.apk
   ```
5. Once the installation finished, now you can open the app from your phone.
6. (If you are on Android 5 or above then...) The first time you run, you will need to quit the app, open the Android settings on your phone and enable all the permissions for the guardian app. And enable the Location Sharing(GPS). Then run the app again from Android Studio.
7. You can change environment of this guardian by enter to Preference menu at top right of the screen.
   - Leave it if you want this guardian to be on production.
   - If you want this guardian to be on staging, add **staging-** in front of the old ones on *api_rest_host* and *api_mqtt_host*.
8. Login (make sure your user account has guardianCreator role). Once logined, the browser will redirect to the app automatically.
9. Press Register button to register the guardian
10. If registration success, guid will show up on the screen and other UIs will show up.
11. Also, login to the *(production/staging)* Console/Dashboard -> Sites -> RFCx Lab -> check that you can see your guardian (you will see the first 4 chars of the guid)
12. You can observe the app by using **adb logcat**
   ```
   adb logcat | grep Rfcx 
   ```

#### Run the admin role

Now admin role cannot be normally installed on the phone because it is now built as system application especially for Orange Pi which the signed key need to be the same as the phone.

#### Run the updater role

This can be done same as guardian role

## Instructions for Orange Pi 3G-IoT

Before getting started, key points to be familiar with:

1. On the Orange Pi board there are two LEDs, red and green. **Red** indicates the **power** is connected and **green** indicates that the device is **ON**. Make sure that the power that connected to the Orange Pi is **5V**. If not the LEDs brightness will be low and cannot start the operating system.
2. By default the Orange Pi comes with both COM 1 jumpers in the correct position. No need to ever change them it seems. COM 2 jumper should be ON only when flashing the device (the rest of the time it should be OFF/removed).
3. The OrangePi does not come with a pre-set **IMEI number**. You will need to set it yourself by following the instructions below.
4. Before using any roles, make sure there is the internet connection.
5. **Timezone automatically** need to uncheck so that Admin role can change it.
6. **Default Write Disk** need to set to **Phone Storage** although sd card is installed.
7. **Default SMS app** need to set to Admin role.
8. The OrangePi comes with USB debugging **enabled** by default.
9. The OrangePi comes with **auto allow the permission** (because it is Android 4.4.2).
10. The guardian role will capture audio and send to server automatically if:
    1. The date/time is (reasonably) close to current time
    2. Guardian is registered
    3. **SD card** is connected *(any size is okay -- tested up to 64GB)*
    4. There is internet connection

#### Install apps

You can install apps by 3 ways
1. [Using script in /bin/](https://github.com/rfcx/rfcx-guardian-android/tree/develop#download-apps-from-script)
2. [Using Android Studio](https://github.com/rfcx/rfcx-guardian-android/tree/develop#run-the-guardian-role-from-android-studio-android-442-recommended)
3. [Using latest release APKs in github](https://github.com/rfcx/rfcx-guardian-android/tree/develop#run-the-guardian-role-from-android-studio-android-442-recommended)

#### Run the guardian role

1. Login (make sure your user account has guardianCreator role). Once logined, the browser will be redirected to the app automatically.
2. You can change environment of this guardian by enter to Preference menu at top right of the screen.
   - Leave it if you want this guardian to be on production.
   - If you want this guardian to be on staging, add **staging-** in front of the old ones on *api_rest_host* and *api_mqtt_host*.
3. Login (make sure your user account has guardianCreator role). Once logined, the browser will redirect to the app automatically.
4. Press Register button to register the guardian
5. If registration success, guid will show up on the screen and other UIs will show up.
6. Also, login to the *(production/staging)* Console/Dashboard -> Sites -> RFCx Lab -> check that you can see your guardian (you will see the first 4 chars of the guid)
7. You can observe the app by using **adb logcat**
   ```
   adb logcat | grep Rfcx 
   ```
8. Or open **Logcat** in Android Studio and observe the logs for "Rfcx". You should see the Audio Capture service begin immediately, and after 90 seconds you should see the Checkin service upload the audio to the server. At this point you can log into the RFCx console and listen to the latest audio from your OrangePi.

#### Run the admin role

OrangePi has already been rooted and signed key also same as in the Android image, so you do not need to do anything extra to run admin role.

### Step 1: Flash Android on OrangePi (using Windows)

1. Make sure COM 2 jumper is ON. (Note: in practice we found that the jumper can be OFF for the flashing process.)
1. You need to download and install these tools
    1. [MTK Driver Installer](https://static.rfcx.org/tools/orangepi-3g-iot/driver_auto_installer_v5.1453.03.zip).
       Choose WIN8 if you are using Windows 8 or above.
       (Some users report failures during install -- [this is the solution](https://static.rfcx.org/tools/orangepi-3g-iot/video-how-to-resolve-mediatek-driver-install-failed-issue-on-windows10-64bit.mp4))
    2. [MTK Flash Tool](https://static.rfcx.org/tools/orangepi-3g-iot/sp_flash_tool_v5.1644_win.zip)
    3. Orange Pi Android image: [Android Image Downloads](https://github.com/rfcx/guardian-opi-android-source/releases/)
2. Open the MTK Flash Tool *(flash_tool)*
    1. Make sure the Orange Pi is NOT connected.
    2. Choose the scatter-loading file **MT6572_Android_scatter** *(This is in the Android_OS_for_Orange_Pi_3G-IoT/images/ folder after extracting the image.)*
    3. If this is the first time flashing the device then select "Download only". If you have flashed it before then select "Format all and download".
    4. Click "Download".
    5. Connect the Orange Pi to your computer using micro-usb cable into the Orange Pi and another side plug into your computer. *(The download process will begin after connecting the Orange Pi)*
    6. Disconnect the Orange Pi micro-usb cable after finishing.
    
### Step 2: Share Orange Pi screen using Vysor

Before following the instruction below. You need to download Vysor first.

- Normal program: https://www.vysor.io/download/

1. After you flashed the Orange Pi and disconnect the usb cable. Please make sure that you remove jumper cap from COM2 and the arrange jumper cap of COM1 as shown in the picture. *(the middle iron of jumper cap also must be the same as in the picture)*

   ![](docs/images/vysor1.PNG?raw=true)

2. Connect usb cable to your computer same as when flashing the device.

3. Once you connect to the computer, the green light led will light up for 5-6 seconds.

4. If the green led has disappeared, you need to press the power button until the green led light up again.

5. This process will take 5-8 minutes if it is first time boot up.

6. If you do these correctly the device will be appear in the Vysor.

   ![](docs/images/vysor2.PNG?raw=true)

7. Click view to show the Orange Pi screen

(Debugging: green led means powered off, red led means powered on.)

### Step 3: Set the IMEI number

Before you start, you will need to generate a suitable IMEI number. Please use this [Online IMEI Generator](https://static.rfcx.org/tools/imei/generator.html) to create a valid value. Use this sheet to keep track of the generated IMEI's:[IMEI Tracker](https://docs.google.com/spreadsheets/d/1FEOK2BMLc4xjuV1slUhLb8irDu9ld1TLiD39v8UPoZ8/edit#gid=0)

1. Make sure that COM 2 jumper is OFF (removed).
2. Make sure you do not have the SD Card inserted.
3. First download [IMEI Writer](https://static.rfcx.org/tools/orangepi-3g-iot/sn_writer_tool_exe_v1.1716.00.zip) (for Windows)
4. Extract and open **SN Write** in **SN_Writer_Tool_exe_v1.1716.00** directory
5. Choose **USB VCOM** and **Smart Phone**
6. Open System Config
    1. In **Write Option** choose **IMEI**
    2. In **IMEI Option** and **Header Option**, choose nothing
    3. In **MD1_DB** choose **BPLGUInfoCustomAppSrcP_MT6572_S00_MOLY_WR8_W1315_MD_WG_MP_V47_1_wg_n** in *Android_OS_for_Orange_Pi_3G-IoT\modem* (directory of android image)
    4. In **AP_DB** choose **APDB_MT6572_S01_KK1.MP7_** in the same directory
    5. In the newer version There are checkboxes called **"Load AP DB from DUT"** and **"Load modem DB from DUT"**. Make sure that you uncheck these two box before the next step.
    6. Save and then Start
    7. Put the IMEI Number with 15 digit
7. In the menus
   1. Open **Identify** -> **Engineer** -> **Enable AutoGen**
   2. In **IMEI** section, Edit **End** field to change the first digit to **9** (change no other digits in the field value)
   3. Click **OK** to save the settings
8. Make sure that Orange Pi is not connected to the PC.
9. Click OK and then connect Orange Pi to PC immediately (be quick else the device will not be detected in time and you will have to disconnect/start again).
10. In Vysor, go to Settings -> About Phone -> Status and scroll down to verify that IMEI number you entered during the flashing process is shown there. (If there is an error then repeat step 5).

   ![](docs/images/checkimei.png?raw=true)

### Step 4: Run the application through Android Studio

1. Make sure the Orange Pi is connected.

2. Open Android Studio

3. Click the **Run** button to see if the device connected. *(If Vysor sees the device, Android Studio should detect it too.)*

   ![](docs/images/androidstudio1.PNG?raw=true)

4. If the device connected, click on the device and OK button.

5. You should also see the device running in Vysor and receive the logcat messages on the Android Studio.

   ![](docs/images/androidstudio2.PNG?raw=true)

### Step 5: How to debug OrangePi over Bluetooth instead of USB cable

1. Make sure Orange Pi is connected, Bluetooth is on and set visibility timeout to never time out as follows.
    1. Bluetooth is *on*

       ![](docs/images/step8-1a.png?raw=true)
    2. Make sure the device is *Visible to all nearby*

       ![](docs/images/step8-1b.png?raw=true)
    3. Open the *Visibility timeout* (bottom right menu)

       ![](docs/images/step8-1c.png?raw=true)
    4. Set the timeout to *Never time out*

       ![](docs/images/step8-1d.png?raw=true)
       
2. Open Terminal or Command prompt

   ```
   $ adb shell
   $ setprop persist.adb.tcp.port 4455
   ```

3. Take USB off and start running Orange Pi from other power resources.

4. Pair Orange Pi with Bluetooth from your machine

5. _For Windows_, go to **\Control Panel\Network and Internet\Network Connections**
    1. Double click on **Bluetooth Network Connection**
    2. Right click on Orange Pi *(probably named as IOT03)*
    3. Connect using > Access point *(if it is **directly connect**, re-pair Orange Pi and try again)* 
    4. Open Command Prompt
    5. Type `ipconfig` and copy **default gateway IP** of Bluetooth Network Connection *(probably 192.168.44.1)*
    6. Type `adb connect <IP>:4455` *(adb connect 192.168.44.1:4455)*. It will show **connected to IP:4455** if successful.

    _For Mac_, open Bluetooth from the System Preferences
    1. Find the device named "IoT03" and connect.
    2. Open Terminal, and run `ifconfig`. You should see an entry (probably `en3`) with `inet 192.168.44.160`.
    3. Type `adb connect 192.168.44.1:4455` and it will show `connected to ...` if successful.

6. Now you can `adb shell` or you can open Vysor.

7. If you want to go back to USB debugging, you need to `adb shell` and then enter `setprop persist.adb.tcp.port ""`.

### Step 6: How to debug OrangePi over Wifi Hotspot

1. Require guardian and admin role installed.

2. Start guardian role app.

3. Open **Preferences setting** at top right menu

4. Set **admin_enable_wifi_hotspot** and **admin_enable_tcp_adb** to true

   ![](docs/images/pref_setting.PNG?raw=true)

5. Wifi hotspot will enable and tcp port will change to 7329. Wifi hotspot name will named as rfcx-{guid} and password is rfcxrfcx

6. Go to Settings > Wireless & Networks - **More...** > Tethering & portable hotspot > Wifi hotspot > Keep Wifi hotspot on > Change to **Always**

   ![](docs/images/wifi_hotspot.PNG?raw=true)

7. Then take USB cable off and power OrangePi with external power source

8. Connect your PC to OrangePi Wifi hotspot

9. Now you can debug your OrangePi using its IP (default is 192.168.43.1)

   ```
   adb connect 192.168.43.1:7329
   ```

10. Then you can see the screen using Vysor

### Step 7: How to set Default SMS app to Admin role

1. Require Admin role installed

2. Go to Settings > Wireless & Networks - **More...** > Default SMS app > Choose **RFCx Admin**

   ![](docs/images/sms_to_admin.PNG?raw=true)

3. Or open the admin role and set it from the app itself

### Step 8: How to set Default Write Disk to Phone storage

1. Go to Settings > Device - **Storage** > Choose Default Write Disk - **Phone storage**

   ![](docs/images/write_disk.PNG?raw=true)

### Step 9: How to set Timezone automatically to off

1. Go to Setting > System - **Date & Time** > Uncheck **Automatic time zone**

   ![](docs/images/timezone_off.PNG?raw=true)

### Step 10: How to connect i2c and load i2c module

1. First, place Orange Pi same position as in the image.

   ![](docs/images/i2c1.PNG?raw=true)

2. This will use the most right 10 pins of the board, so you should take the pin 11-12 off or bend it into the left side.

   ![](docs/images/i2c2.PNG?raw=true)

3. Put the power wires(pack of yellow and red wires) into the most right 10 pins by the red wire should be on the right side.

   ![](docs/images/i2c3.PNG?raw=true)

4. The step to start Orange Pi is the same as before.

5. You can debug OrangePi by using Bluetooth on [Step 5](https://github.com/rfcx/rfcx-guardian-android/tree/develop#step-5-how-to-debug-orangepi-over-bluetooth-instead-of-usb-cable) or [Step 6](https://github.com/rfcx/rfcx-guardian-android/tree/develop#step-6-how-to-debug-orangepi-over-wifi-hotspot)

## Instructions for Android phone

There are differences for normal Android phone and OrangePi.
- It is not rooted by default.
- Android image is not the same
- System app cannot be installed because of different system signed key
- You need to enable permission for apps yourself.

#### Install apps

The process is same as OrangePi

#### Run the guardian role

The process is same as OrangePi but you need to enable Permission yourself by
- (If you are on Android 5 or above then...) The first time you run, you will need to quit the app, open the Android settings on your phone and enable all the permissions for the guardian app. And enable the Location Sharing(GPS) then run the app again.

#### Run the admin role

Now admin role cannot be normally installed on the phone because it is now built as system application especially for Orange Pi which the signed key need to be the same as the phone.

#### Run the updater role

This can be done same as guardian role
