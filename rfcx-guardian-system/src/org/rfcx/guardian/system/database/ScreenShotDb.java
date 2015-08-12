package org.rfcx.guardian.system.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.rfcx.guardian.utility.DateTimeUtils;
import org.rfcx.guardian.utility.RfcxConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class ScreenShotDb {
	
	public ScreenShotDb(Context context, int appVersion) {
		this.VERSION = appVersion;
		this.dbCaptured = new DbCaptured(context);
	}

	private static final String TAG = "Rfcx-"+RfcxConstants.ROLE_NAME+"-"+ScreenShotDb.class.getSimpleName();
	public DateTimeUtils dateTimeUtils = new DateTimeUtils();
	private int VERSION = 1;
	static final String DATABASE = "screenshots";
	static final String C_CREATED_AT = "created_at";
	static final String C_TIMESTAMP = "timestamp";
	static final String C_FORMAT = "format";
	static final String C_DIGEST = "digest";
	private static final String[] ALL_COLUMNS = new String[] { C_CREATED_AT, C_TIMESTAMP, C_FORMAT, C_DIGEST };
	
	private String createColumnString(String tableName) {
		StringBuilder sbOut = new StringBuilder();
		sbOut.append("CREATE TABLE ").append(tableName).append("(").append(C_CREATED_AT).append(" DATETIME");
		sbOut.append(", "+C_TIMESTAMP+" TEXT");
		sbOut.append(", "+C_FORMAT+" TEXT");
		sbOut.append(", "+C_DIGEST+" TEXT");
		return sbOut.append(")").toString();
	}
	
	public class DbCaptured {
		private String TABLE = "captured";
		class DbHelper extends SQLiteOpenHelper {
			public DbHelper(Context context) {
				super(context, DATABASE+"-"+TABLE+".db", null, VERSION);
			}
			@Override
			public void onCreate(SQLiteDatabase db) {
				try {
					db.execSQL(createColumnString(TABLE));
				} catch (SQLException e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC); }
			}
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				try { db.execSQL("DROP TABLE IF EXISTS " + TABLE); onCreate(db);
				} catch (SQLException e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC); }
			}
		}
		final DbHelper dbHelper;
		public DbCaptured(Context context) {
			this.dbHelper = new DbHelper(context);
		}
		public void close() {
			this.dbHelper.close();
		}
		public void insert(String value, String format, String digest) {
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, (new DateTimeUtils()).getDateTime());
			values.put(C_TIMESTAMP, value);
			values.put(C_FORMAT, format);
			values.put(C_DIGEST, digest);
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			} finally {
				db.close();
			}
		}
		public List<String[]> getAllCaptured() {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			ArrayList<String[]> list = new ArrayList<String[]>();
			try { Cursor cursor = db.query(TABLE, ALL_COLUMNS, null, null, null, null, null, null);
				if (cursor.getCount() > 0) {
					try { if (cursor.moveToFirst()) { do { list.add(new String[] { cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3) });
					} while (cursor.moveToNext()); } } finally { cursor.close(); } }
			} catch (Exception e) { Log.e(TAG,(e!=null) ? (e.getMessage() +" ||| "+ TextUtils.join(" | ", e.getStackTrace())) : RfcxConstants.NULL_EXC); } finally { db.close(); }
			return list;
		}
		public void clearCapturedBefore(Date date) {
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			try { db.execSQL("DELETE FROM "+TABLE+" WHERE "+C_CREATED_AT+"<='"+(new DateTimeUtils()).getDateTime(date)+"'");
			} finally { db.close(); }
		}
		
		public String getSerializedCaptured() {
			List<String[]> capturedList = getAllCaptured();
			String[] capturedArray = new String[capturedList.size()];
			for (int i = 0; i < capturedList.size(); i++) {
				capturedArray[i] = TextUtils.join("|", capturedList.get(i));
			}
			return (capturedList.size() > 0) ? TextUtils.join("$", capturedArray) : "";
		}
	}
	public final DbCaptured dbCaptured;
	
}