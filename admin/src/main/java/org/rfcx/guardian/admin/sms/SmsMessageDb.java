package org.rfcx.guardian.admin.sms;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.rfcx.guardian.admin.RfcxGuardian;
import org.rfcx.guardian.utility.database.DbUtils;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.util.Date;
import java.util.List;

public class SmsMessageDb {

	public SmsMessageDb(Context context, String appVersion) {
		this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
		this.dbSmsReceived = new DbSmsReceived(context);
		this.dbSmsSent = new DbSmsSent(context);
		this.dbSmsQueued = new DbSmsQueued(context);
	}

	private static final String logTag = RfcxLog.generateLogTag(RfcxGuardian.APP_ROLE, SmsMessageDb.class);
	private int VERSION = 1;
	static final String DATABASE = "sms";
	static final String C_CREATED_AT = "created_at";
	static final String C_TIMESTAMP = "timestamp";
	static final String C_ADDRESS = "address";
	static final String C_BODY = "body";
	static final String C_MESSAGE_ID = "message_id";
	static final String C_LAST_ACCESSED_AT = "last_accessed_at";
	private static final String[] ALL_COLUMNS = new String[] { C_CREATED_AT, C_TIMESTAMP, C_ADDRESS, C_BODY, C_MESSAGE_ID, C_LAST_ACCESSED_AT };

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

	public class DbSmsReceived {

		final DbUtils dbUtils;

		private String TABLE = "received";

		public DbSmsReceived(Context context) {
			this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE));
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

		public JSONArray getLatestRowAsJsonArray() {
			return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
		}

		public int deleteSingleRowByMessageId(String message_id) {
			this.dbUtils.deleteRowsWithinQueryByTimestamp(TABLE, C_MESSAGE_ID, message_id);
			return 0;
		}

		public long updateLastAccessedAtByMessageId(String message_id) {
			long rightNow = (new Date()).getTime();
			this.dbUtils.setDatetimeColumnValuesWithinQueryByTimestamp(TABLE, C_LAST_ACCESSED_AT, rightNow, C_MESSAGE_ID, message_id);
			return rightNow;
		}

	}
	public final DbSmsReceived dbSmsReceived;

	public class DbSmsSent {

		final DbUtils dbUtils;

		private String TABLE = "sent";

		public DbSmsSent(Context context) {
			this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE));
		}

		public int insert(long timestamp, String address, String body, String message_id) {

			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, (new Date()).getTime());
			values.put(C_TIMESTAMP, timestamp+"");
			values.put(C_ADDRESS, address);
			values.put(C_BODY, body);
			values.put(C_MESSAGE_ID, message_id);
			values.put(C_LAST_ACCESSED_AT, 0);

			return this.dbUtils.insertRow(TABLE, values);
		}

		public List<String[]> getAllRows() {
			return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
		}

		public JSONArray getLatestRowAsJsonArray() {
			return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
		}

		public int deleteSingleRowByMessageId(String message_id) {
			this.dbUtils.deleteRowsWithinQueryByTimestamp(TABLE, C_MESSAGE_ID, message_id);
			return 0;
		}

		public long updateLastAccessedAtByMessageId(String message_id) {
			long rightNow = (new Date()).getTime();
			this.dbUtils.setDatetimeColumnValuesWithinQueryByTimestamp(TABLE, C_LAST_ACCESSED_AT, rightNow, C_MESSAGE_ID, message_id);
			return rightNow;
		}

	}
	public final DbSmsSent dbSmsSent;

	public class DbSmsQueued {

		final DbUtils dbUtils;

		private String TABLE = "queued";

		public DbSmsQueued(Context context) {
			this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE));
		}

		public int insert(long timestamp, String address, String body, String message_id) {

			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, (new Date()).getTime());
			values.put(C_TIMESTAMP, timestamp+"");
			values.put(C_ADDRESS, address);
			values.put(C_BODY, body);
			values.put(C_MESSAGE_ID, message_id);
			values.put(C_LAST_ACCESSED_AT, 0);

			return this.dbUtils.insertRow(TABLE, values);
		}

		public List<String[]> getAllRows() {
			return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
		}

		public List<String[]> getRowsInOrderOfTimestamp() {
			return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, C_TIMESTAMP+" ASC");
		}

		public JSONArray getLatestRowAsJsonArray() {
			return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
		}

		public int deleteSingleRowByMessageId(String message_id) {
			this.dbUtils.deleteRowsWithinQueryByTimestamp(TABLE, C_MESSAGE_ID, message_id);
			return 0;
		}

		public long updateLastAccessedAtByMessageId(String message_id) {
			long rightNow = (new Date()).getTime();
			this.dbUtils.setDatetimeColumnValuesWithinQueryByTimestamp(TABLE, C_LAST_ACCESSED_AT, rightNow, C_MESSAGE_ID, message_id);
			return rightNow;
		}

	}
	public final DbSmsQueued dbSmsQueued;
	
}
