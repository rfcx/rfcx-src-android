package org.rfcx.guardian.admin.comms.sbd;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.misc.DbUtils;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.util.Date;
import java.util.List;

public class SbdMessageDb {

    static final String DATABASE = "sbd";
    static final String C_CREATED_AT = "created_at";
    static final String C_TIMESTAMP = "timestamp";
    static final String C_ADDRESS = "address";
    static final String C_BODY = "body";
    static final String C_MESSAGE_ID = "message_id";
    static final String C_LAST_ACCESSED_AT = "last_accessed_at";
    static final String[] DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS = new String[]{}; // "0.6.43"
    private static final String[] ALL_COLUMNS = new String[]{C_CREATED_AT, C_TIMESTAMP, C_ADDRESS, C_BODY, C_MESSAGE_ID, C_LAST_ACCESSED_AT};
    public final DbSbdReceived dbSbdReceived;
    public final DbSbdSent dbSbdSent;
    public final DbSbdQueued dbSbdQueued;
    private int VERSION = 1;
    private boolean DROP_TABLE_ON_UPGRADE = false;

    public SbdMessageDb(Context context, String appVersion) {
        this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
        this.DROP_TABLE_ON_UPGRADE = ArrayUtils.doesStringArrayContainString(DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS, appVersion);
        this.dbSbdReceived = new DbSbdReceived(context);
        this.dbSbdSent = new DbSbdSent(context);
        this.dbSbdQueued = new DbSbdQueued(context);
    }

    private String createColumnString(String tableName) {
        StringBuilder sbOut = new StringBuilder();
        sbOut.append("CREATE TABLE ").append(tableName)
                .append("(").append(C_CREATED_AT).append(" INTEGER")
                .append(", ").append(C_TIMESTAMP).append(" TEXT")
                .append(", ").append(C_ADDRESS).append(" TEXT")
                .append(", ").append(C_BODY).append(" TEXT")
                .append(", ").append(C_MESSAGE_ID).append(" TEXT")
                .append(", ").append(C_LAST_ACCESSED_AT).append(" INTEGER")
                .append(")");
        return sbOut.toString();
    }

    public class DbSbdReceived {

        final DbUtils dbUtils;
        public String FILEPATH;

        private final String TABLE = "received";

        public DbSbdReceived(Context context) {
            this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
            FILEPATH = DbUtils.getDbFilePath(context, DATABASE, TABLE);
        }

        public int insert(String timestamp, String address, String body, String message_id) {

            ContentValues values = new ContentValues();
            values.put(C_CREATED_AT, (new Date()).getTime());
            values.put(C_TIMESTAMP, timestamp);
            values.put(C_ADDRESS, address);
            values.put(C_BODY, body);
            values.put(C_MESSAGE_ID, message_id);
            values.put(C_LAST_ACCESSED_AT, 0);

            return this.dbUtils.insertRow(TABLE, values);
        }

        public List<String[]> getAllRows() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
        }

        public int getCount() {
            return this.dbUtils.getCount(TABLE, null, null);
        }

        public List<String[]> getRowsInOrderOfTimestamp() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, C_TIMESTAMP + " ASC");
        }

    }

    public class DbSbdSent {

        final DbUtils dbUtils;

        private final String TABLE = "sent";

        public DbSbdSent(Context context) {
            this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
        }

        public int insert(long timestamp, String address, String body, String message_id) {

            ContentValues values = new ContentValues();
            values.put(C_CREATED_AT, (new Date()).getTime());
            values.put(C_TIMESTAMP, timestamp + "");
            values.put(C_ADDRESS, address);
            values.put(C_BODY, body);
            values.put(C_MESSAGE_ID, message_id);
            values.put(C_LAST_ACCESSED_AT, 0);

            return this.dbUtils.insertRow(TABLE, values);
        }

        public JSONArray getSingleRowAsJsonArray() {
            return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0, 1);
        }

        public List<String[]> getAllRows() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
        }

        public int getCount() {
            return this.dbUtils.getCount(TABLE, null, null);
        }

        public List<String[]> getRowsInOrderOfTimestamp() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, C_TIMESTAMP + " ASC");
        }

    }

    public class DbSbdQueued {

        final DbUtils dbUtils;

        private final String TABLE = "queued";

        public DbSbdQueued(Context context) {
            this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
        }

        public int insert(long timestamp, String address, String body, String message_id) {

            ContentValues values = new ContentValues();
            values.put(C_CREATED_AT, (new Date()).getTime());
            values.put(C_TIMESTAMP, timestamp + "");
            values.put(C_ADDRESS, address);
            values.put(C_BODY, body);
            values.put(C_MESSAGE_ID, message_id);
            values.put(C_LAST_ACCESSED_AT, 0);

            return this.dbUtils.insertRow(TABLE, values);
        }

        public List<String[]> getAllRows() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
        }

        public int getCount() {
            return this.dbUtils.getCount(TABLE, null, null);
        }

        public List<String[]> getRowsInOrderOfTimestamp() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, C_TIMESTAMP + " ASC");
        }

        public int deleteSingleRowByMessageId(String message_id) {
            this.dbUtils.deleteRowsWithinQueryByTimestamp(TABLE, C_MESSAGE_ID, message_id);
            return 0;
        }

    }

}
