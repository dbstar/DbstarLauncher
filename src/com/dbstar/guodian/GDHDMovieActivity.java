package com.dbstar.guodian;

import java.util.LinkedList;
import java.util.List;

import com.dbstar.guodian.model.ContentData;
import com.dbstar.guodian.model.GDDataProviderService;
import com.dbstar.guodian.model.Movie;
import com.dbstar.guodian.model.GDDVBDataContract.Content;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class GDHDMovieActivity extends GDBaseActivity {
	private static final String TAG = "GDHDMovieActivity";

	private static final int DIALOG_MOVIEINFO_ID = 0;

	private static final int PAGE_ITEMS = 10;
	private static final int PageSize = PAGE_ITEMS;
	int mPageNumber = 0;
	int mPageCount = 0;

	private String mColumnId;
	private List<Movie[]> mPageDatas;

	Dialog mMovieInfoViewDlg = null;
	GridView mSmallThumbnailView;
	MovieAdapter mAdapter;
	int mSeletedItemIndex = 0;

	private boolean mReachPageEnd = false;

	TextView mPageNumberView;

	ImageView mLargeThumbnailView;
	TextView mMovieTitle;
	TextView mMovieDescription;
	TextView mMovieDirector;
	TextView mMovieActors;
	TextView mMovieType;
	TextView mMovieRegion;
	ScrollView mMovieInFoView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.hdmovie_view);

		Intent intent = getIntent();
		mColumnId = intent.getStringExtra(Content.COLUMN_ID);
		mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
		Log.d(TAG, "column id = " + mColumnId);
		Log.d(TAG, "menu path = " + mMenuPath);
		mPageDatas = new LinkedList<Movie[]>();

		initializeView();
	}

	protected void initializeView() {
		super.initializeView();

		mPageNumberView = (TextView) findViewById(R.id.pageNumberView);
		mSmallThumbnailView = (GridView) findViewById(R.id.gridview);

		mSmallThumbnailView
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						Log.d(TAG, "mSmallThumbnailView selected = " + position);

						mSeletedItemIndex = position;
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});

		mAdapter = new MovieAdapter(this);
		mSmallThumbnailView.setAdapter(mAdapter);
		mSmallThumbnailView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d(TAG, "onKey " + keyCode);
				boolean ret = false;
				int action = event.getAction();
				if (action == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {

					case KeyEvent.KEYCODE_F1: // for debug with keyboard
					case KeyEvent.KEYCODE_F2:
					case KeyEvent.KEYCODE_INFO:
					case KeyEvent.KEYCODE_NOTIFICATION:
					case KeyEvent.KEYCODE_MENU:
						ret = true;
						showMovieInfoView();
						break;

					case KeyEvent.KEYCODE_DPAD_LEFT: {
						int currentItem = mSmallThumbnailView
								.getSelectedItemPosition();
						if (currentItem == PAGE_ITEMS / 2) {
							mSmallThumbnailView.setSelection(currentItem - 1);
							ret = true;
						} else if (currentItem == 0) {
							loadPrevPage();
							ret = true;
						} else {
						}
						break;
					}
					case KeyEvent.KEYCODE_DPAD_RIGHT: {

						int currentItem = mSmallThumbnailView
								.getSelectedItemPosition();
						if (currentItem == (PAGE_ITEMS / 2 - 1)) {
							mSmallThumbnailView.setSelection(currentItem + 1);
							ret = true;
						} else if (currentItem == (PAGE_ITEMS - 1)) {
							loadNextPage();
							ret = true;
						} else {
						}
						break;
					}

					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER: {
						playMovie();
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

		mPageNumberView.setText(formPageText(mPageNumber));
	}

	public void onStart() {
		super.onStart();

		if (mAdapter.getCount() > 0) {
			mSmallThumbnailView.setSelection(mSeletedItemIndex);
		}

		showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
	}

	public void onDestroy() {
		super.onDestroy();

		for (int i = 0; mPageDatas != null && i < mPageDatas.size(); i++) {
			Movie[] movies = mPageDatas.get(i);
			for (int j = 0; j < movies.length; j++) {
				if (movies[j].Thumbnail != null) {
					movies[j].Thumbnail.recycle();
				}
			}
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_MOVIEINFO_ID:
			Log.d(TAG, "show dialog");
			dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.movie_info_view);
			dialog.setCanceledOnTouchOutside(true);

			mMovieTitle = (TextView) dialog.findViewById(R.id.movieTitle);
			mMovieDescription = (TextView) dialog
					.findViewById(R.id.movieDescription);
			mMovieDirector = (TextView) dialog.findViewById(R.id.movieDirector);
			mMovieActors = (TextView) dialog.findViewById(R.id.movieActors);
			mMovieType = (TextView) dialog.findViewById(R.id.movieType);
			mMovieRegion = (TextView) dialog.findViewById(R.id.movieRegion);

			mMovieInFoView = (ScrollView) dialog
					.findViewById(R.id.movieInfoView);

			mLargeThumbnailView = (ImageView) dialog
					.findViewById(R.id.largeThumbnail);

			mMovieInfoViewDlg = dialog;

			updateMovieInfo(getSelectedMovie());
			break;
		default:
			dialog = null;
			break;
		}

		return dialog;
	}

	public void onServiceStart() {
		super.onServiceStart();
		fetchMovieData(mPageNumber);
	}

	private void showMovieInfoView() {
		Movie movie = getSelectedMovie();

//		if (movie.Description == null) {
//			int index = mSmallThumbnailView.getSelectedItemPosition();
//			mService.getDescription(this, mPageNumber, index, movie.Content);
//		}

		if (mMovieInfoViewDlg == null) {
			showDialog(DIALOG_MOVIEINFO_ID);
		} else {
			updateMovieInfo(movie);
			mMovieInfoViewDlg.show();
		}
	}

	private void updateMovieInfo(Movie movie) {
		if (movie != null) {
			mLargeThumbnailView.setImageBitmap(movie.Thumbnail);
			if (movie.Content.ChineseName != null) {
				mMovieTitle.setText(movie.Content.ChineseName);
			}

			if (movie.Description != null) {
				mMovieDescription.setText(movie.Description);
			}

			String director = mResource.HeaderDirector;
			if (movie.Content.Director != null) {
				director += movie.Content.Director;
			}
			mMovieDirector.setText(director);

			String actors = mResource.HeaderActors;
			if (movie.Content.Actors != null) {
				actors += movie.Content.Actors;
			}
			mMovieActors.setText(actors);
		}
	}

	private void loadPrevPage() {
		if ((mPageNumber - 1) >= 0) {
			Log.d(TAG, "loadPrevPage");

			mPageNumber--;
			mPageNumberView.setText(formPageText(mPageNumber));

			Movie[] movies = mPageDatas.get(mPageNumber);
			mAdapter.setDataSet(movies);
			mSmallThumbnailView.clearChoices();
			mSmallThumbnailView.setSelection(movies.length - 1);
			mAdapter.notifyDataSetChanged();
		}
	}

	private void loadNextPage() {
		Log.d(TAG, "loadNextPage");

		if ((mPageNumber + 1) < mPageCount) {
			mPageNumber++;
			mPageNumberView.setText(formPageText(mPageNumber));

			Movie[] movies = mPageDatas.get(mPageNumber);
			mAdapter.setDataSet(movies);
			mSmallThumbnailView.clearChoices();
			mSmallThumbnailView.setSelection(0);
			mAdapter.notifyDataSetChanged();

		} else {
			if (!mReachPageEnd) {

				fetchMovieData(mPageNumber + 1);
			}
		}
	}

	private Movie getSelectedMovie() {
		int currentItem = mSmallThumbnailView.getSelectedItemPosition();
		Movie[] movies = mPageDatas.get(mPageNumber);
		return movies[currentItem];
	}

	private void playMovie() {
		Log.d(TAG, "playMovie");
		Movie movie = getSelectedMovie();
		String file = mService.getMediaFile(movie.Content);
		Log.d(TAG, "file = " + file);
		if (!file.equals("")) {
			Intent intent = new Intent();
			//intent.putExtra("Uri", file);
			//intent.setClass(this, GDVideoPlayer.class);
			
			//intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings"));
			//intent.setAction("android.intent.acction.MAIN");
			//Uri uri = Uri.parse();
			//String uriString = Uri.encode("file://" + file);
			Uri uri = Uri.parse("file://" + file);
			Log.d(TAG, "play = " + uri.toString());

			intent.setData(uri);
			intent.setComponent(new ComponentName("com.farcore.videoplayer","com.farcore.videoplayer.playermenu"));
			intent.setAction("android.intent.action.View");
			startActivity(intent);
		}
	}

	private void fetchMovieData(int pageNumber) {
		showLoadingDialog();
		mService.getContents(this, mColumnId, pageNumber, PageSize);
	}
	
	private int mRequestTaskCount = 0;

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

				Movie[] movies = new Movie[contents.length];

				for (int i = 0; i < contents.length; i++) {
					Movie movie = new Movie();
					movie.Content = contents[i];
					movies[i] = movie;
				}

				mPageDatas.add(pageNumber, movies);

				mAdapter.setDataSet(movies);
				mSmallThumbnailView.clearChoices();
				mSmallThumbnailView.setSelection(0);
				mAdapter.notifyDataSetChanged();

				for (int i = 0; i < movies.length; i++) {
					mRequestTaskCount++;
					mService.getDetailsData(this, mPageNumber, i, contents[i]);
				}

			} else {
				mReachPageEnd = true;
				hideLoadingDialog();
			}

		} else if (type == GDDataProviderService.REQUESTTYPE_GETDETAILSDATA) {
			int pageNumber = param1;
			int index = param2;
			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
					+ index);

			mService.getImage(this, mPageNumber, index, (ContentData)data);

		} else if (type == GDDataProviderService.REQUESTTYPE_GETIMAGE) {
			int pageNumber = param1;
			int index = param2;
			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
					+ index);

			Movie[] movies = mPageDatas.get(pageNumber);
			movies[index].Thumbnail = (Bitmap) data;

			mRequestTaskCount--;
			//if ((index + 1) == movies.length) {
			if (mRequestTaskCount == 0) {
				hideLoadingDialog();
			}

			mAdapter.notifyDataSetChanged();
		} else if (type == GDDataProviderService.REQUESTTYPE_GETDESCRIPTION) {
			int pageNumber = param1;
			int index = param2;
			Log.d(TAG, "updateData page number = " + pageNumber + " index = "
					+ index);

			Movie[] movies = mPageDatas.get(pageNumber);
			movies[index].Description = (String) data;

			if (mMovieInfoViewDlg.isShowing()) {
				updateMovieInfo(movies[index]);
			}
		}
	}

	private class MovieAdapter extends BaseAdapter {

		private Movie[] mDataSet = null;

		private class ViewHolder {
			TextView text;
			ImageView thumbnail;
		}

		public MovieAdapter(Context context) {
		}

		public void setDataSet(Movie[] dataSet) {
			mDataSet = dataSet;
		}

		@Override
		public int getCount() {
			int count = 0;
			if (mDataSet != null) {
				count = mDataSet.length;
			}

			Log.d(TAG, "adapter item count = " + count);

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
				convertView = inflater.inflate(R.layout.small_thumbnail_item,
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
}
