package com.hububnet.util;

import android.graphics.Typeface;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;

public class HububEmulatorParms extends HububPopup implements HububButtonListener{
	HububNVPair _authCode;	// authorization code to use a simulator on Hubub
	HububButton _enterBtn;
	String _deviceID;

	private static HububEmulatorParms _instance;
	
	private HububEmulatorParms(){
		super();
		this.setPadding(8);
		HububLabel header = new HububLabel("Emulator Parameters");
		header.setFontSize(20);
		header.setFontStyle(Typeface.BOLD_ITALIC);
		header.setAlignX(100);
		this.addVertWidget(header);

		_authCode = new HububNVPair("Authorization Code");
		this.addVertWidget(_authCode);
		
		_enterBtn = new HububButton("Enter", "enter");
		_enterBtn.setListener(this);
		this.addVertWidget(_enterBtn);
	}
	
	public static HububEmulatorParms getInstance(){
		if(_instance == null) _instance = new HububEmulatorParms();
		return _instance;
	}
	
	public void show(){
		this.edit();
		super.show();
		_authCode.setFocus();
	}
	
	public String getDeviceID(){
		return _deviceID;
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "buttonPressed...");
		String value = _authCode.getValue();
		int retcode = Hubub._AuthorizedSims.indexOf(value);
		Hubub.Debug("2", "retcode: " +retcode);
		if(retcode < 0){
			_authCode.reset();
			_authCode.setFocus();
			return;
		}
		_deviceID = "Roid:" +value;
		HububCookies.setCookie("DeviceID", _deviceID);
		HububCookies.setCookie("SimDeviceID", _deviceID);
		HububCookies.getInstance().sync();
		DroidHubub.getInstance().finishOnCreate();
		Hubub.Debug("2", "cookies: " +HububCookies.getInstance());
		this.hide();
	}
	
}
