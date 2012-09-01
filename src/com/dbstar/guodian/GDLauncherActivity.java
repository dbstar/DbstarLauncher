package com.dbstar.guodian;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.dbstar.guodian.model.ColumnData;
import com.dbstar.guodian.model.GDApplicationObserver;
import com.dbstar.guodian.model.GDCommon;
import com.dbstar.guodian.model.GDDVBDataContract.Content;
import com.dbstar.guodian.model.GDDataProviderService;
import com.dbstar.guodian.widget.*;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class GDLauncherActivity extends GDBaseActivity implements GDApplicationObserver {

	private static final String TAG = "GDLauncherActivity";
	private static final int START_ITEM_INDEX = 4;

	private static final int COLUMN_LEVEL_1 = 1;
	private static final int COLUMN_LEVEL_2 = 2;

	private static final int DIALOG_SETTINGSCHOOSER_ID = 0;
	
	GDVideoView mVideoView;
	GDMediaScheduler mGDMediaScheduler;
	
	GDPowerUsageController mPowerController;
	GDWeatherController mWeatherController;

	GDMenuGallery mMenuLevel1;
	MenuLevel1Adapter mMenuLevel1Adapter;
	int mMenuLevel1SelectionIndex = 0;
	TextView mMenuLevel1SelectionText;
	ImageView mMenuLevel1SelectionIcon;

	// Menu
	ListView mMenuLevel2;
	MenuLevel2Adapter mMenuLevel2Adapter;
	boolean mMenuLevel2IsHided = true;

	private boolean mShowMenuPathIsOn = true;
	private boolean mMarqeeViewIsOn = false;
	// Marqee view
	GDMarqeeTextView mMarqeeView;
	
	private void turnOnMarqeeView(boolean on) {		
		mShowMenuPathIsOn = !on;
		mMarqeeViewIsOn = on;
		
		if (!mShowMenuPathIsOn) {
			mMenuPathContainer.setVisibility(View.GONE);
		} else {
			mMenuPathContainer.setVisibility(View.VISIBLE);
		}
		
		if (!mMarqeeViewIsOn) {
			if (mMarqeeView.isRunning()) {
				mMarqeeView.stopMarqee();
			}
			mMarqeeView.setVisibility(View.GONE);
		} else {
			mMarqeeView.setVisibility(View.VISIBLE);
		}
	}
	
	private void showMarqueeView() {
		if (!mMarqeeViewIsOn)
			return;
		
		if (mMarqeeView.getVisibility() != View.VISIBLE) {
			mMarqeeView.setVisibility(View.VISIBLE);
		}

		mMarqeeView.startMarqee(GDMarqeeTextView.MarqeeForever);
	}
	
	private void hideMarqeeView() {
		if (mMarqeeView == null)
			return;
		
		if (mMarqeeView.isRunning())
			mMarqeeView.stopMarqee();
		
		if (mMarqeeView.getVisibility() == View.VISIBLE) {
			mMarqeeView.setVisibility(View.GONE);
		}
	}

	// Calendar View
	TextView mTimeView, mCityView, mDateView, mWeekView;
	GDCelanderThread mCelanderThread;

	// Weather View
	TextView mWeatherView, mTemperatureView;
	ImageView mWeatherPictureView;

	// Power View
	TextView mPowerUsedDegreeView, mPowerUsedCostView, mPowerUsedPanelText;

	private static final float POWER_MAX = 200.f;
	private static final float POWER_MIN = 0.f;

	private float powerUsedToDegree(float powerUsed) {
		return 180.f * (powerUsed / (POWER_MAX - POWER_MIN));
	}

	// arrow
	ImageView mLeftArrow, mRightArrow;
	boolean mMoveLeft = true;

	AnimationSet mShowHighlightAnimation, mLeftArrowAnimation,
			mRightArrowAnimation;

	/*
	 * ColumnItem represent on item in the sub-level menu. if it has a sub-level
	 * menu, they are saved as Object SubItems instead ofColumnItem[]. It is
	 * convert to ColumnItem[] on runtime. This means ColumnItem likes a tree
	 * structure.
	 */
	private class ColumnItem {
		public static final int NONE = -1;
		public static final int NO_SUBCOLUMNS = 0;
		public static final int HAS_SUBCOLUMNS = 1;

		public ColumnData Column;

		public int Level;

		public int HasSubColumns;
		public Object SubItems;

		public ColumnItem() {
			Level = 0;
			SubItems = null;
			HasSubColumns = NONE;
		}
	}

	private class MenuItem {
		public int SelectedItemIndex;
		public ColumnItem[] Items;

		public MenuItem() {
			SelectedItemIndex = 0;
			Items = null;
		}
	}

	/*
	 * Each item in level1 menu corresponds to one menu stack in this vector.
	 * When we navigate in the sub-level menu, the sub-level menu is pushed into
	 * this stack as follow: level2/level3(sub-level menu of one item in
	 * level2)/... When we back from sub-level menu to parent level menu,
	 * sub-level menu is popped up. The top is always the one that is shown on
	 * screen.
	 */
	Vector<Stack<MenuItem>> mSubMenuArray;

	private static final int MENULEVEL1_ITEM_SMARTPOWER = 0;
	private static final int MENULEVEL1_ITEM_SMARTHOME = 1;
	private static final int MENULEVEL1_ITEM_GUOWANG = 2;
	private static final int MENULEVEL1_ITEM_HDMOVIE = 3;
	private static final int MENULEVEL1_ITEM_TV = 4;
	private static final int MENULEVEL1_ITEM_RECORD = 5;
	private static final int MENULEVEL1_ITEM_VARIETY = 6;
	private static final int MENULEVEL1_ITEM_CHILDREN = 7;
	private static final int MENULEVEL1_ITEM_3D = 8;
	private static final int MENULEVEL1_ITEM_TRAILER = 9;
	private static final int MENULEVEL1_ITEM_SETTINGS = 10;

	private static final int MENULEVEL1_ITEM_COUNT = 11;
	
	private static final int MENULEVEL1_ITEM_ZONGYI = 11;
	private static final int MENULEVEL1_ITEM_PAPER = 12;
	private static final int MENULEVEL1_ITEM_JOURNAL = 13;
	private static final int MENULEVEL1_ITEM_BOOK = 14;
	private static final int MENULEVEL1_ITEM_DEMO = 15;
	private static final int MENULEVEL1_ITEM_IPTV = 16;
	private static final int MENULEVEL1_ITEM_HEALTHY = 17;
	
	private static final int MENULEVEL2_GUOWANG_KUAIXUNINDEX = 0;
	private static final int MENULEVEL2_GUOWANG_SHIPININDEX = 1;
	private static final int MENULEVEL2_GUOWANG_BAOZHIINDEX = 2;

	class MenuLevel1Item {
		String ColumnId;
		String MenuText;
		Bitmap MenuIcon;
		Bitmap MenuIconFocused;
	};
	
	MenuLevel1Item[] mMenuLevel1Items;

	int[] mWeatherImageIds = { R.drawable.qing };
	Bitmap[] mWeatherImages = null;
	private String Yuan;
	private String Degree;
	ImageView mPanelPointer;
	
	public static final int MSG_UPDATE_DATE = 0;
	public static final int MSG_UPDATE_WEATHER = 1;
	public static final int MSG_UPDATE_POWERCONSUMPTION = 2;
	public static final int MSG_UPDATE_POWERTOTALCOST = 3;
	
	public static final String KeyPowerConsumption= "power_consumption";
	public static final String KeyPowerTotalCost= "power_total_cost";

	public static final String KeyWeather = "weather";
	public static final String KeyTemperature = "temperature";
	public static final String KeyWeatherImageId = "img_id";
	
	private String mPowerConsumption = "0";
	private String mPowerCost = "0";

	private Handler mUIUpdateHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_DATE: {
				
				break;
			}
			
			case MSG_UPDATE_WEATHER: {
				Bundle data = msg.getData();
				String weather = data.getString(KeyWeather);
				String temperature = data.getString(KeyTemperature);
				updateWheatherView(weather, temperature, mWeatherImages[0]);
			}
			
			case MSG_UPDATE_POWERCONSUMPTION: {
				Bundle data = msg.getData();
				String powerConsumption = data.getString(KeyPowerConsumption);
				updatePowerView(powerConsumption, "");
				break;
			}
			
			case MSG_UPDATE_POWERTOTALCOST: {
				Bundle data = msg.getData();
				String powerTotalCost = data.getString(KeyPowerTotalCost);
				updatePowerView("", powerTotalCost);
				break;
			}

			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LOW_PROFILE);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

		setContentView(R.layout.main_view);

		initalizeView();
	}

	private Bitmap mDefaultPoster = null;
	
	private static final int MENULEVEL1ITEMWIDTH = 111;
	private static final int MENULEVEL1ITEMHEIGHT= 131;
	private static final int MENULEVEL1FOCUSEDITEMWIDTH = 174;
	private static final int MENULEVEL1FOCUSEDITEMHEIGHT=157;
	
	private int mMenuLevel1ItemWidth, mMenuLevel1ItemHeight;
	private int mMenuLevel1FocusedItemWidth, mMenuLevel1FocusedItemHeight;
	
	protected void initalizeView() {

		super.initializeView();
		
		float density = getResources().getDisplayMetrics().density;
		mMenuLevel1ItemWidth = (int)density * MENULEVEL1ITEMWIDTH;
		mMenuLevel1ItemHeight = (int)density * MENULEVEL1ITEMHEIGHT;
		mMenuLevel1FocusedItemWidth = (int)density * MENULEVEL1FOCUSEDITEMWIDTH;
		mMenuLevel1FocusedItemHeight = (int)density * MENULEVEL1FOCUSEDITEMHEIGHT;
		
		AssetManager am = getAssets();
		
		try {
			InputStream is = am.open("default/default_0.jpg");
			mDefaultPoster = BitmapFactory.decodeStream(is);
			Log.d(TAG, "mDefaultPoster = " + mDefaultPoster);
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		mWeatherImages = new Bitmap[mWeatherImageIds.length];
		for(int i=0; i<mWeatherImageIds.length ; i++) {
			mWeatherImages[i] = BitmapFactory.decodeResource(getResources(), mWeatherImageIds[i]);
		}

		mMarqeeView = (GDMarqeeTextView) findViewById(R.id.marqeeView);
		
		mMenuLevel1Items = new MenuLevel1Item[MENULEVEL1_ITEM_COUNT];
		for (int i=0 ; i<MENULEVEL1_ITEM_COUNT ; i++) {
			mMenuLevel1Items[i] = new MenuLevel1Item();
		}
		
		mMenuLevel1Items[MENULEVEL1_ITEM_GUOWANG].ColumnId = "160";
		mMenuLevel1Items[MENULEVEL1_ITEM_GUOWANG].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_guowang);
		mMenuLevel1Items[MENULEVEL1_ITEM_GUOWANG].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_guowang_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_GUOWANG].MenuText = getResources()
				.getString(R.string.menulevel1_item_guowang);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTPOWER].ColumnId = "158";
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTPOWER].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_smartpower);
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTPOWER].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_smartpower_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTPOWER].MenuText = getResources()
				.getString(R.string.menulevel1_item_smartpower);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTHOME].ColumnId = "159";
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTHOME].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_smarthome);
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTHOME].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_smarthome_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_SMARTHOME].MenuText = getResources()
				.getString(R.string.menulevel1_item_smarthome);

		mMenuLevel1Items[MENULEVEL1_ITEM_HDMOVIE].ColumnId = "50";
		mMenuLevel1Items[MENULEVEL1_ITEM_HDMOVIE].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_hdmovie);
		mMenuLevel1Items[MENULEVEL1_ITEM_HDMOVIE].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_hdmovie_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_HDMOVIE].MenuText = getResources()
				.getString(R.string.menulevel1_item_hdmovie);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_TV].ColumnId = "150";
		mMenuLevel1Items[MENULEVEL1_ITEM_TV].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_tv);
		mMenuLevel1Items[MENULEVEL1_ITEM_TV].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_tv_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_TV].MenuText = getResources()
				.getString(R.string.menulevel1_item_tv);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_RECORD].ColumnId = "151";
		mMenuLevel1Items[MENULEVEL1_ITEM_RECORD].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_documentary);
		mMenuLevel1Items[MENULEVEL1_ITEM_RECORD].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_documentary_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_RECORD].MenuText = getResources()
				.getString(R.string.menulevel1_item_record);
		
