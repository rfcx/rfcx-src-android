package org.rfcx.guardian.guardian.file;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.misc.FileUtils;
import org.rfcx.guardian.utility.network.SocketUtils;
import org.rfcx.guardian.utility.rfcx.RfcxComm;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxPrefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileSocketUtils {

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "FileSocketUtils");
    private final RfcxGuardian app;
    public SocketUtils socketUtils;
    private boolean isReading = false;
    private JSONObject pingObj;

    public FileSocketUtils(Context context) {
        this.app = (RfcxGuardian) context.getApplicationContext();
        this.socketUtils = new SocketUtils();
        this.socketUtils.setSocketPort(RfcxComm.TCP_PORTS.GUARDIAN.SOCKET.FILE);
        this.pingObj = new JSONObject();
    }

    public boolean sendDownloadResult(String result) {
        return this.socketUtils.sendJson(result, areSocketInteractionsAllowed());
    }

    public boolean sendPingCheckingConnection() {
        return this.socketUtils.sendJson(getPingObject().toString(), areSocketInteractionsAllowed());
    }

    public void resetObject() {
        this.pingObj = new JSONObject();
        isReading = false;
    }

    public JSONObject getPingObject() {
        return this.pingObj;
    }

    private boolean areSocketInteractionsAllowed() {

        if ((app != null) && socketUtils.isServerRunning) {
            return true;
        }
        Log.d(logTag, "FileSocket interaction blocked.");
        return false;
    }

    public boolean isSocketServerEnablable(boolean verboseLogging, RfcxPrefs rfcxPrefs) {

        boolean prefsEnableSocketServer = rfcxPrefs.getPrefAsBoolean(RfcxPrefs.Pref.ENABLE_FILE_SOCKET);

        String prefsWifiFunction = rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION);
        boolean isWifiEnabled = prefsWifiFunction.equals("hotspot") || prefsWifiFunction.equals("client");

        String prefsBluetoothFunction = rfcxPrefs.getPrefAsString(RfcxPrefs.Pref.ADMIN_BLUETOOTH_FUNCTION);
        boolean isBluetoothEnabled = prefsBluetoothFunction.equals("pan");

        if (verboseLogging && prefsEnableSocketServer && !isWifiEnabled && !isBluetoothEnabled) {
            Log.e(logTag, "Socket Server could not be enabled because '" + RfcxPrefs.Pref.ADMIN_WIFI_FUNCTION + "' and '" + RfcxPrefs.Pref.ADMIN_BLUETOOTH_FUNCTION + "' are set to off.");
        }

        return prefsEnableSocketServer && (isWifiEnabled || isBluetoothEnabled);
    }

    public void startServer() {

        socketUtils.serverThread = new Thread(() -> {
            Looper.prepare();
            try {
                socketUtils.serverSetup();
                while (true) {
                    if (socketUtils.serverThread.isInterrupted()) {
                        Log.d(logTag, "interrupted");
                        Looper.myLooper().quit();
                        return;
                    }
                    InputStream socketInput = socketUtils.socketSetup();
                    if (socketInput != null) {
                        InputStream fileInput = socketUtils.streamFileSetup(socketInput);
                        StringBuilder fileName = new StringBuilder();

                        //read until reach '|'
                        Log.d(logTag, "Receiving file transfer from Companion");
                        if (!isReading) {
                            isReading = true;

                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

                            int bRead;
                            byte[] buffer = new byte[8192];

                            while ((bRead = fileInput.read(buffer, 0, buffer.length)) != -1) {
                                String firstChr = Character.toString((char) (buffer[0] & 0xFF));
                                String secondChr = Character.toString((char) (buffer[1] & 0xFF));
                                String thirdChr = Character.toString((char) (buffer[2] & 0xFF));
                                String forthChr = Character.toString((char) (buffer[3] & 0xFF));
                                String exit = firstChr + secondChr + thirdChr + forthChr;
                                if (exit.equals("****")) {
                                    Log.d(logTag, "Received file transfer from Companion");
                                    break;
                                }
                                byteOut.write(buffer, 0, bRead);
                            }

                            byte[] fullRead = byteOut.toByteArray();

                            int derimeter = -1;
                            int count = 0;

                            // Get File name if needed
                            while (true) {
                                char chr = (char) (fullRead[count] & 0xFF);
                                if (Character.toString(chr).equals("|")) {
                                    Log.d(logTag, "Received file name: " + fileName);
                                    break;
                                }
                                count++;
                                derimeter = count;
                                fileName.append(chr);
                            }

                            // Get Meta file if needed
                            count++;
                            StringBuilder fileMeta = new StringBuilder();
                            while (true) {
                                char chr = (char) (fullRead[count] & 0xFF);
                                if (Character.toString(chr).equals("|")) {
                                    Log.d(logTag, "Received file meta: " + fileMeta);
                                    break;
                                }
                                count++;
                                derimeter = count;
                                fileMeta.append(chr);
                            }

                            if (derimeter != -1) {
                                Log.d(logTag, "Writing: " + fileName);
                                FileType type = FileType.APK;
                                if (fileName.toString().endsWith(".tflite.gz")) {
                                    type = FileType.MODEL;
                                }
                                InputStream fullInput = new ByteArrayInputStream(Arrays.copyOfRange(fullRead, count + 1, fullRead.length));
                                // Write file to disk with extension
                                boolean result = writeStreamToDisk(fullInput, fileName.toString(), type);
                                Log.d(logTag, "Writing: " + fileName + " " + result);
                                isReading = false;
                                if (result) {
                                    if (type == FileType.APK) {
                                        String role = fileName.toString().split("-")[0];
                                        String versionWithExtension = fileName.toString().split("-")[1];
                                        if (versionWithExtension.endsWith(".apk.gz")) {
                                            String version = versionWithExtension.substring(0, versionWithExtension.length() - ".apk.gz".length());
                                            app.installUtils.setInstallConfig(role, version);

                                            FileUtils.gUnZipFile(app.installUtils.apkPathDownload, app.installUtils.apkPathPostDownload);
                                            Log.d(logTag, "APK Uncompresssed. Moving APK file to external storage...");
                                            FileUtils.delete(app.installUtils.apkPathDownload);
                                            FileUtils.chmod(app.installUtils.apkPathPostDownload, "rw", "rw");

                                            JSONObject installInfo = new JSONObject();
                                            installInfo.put("role", role);
                                            installInfo.put("version", version);
                                            JSONArray installResult = RfcxComm.getQuery(
                                                    "updater",
                                                    "software_install_companion",
                                                    installInfo.toString(),
                                                    app.getContentResolver());

                                            JSONObject resultJson = new JSONObject();
                                            resultJson.put(role, false);
                                            if (installResult.length() > 0) {
                                                resultJson = installResult.getJSONObject(0);
                                            }
                                            sendDownloadResult(resultJson.toString());
                                        }
                                    } else {
                                        String modelTimestamp = fileName.toString().split("\\.")[0];
                                        String modelSrcPath = Environment.getExternalStorageDirectory().toString() + "/rfcx/classifier/" + fileName;
                                        String unzippedPath = Environment.getExternalStorageDirectory().toString() + "/rfcx/classifier/" + "unzipped-" + fileName;
                                        FileUtils.gUnZipFile(modelSrcPath, unzippedPath);

                                        // Move file to classifier library
                                        String libDstPath = app.assetLibraryUtils.getLibraryAssetFilePath("classifier", modelTimestamp, null);
                                        FileUtils.initializeDirectoryRecursively(libDstPath.substring(0, libDstPath.lastIndexOf("/")), false);
                                        FileUtils.delete(libDstPath);
                                        FileUtils.copy(unzippedPath, libDstPath);

                                        Log.d(logTag, fileMeta.toString() + "meta");
                                        if (!fileMeta.toString().equals("")) {
                                            JSONObject obj = new JSONObject(fileMeta.toString());
                                            String assetId = obj.getString("asset_id");
                                            String fileType = obj.getString("file_type");
                                            String checksum = obj.getString("checksum");
                                            String metaJsonBlob = obj.getString("meta_json_blob");

                                            app.assetLibraryDb.dbClassifier.insert(assetId, fileType, checksum, libDstPath,
                                                    FileUtils.getFileSizeInBytes(libDstPath), metaJsonBlob, 0, 0);
                                            app.audioClassifyUtils.activateClassifier(assetId);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            } catch (IOException | JSONException | NullPointerException e) {
                // Mostly on server socket get closed from its service to keep socket alive all time.
                Looper.myLooper().quit();
                isReading = false;
            }
            Looper.loop();
        });
        socketUtils.serverThread.start();
        socketUtils.isServerRunning = true;
    }

    private boolean writeStreamToDisk(InputStream body, String fullFileName, FileType type) {
        try {
            Log.d(logTag, type.name());
            File dir = new File(Environment.getExternalStorageDirectory().toString() + "/rfcx", "apk");
            if (type == FileType.MODEL) {
                dir = new File(Environment.getExternalStorageDirectory().toString() + "/rfcx", "classifier");
            }
            if (!dir.exists()) {
                dir.mkdir();
            }
            FileOutputStream output = null;
            File file = new File(dir, fullFileName);

            if (file.exists()) {
                file.delete();
            }

            try {
                output = new FileOutputStream(file);
                byte[] buffer = new byte[8192]; // or other buffer size
                int read;

                while ((read = body.read(buffer)) != -1) {
                    if (read > 0) {
                        output.write(buffer, 0, read);
                    }
                }

                output.flush();
                return true;
            } catch (IOException e) {
                RfcxLog.logExc(logTag, e);
                return false;
            } finally {
                if (body != null) {
                    body.close();
                }
                if (output != null) {
                    output.close();
                }
            }
        } catch (IOException e) {
            RfcxLog.logExc(logTag, e);
            return false;
        }
    }
}
