package com.dbstar.guodian;

import android.os.Bundle;
import android.widget.TextView;

public class GDUserInfoActivity extends GDBaseActivity {

	private TextView mVersionView, mCardNumberView, mMacAddressView,
			mCardValidityTermView, mUerBussiness, mUserResidualAmountView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.userinfo_view);

		mVersionView = (TextView) findViewById(R.id.text_version);
		mCardNumberView = (TextView) findViewById(R.id.text_usercardnumber);
		mMacAddressView = (TextView) findViewById(R.id.text_macaddress);
		mCardValidityTermView = (TextView) findViewById(R.id.text_usercard_validityterm);
		mUerBussiness = (TextView) findViewById(R.id.text_user_bussiness);
		mUserResidualAmountView = (TextView) findViewById(R.id.text_user_residual_amount);
	}
}