//		mMenuLevel1Items[MENULEVEL1_ITEM_ZONGYI].ColumnId = "152";
//		mMenuLevel1Items[MENULEVEL1_ITEM_ZONGYI].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_zongyi);
//		mMenuLevel1Items[MENULEVEL1_ITEM_ZONGYI].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_zongyi_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_ZONGYI].MenuText = getResources()
//				.getString(R.string.menulevel1_item_zongyi);
		
		
//		mMenuLevel1Items[MENULEVEL1_ITEM_PAPER].ColumnId = "153";
//		mMenuLevel1Items[MENULEVEL1_ITEM_PAPER].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default);
//		mMenuLevel1Items[MENULEVEL1_ITEM_PAPER].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_PAPER].MenuText = getResources()
//				.getString(R.string.menulevel1_item_paper);
//		
//		
//		mMenuLevel1Items[MENULEVEL1_ITEM_JOURNAL].ColumnId = "154";
//		mMenuLevel1Items[MENULEVEL1_ITEM_JOURNAL].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default);
//		mMenuLevel1Items[MENULEVEL1_ITEM_JOURNAL].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_JOURNAL].MenuText = getResources()
//				.getString(R.string.menulevel1_item_journal);
//		
//		mMenuLevel1Items[MENULEVEL1_ITEM_BOOK].ColumnId = "155";
//		mMenuLevel1Items[MENULEVEL1_ITEM_BOOK].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default);
//		mMenuLevel1Items[MENULEVEL1_ITEM_BOOK].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_default_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_BOOK].MenuText = getResources()
//				.getString(R.string.menulevel1_item_book);
//
//		
		
		mMenuLevel1Items[MENULEVEL1_ITEM_3D].ColumnId = "157";
		mMenuLevel1Items[MENULEVEL1_ITEM_3D].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_3d);
		mMenuLevel1Items[MENULEVEL1_ITEM_3D].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_3d_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_3D].MenuText = getResources()
				.getString(R.string.menulevel1_item_3d);
		
		
//		mMenuLevel1Items[MENULEVEL1_ITEM_IPTV].ColumnId = "161";
//		mMenuLevel1Items[MENULEVEL1_ITEM_IPTV].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_iptv);
//		mMenuLevel1Items[MENULEVEL1_ITEM_IPTV].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_iptv_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_IPTV].MenuText = getResources()
//				.getString(R.string.menulevel1_item_iptv);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_VARIETY].ColumnId = "152";
		mMenuLevel1Items[MENULEVEL1_ITEM_VARIETY].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_variety);
		mMenuLevel1Items[MENULEVEL1_ITEM_VARIETY].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_variety_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_VARIETY].MenuText = getResources()
				.getString(R.string.menulevel1_item_variety);
		
		
