package com.hububnet.util;

import android.text.method.PasswordTransformationMethod;

public class HububPasswordBox extends HububNVPair implements HububNVPair.Listener{
	HububPasswordBoxListener _listener;
	
	public HububPasswordBox(String label){
		super(label);
		this._value.setTransformationMethod(new PasswordTransformationMethod());
	}
	
	public void setPasswordBoxListener(HububPasswordBoxListener listener){
		_listener = listener;
		super.setListener(this);
	}

	public void onUnFocus(HububNVPair nvPair) {
		if(_listener != null){
			_listener.passwordEntered(this);
		}
	}


}
