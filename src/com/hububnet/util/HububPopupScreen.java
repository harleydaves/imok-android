package com.hububnet.util;

import com.hububnet.Hubub;

import android.app.Dialog;
import android.content.Context;

public class HububPopupScreen extends Dialog{

	public HububPopupScreen(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void onBackPressed (){
		Hubub.Logger("HububPopupScreen: onBackPressed...");
	}

}