//		mMenuLevel1Items[MENULEVEL1_ITEM_HEALTHY].ColumnId = "161";
//		mMenuLevel1Items[MENULEVEL1_ITEM_HEALTHY].MenuIcon = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_healthy);
//		mMenuLevel1Items[MENULEVEL1_ITEM_HEALTHY].MenuIconFocused = 
//				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_healthy_focused);
//		mMenuLevel1Items[MENULEVEL1_ITEM_HEALTHY].MenuText = getResources()
//				.getString(R.string.menulevel1_item_healthy);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_TRAILER].ColumnId = "156";
		mMenuLevel1Items[MENULEVEL1_ITEM_TRAILER].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_trailer);
		mMenuLevel1Items[MENULEVEL1_ITEM_TRAILER].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_trailer_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_TRAILER].MenuText = getResources()
				.getString(R.string.menulevel1_item_trailer);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_CHILDREN].ColumnId = "0";
		mMenuLevel1Items[MENULEVEL1_ITEM_CHILDREN].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_children);
		mMenuLevel1Items[MENULEVEL1_ITEM_CHILDREN].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_children_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_CHILDREN].MenuText = getResources()
				.getString(R.string.menulevel1_item_children);
		
		mMenuLevel1Items[MENULEVEL1_ITEM_SETTINGS].ColumnId = "0";
		mMenuLevel1Items[MENULEVEL1_ITEM_SETTINGS].MenuIcon = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_settings);
		mMenuLevel1Items[MENULEVEL1_ITEM_SETTINGS].MenuIconFocused = 
				BitmapFactory.decodeResource(getResources(), R.drawable.menulevel1_item_settings_focused);
		mMenuLevel1Items[MENULEVEL1_ITEM_SETTINGS].MenuText = getResources()
				.getString(R.string.menulevel1_item_settings);
		
		mMenuLevel1ItemsCount = MENULEVEL1_ITEM_COUNT * 2;

		Yuan = getResources().getString(R.string.string_yuan);
		Degree = getResources().getString(R.string.string_degree);

		mSubMenuArray = new Vector<Stack<MenuItem>>(mMenuLevel1Items.length);

		for (int i = 0; i < mMenuLevel1Items.length; i++) {
			Stack<MenuItem> menuStack = new Stack<MenuItem>();
			mSubMenuArray.add(menuStack);
		}

		mLeftArrow = (ImageView) findViewById(R.id.main_menu_left_arrow);
		mRightArrow = (ImageView) findViewById(R.id.main_menu_right_arrow);

		mShowHighlightAnimation = (AnimationSet) AnimationUtils.loadAnimation(
				this, R.anim.show_highlight_animation);
		mLeftArrowAnimation = (AnimationSet) AnimationUtils.loadAnimation(this,
				R.anim.left_arrow_animation);
		mRightArrowAnimation = (AnimationSet) AnimationUtils.loadAnimation(
				this, R.anim.right_arrow_animation);

		// Calendar View
		mTimeView = (TextView) findViewById(R.id.time_view);
		mDateView = (TextView) findViewById(R.id.date_view);
		mWeekView = (TextView) findViewById(R.id.week_view);
		
		// Location
		mCityView = (TextView) findViewById(R.id.city_view);

		// Weather View
		mWeatherView = (TextView) findViewById(R.id.wheather_wheather_view);
		mTemperatureView = (TextView) findViewById(R.id.wheather_temperature_view);
		mWeatherPictureView = (ImageView) findViewById(R.id.weather_picture);

		// Power View
		mPowerUsedDegreeView = (TextView) findViewById(R.id.mypower_degree);
		mPowerUsedCostView = (TextView) findViewById(R.id.mypower_cost);
		mPowerUsedPanelText = (TextView) findViewById(R.id.mypower_paneltext);
		mPanelPointer = (ImageView) findViewById(R.id.mypower_pointer);

		mVideoView = (GDVideoView) findViewById(R.id.player_view);

		View highlightView = (View) findViewById(R.id.highlight_item);
		mMenuLevel1SelectionText = (TextView) highlightView
				.findViewById(R.id.item_text);
		mMenuLevel1SelectionIcon = (ImageView) highlightView
				.findViewById(R.id.item_icon);

		mMenuLevel1 = (GDMenuGallery) findViewById(R.id.menu_level_1);
		mMenuLevel1Adapter = new MenuLevel1Adapter(this);
		mMenuLevel1.setAdapter(mMenuLevel1Adapter);

		mMenuLevel1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "menu level 1 selected = " + position);

				mMenuLevel1SelectionIndex = mMenuLevel1
						.getSelectedItemPosition() - START_ITEM_INDEX;
				
