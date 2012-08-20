package com.dbstar.guodian.model;

import java.util.List;

import android.graphics.Bitmap;

/*
{ 
"weatherinfo": {
    "city":"成都",
    "city_en":"chengdu",
    "date_y":"2011年11月30日",
    "date":"辛卯年",
    "week":"星期三",
    "fchh":"11",           //预报发布时间
    "cityid":"101270101",
    "temp1":"13℃~10℃",
    "temp2":"14℃~6℃",
    "temp3":"13℃~5℃",
    "temp4":"14℃~8℃",
    "temp5":"10℃~8℃",
    "temp6":"11℃~6℃",
    "tempF1":"55.4℉~50℉",
    "tempF2":"57.2℉~42.8℉",
    "tempF3":"55.4℉~41℉",
    "tempF4":"57.2℉~46.4℉",
    "tempF5":"50℉~46.4℉",
    "tempF6":"51.8℉~42.8℉",
    "weather1":"阴转多云",
    "weather2":"多云转晴",
    "weather3":"多云转阴",
    "weather4":"阵雨",
    "weather5":"阵雨转小雨",
    "weather6":"小雨转阴",
    "img1":"2",
    "img2":"1",
    "img3":"1",
    "img4":"0",
    "img5":"1",
    "img6":"2",
    "img7":"3",
    "img8":"99",
    "img9":"3",
    "img10":"7",
    "img11":"7",
    "img12":"2",
    "img_single":"2",
    "img_title1":"阴",
    "img_title2":"多云",
    "img_title3":"多云",
    "img_title4":"晴",
    "img_title5":"多云",
    "img_title6":"阴",
    "img_title7":"阵雨",
    "img_title8":"阵雨",
    "img_title9":"阵雨",
    "img_title10":"小雨",
    "img_title11":"小雨",
    "img_title12":"阴",
    "img_title_single":"阴",
    "wind1":"北风小于3级",
    "wind2":"北风小于3级",
    "wind3":"北风小于3级",
    "wind4":"南风转北风小于3级",
    "wind5":"北风小于3级",
    "wind6":"北风小于3级",
    "fx1":"北风",
    "fx2":"北风",
    "fl1":"小于3级",
    "fl2":"小于3级",
    "fl3":"小于3级",
    "fl4":"小于3级",
    "fl5":"小于3级",
    "fl6":"小于3级",
    "index":"舒适",
    "index_d":"建议着薄型套装或牛仔衫裤等春秋过渡装。年老体弱者宜着套装、夹克衫等。",
    "index48":"舒适",
    "index48_d":"建议着薄型套装或牛仔衫裤等春秋过渡装。年老体弱者宜着套装、夹克衫等。",
    "index_uv":"最弱",
    "index48_uv":"弱",
    "index_xc":"适宜",
    "index_tr":"很适宜",
    "index_co":"较舒适",
    "st1":"14",
    "st2":"10",
    "st3":"14",
    "st4":"6",
    "st5":"13",
    "st6":"4",
    "index_cl":"较适宜",
    "index_ls":"不太适宜",
    "index_ag":"较易发"
    }
}
 * */
public class WeatherData {
	
	public static final String KeyWeatherInfo = "weatherinfo";
	public static final String KeyCity="city";
	public static final String KeyCityId = "cityid";
	public static final String KeyDate="date_y";
	public static final String KeyChineseDate = "date";
	public static final String KeyWeek = "week";
	public static final String KeyTemperature = "temp";
	public static final String KeyWeather = "weather";
	public static final String KeyImage = "img";
	public static final String KeyImageTitle = "img_title";
	public static final String KeyWind = "wind";
	public static final int DayCount = 6;


	public String CityName;
	public String CityId;
	public String Date;
	public String ChineseDate;
	public String Week;
	
	public List<String> Temperatures;
	public List<String> Weathers;
	public List<String> ImageIds;
	public List<Bitmap> Images;
	public List<String> ImageTitles;
	public List<String> Winds;
}
