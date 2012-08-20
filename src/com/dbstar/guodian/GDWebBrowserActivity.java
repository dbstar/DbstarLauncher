package com.dbstar.guodian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.dbstar.guodian.util.GDNetworkUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class GDWebBrowserActivity extends Activity {
	private static final String TAG = "WebBrowserActivity";

	WebView mWebView;
	WebViewClient mWVClient;
	WebChromeClient mChromeClient;
	Activity activity = this;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWVClient = new GDWebViewClient();
		mChromeClient = new GDWebChromeClient(this);
		setContentView(R.layout.webview);

		mWebView = (WebView) findViewById(R.id.web_view);
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);

		mWebView.addJavascriptInterface(new JavaScriptInterface(this),
				"Utility");

		if (settings.getBlockNetworkLoads()) {
			settings.setBlockNetworkLoads(false);
			Log.d(TAG, "setBlockNetworkLoads false");
		}

		if (settings.getBlockNetworkImage()) {
			settings.setBlockNetworkImage(false);
			Log.d(TAG, "setBlockNetworkImage false");
		}

		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
		mWebView.getSettings().setAllowContentAccess(true);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setAppCacheMaxSize(1024*1024*100);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		
		File cacheDir = getCacheDir();
		String webCacheDir = cacheDir.getAbsolutePath() + "/webcache";
		File webCache = new File(webCacheDir);
		
		if (!webCache.exists()) {
			webCache.mkdirs();
		}
		
		mWebView.getSettings().setAppCachePath(webCacheDir);
		mWebView.getSettings().setDatabaseEnabled(true);

		String webDatabaseDir = cacheDir.getAbsolutePath() + "/webdatabase";
		File webDatabase = new File(webDatabaseDir);
		
		if (!webDatabase.exists()) {
			webDatabase.mkdirs();
		}
		mWebView.getSettings().setDatabasePath(webDatabaseDir);
		mWebView.getSettings().setEnableSmoothTransition(true);
		//mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		mWebView.getSettings().setLightTouchEnabled(false);
		mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
		mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.NORMAL);
		//mWebView.getSettings().setSupportMultipleWindows(true);
		mWebView.getSettings().setUseWideViewPort(true);
		//
		// mWebView.getSettings().setHardwareAccelSkiaEnabled(true);
		//
//		mWebView.setWebViewClient(mWVClient);
//		mWebView.setWebChromeClient(mChromeClient);
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.setWebChromeClient(new WebChromeClient());

		mWebView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "onKey " + keyCode);
				boolean ret = false;
				int action = event.getAction();
				if (action == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_ESCAPE:
					case KeyEvent.KEYCODE_BACK: {
						ret = true;
						activity.onBackPressed();
						break;
					}
					default:
						break;
					}
				}
				return ret;
			}
		});
		
		//loadDefaultHomePage(mWebView);

		Intent intent = getIntent();
		String url = intent.getStringExtra("url");

		Log.d(TAG, "url = " + url);
		mWebView.loadUrl(url);
		//loadPageDelay(url);
	}

	private static final int MSG_BACKEVENT = 0;
	private static final int  MSG_LOADPAGE = 1;
	
	private static final int LOADPAGE_DELAY = 20;
	
	private void loadPageDelay(String url) {
		Message msg = mHandler.obtainMessage(MSG_LOADPAGE);
		Bundle data = new Bundle();
		data.putString("url", url);
		msg.setData(data);
		mHandler.sendMessageDelayed(msg, LOADPAGE_DELAY);
	}
	
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_BACKEVENT: {
				activity.onBackPressed();
				break;
			}
			
			case MSG_LOADPAGE:
				Bundle data = msg.getData();
				String url = (String)data.get("url");
				Log.d(TAG, "load " + url);
				mWebView.loadUrl(url);
				break;
			default:
				break;
			}
		}
	};

	public class JavaScriptInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		JavaScriptInterface(Context c) {
			mContext = c;
		}

		public String get(String key) {
			Log.d(TAG, "JavaScript get " + key);
			String value = "";
			if (key.equals("mac")) {
				value = getMacAddress();
			} else if (key.equals("back")) {
				value = "";
				Log.d(TAG, "JavaScript back");
				mHandler.sendEmptyMessage(MSG_BACKEVENT);
			}

			Log.d(TAG, "JavaScript value " + value);

			return value;
		}
	}

	private ConnectivityManager mConnectManager;
	private String mMacAddress = "";

	public String getMacAddress() {
		Log.d(TAG, "getMacAddress");
		if (mMacAddress.equals("")) {
			mConnectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			mMacAddress = GDNetworkUtil.getMacAddress(this, mConnectManager);
		}

		return mMacAddress;
	}

	
	public void loadDefaultHomePage(WebView webView) {
//	    final String mimeType = "text/html";
//	    final String encoding = "utf-8";
//	    final String html = "<h1>Header</h1><p>Custom HTML</p><p><img src=\"file:///android_asset/images/webview_background.png\" /></p>";
	    
	    //webView.loadDataWithBaseURL("", html, mimeType, encoding, "");
		webView.loadUrl("file:///android_asset/images/container_bj.jpg");
	}
}
