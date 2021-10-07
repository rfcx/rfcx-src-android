package org.rfcx.guardian.admin.comms.swm;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.misc.DbUtils;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.util.Date;
import java.util.List;

public class SwmMetaDb {

    public SwmMetaDb(Context context, String appVersion) {
        this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
        this.DROP_TABLE_ON_UPGRADE = ArrayUtils.doesStringArrayContainString(DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS, appVersion);
        this.dbSwmDiagnostic = new DbSwmDiagnostic(context);
    }

    private int VERSION = 1;
    static final String DATABASE = "swm-meta";
    static final String C_MEASURED_AT = "measured_at";
    static final String C_RSSI_BACKGROUND = "rssi_background";
    static final String C_RSSI_SAT = "rssi_sat";
    static final String C_SNR = "snr";
    static final String C_FDEV = "fdev";
    static final String C_TIME = "time";
    static final String C_SAT_ID = "sat_id";
    private static final String[] ALL_COLUMNS = new String[] { C_MEASURED_AT, C_RSSI_BACKGROUND, C_RSSI_SAT, C_SNR, C_FDEV, C_TIME, C_SAT_ID };

    static final String[] DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS = new String[] { }; // "0.6.43"
    private boolean DROP_TABLE_ON_UPGRADE = false;

    private String createColumnString(String tableName) {
        StringBuilder sbOut = new StringBuilder();
        sbOut.append("CREATE TABLE ").append(tableName)
                .append("(").append(C_MEASURED_AT).append(" INTEGER")
                .append(", ").append(C_RSSI_BACKGROUND).append(" INTEGER")
                .append(", ").append(C_RSSI_SAT).append(" INTEGER")
                .append(", ").append(C_SNR).append(" INTEGER")
                .append(", ").append(C_FDEV).append(" INTEGER")
                .append(", ").append(C_TIME).append(" TEXT")
                .append(", ").append(C_SAT_ID).append(" TEXT")
                .append(")");
        return sbOut.toString();
    }

    public class DbSwmDiagnostic {

        final DbUtils dbUtils;
        public String FILEPATH;

        private String TABLE = "diagnostic";

        public DbSwmDiagnostic(Context context) {
            this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
            FILEPATH = DbUtils.getDbFilePath(context, DATABASE, TABLE);
        }

        public int insert(long measuredAt, int rssiBackground, int rssiSat, int snr, int fdev, String time, String satId) {

            ContentValues values = new ContentValues();
            values.put(C_MEASURED_AT, measuredAt);
            values.put(C_RSSI_BACKGROUND, rssiBackground);
            values.put(C_RSSI_SAT, rssiSat);
            values.put(C_SNR, snr);
            values.put(C_FDEV, fdev);
            values.put(C_TIME, time);
            values.put(C_SAT_ID, satId);

            return this.dbUtils.insertRow(TABLE, values);
        }

        public JSONArray getLatestRowAsJsonArray() {
            return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
        }

        private List<String[]> getAllRows() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
        }

        public void clearRowsBefore(Date date) {
            this.dbUtils.deleteRowsOlderThan(TABLE, C_MEASURED_AT, date);
        }

        public String getConcatRows() {
            return DbUtils.getConcatRows(getAllRows());
        }

        public String getConcatRowsWithLabelPrepended(String labelToPrepend) {
            return DbUtils.getConcatRowsWithLabelPrepended(labelToPrepend, getAllRows());
        }

    }
    public final DbSwmDiagnostic dbSwmDiagnostic;
}
