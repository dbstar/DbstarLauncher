package com.dbstar.guodian;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.dbstar.guodian.model.ClientObserver;
import com.dbstar.guodian.model.GDSettings;

import com.dbstar.guodian.model.GDDataProviderService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GDPowerUsageController implements ClientObserver {
	
	private static final String TAG = "GDPowerUsageController";
	private static final int RequestPowerPeriod = 900000; // 15minutes
	
	private String mCCID = "89277089728430810813";
	
	private static final String DateFormatStr = "yyyy-MM-dd HH:mm:ss";
	
	private Handler mUiHandler;
	private GDDataProviderService mService;
	
	private Handler mScheduler = new Handler();
	
	private ClientObserver mObserver = null;
	
	private Runnable mTask = new Runnable() {

		@Override
		public void run() {
			if (mService != null && !mCCID.isEmpty()) {
				String date_start = getDateStart();
				String date_end = getDateEnd();
				String charge_type = "single";
				
				Log.d(TAG, "date_start = " + date_start + " date_end = " + date_end);
				
				mService.getPowerConsumption(mObserver, mCCID, date_start, date_end);
				mService.getTotalCostByChargeType(mObserver, mCCID, date_start, date_end, charge_type);
			}

			mScheduler.postDelayed(mTask, RequestPowerPeriod);
		}
		
	};
	
	public GDPowerUsageController(Handler uiHander) {
		mUiHandler = uiHander;
		mObserver = this;
	}
	
	public void start(GDDataProviderService service) {
		mService = service;
		
		if (mCCID.isEmpty()) {
			mService.getSettingsValue(mObserver, GDSettings.SettingSerialNumber);
		}
		
		mScheduler.postDelayed(mTask, 0);
	}

	private String getDateStart() {
		// get today and clear time of day
		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.HOUR_OF_DAY);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);

		// get start of the month
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		Date startOfMonth = cal.getTime();

		SimpleDateFormat sdf = new SimpleDateFormat(DateFormatStr, Locale.US);
		return sdf.format(startOfMonth);
	}
	
	private String getDateEnd() {
		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.MILLISECOND);
		
		Date today = cal.getTime();
		
		SimpleDateFormat sdf = new SimpleDateFormat(DateFormatStr, Locale.US);
		return sdf.format(today);
	}
	
	@Override
	public void updateData(int type, int param1, int param2, Object data) {
		if (type == GDDataProviderService.REQUESTTYPE_GETPOWERCONSUMPTION) {
			String consumption = (String)data;
			Message message = mUiHandler.obtainMessage(GDLauncherActivity.MSG_UPDATE_POWERCONSUMPTION);
			Bundle parm = new Bundle();
			parm.putString(GDLauncherActivity.KeyPowerConsumption, consumption);
			message.setData(parm);
			mUiHandler.sendMessage(message);
		} else if (type == GDDataProviderService.REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE) {
			String totalCost = (String)data;
			
			Message message = mUiHandler.obtainMessage(GDLauncherActivity.MSG_UPDATE_POWERTOTALCOST);
			Bundle parm = new Bundle();
			parm.putString(GDLauncherActivity.KeyPowerTotalCost, totalCost);
			message.setData(parm);
			mUiHandler.sendMessage(message);
		} else {
			
		}
	}

	@Override
	public void updateData(int type, Object key, Object data) {
		if (type == GDDataProviderService.REQUESTTYPE_GETSETTINGS) {
			String settingName = (String)key;
			if (settingName.equals(GDSettings.SettingSerialNumber)) {
				mCCID = (String)data;
				Log.d(TAG, "cc_id=" + mCCID);
				mScheduler.postDelayed(mTask, 0);
			}
		}
	}
	
}
