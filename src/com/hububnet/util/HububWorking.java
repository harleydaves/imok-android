package com.hububnet.util;

import com.hububnet.Hubub;

public class HububWorking extends HububPopup implements HububButtonListener{
	private static HububWorking _instance;
	HububLabel _heading;
	HububButton _button;
	int _count = 0;

	private HububWorking(){
		super();
		_heading = new HububLabel("Working...", false);
		_button = new HububButton("Dismiss", "dismiss");
		_button.setListener(this);
		_button.setGaps(20, 0);
		_button.setSingleLine(true);
		_button.setVisibility(GONE);
		this.setPosition(0, 0);
		this.setPadding(8);
		this.addVertWidget(_heading);
		this.addVertWidget(_button);
		this.setButtonVisible(false);

	}

	public static HububWorking getInstance(){
		if(_instance == null)
			_instance = new HububWorking();
		return _instance;

	}

	public void working(){
		//if(_button.isVisible()) this.delete(_button);
		this.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		if(_count == 0){
			//_button.setVisibility(GONE);
			super.show();
		}
		_count++;
	}

	public void doneWorking(){
		if(_count > 0){
			_count--;
			if(_count == 0) 
				super.hide();
		}
	}

	public void setButtonVisible(boolean visible){
		_button.setVisibility((visible)?VISIBLE:GONE);
	}


	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububWorking: buttonPressed: _count before: " +_count);
		this.doneWorking();
		Hubub.Logger("HububWorking: buttonPressed: _count after: " +_count);

	}
	
	public void releaseInstance() {
		_instance = null;
		
	}


}
