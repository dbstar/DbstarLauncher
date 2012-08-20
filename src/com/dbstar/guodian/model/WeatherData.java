package com.dbstar.guodian.model;

import java.util.List;

import android.graphics.Bitmap;

/*
{ 
"weatherinfo": {
    "city":"�ɶ�",
    "city_en":"chengdu",
    "date_y":"2011��11��30��",
    "date":"��î��",
    "week":"������",
    "fchh":"11",           //Ԥ������ʱ��
    "cityid":"101270101",
    "temp1":"13��~10��",
    "temp2":"14��~6��",
    "temp3":"13��~5��",
    "temp4":"14��~8��",
    "temp5":"10��~8��",
    "temp6":"11��~6��",
    "tempF1":"55.4�H~50�H",
    "tempF2":"57.2�H~42.8�H",
    "tempF3":"55.4�H~41�H",
    "tempF4":"57.2�H~46.4�H",
    "tempF5":"50�H~46.4�H",
    "tempF6":"51.8�H~42.8�H",
    "weather1":"��ת����",
    "weather2":"����ת��",
    "weather3":"����ת��",
    "weather4":"����",
    "weather5":"����תС��",
    "weather6":"С��ת��",
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
    "img_title1":"��",
    "img_title2":"����",
    "img_title3":"����",
    "img_title4":"��",
    "img_title5":"����",
    "img_title6":"��",
    "img_title7":"����",
    "img_title8":"����",
    "img_title9":"����",
    "img_title10":"С��",
    "img_title11":"С��",
    "img_title12":"��",
    "img_title_single":"��",
    "wind1":"����С��3��",
    "wind2":"����С��3��",
    "wind3":"����С��3��",
    "wind4":"�Ϸ�ת����С��3��",
    "wind5":"����С��3��",
    "wind6":"����С��3��",
    "fx1":"����",
    "fx2":"����",
    "fl1":"С��3��",
    "fl2":"С��3��",
    "fl3":"С��3��",
    "fl4":"С��3��",
    "fl5":"С��3��",
    "fl6":"С��3��",
    "index":"����",
    "index_d":"�����ű�����װ��ţ������ȴ������װ������������������װ���п����ȡ�",
    "index48":"����",
    "index48_d":"�����ű�����װ��ţ������ȴ������װ������������������װ���п����ȡ�",
    "index_uv":"����",
    "index48_uv":"��",
    "index_xc":"����",
    "index_tr":"������",
    "index_co":"������",
    "st1":"14",
    "st2":"10",
    "st3":"14",
    "st4":"6",
    "st5":"13",
    "st6":"4",
    "index_cl":"������",
    "index_ls":"��̫����",
    "index_ag":"���׷�"
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
