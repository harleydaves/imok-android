package com.hububnet.util;

import com.hububnet.Hubub;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.DatePicker;

public class HububDatePicker extends DatePicker{

	public HububDatePicker(Context context) {
		super(context);
		setFocusable(true); 
        setFocusableInTouchMode(true); 
		// TODO Auto-generated constructor stub
	}
	
	public boolean onKeyPreIme (int keyCode, KeyEvent event){
		Hubub.Debug("2", "event: " +event);
		return false;
	}
	
}
