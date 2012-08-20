package com.dbstar.guodian.model;

import android.net.Uri;
import android.provider.BaseColumns;

public class GDDVBDataContract {
	public static final String AUTHORITY = "com.dbstar.guodian.provider";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
	
	public static final class Column implements BaseColumns {

		public static final Uri CONTENT_URI = 
				Uri.withAppendedPath(AUTHORITY_URI, "column");

		public static final String CONTENT_TYPE =
	            "vnd.android.cursor.dir/com.dbstar.guodian.provider.column";
		
		public static final String CONTENT_ITEM_TYPE =
	            "vnd.android.cursor.item/com.dbstar.guodian.provider.column";
		
		public static final String ID = "id";
		public static final String NAME = "name";
		public static final String TYPE = "type";
		public static final String PARENT_ID = "parent_id";
	}

	public static final class Content implements BaseColumns {

		public static final Uri CONTENT_URI = 
				Uri.withAppendedPath(AUTHORITY_URI, "content");

		public static final String CONTENT_TYPE =
	            "vnd.android.cursor.dir/com.dbstar.guodian.provider.content";
		
		public static final String CONTENT_ITEM_TYPE =
	            "vnd.android.cursor.item/com.dbstar.guodian.provider.content";
		
		public static final String ID = "id";
		public static final String READY = "ready";
		public static final String COLUMN_ID = "column_id";
		public static final String PATH = "path";
		public static final String SENDUSER = "senduser";
		public static final String SENDTIME = "sendtime";
		public static final String CONTENTNAME = "contentname";
		public static final String COREGTAG_ID = "coretag_id";
		public static final String CHINESENAME = "chineseName";
		public static final String ENGLISHNAME = "englishName";
		public static final String DIRECTOR = "director";
		public static final String ACTOR = "actor";
		public static final String FAVORITE = "favorite";
		public static final String BOOKMARK = "bookmark";
	}

	public static final class Brand implements BaseColumns {

		public static final Uri CONTENT_URI = 
				Uri.withAppendedPath(AUTHORITY_URI, "brand");

		public static final String CONTENT_TYPE =
	            "vnd.android.cursor.dir/com.dbstar.guodian.provider.brand";
		
		public static final String CONTENT_ITEM_TYPE =
	            "vnd.android.cursor.item/com.dbstar.guodian.provider.brand";
		
		public static final String ID = "id";
		public static final String REGIST_DIR = "regist_dir";
		public static final String DOWNLOAD = "download";
		public static final String TOTALSIZE = "totalsize";
		public static final String CNAME = "cname";
	}
}
