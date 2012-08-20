package com.dbstar.guodian.model;

import android.graphics.Bitmap;

public class TV {
	public ContentData Content;

	public String Type;
	public String Region;
	public String Rate;
	public String Description;
	public Bitmap Thumbnail;
	public int Episodes;
	
	public TV() {
		Content = null;
		Description = null;
		Thumbnail = null;
	}
}
