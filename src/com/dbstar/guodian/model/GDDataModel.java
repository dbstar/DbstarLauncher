package com.dbstar.guodian.model;

import android.net.Uri;
import android.util.Log;
import android.util.Xml;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.dbstar.guodian.model.GDDVBDataContract.Brand;
import com.dbstar.guodian.model.GDDVBDataContract.Column;
import com.dbstar.guodian.model.GDDVBDataContract.Content;
import com.dbstar.guodian.model.GDDVBDataProvider.ColumnQuery;
import com.dbstar.guodian.model.GDSmartHomeContract.Global;

public class GDDataModel {
	private static final String TAG = "GDDataModel";

	private Context mContext = null;

	public GDDataModel(Context context) {
		mContext = context;
	}

	public void initialize() {

	}

	public void deInitialize() {
		
	}
	
	public ColumnData[] getColumns(String columnId) {
		Log.d(TAG, "getColumn id=" + columnId);
		Cursor cursor = null;
		ColumnData[] Columns = null;

		String selection = Column.PARENT_ID + "=?";
		String[] selectionArgs = new String[] { columnId };

		cursor = mContext.getContentResolver().query(Column.CONTENT_URI, ColumnQuery.COLUMNS,
				selection, selectionArgs, null);
		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				Log.d(TAG, "query cursor size = " + cursor.getCount());
				Columns = new ColumnData[cursor.getCount()];
				int i = 0;
				do {
					Columns[i] = new ColumnData();
					Columns[i].Id = cursor.getString(ColumnQuery.ID);
					Columns[i].Name = cursor.getString(ColumnQuery.NAME);

					Log.d(TAG, "column " + columnId + " item " + i + " name="
							+ Columns[i].Name);

					i++;
				} while (cursor.moveToNext());
			}
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return Columns;
	}
	
	private final static String[] ProjectionQueryContent = {
		Content.ID, Content.PATH
	};
	
	private final static int QUERYCONTENT_ID = 0;
	private final static int QUERYCONTENT_PATH = 1;
	
	private final static String[] ProjectionQueryBrand = {
		Brand.ID, Brand.DOWNLOAD, Brand.TOTALSIZE, Brand.CNAME
	};
	
	private final static int QUERYBRAND_ID = 0;
	private final static int QUERYBRAND_DOWNLOAD = 1;
	private final static int QUERYBRAND_TOTALSIZE = 2;
	private final static int QUERYBRAND_CNAME = 3;
	
	public ContentData[] getContents(String columnId, int pageNumber, int pageSize) {

		Cursor cursor = null;
		ContentData[] Contents = null;
		
		String selection = Content.COLUMN_ID + "=?  AND " + Content.READY + "=1" + " Limit ? Offset ?";
		String[] selectionArgs = new String[] { columnId,
				Integer.toString(pageSize),
				Integer.toString(pageNumber * pageSize) };

		cursor = mContext.getContentResolver().query(Content.CONTENT_URI, ProjectionQueryContent,
				selection, selectionArgs, null);
		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				Log.d(TAG, "query cursor size = " + cursor.getCount());
				Contents = new ContentData[cursor.getCount()];
				int i = 0;
				do {
					ContentData content = new ContentData();
					content.Id = cursor.getString(QUERYCONTENT_ID);
					content.XMLFilePath = cursor.getString(QUERYCONTENT_PATH);

					Log.d(TAG, "coloumn " + columnId + " item " + i + " name="
							+ content.XMLFilePath);

					Contents[i] = content;
					i++;
				} while (cursor.moveToNext());
			}
		}
		return Contents;
	}

	public ReceiveEntry[] getDownloadStatus(int pageNumber, int pageSize) {

		Log.d(TAG, "getDownloadStatus");
		
		Cursor cursor = null;
		ReceiveEntry[] Entries = null;
		
		String sortOrder = Brand.ID + " Limit " + Integer.toString(pageSize) +" Offset " + Integer.toString(pageNumber * pageSize);
		
		cursor = mContext.getContentResolver().query(Brand.CONTENT_URI, ProjectionQueryBrand,
				null, null, sortOrder);

		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				Log.d(TAG, "query cursor size = " + cursor.getCount());
				Entries = new ReceiveEntry[cursor.getCount()];
				int i = 0;
				do {
					ReceiveEntry entry = new ReceiveEntry();
					entry.Id = cursor.getString(QUERYBRAND_ID);
					entry.Name = cursor.getString(QUERYBRAND_CNAME);
					entry.RawProgress = cursor.getLong(QUERYBRAND_DOWNLOAD);
					entry.RawTotal = cursor.getLong(QUERYBRAND_TOTALSIZE);
					entry.ConverSize();

					Log.d(TAG, "Name " + entry.Name + " item " + i + " progress="
							+ entry.RawProgress + " total=" + entry.RawTotal);

					Entries[i] = entry;
					i++;
				} while (cursor.moveToNext());
			}
		}
		return Entries;
	}

	private final static String[] ProjectionQueryGlobal = {
		Global.ID, Global.NAME, Global.VALUE
	};
	
	private final static int QUERYGLOBAL_ID = 0;
	private final static int QUERYGLOBAL_NAME = 1;
	private final static int QUERYGLOBAL_VALUE = 2;
	
	public boolean setGuodianServerIP (String ip) {
		return setSettingValue(GDSettings.SettingServerIP, ip);
	}
	
	public boolean setGuodianServerPort(String port) {
		return setSettingValue(GDSettings.SettingServerPort, port);
	}
	
	public boolean setGuodianSerialNumber(String serialNumber) {
		return setSettingValue(GDSettings.SettingSerialNumber, serialNumber);
	}
	
	public boolean setGuodianUserName(String userName) {
		return setSettingValue(GDSettings.SettingUserName, userName);
	}
	
	public boolean setGuodianPassword(String passwd) {
		return setSettingValue(GDSettings.SettingPasswrod, passwd);
	}
	
	public boolean setGuodianVersion(String version) {
		return setSettingValue(GDSettings.SettingVersion, version);
	}
	
	public boolean setSettingValue(String key, String value) {
		
		boolean ret = true;
		int Id = -1;
		String oldValue = "";
		Cursor cursor = null;
		
		String selection = Global.NAME + "=?";
		String[] selectionArgs = new String[] { key };
		
		cursor = mContext.getContentResolver().query(Global.CONTENT_URI, ProjectionQueryGlobal,
				selection, selectionArgs, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				Log.d(TAG, "query cursor size = " + cursor.getCount());

				do {
					Id = cursor.getInt(QUERYGLOBAL_ID);
					oldValue = cursor.getString(QUERYGLOBAL_VALUE);
				} while (cursor.moveToNext());
			}
		}
		
		if (Id < 0) {
			// insert
			ContentValues values = new ContentValues();
			//values.put(Global.ID, -1);
			values.put(Global.NAME, key);
			values.put(Global.VALUE, value);
			Uri retUri = mContext.getContentResolver().insert(Global.CONTENT_URI, values);
			/*long rowId = Long.valueOf(retUri.getLastPathSegment());
			if (rowId > 0)*/
			if (retUri != null)
				ret = true;
		} else {
			if (!oldValue.equals(value)) {
				// update
				selection = Global.ID + "=?";
				selectionArgs = new String[] { String.valueOf(Id) };
				
				ContentValues values = new ContentValues();
				values.put(Global.ID, Id);
				values.put(Global.NAME, key);
				values.put(Global.VALUE, value);
				int count = mContext.getContentResolver().update(Global.CONTENT_URI, values, selection, selectionArgs);
				if (count == 1)
					ret = true;
			}			
		}

		return ret;
	}
	
	public String getSettingValue(String key) {
		Cursor cursor = null;
		String value = "";
		String selection = Global.NAME + "=?";
		String[] selectionArgs = new String[] { key };
		
		cursor = mContext.getContentResolver().query(Global.CONTENT_URI, ProjectionQueryGlobal,
				selection, selectionArgs, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToFirst()) {
				Log.d(TAG, "query cursor size = " + cursor.getCount());

				do {
					value = cursor.getString(QUERYGLOBAL_VALUE);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
		}
		
		return value;
	}
	

	public Bitmap getImage (String file) {
		Log.d(TAG, "image =" + file);

		Bitmap image = BitmapFactory.decodeFile(file);

		Log.d(TAG, "image =" + image);

		return image;
	}

	public String getTextContent(String file) {
		String text = "";
		File descriptionFile = new File(file);
		Log.d(TAG, "get Description path=" + descriptionFile.getAbsolutePath());

		if (descriptionFile.exists() && descriptionFile.length() > 0) {
			BufferedReader br = null;

			try {
				br = new BufferedReader(new FileReader(descriptionFile));

				text = "";
				String line;
				while ((line = br.readLine()) != null) {
					text += line;
				}

				Log.d(TAG, "text = " +text);
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return text;
	}
	
	public void getDetailsData(String xmlFile, ContentData content) {

		File file = new File(xmlFile);

		if (file.exists() && file.length() > 0) {
			InputStream in = null;

			try {
				in = new BufferedInputStream(new FileInputStream(file));
				
				parseDetailData(in, content);
				
				if (in != null) {
					in.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	private static final String ns = null;
	private static final String TAGContentTags = "contenttags";
	private static final String TAGContent = "content";
	
	private static final String TAGMainfile = "mainfile";
	private static final String TAGChineseName = "chinesename";
	private static final String TAGPosterFiles = "posterfiles";
	private static final String TAGPosterFile = "posterfile";
	private static final String TAGTrailerFiles = "trailerfiles";
	private static final String TAGTrailerFile = "trailerfile";
	private static final String TAGCaptionFiles = "captionfiles";
	private static final String TAGCaptionFile = "captionfile";
	private static final String TAGResolution = "resolution";
	private static final String TAGActor = "actor";
	private static final String TAGType = "type";
	private static final String TAGDirector = "director";
	private static final String TAGCatagory = "catagory";
	private static final String TAGEnglishName = "englishname";
	private static final String TAGSubTitle = "subTitle";
	private static final String TAGArea = "area";
	private static final String TAGSource = "source";
	private static final String TAGDescription = "description";

	private void parseDetailData (InputStream in, ContentData content) {
		try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            readData(parser, content);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	private void readData(XmlPullParser parser, ContentData content) throws XmlPullParserException, IOException {
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, ns, TAGContentTags);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, ns, TAGContent);

	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();

	        if (name.equals(TAGMainfile)) {
	        	content.Mainfile = readTag(parser, TAGMainfile);
	        } else if (name.equals(TAGChineseName)) {
	        	content.ChineseName = readTag(parser, TAGChineseName);
	        } else if (name.equals(TAGResolution)) {
	        	content.Resolution = readTag(parser, TAGResolution);
	        } else if (name.equals(TAGActor)) {
	        	content.Actors = readTag(parser, TAGActor);
	        } else if (name.equals(TAGType)) {
	        	content.Type = readTag(parser, TAGType);
	        } else if (name.equals(TAGDirector)) {
	        	content.Director = readTag(parser, TAGDirector);
	        } else if (name.equals(TAGCatagory)) {
	        	content.Category = readTag(parser, TAGCatagory);
	        } else if (name.equals(TAGEnglishName)) {
	        	content.EnglishName = readTag(parser, TAGEnglishName);
	        } else if (name.equals(TAGSubTitle)) {
	        	content.SubTitle = readTag(parser, TAGSubTitle);
	        } else if (name.equals(TAGArea)) {
	        	content.Area = readTag(parser, TAGArea);
	        } else if (name.equals(TAGDescription)) {
	        	content.Description = readTag(parser, TAGDescription);
	        } else if (name.equals(TAGSource)) {
	        	content.Source = readTag(parser, TAGSource);
	        } else if (name.equals(TAGPosterFiles)) {
	        	content.PosterFiles = readList(parser, TAGPosterFiles, TAGPosterFile);
	        } else if (name.equals(TAGTrailerFiles)) {
	        	content.TrailerFiles = readList(parser, TAGTrailerFiles, TAGTrailerFile);
	        } else if (name.equals(TAGCaptionFiles)) { 
	        	content.CaptionFiles = readList(parser, TAGCaptionFiles, TAGCaptionFile);
	    	} else {
	        	skip(parser);
	        }
	    }

	}
	
	private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
	    parser.require(XmlPullParser.START_TAG, ns, tag);
	    String value = readText(parser);
	    Log.d(TAG, tag + "value = " + value);
	    parser.require(XmlPullParser.END_TAG, ns, tag);
	    return value;
	}

	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}
	
	private List<String> readList(XmlPullParser parser, String rootTag, String tag) throws XmlPullParserException, IOException {
		List<String> items = new LinkedList<String>();
		parser.require(XmlPullParser.START_TAG, ns, rootTag);

		while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        
	        String name = parser.getName();
	        if (name.equals(tag)) {
	        	String value = readTag(parser, tag);
	        	items.add(value);
	        } else {
	        	skip(parser);
	        }
		}
		
		return items;
	}
	
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }

}
