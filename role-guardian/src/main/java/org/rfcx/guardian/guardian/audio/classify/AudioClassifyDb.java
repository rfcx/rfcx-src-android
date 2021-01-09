package org.rfcx.guardian.guardian.audio.classify;

import android.content.ContentValues;
import android.content.Context;

import org.rfcx.guardian.utility.database.DbUtils;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.util.Date;
import java.util.List;

public class AudioClassifyDb {

	public AudioClassifyDb(Context context, String appVersion) {
		this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
		this.DROP_TABLE_ON_UPGRADE = true; //ArrayUtils.doesStringArrayContainString(DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS, appVersion);
		this.dbQueued = new DbQueued(context);
	}

	private int VERSION = 1;
	static final String DATABASE = "audio-classify";
	
	static final String C_CREATED_AT = "created_at";
	static final String C_AUDIO_ID = "audio_id";
	static final String C_CLASSIFIER_ID = "classifier_id";
	static final String C_ORIGINAL_SAMPLE_RATE = "original_sample_rate";
	static final String C_CLASSIFIER_SAMPLE_RATE = "input_sample_rate";
	static final String C_AUDIO_FILEPATH = "audio_filepath";
	static final String C_CLASSIFIER_FILEPATH = "classifier_filepath";
	static final String C_CLASSIFIER_WINDOW_SIZE = "classifier_window_size";
	static final String C_CLASSIFIER_STEP_SIZE = "classifier_step_size";
	static final String C_CLASSIFIER_CLASSES = "classifier_classes";
	static final String C_ATTEMPTS = "attempts";
	
	private static final String[] ALL_COLUMNS = new String[] {  C_CREATED_AT, C_AUDIO_ID, C_CLASSIFIER_ID, C_ORIGINAL_SAMPLE_RATE, C_CLASSIFIER_SAMPLE_RATE, C_AUDIO_FILEPATH, C_CLASSIFIER_FILEPATH, C_CLASSIFIER_WINDOW_SIZE, C_CLASSIFIER_STEP_SIZE, C_CLASSIFIER_CLASSES, C_ATTEMPTS };

	static final String[] DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS = new String[] { }; // "0.6.43"
	private boolean DROP_TABLE_ON_UPGRADE = false;
	
	private static String createColumnString(String tableName) {
		StringBuilder sbOut = new StringBuilder();
		sbOut.append("CREATE TABLE ").append(tableName)
			.append("(").append(C_CREATED_AT).append(" INTEGER")
			.append(", ").append(C_AUDIO_ID).append(" TEXT")
			.append(", ").append(C_CLASSIFIER_ID).append(" TEXT")
			.append(", ").append(C_ORIGINAL_SAMPLE_RATE).append(" INTEGER")
			.append(", ").append(C_CLASSIFIER_SAMPLE_RATE).append(" INTEGER")
			.append(", ").append(C_AUDIO_FILEPATH).append(" TEXT")
			.append(", ").append(C_CLASSIFIER_FILEPATH).append(" TEXT")
			.append(", ").append(C_CLASSIFIER_WINDOW_SIZE).append(" TEXT")
			.append(", ").append(C_CLASSIFIER_STEP_SIZE).append(" TEXT")
			.append(", ").append(C_CLASSIFIER_CLASSES).append(" TEXT")
			.append(", ").append(C_ATTEMPTS).append(" INTEGER")
			.append(")");
		return sbOut.toString();
	}
	

	public class DbQueued {

		final DbUtils dbUtils;

		private String TABLE = "queued";
		
		public DbQueued(Context context) {
			this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
		}
		
		public int insert(String audioId, String classifierId, int originalSampleRate, int classifierSampleRate, String audioFilepath, String classifierFilepath, String windowSize, String stepSize, String classes) {
			
			ContentValues values = new ContentValues();
			values.put(C_CREATED_AT, (new Date()).getTime());
			values.put(C_AUDIO_ID, audioId);
			values.put(C_CLASSIFIER_ID, classifierId);
			values.put(C_ORIGINAL_SAMPLE_RATE, originalSampleRate);
			values.put(C_CLASSIFIER_SAMPLE_RATE, classifierSampleRate);
			values.put(C_AUDIO_FILEPATH, audioFilepath);
			values.put(C_CLASSIFIER_FILEPATH, classifierFilepath);
			values.put(C_CLASSIFIER_WINDOW_SIZE, windowSize);
			values.put(C_CLASSIFIER_STEP_SIZE,stepSize );
			values.put(C_CLASSIFIER_CLASSES, classes);
			values.put(C_ATTEMPTS, 0);
			
			return this.dbUtils.insertRow(TABLE, values);
		}
		
		public List<String[]> getAllRows() {
			return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
		}
		
//		public String[] getLatestRow() {
//			return this.dbUtils.getSingleRow(TABLE, ALL_COLUMNS, null, null, C_CREATED_AT, 0);
//		}
//
//		public void clearRowsBefore(Date date) {
//			this.dbUtils.deleteRowsOlderThan(TABLE, C_CREATED_AT, date);
//		}
//

		public void deleteSingleRow(String audioId, String classifierId) {
			this.dbUtils.deleteRowsWithinQueryByTwoColumns(TABLE, C_AUDIO_ID, audioId, C_CLASSIFIER_ID, classifierId);
		}
		
		public int getCount() {
			return this.dbUtils.getCount(TABLE, null, null);
		}
		
//		public String[] getSingleRowByAudioId(String audioId) {
//			String timestamp = audioId.contains(".") ? audioId.substring(0, audioId.lastIndexOf(".")) : audioId;
//			return this.dbUtils.getSingleRow(TABLE, ALL_COLUMNS, "substr("+ C_CLASSIFIER_ID +",1,"+timestamp.length()+") = ?", new String[] { timestamp }, null, 0);
//		}
//
//		public void incrementSingleRowAttempts(String audioId) {
//			String timestamp = audioId.contains(".") ? audioId.substring(0, audioId.lastIndexOf(".")) : audioId;
//			this.dbUtils.adjustNumericColumnValuesWithinQueryByTimestamp("+1", TABLE, C_ATTEMPTS, C_CLASSIFIER_ID, timestamp);
//		}
//
//		public void decrementSingleRowAttempts(String audioId) {
//			String timestamp = audioId.contains(".") ? audioId.substring(0, audioId.lastIndexOf(".")) : audioId;
//			this.dbUtils.adjustNumericColumnValuesWithinQueryByTimestamp("-1", TABLE, C_ATTEMPTS, C_CLASSIFIER_ID, timestamp);
//		}
		
	}
	public final DbQueued dbQueued;

	
}