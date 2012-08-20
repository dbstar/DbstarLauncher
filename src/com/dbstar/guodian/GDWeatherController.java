package com.dbstar.guodian;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dbstar.guodian.model.ClientObserver;
import com.dbstar.guodian.model.GDDataProviderService;
import com.dbstar.guodian.model.WeatherData;

public class GDWeatherController implements ClientObserver {

	private static final int RequestPeriod = 900000; // 15minutes
	
	private GDDataProviderService mService;
	private Handler mUiHandler;
	
	private Handler mScheduler = new Handler();
	
	private ClientObserver mObserver = null;
	
	private String mCity = "北京.北京";
	
	private Runnable mTask = new Runnable() {

		@Override
		public void run() {
			if (mService != null) {
			
				mService.getWeatherData(mObserver, mCity);
			}

			mScheduler.postDelayed(mTask, RequestPeriod);
		}
		
	};
	
	public GDWeatherController(Handler uiHandler) {
		mUiHandler = uiHandler;
	}

	public void start(GDDataProviderService service) {
		mService = service;
		mScheduler.postDelayed(mTask, 0);
	}
	
	@Override
	public void updateData(int type, int param1, int param2, Object data) {
		
	}

	@Override
	public void updateData(int type, Object key, Object data) {
		if (type == GDDataProviderService.REQUESTTYPE_GETWEATHER) {
			if (key.equals(mCity)) {
				WeatherData weatherData = (WeatherData)data;
				
				Message message = mUiHandler.obtainMessage(GDLauncherActivity.MSG_UPDATE_WEATHER);
				Bundle parm = new Bundle();
				String weather = "";
				String temperature = "";
				String imageId = "";
				if (weatherData.Weathers != null && weatherData.Weathers.size() > 0) {
					weather = weatherData.Weathers.get(0);
				}
				if (weatherData.Temperatures != null && weatherData.Temperatures.size() > 0) {
					temperature = weatherData.Temperatures.get(0);
				}
				if (weatherData.ImageIds != null && weatherData.ImageIds.size() > 0) {
					imageId = weatherData.ImageIds.get(0);
				}
				
				parm.putString(GDLauncherActivity.KeyWeather, weather);
				parm.putString(GDLauncherActivity.KeyTemperature, temperature);
				parm.putString(GDLauncherActivity.KeyWeatherImageId, imageId);
				message.setData(parm);
				mUiHandler.sendMessage(message);
			}
		}
	}

}
