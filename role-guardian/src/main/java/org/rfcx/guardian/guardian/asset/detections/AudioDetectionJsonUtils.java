package org.rfcx.guardian.guardian.asset.detections;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rfcx.guardian.guardian.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;

import java.util.ArrayList;
import java.util.List;

public class AudioDetectionJsonUtils {

    private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, "AudioDetectionJsonUtils");
    private final RfcxGuardian app;

    public AudioDetectionJsonUtils(Context context) {

        this.app = (RfcxGuardian) context.getApplicationContext();

    }

    public JSONObject retrieveAndBundleDetectionJson(JSONObject insertDetectionsInto, int maxDtcnRowsToBundle, boolean overrideFilterByLastAccessedAt, boolean forSatellite) throws JSONException {

        if (insertDetectionsInto == null) {
            insertDetectionsInto = new JSONObject();
        }

        JSONArray dtcnIds = new JSONArray();
        ArrayList<String> dtcnList = new ArrayList<>();
        List<String[]> dtcnRows;
        if (forSatellite) {
            dtcnRows = app.audioDetectionDb.dbFiltered.getLatestRowsNotAccessedWithLimit(maxDtcnRowsToBundle);
        } else {
            dtcnRows = (overrideFilterByLastAccessedAt) ? app.audioDetectionDb.dbFiltered.getLatestRowsWithLimit(maxDtcnRowsToBundle) :
                    app.audioDetectionDb.dbFiltered.getLatestRowsNotAccessedSinceWithLimit((System.currentTimeMillis() - app.apiMqttUtils.getSetCheckInPublishTimeOutLength()), maxDtcnRowsToBundle);
        }

        for (String[] dtcnRow : dtcnRows) {

            // add detection set ID to array of IDs
            dtcnIds.put(dtcnRow[0]);
            dtcnList.add(TextUtils.join("*", new String[]{dtcnRow[1], dtcnRow[3] + "-v" + dtcnRow[4], dtcnRow[7], "" + Math.round(Double.parseDouble(dtcnRow[8])), dtcnRow[10]}));

            // mark this row as accessed in the database
            if (forSatellite) {
                app.audioDetectionDb.dbFiltered.deleteSingleRow(dtcnRow[1], dtcnRow[6]);
            } else {
                app.audioDetectionDb.dbFiltered.updateLastAccessedAtByCreatedAt(dtcnRow[0]);
            }

            // if the bundle already contains max number of snapshots, stop here
            if (dtcnIds.length() >= maxDtcnRowsToBundle) {
                break;
            }
        }

        if (dtcnList.size() > 0) {
            insertDetectionsInto.put("detections", TextUtils.join("|", dtcnList));
            insertDetectionsInto.put("detection_ids", dtcnIds);
        }

        return insertDetectionsInto;
    }


}
