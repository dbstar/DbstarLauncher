package com.dbstar.guodian;

import java.util.LinkedList;
import java.util.List;

import com.dbstar.guodian.model.ContentData;
import com.dbstar.guodian.model.GDDataProviderService;
import com.dbstar.guodian.model.TV;
import com.dbstar.guodian.model.GDDVBDataContract.Content;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class GDTVActivity extends GDBaseActivity {
	private static final String TAG = "GDTVActivity";

	private static final int PAGE_ITEMS = 7;
	private static final int EPISODES_VIEW_COLUMN = 7;

	private static final int PageSize = PAGE_ITEMS;

	String mColumnId;
	List<TV[]> mPageDatas = null;
	GridView mSmallThumbnailView;
	TVAdapter mAdapter;
	EpisodesAdapter mEpisodesAdapter;

	int mSeletedItemIndex = 0;
	int mSelectedEpisodeIndex = 0;
	int mPageNumber = 0;
	int mPageCount = 0;
	boolean mReachPageEnd = false;
	int mTotalRequests;

	TextView mTVTitle;
	TextView mTVDescription;
	TextView mTVDirector;
	TextView mTVActors;
	TextView mTVType;
	TextView mTVRegion;
	ScrollView mTVInFoView;
	GridView mEpisodesView;
	TextView mPageNumberView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tv_view);

		Intent intent = getIntent();
		mColumnId = intent.getStringExtra(Content.COLUMN_ID);
		mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
		Log.d(TAG, "column id = " + mColumnId);
		Log.d(TAG, "menu path = " + mMenuPath);

		mPageDatas = new LinkedList<TV[]>();

		initializeView();
	}

	protected void initializeView() {
		super.initializeView();

		mPageNumberView = (TextView) findViewById(R.id.pageNumberView);
		mTVTitle = (TextView) findViewById(R.id.movieTitle);
		mTVDescription = (TextView) findViewById(R.id.movieDescription);
		mTVDirector = (TextView) findViewById(R.id.movieDirector);
		mTVActors = (TextView) findViewById(R.id.movieActors);
		mTVType = (TextView) findViewById(R.id.movieType);
		mTVRegion = (TextView) findViewById(R.id.movieRegion);
		mTVInFoView = (ScrollView) findViewById(R.id.movieInfoView);
		mTVInFoView.setEnabled(false);
		mEpisodesView = (GridView) findViewById(R.id.tvEpisodesListView);
		mSmallThumbnailView = (GridView) findViewById(R.id.gridview);
		mSmallThumbnailView
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {

						Log.d(TAG, "mSmallThumbnailView item " + position
								+ " seletected!");

						showSelectedTV(position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});

		mSmallThumbnailView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "mSmallThumbnailView onKey " + keyCode);
				boolean ret = false;
				int action = event.getAction();
				if (action == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT: {
						int currentItem = mSmallThumbnailView
								.getSelectedItemPosition();
						if (currentItem == 0) {
							loadPrevPage();
							ret = true;
						}
						break;
					}
					case KeyEvent.KEYCODE_DPAD_RIGHT: {
						int currentItem = mSmallThumbnailView
								.getSelectedItemPosition();
						if (currentItem == (PAGE_ITEMS - 1)) {
							loadNextPage();
							ret = true;
						}
						break;
					}

					default:
						break;
					}

				}
				return ret;
			}
		});

		/*
		 * mSmallThumbnailView.setOnFocusChangeListener(new
		 * View.OnFocusChangeListener() {
		 * 
		 * @Override public void onFocusChange(View v, boolean hasFocus) { if
		 * (hasFocus) { Log.d(TAG, "mSmallThumbnailView get focus!"); } else {
		 * Log.d(TAG, "mSmallThumbnailView lose focus!"); } } });
		 */

		mAdapter = new TVAdapter(this);
		mSmallThumbnailView.setAdapter(mAdapter);

		mEpisodesAdapter = new EpisodesAdapter(this);
		mEpisodesView.setAdapter(mEpisodesAdapter);

		mEpisodesView
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							if (mEpisodesAdapter.getCount() > 0) {
								Log.d(TAG, "mEpisodesView get focus!");
								mEpisodesView.setSelection(0);
							}
						}

					}
				});

		mEpisodesView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "mEpisodesView item " + position + " seletected!");
				mSelectedEpisodeIndex = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		mEpisodesView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "mEpisodesView onKey " + keyCode);
				boolean ret = false;
				int action = event.getAction();
				if (action == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_LEFT: {
						int currentItem = mEpisodesView
								.getSelectedItemPosition();
						if ((currentItem % EPISODES_VIEW_COLUMN) == 0
								&& currentItem > 0) {
							mEpisodesView.setSelection(currentItem - 1);
							ret = true;
						}
						break;
					}
					case KeyEvent.KEYCODE_DPAD_RIGHT: {
						int currentItem = mEpisodesView
								.getSelectedItemPosition();
						if ((currentItem % EPISODES_VIEW_COLUMN) == (EPISODES_VIEW_COLUMN - 1)
								&& currentItem < (mEpisodesAdapter.getCount() - 1)) {
							mEpisodesView.setSelection(currentItem + 1);
							ret = true;
						}
						break;
					}

					case KeyEvent.KEYCODE_ENTER:
						ret = true;
						playTV();
						break;
					}

				}
				return ret;
			}
		});

		mPageNumberView.setText(formPageText(mPageNumber));
	}

	public void onStart() {
		super.onStart();

		showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
	}

	public void onDestroy() {
		super.onDestroy();

		for (int i = 0; mPageDatas != null && i < mPageDatas.size(); i++) {
			TV[] tvs = mPageDatas.get(i);
			for (int j = 0; j < tvs.length; j++) {
				TV tv = tvs[j];
				if (tv.Thumbnail != null) {
					tv.Thumbnail.recycle();
				}
			}
		}
	}

	private void playTV() {
		Log.d(TAG, "playTV");

		TV[] tvs = mPageDatas.get(mPageNumber);
		TV tv = tvs[mSeletedItemIndex];
		String file = mService.getMediaFile(tv.Content);
		if (!file.equals("")) {
			Intent intent = new Intent();
			intent.putExtra("Uri", file);
			intent.setClass(this, GDVideoPlayer.class);
			startActivity(intent);
		}
	}

	private void showSelectedTV(int position) {
		Log.d(TAG, "showSelectedTV " + position);

		mSeletedItemIndex = position;

		TV[] tvs = mPageDatas.get(mPageNumber);
		TV tv = tvs[position];

		if (tv.Content.ChineseName != null) {
			mTVTitle.setText(tv.Content.ChineseName);
		}

		if (tv.Description != null) {
			mTVDescription.setText(tv.Description);
		}

		String director = mResource.HeaderDirector;
		if (tv.Content.Director != null) {
			director += tv.Content.Director;
		}
		mTVDirector.setText(director);

		String actors = mResource.HeaderActors;
		if (tv.Content.Actors != null) {
			actors += tv.Content.Actors;
		}
		mTVActors.setText(actors);
	}

	public void onServiceStart() {
		super.onServiceStart();
		fetchData(mPageNumber);
	}

	private void fetchData(int pageNumber) {
		showLoadingDialog();
		mService.getContents(this, mColumnId, pageNumber, PageSize);
	}

	public void updateData(int type, int param1, int param2, Object data) {

		if (type == GDDataProviderService.REQUESTTYPE_GETCONTENTS) {
			int pageNumber = param1;
			int pageSize = param2;
			Log.d(TAG, "updateData page number = " + pageNumber
					+ " page size = " + pageSize);

			ContentData[] contents = (ContentData[]) data;

			if (contents != null && contents.length > 0) {

				if (contents.length < pageSize) {
					mReachPageEnd = true;
				}

				mPageNumber = pageNumber;
				mPageCount++;

				mPageNumberView.setText(formPageText(mPageNumber));

				TV[] tvs = new TV[contents.length];

				for (int i = 0; i < contents.length; i++) {
					TV tv = new TV();
					tv.Episodes = 20;
					tv.Content = contents[i];
					tvs[i] = tv;
				}

				mPageDatas.add(pageNumber, tvs);

				mAdapter.setDataSet(tvs);
				mSmallThumbnailView.clearChoices();
				mSmallThumbnailView.setSelection(0);
				mAdapter.notifyDataSetChanged();

				mTotalRequests = tvs.length + tvs.length;
				for (int i = 0; i < tvs.length; i++) {
					mService.getImage(this, mPageNumber, i, contents[i]);
				}

				for (int i = 0; i < tvs.length; i++) {
					mService.getDescription(this, pageNumber, i, contents[i]);
				}

			} else {
				mReachPageEnd = true;
				hideLoadingDialog();
			}

		} else if (type == GDDataProviderService.REQUESTTYPE_GETIMAGE) {
			int pageNumber = param1;
			int index = param2;
			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
					+ index);

			TV[] tvs = mPageDatas.get(pageNumber);
			tvs[index].Thumbnail = (Bitmap) data;

			mTotalRequests--;

			if (mTotalRequests == 0) {
				hideLoadingDialog();
			}

			mAdapter.notifyDataSetChanged();
		} else if (type == GDDataProviderService.REQUESTTYPE_GETDESCRIPTION) {
			int pageNumber = param1;
			int index = param2;
			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
					+ index);

			TV[] tvs = mPageDatas.get(pageNumber);
			tvs[index].Description = (String) data;

			mTotalRequests--;

			if (index == mSmallThumbnailView.getSelectedItemPosition())
				showSelectedTV(index);

			if (mTotalRequests == 0) {
				hideLoadingDialog();
			}
		}
	}

	private void loadPrevPage() {
		if ((mPageNumber - 1) >= 0) {
			mPageNumber--;
			mPageNumberView.setText(formPageText(mPageNumber));

			TV[] tvs = mPageDatas.get(mPageNumber);
			mAdapter.setDataSet(tvs);
			mSmallThumbnailView.clearChoices();
			mSmallThumbnailView.setSelection(tvs.length - 1);
			mAdapter.notifyDataSetChanged();
		}
	}

	private void loadNextPage() {

		if ((mPageNumber + 1) < mPageCount) {
			mPageNumber++;
			mPageNumberView.setText(formPageText(mPageNumber));

			TV[] tvs = mPageDatas.get(mPageNumber);
			mAdapter.setDataSet(tvs);
			mSmallThumbnailView.clearChoices();
			mSmallThumbnailView.setSelection(0);
			mAdapter.notifyDataSetChanged();

		} else {
			if (!mReachPageEnd) {

				fetchData(mPageNumber + 1);
			}
		}
	}

	private String formEpisodesText(int num) {
		String str = new String();
		str += mResource.HanZi_Di;
		str += num + 1;
		str += mResource.HanZi_Ji;

		return str;
	}

	private class TVAdapter extends BaseAdapter {

		private TV[] mDataSet;

		public void setDataSet(TV[] dataSet) {
			mDataSet = dataSet;
		}

		private class ViewHolder {
			TextView text;
			ImageView thumbnail;
		}

		public TVAdapter(Context context) {
		}

		@Override
		public int getCount() {
			int count = 0;
			if (mDataSet != null) {
				count = mDataSet.length;
			}

			return count;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = new ViewHolder();

			if (null == convertView) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.small_thumbnail_item2,
						null);
				holder.text = (TextView) convertView
						.findViewById(R.id.ItemText);
				holder.thumbnail = (ImageView) convertView
						.findViewById(R.id.smallThumbnail);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Bitmap thumbnail = mDataSet[position].Thumbnail;
			holder.thumbnail.setImageBitmap(thumbnail);

			return convertView;
		}
	}

	private class EpisodesAdapter extends BaseAdapter {

		private class ViewHolder {
			TextView text;
		}

		public EpisodesAdapter(Context context) {
		}

		@Override
		public int getCount() {
			int count = 0;

			if (mPageDatas != null && mPageDatas.size() > 0) {
				TV[] tvs = mPageDatas.get(mPageNumber);
				int index = mSmallThumbnailView.getSelectedItemPosition();
				TV tv = tvs[index];
				count = tv.Episodes;
			}

			return count;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = new ViewHolder();

			if (null == convertView) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.tv_episode_item, null);
				holder.text = (TextView) convertView
						.findViewById(R.id.tv_episode_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(formEpisodesText(position));

			return convertView;
		}
	}
}
