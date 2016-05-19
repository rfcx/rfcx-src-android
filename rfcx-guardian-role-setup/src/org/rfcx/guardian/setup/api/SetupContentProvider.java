package org.rfcx.guardian.setup.api;

import java.util.Calendar;

import org.rfcx.guardian.setup.RfcxGuardian;
import org.rfcx.guardian.utility.rfcx.RfcxLog;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class SetupContentProvider extends ContentProvider {
	
	private static final String TAG = "Rfcx-"+RfcxGuardian.APP_ROLE+"-"+SetupContentProvider.class.getSimpleName();
	
	private static final String AUTHORITY = RfcxRole.ContentProvider.setup.AUTHORITY;
	private static final String ENDPOINT_1 = RfcxRole.ContentProvider.setup.ENDPOINT_1;
	private static final String[] PROJECTION_1 = RfcxRole.ContentProvider.setup.PROJECTION_1;
	
	private static final int ENDPOINT_1_LIST = 1;
	private static final int ENDPOINT_1_ID = 2;

	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_1, ENDPOINT_1_LIST);
		URI_MATCHER.addURI(AUTHORITY, ENDPOINT_1+"/#", ENDPOINT_1_ID);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			MatrixCursor cursor = new MatrixCursor(PROJECTION_1);
			
			cursor.addRow(new Object[] { 
					Calendar.getInstance().getTimeInMillis()
				});
			
			return cursor;
		} catch (Exception e) {
			RfcxLog.logExc(TAG, e);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public boolean onCreate() {
		return true;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}
	
}