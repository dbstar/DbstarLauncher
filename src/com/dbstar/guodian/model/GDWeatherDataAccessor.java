package com.dbstar.guodian.model;

import java.io.File;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GDWeatherDataAccessor {

	private static final String TAG = "GDWeatherDataAccessor";
	private static final String ProvinceCodeUrl = "http://m.weather.com.cn/data5/city.xml";
	private static final String CityCodeUrlPrefix = "http://m.weather.com.cn/data5/city";
	private static final String CityWeatherUrlPrefix = "http://m.weather.com.cn/data/";
	private static final String ProvincesTable = "provinces";
	private static final String CityCodeTable = "citycodes";

	private static final String ColumnID = "_id";
	private static final String ColumnName = "name";
	private static final String ColumnParentID = "parent_id";
	private static final String ColumnCityCode = "city_code";

	private static final String CreateProvinceTableStatement = "create table if not exists "
			+ ProvincesTable
			+ " ("
			+ ColumnID
			+ " integer primary key autoincrement, "
			+ ColumnName
			+ " nvarchar(40))";
	private static final String CreateCitysTableStatement = "create table if not exists "
			+ CityCodeTable
			+ " ("
			+ ColumnID
			+ " integer primary key autoincrement, "
			+ ColumnParentID
			+ " integer, "
			+ ColumnName
			+ " nvarchar(40), "
			+ ColumnCityCode
			+ " nvarchar(40))";

	String[] mProvinceNames;
	String[][] mCityNames;
	String[][] mCityCodes;

	private String mDBFile = null;

	public GDWeatherDataAccessor(String dbFile) {
		mDBFile = dbFile;
	}

	public WeatherData getWeatherData(String location) {
		String cityCode = getCityCodes(location);
		WeatherData data = getCityWeather(cityCode);
		
		return data;
	}

	private String getCityCodes(String location) {
		String cityCode = "";

		File dbFile = new File(mDBFile);
		if (!dbFile.exists()) {
			initCityCodes();
			saveCityCode(mDBFile);
		}
		
		cityCode = queryCityCode(location);

		return cityCode;
	}
	
	private static final String[] ProjectionProvince = new String[] {ColumnID};
	private static final int ColumnIDIndex = 0;

	private static final String[] ProjectionCitys = new String[] {
		ColumnCityCode
	};

	private static final int ColumnCityCodeIndex = 0;
	
	
	//city=province.city.town
	private String queryCityCode(String location) {
		int index = location.indexOf(".");
		String province = location.substring(0, index);
		String city = location.substring(index+1);
		int parentId = -1;
		String cityCode = "";
		
		String selection = ColumnName + "=?";
		String[] selectionArgs = new String[] { province };
		
		SQLiteDatabase db = null;
		try {
			db = SQLiteDatabase.openDatabase(mDBFile, null,
					SQLiteDatabase.OPEN_READONLY
							| SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		} catch (Exception e) {
			e.printStackTrace();
			return cityCode;
		}
		
		Cursor cursor = null;
		cursor = db.query(ProvincesTable, ProjectionProvince, selection, selectionArgs, null, null, null, null);
		
		if (cursor != null) {
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				parentId = cursor.getInt(ColumnIDIndex);
			}
			
			cursor.close();
		}
		
		if (parentId < 0) {
			return cityCode;
		}
		
		selection = ColumnParentID + "=? AND " + ColumnName + "=?";
		selectionArgs = new String[] { Integer.toString(parentId), city };
		
		cursor = db.query(CityCodeTable, ProjectionCitys, selection, selectionArgs, null, null, null, null);
		
		if (cursor != null) {

			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				cityCode = cursor.getString(ColumnCityCodeIndex);
			}
			
			cursor.close();
		}
		
		db.close();
		
		return cityCode;
	}

	
	private WeatherData getCityWeather (String cityCode) {
		StringBuffer urlBuilder = new StringBuffer(CityWeatherUrlPrefix);
		urlBuilder.append(cityCode);
		urlBuilder.append(".html");
		String webContent = getWebContent(urlBuilder.toString());
		
		WeatherData weatherData =  parseWeatherData(webContent);
		
		return weatherData;
	}
	
	
	private WeatherData parseWeatherData (String data) {
		WeatherData weatherData = null;

		if (data != null) {
			JSONTokener jsonParser = new JSONTokener(data);
	
			try {
				JSONObject jsonData = (JSONObject) jsonParser.nextValue();
				JSONObject weatherInfo = jsonData.getJSONObject(WeatherData.KeyWeatherInfo);
				if (weatherInfo != null && weatherInfo.length() > 0) {
					weatherData = new WeatherData();

					weatherData.CityName = weatherInfo.getString(WeatherData.KeyCity);
					weatherData.Date = weatherInfo.getString(WeatherData.KeyDate);
					weatherData.ChineseDate = weatherInfo.getString(WeatherData.KeyChineseDate);
					weatherData.Week = weatherInfo.getString(WeatherData.KeyWeek);
					weatherData.CityId = weatherInfo.getString(WeatherData.KeyCityId);
					
					weatherData.Temperatures = new LinkedList<String>();
					weatherData.Weathers = new LinkedList<String>();
					weatherData.ImageIds = new LinkedList<String>();
					weatherData.ImageTitles = new LinkedList<String>();
					weatherData.Winds = new LinkedList<String>();

					for(int i=0; i<WeatherData.DayCount ; i++) {
						String keyTemp = new StringBuffer(WeatherData.KeyTemperature).append(String.valueOf(i + 1)).toString();
						String keyWeather = new StringBuffer(WeatherData.KeyWeather).append(String.valueOf(i + 1)).toString();
						String keyImageIds = new StringBuffer(WeatherData.KeyImage).append(String.valueOf(i + 1)).toString();
						String keyImageTitle = new StringBuffer(WeatherData.KeyImageTitle).append(String.valueOf(i + 1)).toString();
						String keyWind = new StringBuffer(WeatherData.KeyWind).append(String.valueOf(i + 1)).toString();
						
						String temp = weatherInfo.getString(keyTemp);
						String weather = weatherInfo.getString(keyWeather);
						String imageId = weatherInfo.getString(keyImageIds);
						String imageTitle = weatherInfo.getString(keyImageTitle);
						String wind = weatherInfo.getString(keyWind);
						
						weatherData.Temperatures.add(temp);
						weatherData.Weathers.add(weather);
						weatherData.ImageIds.add(imageId);
						weatherData.ImageTitles.add(imageTitle);
						weatherData.Winds.add(wind);
					}
				}
				
			} catch (JSONException e) {
				Log.d(TAG, "error!");
				e.printStackTrace();
			}
		}
		
		return weatherData;
	}
	
	
	// mProvinceNames: 34 name string of provinces or first level citys
	// mCityNames: names of citys and towns of every province
	// mCityCodes: the city code of every item in mCityNames
	private void initCityCodes() {

		// Get province code
		String webContent = getWebContent(ProvinceCodeUrl);
		Log.d(TAG, "1 webContent = " + webContent);
		String[][] provinces = parseCity(webContent);

		mProvinceNames = new String[provinces.length];
		mCityNames = new String[provinces.length][];
		mCityCodes = new String[provinces.length][];

		for (int provinceIndex = 0; provinceIndex < provinces.length; provinceIndex++) {

			mProvinceNames[provinceIndex] = provinces[provinceIndex][1];

			StringBuffer urlBuilder = new StringBuffer(CityCodeUrlPrefix);
			urlBuilder.append(provinces[provinceIndex][0]);
			urlBuilder.append(".xml");
			webContent = getWebContent(urlBuilder.toString());
			Log.d(TAG, "2 " + provinceIndex + " webContent = " + webContent);
			String[][] citys = parseCity(webContent);

			String[][][] towns = new String[citys.length][][];
			int townsCountPerProvince = 0;
			for (int cityIndex = 0; cityIndex < citys.length; cityIndex++) {
				urlBuilder = new StringBuffer(CityCodeUrlPrefix);
				urlBuilder.append(citys[cityIndex][0]);
				urlBuilder.append(".xml");
				webContent = getWebContent(urlBuilder.toString());
				
				Log.d(TAG, "3 " + cityIndex + " webContent = " + webContent);
				
				towns[cityIndex] = parseCity(webContent);
				townsCountPerProvince = townsCountPerProvince
						+ towns[cityIndex].length;
			}

			mCityNames[provinceIndex] = new String[townsCountPerProvince];
			mCityCodes[provinceIndex] = new String[townsCountPerProvince];

			int sum = 0;
			for (int cityIndex = 0; cityIndex < citys.length; cityIndex++) {
				for (int townIndex = 0; townIndex < towns[cityIndex].length; townIndex++) {
					if (townIndex == 0)
						mCityNames[provinceIndex][sum] = towns[cityIndex][0][1];
					else
						mCityNames[provinceIndex][sum] = towns[cityIndex][0][1]
								+ "." + towns[cityIndex][townIndex][1];

					urlBuilder = new StringBuffer(CityCodeUrlPrefix);
					urlBuilder.append(towns[cityIndex][townIndex][0]);
					urlBuilder.append(".xml");
					
					Log.d(TAG, "request url = " + urlBuilder.toString());

					webContent = getWebContent(urlBuilder.toString());
					Log.d(TAG, "4 " + sum + " webContent = " + webContent);
					String[][] code = parseCity(webContent);
					mCityCodes[provinceIndex][sum] = code[0][1]; // error!
					sum = sum + 1;
				}
			}
			urlBuilder = null;
		}
	}

	private String getWebContent(String url) {
		HttpGet request = new HttpGet(url);
		//HttpParams params = new BasicHttpParams();
		//HttpConnectionParams.setConnectionTimeout(params, 10000);
		//HttpConnectionParams.setSoTimeout(params, 10000);
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String content = EntityUtils.toString(response.getEntity());
				return content;
			} else {
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return null;
	}

	/**
	 * 通过解析content，得到一个一维为城市编号，二维为城市名的二维数组 解析的字符串的形式为:
	 * <code>编号|城市名,编号|城市名,.....</code>
	 * 
	 * @param content
	 *            需要解析的字符串
	 * @return 封装有城市编码与名称的二维数组
	 */
	private String[][] parseCity(String content) {
		if (content == null || content.trim().length() == 0) {
			return null;
		}

		StringTokenizer st = new StringTokenizer(content, ",");
		int count = st.countTokens();
		String[][] citys = new String[count][2];
		int i = 0, index = 0;
		while (st.hasMoreTokens()) {
			String city = st.nextToken();
			index = city.indexOf('|');
			citys[i][0] = city.substring(0, index);
			citys[i][1] = city.substring(index + 1);
			i = i + 1;
		}
		return citys;
	}

	private void saveCityCode(String dbFile) {
		SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(dbFile,
				null);

		database.execSQL(CreateProvinceTableStatement);
		database.execSQL(CreateCitysTableStatement);

		ContentValues cv = null;
		for (int i = 0; i < mProvinceNames.length; i++) {
			cv = new ContentValues();
			cv.put("name", mProvinceNames[i]);
			database.insert("provinces", null, cv);
		}

		for (int i = 0; i < mCityNames.length; i++) {
			for (int j = 0; j < mCityNames[i].length; j++) {
				cv = new ContentValues();
				cv.put("province_id", i);
				cv.put("name", mCityNames[i][j]);
				cv.put("city_num", mCityCodes[i][j]);
				database.insert("citys", null, cv);
			}
		}
		cv = null;
		database.close();
	}

}
