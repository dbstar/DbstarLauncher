package com.dbstar.guodian.model;

import com.dbstar.guodian.model.GDDVBDataContract.Brand;
import com.dbstar.guodian.model.GDDVBDataContract.Column;
import com.dbstar.guodian.model.GDDVBDataContract.Content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class GDDVBDataProvider extends ContentProvider {

	private static final String TAG = "DVBDataProvider";

	private static final int COLUMN = 1001;
	private static final int CONTENT = 1002;
	private static final int BRAND = 1003;

	private SQLiteDatabase mDataBase = null;
	// private final ThreadLocal<SQLiteDatabase> mActiveDb = new
	// ThreadLocal<SQLiteDatabase>();
	GDDataAccessor mDataAccessor = new GDDataAccessor();

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(GDDVBDataContract.AUTHORITY, "column", COLUMN);
		sURIMatcher.addURI(GDDVBDataContract.AUTHORITY, "content", CONTENT);
		sURIMatcher.addURI(GDDVBDataContract.AUTHORITY, "brand", BRAND);
	}

	interface Tables {
		String COLOUMN = "column";
		String CONTENT = "content";
		String BRAND = "brand";
	}

	public interface ColumnQuery {
		String TABLE = Tables.COLOUMN;

		String[] COLUMNS = new String[] { Column.ID, Column.NAME, Column.TYPE,
				Column.PARENT_ID };

		int ID = 0;
		int NAME = 1;
		int TYPE = 2;
		int PARENT_ID = 3;
	}

	public interface ContentQuery {
		String TABLE = Tables.CONTENT;

		String[] COLUMNS = new String[] { Content.ID, Content.READY,
				Content.SENDUSER, Content.SENDTIME, Content.CONTENTNAME,
				Content.COLUMN_ID, Content.PATH, Content.COREGTAG_ID,
				Content.CHINESENAME, Content.ENGLISHNAME, Content.DIRECTOR,
				Content.ACTOR, Content.FAVORITE, Content.BOOKMARK };

		int ID = 0;
		int READY = 1;
		int SENDUSER = 2;
		int SENDTIME = 3;
		int CONTENTNAME = 4;
		int PATH = 5;
		int COLUMN_ID = 6;
		int COREGTAG_ID = 7;
		int CHINESENAME = 8;
		int ENGLISHNAME = 9;
		int DIRECTOR = 10;
		int ACTOR = 11;
		int FAVORITE = 12;
		int BOOKMARK = 13;
	}

	public interface BrandQuery {
		String TABLE = Tables.BRAND;

		String[] COLUMNS = new String[] { Brand.ID, Brand.REGIST_DIR,
				Brand.DOWNLOAD, Brand.TOTALSIZE, Brand.CNAME };

		int ID = 0;
		int REGIST_DIR = 1;
		int DOWNLOAD = 2;
		int TOTALSIZE = 3;
		int CNAME = 4;
	}

	@Override
	public boolean onCreate() {

		Log.d(TAG, "onCreate");

		boolean ret = true;
		mDataAccessor.configure();

		String dbFile = mDataAccessor.getDatabaseFile();
		if (dbFile == null || dbFile.isEmpty()) {
			return true;
		}

		Log.d(TAG, "mDBFile = " + dbFile);
		try {
			mDataBase = SQLiteDatabase.openDatabase(dbFile, null,
					SQLiteDatabase.OPEN_READONLY
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
			// mActiveDb.set(mDataBase);
		} catch (Exception e) {
			ret = false;
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public String getType(Uri uri) {
		int match = sURIMatcher.match(uri);
		String typeStr;
		switch (match) {
		case COLUMN:
			typeStr = GDDVBDataContract.Column.CONTENT_TYPE;
			break;
		case CONTENT:
			typeStr = GDDVBDataContract.Content.CONTENT_TYPE;
			break;
		case BRAND:
			typeStr = GDDVBDataContract.Brand.CONTENT_TYPE;
			break;
		default:
			typeStr = null;
			break;
		}

		return typeStr;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		if (mDataBase == null) {
			mDataAccessor.configure();

			String dbFile = mDataAccessor.getDatabaseFile();
			Log.d(TAG, "mDBFile = " + dbFile);
			try {
				mDataBase = SQLiteDatabase.openDatabase(dbFile, null,
						SQLiteDatabase.OPEN_READONLY
								| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
				// mActiveDb.set(mDataBase);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Cursor curosr = null;
		String table = null;

		int match = sURIMatcher.match(uri);
		switch (match) {
		case COLUMN:
			table = Tables.COLOUMN;
			break;
		case CONTENT:
			table = Tables.CONTENT;
			break;
		case BRAND:
			table = Tables.BRAND;
			break;
		default:
			break;
		}

		SQLiteDatabase db = mDataBase;// mActiveDb.get();

		if (table != null && db != null && db.isOpen()) {
			Log.d(TAG, " query");

			curosr = db.query(table, projection, selection, selectionArgs,
					null, null, sortOrder);
		}

		return curosr;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