//				if (mMenuLevel1SelectionIndex < 0) {
//					mMenuLevel1SelectionIndex = mMenuLevel1SelectionIndex +  mMenuLevel1Items.length;
//				}
				
				mMenuLevel1SelectionIndex = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;
				
				showMenuLevel1Highlight(false);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "menu level 1 no selection");
			}
		});

		mMenuLevel1.setMostLeftItem(START_ITEM_INDEX);

		mMenuLevel2 = (ListView) findViewById(R.id.menu_level_2);
		mMenuLevel2Adapter = new MenuLevel2Adapter(this);
		mMenuLevel2.setAdapter(mMenuLevel2Adapter);
		mMenuLevel2.setDrawSelectorOnTop(false);
		mMenuLevel2.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "menu level 2  selected = " + position);

				int index = mMenuLevel2.getSelectedItemPosition();
				MenuItem menuItem = getCurrentSubMenu();
				if (menuItem != null) {
					if (menuItem.SelectedItemIndex != index) {
						menuItem.SelectedItemIndex = index;
					}
				}

				showMenuPath();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "menu level 2 no selection");
			}
		});

		// start background engines
		mCelanderThread = new GDCelanderThread(this, mTimeView, mDateView,
				mWeekView);
		mCelanderThread.start();

		mMenuLevel1.setSelection(START_ITEM_INDEX, true);
		
		//mDefaultPoster
		Drawable d = new BitmapDrawable(getResources(), mDefaultPoster);
		mVideoView.setBackgroundDrawable(d);
		
		mGDMediaScheduler = new GDMediaScheduler(this, mVideoView);
		mPowerController = new GDPowerUsageController(mUIUpdateHandler);
		mWeatherController = new GDWeatherController(mUIUpdateHandler);
		
	}

	public void onServiceStart() {
		super.onServiceStart();

		mService.registerAppObserver(this);

		startEngine();
		
		initializeData();

		updatePowerView(mPowerConsumption, mPowerCost);
		updateLocationView(getResources().getString(R.string.demo_string_city));
		updateWheatherView(getResources().getString(R.string.demo_string_wheather),
				getResources().getString(R.string.demo_string_temperature),
				mWeatherImages[0]);
		
		List<String> pushedMessage = new ArrayList<String>();
		mService.getPushedMessage(pushedMessage);
		for (int i=0 ; i<pushedMessage.size() ; i++) {
			mMarqeeView.addText(pushedMessage.get(i));
		}
		
		//mMarqeeView.setText(marqeeText);
//		mMarqeeView.addText("俄罗斯军工新闻网、美国《战略之页》1");
//		mMarqeeView.addText("俄罗斯军工新闻网、美国《战略之页》2");
//		mMarqeeView.addText("俄罗斯军工新闻网、美国《战略之页》3");
		//mMarqeeView.addText("【搜狐数码】诺基亚与微软合作推出的Lumia系列产品在今年第二季度取得了不错的销量，不过随后却由于不支持升级Windows Phone8系统而饱受诟病。");
		//mMarqeeView.addText("【搜狐数码】诺基亚与微软合作推出的Lumia系列产品在今年第二季度取得了不错的销量，不过随后却由于不支持升级Windows Phone8系统而饱受诟病。今天来自国外媒体消息诺基亚将会与微软联合与9月5日在美国举办发布会，很可能是公布最新的Windows Phone 8手机。");
		//mMarqeeView.addText("俄罗斯军工新闻网、美国《战略之页》等媒体揣测，中国开发出一款新型空射战术导弹，其性能不逊于美国著名的“地狱火”导弹。这款“中国版地狱火”令解放军无人机部队如虎添翼。美军不得不调整其空中装备的发展计划，开发新型战斗机与之进行对抗。");
				
	}
	
	public void onServiceStop() {
		super.onServiceStop();
		
		mService.unRegisterAppObserver(this);
	}

	private void startEngine() {
		mGDMediaScheduler.start(mService);
		mPowerController.start(mService);
		//mWeatherController.start(mService);
	}
	
	private void stopEngine() {
//		mGDMediaScheduler.start(mService);
//		mPowerController.start(mService);
		//mWeatherController.start(mService);
	}
	
	private void initializeData() {
		//showLoadingDialog();
		
		Stack<MenuItem> menuStack = null;
		MenuItem menuItem = null;
		ColumnData[] columns = null;
		ColumnItem[] columnItems = null;
		
		// GuoWang
		menuItem = new MenuItem();
		columns = new ColumnData[2];
		for(int i=0 ; i<columns.length ; i++) {
			columns[i] = new ColumnData();
		}
		
		columns[0].Name = getResources().getString(R.string.menulevel2_item_guowangkuaixun);
		columns[1].Name = getResources().getString(R.string.menulevel2_item_shipindongtai);
		columns[1].Id = "250";
		//columns[2].Name = getResources().getString(R.string.menulevel2_item_guojiadianwangbao);

		columnItems = new ColumnItem[columns.length];
		for (int i=0 ; i<columnItems.length ; i++) {
			columnItems[i] = new ColumnItem();
			columnItems[i].Level = COLUMN_LEVEL_2;
			columnItems[i].HasSubColumns = ColumnItem.NO_SUBCOLUMNS;
			columnItems[i].SubItems = null;
			
			columnItems[i].Column = columns[i];
		}
		
		menuItem.Items = columnItems;
		menuItem.SelectedItemIndex = 0;
		
		menuStack = mSubMenuArray.get(MENULEVEL1_ITEM_GUOWANG);
		menuStack.add(menuItem);
		
		
		// Smart Power
		menuItem = new MenuItem();
		columns = new ColumnData[3];
		for(int i=0 ; i<columns.length ; i++) {
			columns[i] = new ColumnData();
		}
		
		columns[0].Name = getResources().getString(R.string.menulevel2_item_wodeyongdian);
		columns[1].Name = getResources().getString(R.string.menulevel2_item_yongdianmingxi);
		columns[2].Name = getResources().getString(R.string.menulevel2_item_haongnengfenxi);
		//columns[3].Name = getResources().getString(R.string.menulevel2_item_jienengchangshi);
		//columns[4].Name = getResources().getString(R.string.menulevel2_item_yongidantiyan);

		columnItems = new ColumnItem[columns.length];
		for (int i=0 ; i<columnItems.length ; i++) {
			columnItems[i] = new ColumnItem();
			columnItems[i].Level = COLUMN_LEVEL_2;
			columnItems[i].HasSubColumns = ColumnItem.NO_SUBCOLUMNS;
			columnItems[i].SubItems = null;
			
			columnItems[i].Column = columns[i];
		}
		
		menuItem.Items = columnItems;
		menuItem.SelectedItemIndex = 0;
		
		menuStack = mSubMenuArray.get(MENULEVEL1_ITEM_SMARTPOWER);
		menuStack.add(menuItem);
		
		// Smart Home
		menuItem = new MenuItem();
		columns = new ColumnData[3];
		for(int i=0 ; i<columns.length ; i++) {
			columns[i] = new ColumnData();
		}
		columns[0].Name = getResources().getString(R.string.menulevel2_item_wodedianqi);
		columns[1].Name = getResources().getString(R.string.menulevel2_item_yijiankongzhi);
		columns[2].Name = getResources().getString(R.string.menulevel2_item_dingshirenwu);
		
		columnItems = new ColumnItem[columns.length];
		for (int i=0 ; i<columnItems.length ; i++) {
			columnItems[i] = new ColumnItem();

			columnItems[i].Level = COLUMN_LEVEL_2;
			columnItems[i].HasSubColumns = ColumnItem.NO_SUBCOLUMNS;
			columnItems[i].SubItems = null;
			
			columnItems[i].Column = columns[i];
		}
		
		menuItem.Items = columnItems;
		menuItem.SelectedItemIndex = 0;
		
		menuStack = mSubMenuArray.get(MENULEVEL1_ITEM_SMARTHOME);
		menuStack.add(menuItem);
		

		// Settings
		menuItem = new MenuItem();
		columns = new ColumnData[6];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = new ColumnData();
		}
		columns[0].Name = getResources().getString(
				R.string.systemsettings_userinfo);
		columns[1].Name = getResources().getString(
				R.string.systemsettings_receivestatus);
		columns[2].Name = getResources().getString(
				R.string.systemsettings_diskspace);
