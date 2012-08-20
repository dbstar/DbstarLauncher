package com.dbstar.guodian;

import com.dbstar.guodian.R;
import com.dbstar.guodian.model.ClientObserver;
import com.dbstar.guodian.model.GDDataProviderService;
import com.dbstar.guodian.model.GDDataProviderService.DataProviderBinder;
import com.dbstar.guodian.model.GDResourceAccessor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class GDBaseActivity extends Activity implements ClientObserver {
	private static final String TAG = "GDBaseActivity";

	protected static final String INTENT_KEY_MENUPATH = "menu_path";

	protected boolean mBound = false;
	protected GDDataProviderService mService;
	
	private ProgressDialog mLoadingDialog = null;
	private String mLoadingText = null;

	protected GDResourceAccessor mResource;
	
	protected static final int MENU_LEVEL_1 = 0;
	protected static final int MENU_LEVEL_2 = 1;
	protected static final int MENU_LEVEL_3 = 2;
	protected static final int MENU_LEVEL_COUNT = 3;
	protected static final String MENU_STRING_DELIMITER = ">";
	protected String mMenuPath;
	protected MenuPathItem[] mMenuPathItems = new MenuPathItem[MENU_LEVEL_COUNT];
	// Menu path container view
	ViewGroup mMenuPathContainer;

	protected class MenuPathItem {
		TextView sTextView;
		ImageView sDelimiter;
	}
	
	protected void initializeMenuPath() {
		
		mMenuPathContainer = (ViewGroup) findViewById(R.id.menupath_view);
		
		for (int i=0 ; i<MENU_LEVEL_COUNT; i++) {
			mMenuPathItems[i] = new MenuPathItem();
		}
		
		TextView textView = (TextView) findViewById (R.id.menupath_level1);
		mMenuPathItems[0].sTextView = textView;
		textView = (TextView) findViewById (R.id.menupath_level2);
		mMenuPathItems[1].sTextView = textView;
		textView = (TextView) findViewById (R.id.menupath_level3);
		mMenuPathItems[2].sTextView = textView;
		
		mMenuPathItems[0].sDelimiter = null;
		ImageView delimiterView = (ImageView) findViewById (R.id.menupath_level1_delimiter);
		mMenuPathItems[1].sDelimiter = delimiterView;
		delimiterView = (ImageView) findViewById (R.id.menupath_level2_delimiter);
		mMenuPathItems[2].sDelimiter = delimiterView;		
	}
	
	protected void initializeView () {
		initializeMenuPath();
	}
	
	protected void showMenuPath(String[] menuPath) {
		
		for(int i=0 ; i<mMenuPathItems.length ; i++) {
			if (i<menuPath.length) {
				mMenuPathItems[i].sTextView.setVisibility(View.VISIBLE);
				mMenuPathItems[i].sTextView.setText(menuPath[i]);
				
				if (mMenuPathItems[i].sDelimiter != null) {
					mMenuPathItems[i].sDelimiter.setVisibility(View.VISIBLE);
				}
			} else {
				mMenuPathItems[i].sTextView.setVisibility(View.INVISIBLE);
				
				if (mMenuPathItems[i].sDelimiter != null) {
					mMenuPathItems[i].sDelimiter.setVisibility(View.INVISIBLE);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mResource = new GDResourceAccessor(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!mBound) {
			Intent intent = new Intent(this, GDDataProviderService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			DataProviderBinder binder = (DataProviderBinder) service;
			mService = binder.getService();
			mBound = true;

			onServiceStart();
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBound = false;

			onServiceStop();
		}
	};
	
	
	protected void onServiceStart() {
		Log.d(TAG, "onServiceStart");
	}

	protected void onServiceStop() {
		Log.d(TAG, "onServiceStop");
	}
	
	public void updateData(int type, int param1, int param2, Object data) {
		
	}
	
	public void updateData(int type, Object key, Object data) {
		
	}

	protected boolean checkLoadingIsFinished() {
		return true;
	}
	
	protected void showLoadingDialog() {
		
		if (mLoadingText == null ) {
			mLoadingText = getResources().getString(R.string.loading_text);
		}
		
		if(mLoadingDialog == null || !mLoadingDialog.isShowing()) {
			Log.d(TAG, "show loading dialog");
			mLoadingDialog = ProgressDialog.show(this, "", mLoadingText, true);
			mLoadingDialog.setCancelable(true);
			mLoadingDialog.setCanceledOnTouchOutside(true);
			mLoadingDialog.setOnCancelListener(new LoadingCancelListener());
		}
	}
	
	protected void hideLoadingDialog() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()
				&& checkLoadingIsFinished()) {
			Log.d(TAG, "hide loading dialog");
			mLoadingDialog.dismiss();
		}
	}
	
	private class LoadingCancelListener implements OnCancelListener {
		public void onCancel(DialogInterface dialog) {
			onLoadingCancelled();
		}
	}
	
	protected void onLoadingCancelled() {
		Log.d(TAG, "onLoadingCancelled");

		cancelRequests(this);
	}
	
	protected void cancelRequests(ClientObserver observer) {
		Log.d(TAG, "cancelRequests");

		mService.cancelRequests(observer);
	}

	protected String formPageText(int pageNumber) {
		String str = mResource.HanZi_Di;
		str += (pageNumber + 1) + mResource.HanZi_Ye;

		return str;
	}
	
}
