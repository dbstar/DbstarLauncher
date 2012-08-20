package com.dbstar.guodian.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;

public class GDMenuGallery extends Gallery {

	private static final String TAG = "MenuGallery";

	int mMostLeftItemIndex = 0;

	public GDMenuGallery(Context context) {
		super(context);
		
		initialize();
	}
	
	public GDMenuGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize();
	}

	public GDMenuGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize();
	}
	
	private void initialize() {
		this.setAnimationDuration(0);
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
	
	public void onShowPress (MotionEvent e) {
		;
	}

	public boolean onSingleTapUp (MotionEvent e) {
		return false;
	}

	public void  onLongPress(MotionEvent e) {
		;
	}
	
	public boolean onDown(MotionEvent e)  {
		return false;
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		return false;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)  {
		Log.d(TAG, "onScroll");
		return false;
	}
	
	
//	public void moveLeft() {
//		int selectedItemIndex = getSelectedItemPosition();
//		Log.d(TAG, "moveLeft");
//		int newPosition = selectedItemIndex + 1;
//		setSelection(newPosition, false);
//	}
//	
//	public void moveRight() {
//		int selectedItemIndex = getSelectedItemPosition();
//		Log.d(TAG, "moveRight");
//		if (selectedItemIndex > mMostLeftItemIndex) {
//			int newPosition = selectedItemIndex - 1;
//			setSelection(newPosition, false);
//		}
//
//	}
	
	public void setMostLeftItem(int index) {
		mMostLeftItemIndex = index;
	}
}