//		columns[3].Name = getResources().getString(
//				R.string.systemsettings_settings);
		columns[3].Name = getResources().getString(
				R.string.systemsettings_guodian);
		columns[4].Name = getResources().getString(
				R.string.systemsettings_filebrowser);
		columns[5].Name = getResources().getString(
				R.string.systemsettings_advanced);

		columnItems = new ColumnItem[columns.length];
		for (int i = 0; i < columnItems.length; i++) {
			columnItems[i] = new ColumnItem();

			columnItems[i].Level = COLUMN_LEVEL_2;
			columnItems[i].HasSubColumns = ColumnItem.NO_SUBCOLUMNS;
			columnItems[i].SubItems = null;

			columnItems[i].Column = columns[i];
		}

		menuItem.Items = columnItems;
		menuItem.SelectedItemIndex = 0;

		menuStack = mSubMenuArray.get(MENULEVEL1_ITEM_SETTINGS);
		menuStack.add(menuItem);
		

		// not incude the last item "Settings"
		for (int i = MENULEVEL1_ITEM_HDMOVIE; i < MENULEVEL1_ITEM_COUNT - 1; i++) {
			mService.getColumns(this, COLUMN_LEVEL_1, i, mMenuLevel1Items[i].ColumnId);
		}

	}

	private void updatePowerView(String powerUsed, String cost) {
		
		if (!powerUsed.isEmpty()) {
			mPowerConsumption = powerUsed;
			
			String powerUsedDegree = getResources().getString(
					R.string.mypower_powerusage);
			mPowerUsedDegreeView.setText(powerUsedDegree + powerUsed + Degree);

			mPowerUsedPanelText.setText(powerUsed + Degree);

			float powerUsedValue = Float.valueOf(powerUsed).floatValue();
			float degree = powerUsedToDegree(powerUsedValue);
			mPanelPointer.setRotation(degree);
		}
		
		if (!cost.isEmpty()) {
			mPowerCost = cost;
			
			String powerUsedCost = getResources().getString(
					R.string.mypower_powercost);
			mPowerUsedCostView.setText(powerUsedCost + cost + Yuan);
		}
		
	}

	private void updateLocationView(String city) {
		mCityView.setText(city);
	}

	private void updateWheatherView(String wheather, String temperature, Bitmap image) {
		mWeatherView.setText(wheather);
		mTemperatureView.setText(temperature);
		mWeatherPictureView.setImageBitmap(image);
	}

	public void updateData(int type, int columnLevel, int index, Object data) {
		Log.d(TAG, "updateData level = " + columnLevel + " index = " + index);

		if (type == GDDataProviderService.REQUESTTYPE_GETCOLUMNS) {

			MenuItem menuItem = null;

			ColumnData[] columns = (ColumnData[]) data;

			ColumnItem[] columnItems = null;
			if (columns != null && columns.length > 0) {
				columnItems = new ColumnItem[columns.length];
				for (int i = 0; i < columns.length; i++) {
					ColumnItem columnItem = new ColumnItem();
					columnItem.Column = columns[i];
					columnItem.Level = columnLevel + 1;
					columnItems[i] = columnItem;
				}

				menuItem = new MenuItem();
				menuItem.Items = columnItems;
				menuItem.SelectedItemIndex = -1;
			}

			if (columnLevel == COLUMN_LEVEL_1) {

				if (menuItem != null) {
					Stack<MenuItem> menuStack = mSubMenuArray.get(index);
					menuStack.add(menuItem);

					if (mMenuLevel1SelectionIndex == index) {
						showMenuLevel1Highlight(true);
					}
				}

				if (index == mMenuLevel1Items.length - 1) {
					hideLoadingDialog();
				}

			} else {
				hideLoadingDialog();

				int level1Index = mMenuLevel1SelectionIndex
						% mMenuLevel1Items.length;
				Stack<MenuItem> menuStack = mSubMenuArray.get(level1Index);
				MenuItem currSubMenu = menuStack.peek();
				ColumnItem parentColumnItem = currSubMenu.Items[currSubMenu.SelectedItemIndex];

				if (menuItem != null) {
					// make sure when loading new column, user can't operation
					parentColumnItem.SubItems = columnItems;
					parentColumnItem.HasSubColumns = ColumnItem.HAS_SUBCOLUMNS;

					menuStack.add(menuItem);
					menuItem.SelectedItemIndex = 0;
					mMenuLevel2.clearChoices();
					mMenuLevel2Adapter.setDataSet(columnItems);
					mMenuLevel2Adapter.notifyDataSetChanged();
					mMenuLevel2.setSelection(0);
					// if enter into sub menu, then show the new menu path.
					showMenuPath();
				} else {
					// no sub columns
					parentColumnItem.SubItems = null;
					parentColumnItem.HasSubColumns = ColumnItem.NO_SUBCOLUMNS;

					if (level1Index == mPendingAction.Level1Index
							&& parentColumnItem.Level == mPendingAction.CurrentLevel
							&& currSubMenu.SelectedItemIndex == mPendingAction.CurrentIndex) {
						handlePendingAction(mPendingAction);
					}
				}
			}

		}
	}

	private void handlePendingAction(MenuAction pendingAction) {
		onItemSelected();
	}

	public void onStart() {
		super.onStart();

		mCelanderThread.setUpdate(true);

		turnOnMarqeeView(true);
		showMarqueeView();
	}

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();

		mGDMediaScheduler.saveMediaState();
	}

	public void onStop() {
		super.onStop();

		mCelanderThread.setUpdate(false);
		
		hideMarqeeView();
	}

	public void onDestroy() {
		super.onDestroy();

		mCelanderThread.setExit(true);
		mGDMediaScheduler.stopMediaPlay();
	}

	private void showMenuPath() {
		buildMenuPath();
		String[] menuPath = mMenuPath.split(MENU_STRING_DELIMITER);
		
		if (!mShowMenuPathIsOn)
			return;

		if (mMenuPathContainer.getVisibility() != View.VISIBLE) {
			mMenuPathContainer.setVisibility(View.VISIBLE);
		}
		super.showMenuPath(menuPath);
	}

	private void buildMenuPath() {
		StringBuilder builder = new StringBuilder();

		int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;
		builder.append(mMenuLevel1Items[index].MenuText);

		if (!mMenuLevel2IsHided) {
			Stack<MenuItem> menuStack = mSubMenuArray.get(index);
			
			for (int i = 0; i < menuStack.size(); i++) {
				MenuItem menuItem = menuStack.get(i);
				
				ColumnItem[] columItems = menuItem.Items;
				ColumnItem item = columItems[menuItem.SelectedItemIndex];
				builder.append(MENU_STRING_DELIMITER);
				builder.append(item.Column.Name);
			}
		}
		mMenuPath = builder.toString();
	}

	private MenuItem getCurrentSubMenu() {
		int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;

		Stack<MenuItem> menuStack = mSubMenuArray.get(index);
		MenuItem subMenu = null;

		if (menuStack != null && menuStack.size() > 0) {
			subMenu = menuStack.peek();
		}

		return subMenu;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown " + keyCode);
		boolean ret = false;

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP: {
			MenuItem menuItem = getCurrentSubMenu();
			if (menuItem != null) {
				ColumnItem[] subItems = menuItem.Items;

				int index = mMenuLevel2.getSelectedItemPosition();
				if (index > 0) {
					index--;
					menuItem.SelectedItemIndex = index;
					mMenuLevel2.setSelection(index);
				} else {
					if (index < 0) {
						menuItem.SelectedItemIndex = subItems.length - 1;
						mMenuLevel2.setSelection(menuItem.SelectedItemIndex);
					}
				}
			}
			ret = true;
			break;
		}
		case KeyEvent.KEYCODE_DPAD_DOWN: {
			MenuItem menuItem = getCurrentSubMenu();
			if (menuItem != null) {
				int index = mMenuLevel2.getSelectedItemPosition();
				if (index < mMenuLevel2Adapter.getCount() - 1 && index >= 0) {
					index++;
					menuItem.SelectedItemIndex = index;
					mMenuLevel2.setSelection(index);
				} else {
					if (index < 0) {
						menuItem.SelectedItemIndex = 0;
						mMenuLevel2.setSelection(0);
					}
				}
			}
			ret = true;
			break;
		}
		case KeyEvent.KEYCODE_DPAD_LEFT: {
			mMoveLeft = true;
//			mMenuLevel1.moveRight();
//			mMenuLevel1.onKeyDown(keyCode, event);
			
			int selectedItemIndex = mMenuLevel1.getSelectedItemPosition();
			Log.d(TAG, "moveRight");
			int newPosition = 0;
			if (selectedItemIndex > START_ITEM_INDEX) {
				newPosition = selectedItemIndex - 1;
			} else {
				newPosition = mMenuLevel1Items.length + selectedItemIndex - 1;
			}
			mMenuLevel1.setSelection(newPosition, false);
			ret = true;
			break;
		}
		case KeyEvent.KEYCODE_DPAD_RIGHT: {
			mMoveLeft = false;
//			mMenuLevel1.moveLeft();
//			mMenuLevel1.onKeyDown(keyCode, event);

			int selectedItemIndex = mMenuLevel1.getSelectedItemPosition();
			if (selectedItemIndex >= mMenuLevel1Items.length + START_ITEM_INDEX ) {
				selectedItemIndex -= mMenuLevel1Items.length;
			}
			Log.d(TAG, "moveLeft");
			int newPosition = selectedItemIndex + 1;
			mMenuLevel1.setSelection(newPosition, false);
			
			ret = true;
			break;
		}

		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {
			ret = onItemSelected();
			break;
		}

		case KeyEvent.KEYCODE_BACK:
			ret = onBackKeyEvent();
			ret = true; // not handle back key in main view
			break;

//		case KeyEvent.KEYCODE_F1: // for debug with keyboard
//		case KeyEvent.KEYCODE_F2:
//		case KeyEvent.KEYCODE_INFO:
//		case KeyEvent.KEYCODE_NOTIFICATION:
//		case KeyEvent.KEYCODE_MENU:
//			ret = true;
//			showSettingsChooserView();
//			break;
		default:
			break;
		}

		if (!ret) {
			// some default key events, such back, home, etc.
			ret = super.onKeyDown(keyCode, event);
		}

		return ret;
	}

	private boolean onBackKeyEvent() {
		boolean ret = false;
		int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;

		Stack<MenuItem> menuStack = mSubMenuArray.get(index);
		ColumnItem[] currentSubMenuItems = null;

		if (menuStack.size() > 1) {
			ret = true;
			MenuItem menuItems = menuStack.pop();
			menuItems.SelectedItemIndex = 0; // clear the previous selection

			menuItems = menuStack.peek();
			currentSubMenuItems = menuItems.Items;
			mMenuLevel2Adapter.setDataSet(currentSubMenuItems);
			mMenuLevel2.clearChoices();
			mMenuLevel2Adapter.notifyDataSetChanged();

			mMenuLevel2.setSelection(menuItems.SelectedItemIndex);

			showMenuPath();
		} else if (menuStack.size() == 1) {
			// hide the level 2 menu
			if (!mMenuLevel2IsHided) {
				ret = true;
				hideMenuLevel2();
				showMenuPath();
			}
		} else {
			;
		}

		return ret;
	}

	class MenuAction {
		int Level1Index;
		int CurrentLevel;
		int CurrentIndex;

		MenuAction() {
			Level1Index = -1;
			CurrentLevel = -1;
			CurrentIndex = -1;
		}
	}

	MenuAction mPendingAction = new MenuAction();

	private void showMenuLevel2() {
		mMenuLevel2IsHided = false;
		mMenuLevel2.setVisibility(View.VISIBLE);
	}
	
	private void hideMenuLevel2() {
		mMenuLevel2IsHided = true;
		mMenuLevel2.setVisibility(View.GONE);
	}
	
	private boolean onItemSelected() {
		boolean ret = true;

		int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;
		Stack<MenuItem> menuStack = mSubMenuArray.get(index);
		
		MenuItem menuItem = null;
		ColumnItem[] currentSubMenuItems = null;

		if (menuStack.size() > 0) {
			menuItem = menuStack.peek();
			currentSubMenuItems = menuItem.Items;

			if (mMenuLevel2.getVisibility() != View.VISIBLE) {
				mMenuLevel2Adapter.setDataSet(currentSubMenuItems);
				mMenuLevel2Adapter.notifyDataSetChanged();
				showMenuLevel2();
				showMenuPath();
				return ret;
			}
			
		}

		if (index == MENULEVEL1_ITEM_PAPER || index == MENULEVEL1_ITEM_JOURNAL) {
			String ebookCategory = mMenuLevel1Items[index].MenuText;

			if (!ebookCategory.equals("")) {
				Intent intent = new Intent();
				intent.putExtra("category", ebookCategory);
				intent.setClass(this, GDEReaderActivity.class);
				startActivity(intent);
			}
		} else if (index == MENULEVEL1_ITEM_HDMOVIE) {
			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				int menuLevel2ItemIndex = menuItem.SelectedItemIndex;
				ColumnItem curItem = currentSubMenuItems[menuLevel2ItemIndex];

				boolean entered = false;

				if (curItem.Level == COLUMN_LEVEL_2) {

					Object item = null;

					if (curItem.HasSubColumns == ColumnItem.NONE) {
						entered = true;
						mPendingAction.Level1Index = index;
						mPendingAction.CurrentLevel = 2;
						mPendingAction.CurrentIndex = menuLevel2ItemIndex;
						showLoadingDialog();
						mService.getColumns(this, curItem.Level,
								menuLevel2ItemIndex, curItem.Column.Id);

					} else if (curItem.HasSubColumns == ColumnItem.HAS_SUBCOLUMNS) {
						entered = true;
						item = curItem.SubItems;
						MenuItem subMenuItem = new MenuItem();
						subMenuItem.Items = (ColumnItem[]) item;
						subMenuItem.SelectedItemIndex = 0;
						menuStack.add(subMenuItem);

						mMenuLevel2Adapter.setDataSet(subMenuItem.Items);
						mMenuLevel2.clearChoices();
						mMenuLevel2Adapter.notifyDataSetChanged();

						mMenuLevel2.setSelection(subMenuItem.SelectedItemIndex);

						showMenuPath();
					} else {
						entered = false;
					}
				}

				if (!entered) {
					Intent intent = new Intent();
					intent.putExtra(Content.COLUMN_ID, curItem.Column.Id);
					intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
					intent.setClass(this, GDHDMovieActivity.class);
					startActivity(intent);
				}
			}

		} else if (index == MENULEVEL1_ITEM_TV || index == MENULEVEL1_ITEM_RECORD
				|| index == MENULEVEL1_ITEM_VARIETY || index == MENULEVEL1_ITEM_3D) {

			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				int menuLevel2ItemIndex = menuItem.SelectedItemIndex;
				ColumnItem curItem = currentSubMenuItems[menuLevel2ItemIndex];

				boolean entered = false;

				if (curItem.Level == COLUMN_LEVEL_2) {

					Object item = null;

					if (curItem.HasSubColumns == ColumnItem.NONE) {
						entered = true;
						mPendingAction.Level1Index = index;
						mPendingAction.CurrentLevel = 2;
						mPendingAction.CurrentIndex = menuLevel2ItemIndex;
						showLoadingDialog();
						mService.getColumns(this, curItem.Level,
								menuLevel2ItemIndex, curItem.Column.Id);

					} else if (curItem.HasSubColumns == ColumnItem.HAS_SUBCOLUMNS) {
						entered = true;
						item = curItem.SubItems;
						MenuItem subMenuItem = new MenuItem();
						subMenuItem.Items = (ColumnItem[]) item;
						subMenuItem.SelectedItemIndex = 0;
						menuStack.add(subMenuItem);

						mMenuLevel2Adapter.setDataSet(subMenuItem.Items);
						mMenuLevel2.clearChoices();
						mMenuLevel2Adapter.notifyDataSetChanged();

						mMenuLevel2.setSelection(subMenuItem.SelectedItemIndex);
					} else {
						entered = false;
					}
				}

				if (!entered) {
					Intent intent = new Intent();
					intent.putExtra(Content.COLUMN_ID, curItem.Column.Id);
					intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
					Log.d(TAG, "menu path = " + mMenuPath);
					intent.setClass(this, GDTVActivity.class);
					startActivity(intent);
				}
			} else {
				// Has no sub-menu, only level1
				String columnId = mMenuLevel1Items[index].ColumnId;
				Intent intent = new Intent();
				intent.putExtra(Content.COLUMN_ID, columnId);
				intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
				Log.d(TAG, "menu path = " + mMenuPath);
				intent.setClass(this, GDHDMovieActivity.class);
				startActivity(intent);
			}
		} else if (index == MENULEVEL1_ITEM_IPTV) {
			
			// set android web browser
			//SharedPreferences sp = getSharedPreferences("com.android.browser.PreferenceScreen",
			//		Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			Context BrowserContext = null;
			try {
				BrowserContext = createPackageContext("com.android.browser", 
						Context.CONTEXT_IGNORE_SECURITY);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (BrowserContext != null) {
				Log.d(TAG, "get brower preferences!");
				SharedPreferences sp = BrowserContext.getSharedPreferences("lab_preferences", Context.MODE_WORLD_WRITEABLE);
//				boolean isFullScreen = sp.getBoolean("fullscreen", false);
//				Log.d(TAG, "WebBrowser Settings: full screen = " + isFullScreen);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("fullscreen", true);
				editor.commit();
			}
			
			String url = mService.getHomePage();
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_uri_browsers = Uri.parse(url);
			intent.setData(content_uri_browsers);
			intent.setClassName("com.android.browser",
					"com.android.browser.BrowserActivity");
			startActivity(intent);
		} else if (index == MENULEVEL1_ITEM_GUOWANG) {
			/*String url = mService
					.getCategoryContent(mMenuLevel1Items[index].MenuText);
//			String macAddress = mService.getMacAddress();
//			if (!macAddress.equals("")) {
//				url += "&mac=" + macAddress;
//			}
			Log.d(TAG, mMenuLevel1Items[index].MenuText + " url = " + url);
			Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.setClass(this, GDWebBrowserActivity.class);
			startActivity(intent);*/
			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				String url = null;
				int menuLevel2ItemIndex = mMenuLevel2.getSelectedItemPosition();
				if (menuLevel2ItemIndex >= 0
						&& menuLevel2ItemIndex < currentSubMenuItems.length) {
					Log.d(TAG, "menuLevel2ItemIndex = " + menuLevel2ItemIndex);
					
					if (menuLevel2ItemIndex == MENULEVEL2_GUOWANG_KUAIXUNINDEX) {
						String category = currentSubMenuItems[menuLevel2ItemIndex].Column.Name;
						url = mService.getCategoryContent(category);
						Log.d(TAG, category + " url = " + url);
						
						if (!url.equals("")) {
//							String macAddress = mService.getMacAddress();
//							if (!macAddress.equals("")) {
//								url += "&mac=" + macAddress;
//							}
							Intent intent = new Intent();
							intent.putExtra("url", url);
							intent.setClass(this, GDWebBrowserActivity.class);
							startActivity(intent);
						}
					} else if (menuLevel2ItemIndex == MENULEVEL2_GUOWANG_SHIPININDEX) {
						Intent intent = new Intent();
						intent.putExtra(Content.COLUMN_ID, currentSubMenuItems[menuLevel2ItemIndex].Column.Id);
						intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
						intent.setClass(this, GDHDMovieActivity.class);
						startActivity(intent);
					} else {
						// show baozhi
					}
				}
			}
			
		} else if (index == MENULEVEL1_ITEM_SMARTHOME) {
			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				String url = null;
				int menuLevel2ItemIndex = mMenuLevel2.getSelectedItemPosition();
				if (menuLevel2ItemIndex >= 0
						&& menuLevel2ItemIndex < currentSubMenuItems.length) {
					String category = currentSubMenuItems[menuLevel2ItemIndex].Column.Name;
					url = mService.getCategoryContent(category);
					Log.d(TAG, category + " url = " + url);
				}

				if (!url.equals("")) {
//					String macAddress = mService.getMacAddress();
//					if (!macAddress.equals("")) {
//						url += "&mac=" + macAddress;
//					}
					Intent intent = new Intent();
					intent.putExtra("url", url);
					intent.setClass(this, GDWebBrowserActivity.class);
					startActivity(intent);
				}
			}
		} else if (index == MENULEVEL1_ITEM_SMARTPOWER) {
			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				String url = null;
				int menuLevel2ItemIndex = menuItem.SelectedItemIndex;
				if (menuLevel2ItemIndex >= 0
						&& menuLevel2ItemIndex < currentSubMenuItems.length) {
					String category = currentSubMenuItems[menuLevel2ItemIndex].Column.Name;
					url = mService.getCategoryContent(category);

					Log.d(TAG, category + " url = " + url);
				}

				if (!url.equals("")) {

//					String macAddress = mService.getMacAddress();
//					if (!macAddress.equals("")) {
//						url += "&mac=" + macAddress;
//					}

					Intent intent = new Intent();
					intent.putExtra("url", url);
					intent.setClass(this, GDWebBrowserActivity.class);
					startActivity(intent);
				}
			}
		} else if (index == MENULEVEL1_ITEM_SETTINGS) { 
			//showSettingsChooserView();
			if (currentSubMenuItems != null && currentSubMenuItems.length > 0) {
				int menuLevel2ItemIndex = menuItem.SelectedItemIndex;
				if (menuLevel2ItemIndex >= 0
						&& menuLevel2ItemIndex < currentSubMenuItems.length) {
					showSettingView(menuLevel2ItemIndex);
				}
			}
		}else {
			;
		}

		return ret;
	}

	private void showMenuLevel1Highlight(boolean updateSubMenu) {
		//int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;
		int index = mMenuLevel1SelectionIndex % mMenuLevel1Items.length;
		Log.d(TAG, "hightlight=" + mMenuLevel1SelectionIndex + " text="
				+ mMenuLevel1Items[index].MenuText);

		mMenuLevel1SelectionText.setText(mMenuLevel1Items[index].MenuText);
		mMenuLevel1SelectionIcon.setImageBitmap(mMenuLevel1Items[index].MenuIconFocused);

		if (!updateSubMenu) {
			long time = AnimationUtils.currentAnimationTimeMillis();
			mShowHighlightAnimation.setStartTime(time);
			// mLeftArrowAnimation.setStartTime(time);
			// mRightArrowAnimation.setStartTime(time);

			// mMenuLevel1SelectionText.startAnimation(mShowHighlightAnimation);
			// mMenuLevel1SelectionIcon.startAnimation(mShowHighlightAnimation);

			if (mMoveLeft) {
				mLeftArrow.startAnimation(mShowHighlightAnimation);
			} else {
				mRightArrow.startAnimation(mShowHighlightAnimation);
			}
		}

		// set data set
		// TODO: change dataset for this menu item
		Stack<MenuItem> menuStack = mSubMenuArray.get(index);
		if (menuStack != null && menuStack.size() > 0) {
			MenuItem menuItem = menuStack.peek();
			if (menuItem != null) {
				ColumnItem[] subMenuItem = menuItem.Items;

				if (!mMenuLevel2IsHided) {
					if (mMenuLevel2.getVisibility() != View.VISIBLE) {
						mMenuLevel2.setVisibility(View.VISIBLE);
					}
				}
				mMenuLevel2.clearChoices();
				mMenuLevel2Adapter.setDataSet(subMenuItem);
				mMenuLevel2Adapter.notifyDataSetChanged();
				if (subMenuItem != null && subMenuItem.length > 0) {
					if (menuItem.SelectedItemIndex >= 0
							&& menuItem.SelectedItemIndex < subMenuItem.length) {
						mMenuLevel2.setSelection(menuItem.SelectedItemIndex);
					} else {
						menuItem.SelectedItemIndex = 0;
						mMenuLevel2.setSelection(0);
					}
				}
			}
		} else {
			mMenuLevel2.setVisibility(View.GONE);
			mMenuLevel2.clearChoices();
			mMenuLevel2Adapter.setDataSet(null);
			mMenuLevel2Adapter.notifyDataSetChanged();
		}
		// when select a new menu item leve1, call this
		// for level 2 item selected listener may not be called sometimes.
		// when press key up/down, the listener will be called.
		showMenuPath();
	}
	
	private Dialog mSettingsChooserDlg = null;
	private ListView mSettingChooserView = null;
	
	private void showSettingsChooserView() {
		if (mSettingsChooserDlg != null && mSettingsChooserDlg.isShowing()) {
			mSettingsChooserDlg.show();
		} else {
			showDialog(DIALOG_SETTINGSCHOOSER_ID);
		}
	}
	
	public static final int SystemSettingsUserInfo = 0;
	public static final int SystemSettingsDownloadingStatus = 1;
	public static final int SystemSettingsDiskSpace = 2;
	//public static final int SystemSettingsSettings = 3;
	public static final int SystemSettingsGuodian = 3;
	public static final int SystemSettingsFileBrowser = 4;
	public static final int SystemSettingsAdvanced = 5;
	public static final int SystemSettingsCount = 6;
	
	private void showSettingView(int settingsItemIndex) {
		if (settingsItemIndex < 0 || settingsItemIndex > SystemSettingsCount - 1) {
			return;
		}
		Log.d(TAG, "show settings view " + settingsItemIndex);
		Intent intent = null;
		
		switch (settingsItemIndex) {
		case SystemSettingsUserInfo:
			intent = new Intent();
			intent.setClass(this, GDUserInfoActivity.class);
			break;
		case SystemSettingsDownloadingStatus:
			intent = new Intent();
			intent.setClass(this, GDReceiveStatusActivity.class);
			break;
		case SystemSettingsDiskSpace:
			String disk = null;
			if (mBound && mService != null) {
				disk = mService.getStorageDisk();
			}
			
			if (disk != null) {
				intent = new Intent();
				intent.putExtra(GDCommon.KeyDisk, disk);
				intent.setClass(this, GDDiskManagmentActivity.class);
			}
			break;
//		case SystemSettingsSettings:
//			intent = new Intent();
//			intent.setClass(this, GDSystemSettingsActivity.class);
//			break;
			
//			return;
			
		case SystemSettingsGuodian:
			intent = new Intent();
			intent.setClass(this, GDGuodianSettingsActivity.class);
			break;
			
		case SystemSettingsFileBrowser:
			intent=new Intent();
			intent.setComponent(new ComponentName("com.fb.FileBrower","com.fb.FileBrower.FileBrower"));
			intent.setAction("android.intent.action.MAIN");
			break;

		case SystemSettingsAdvanced:
			intent=new Intent();
			intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings"));
			intent.setAction("android.intent.action.MAIN");
			break;
		default:
			break;
		}

		intent.putExtra(INTENT_KEY_MENUPATH, mMenuPath);
		startActivity(intent);
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_SETTINGSCHOOSER_ID:
			Log.d(TAG, "show settings dialog");
			dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.settings_launcher_view);
			dialog.setCanceledOnTouchOutside(true);
			
			mSettingChooserView = (ListView) dialog.findViewById(R.id.setting_list);
			
			ArrayList<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = null;
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_userinfo));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_receivestatus));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_diskspace));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_settings));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_guodian));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_filebrowser));
			listData.add(map);
			
			map = new HashMap<String, String>();
			map.put("ItemText", getResources().getString(R.string.systemsettings_advanced));
			listData.add(map);
			
			SimpleAdapter adapter = new SimpleAdapter(this, listData,
					R.layout.settings_chooser_item,
					new String[] {"ItemText"},
					new int[] {R.id.item_text} );
					
		    mSettingChooserView.setAdapter(adapter);
		    
		    mSettingChooserView.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					boolean ret = false;
					int action = event.getAction();
					if (action == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER: {
							int selectedSettingsItem = mSettingChooserView.getSelectedItemPosition();
							mSettingsChooserDlg.hide();
							showSettingView(selectedSettingsItem);
							ret = true;
							break;
						}

						default:
							break;
						}
					}
					return ret;
				}
			});
			
		    mSettingsChooserDlg = dialog;
			break;
		default:
			dialog = null;
			break;
		}

		return dialog;
	}

	public class MenuLevel2Adapter extends BaseAdapter {

		ColumnItem[] mDataSet = null;

		public void setDataSet(ColumnItem[] dataSet) {
			mDataSet = dataSet;
		}

		private class ItemHolder {
			TextView text;
		}

		public MenuLevel2Adapter(Context context) {
		}

		public int getCount() {
			int size = 0;

			if (mDataSet != null) {
				size = mDataSet.length;
			}

			Log.d(TAG, " MenuLevel2Adapter count = " + size);
			return size;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder = null;

			if (convertView == null) {
				holder = new ItemHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.menu_level_2_item, null);

				holder.text = (TextView) convertView
						.findViewById(R.id.item_text);

				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}

			holder.text.setText(mDataSet[position].Column.Name);

			return convertView;
		}

	}

	private int mMenuLevel1ItemsCount = 0;
	
	public class MenuLevel1Adapter extends BaseAdapter {

		Context mContext;

		class ItemHolder {
			TextView text;
			ImageView icon;
		}

		public MenuLevel1Adapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return mMenuLevel1ItemsCount;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
		
		/*public View getView(int position, View convertView, ViewGroup parent) {

			Log.d(TAG, "get position= " + position);

			if (convertView == null) {
				FrameLayout view = new FrameLayout(mContext);
				ImageView itemIcon = new ImageView(mContext);
				
				position = position % mMenuLevel1Items.length;
				
				itemIcon.setImageBitmap(mMenuLevel1Items[position].MenuIcon);
				itemIcon.setScaleType(ImageView.ScaleType.FIT_XY);

				FrameLayout.LayoutParams frameLayout = new FrameLayout.LayoutParams(
						mMenuLevel1ItemWidth, mMenuLevel1ItemHeight);
				frameLayout.gravity = Gravity.CENTER;

				view.addView(itemIcon, frameLayout);
				
				convertView = view;
			}

			return convertView;
		}*/
		
		public View getView(int position, View convertView, ViewGroup parent) {

			Log.d(TAG, "get position= " + position);

			ItemHolder holder = null;

			if (convertView == null) {
				holder = new ItemHolder();

				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater
						.inflate(R.layout.menu_level_1_item, null);
				holder.text = (TextView) convertView
						.findViewById(R.id.item_text);
				holder.icon = (ImageView) convertView
						.findViewById(R.id.item_icon);
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}
			
			position = position % mMenuLevel1Items.length;
			holder.icon.setImageBitmap(mMenuLevel1Items[position].MenuIcon);
			holder.text.setText(mMenuLevel1Items[position].MenuText);

			return convertView;
		}

	}

	@Override
	public void initializeApp() {
		Log.d(TAG, "++++++++++==========initializeApp ================  =====");
		//mGDMediaScheduler.start(mService);
		startEngine();

		initializeData();
	}
	
	public void handleNotifiy(int what, Object data) {
		if (what == GDCommon.MSG_DISK_SPACEWARNING) {
			String disk = (String)data;
			if (disk != null && !disk.isEmpty()) {
				Intent intent = new Intent();
				intent.putExtra(GDCommon.KeyDisk, disk);
				intent.setClass(this, GDDiskManagmentActivity.class);
			}
		}
	}
}
