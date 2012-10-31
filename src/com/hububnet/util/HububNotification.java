package com.hububnet.util;

import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;

import android.view.View;
import com.hububnet.ruok.AlertSelector;

public class HububNotification extends HububPopup implements HububButtonListener, HububTimer.Listener{
	private static HububNotification _instance;
	//HububPopup _popup;
	HububNotificationNote _note;
	HububLabel _heading;
	HububButton _deferBtn;
	boolean _loaded = false;
	HububTimer _timer;
	AlertSelector _alertSelector;
	boolean _alertSelectorVisible;

	private HububNotification(){
		super();
		_alertSelectorVisible = false;
		_alertSelector = AlertSelector.getInstance();
		//_popup = new HububPopup();
		_deferBtn = new HububButton("Defer ", "defer");
		_deferBtn.setListener(this);
		_deferBtn.setGaps(5, 5);
		String headString = HububCookies.getCookie("CarrierName");
		if(headString == null)
			headString = "";
		_heading = new HububLabel(headString + " Notification");
		addVertWidget(_heading);
		//addVertWidget(_deferBtn);
		//_deferBtn.setVisible(false);
		//_popup.add(this);
	}

	public static HububNotification getInstance(){
		if(_instance == null) _instance = new HububNotification();
		return _instance;
	}

	public void alert(HububNotificationNote note){
		Hubub.Logger("HububNotification: alert...");
		if(_timer != null) _timer.cancel();
		_timer = new HububTimer(this);
		_timer.schedule(15000);
		if(_note != null) this.removeView(_note);
		int vis = _alertSelector.getVisibility();
		_alertSelectorVisible = (vis == View.VISIBLE)?true:false;
		_note = note;
		if(_deferBtn.getParent() != null) this.removeView(_deferBtn);
		//_deferBtn.setVisible(false);
		addVertWidget(_note);
		//_deferBtn.setVisible(true);
		addVertWidget(_deferBtn);
		//_popup.show();
		this.show();
		//_deferBtn.reDrawThis();
		if(_alertSelectorVisible){
			_alertSelector.hide();
		}
		_deferBtn.setFocus();
	}
	
	public HububButton getDeferBtn(){
		return _deferBtn;
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububNotification: buttonPressed...");
		if(button == _deferBtn){
			if(_timer != null) _timer.cancel();
			//_popup.hide();
			this.hide();
			if(_alertSelectorVisible){
				_alertSelector.hide();
			}
		}
	}

	/* HububTimer.Listener Protocol */
	public void timerExpired(HububTimer timer) {
		Hubub.Logger("HububNotification: timerExpired...");
		_timer.cancel();
		_timer = null;
		//_popup.hide();
		this.hide();
	}

}
