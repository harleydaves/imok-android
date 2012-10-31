package com.hububnet.util;

import com.hububnet.Hubub;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;

public class HububPinInfo extends HububWidgett implements HububButtonListener, OnClickListener{
	//String _name;
	HububPinNote _pin;	// Link back to owning pin...
	HububLabel _title;
	HububButton _call;
	HububLabel _address;
	HububLabel _address2;
	HububWidgett _header;

	public HububPinInfo(HububPinNote pin){
		super();
		this.setOnClickListener(this);
		_pin = pin;
		this.setBorder(true);
		this.setPadding(5);
		_header = new HububWidgett();
		_header.setAlignX(1);
		_title = new HububLabel(_pin.getFirstName());
		//_title.setGaps(5, 0);
		_call = new HububButton("Call", "call");
		_call.setListener(this);
		_header.addHorizWidget(_call);
		_header.addHorizWidget(_title);
		this.addVertWidget(_header);
		//_call.setVisible(false);
		_call.setVisibility(View.GONE);
		_address = new HububLabel("");
		_address.setFontSize(10);
		_address.setAlignX(1);		//_address.setPadding(2);
		this.addVertWidget(_address);
		_address2 = new HububLabel("");
		_address2.setFontSize(10);
		_address2.setAlignX(1);
		this.addVertWidget(_address2);
		_address2.setVisible(false);
		_address.setVisible(false);
	}

	public void setFirstName(String firstName){
		String title = firstName;
		if(_pin._dist != null){
			title += ": " +_pin._dist +" mi";
		}
		_title.setText(title);
	}

	public HububPinNote getPin(){
		return _pin;
	}

	public void addCallButton(){
		Hubub.Logger("HububPinInfo: this: " +this +", addCallButton...");
		_call.setVisible(true);
		if(_pin._phoneNumber == null)
			_call.setTextColor(Color.RED);
		this.prepareToDisplay();
	}

	public void addAddress(String address, String address2){
		Hubub.Logger("HububPinInfo: addAddress: address: " +address);
		HububWorking.getInstance().doneWorking();
		_address.setVisible(true);
		_address.setText(address);
		if(address2 != null){
			_address2.setVisible(true);
			_address2.setText(address2);
		}
		this.prepareToDisplay();
	}
	
	private void prepareToDisplay(){
		Hubub.Logger("HububPinInfo: this: " +this +", prepareToDisplay...");
		_address2.setVisibility(View.GONE);
		_call.setVisibility(View.GONE);
		if(_pin.getPinColor().equals("Green") && _pin.getPhoneNumber() != null)
			_call.setVisible(true);
	}

	public void reset(){
		_call.setVisibility(View.GONE);
		_address2.setVisible(false);
		_address.setVisible(false);
	}


	//protected void onUnfocus(){
	//	Hubub.Logger("HububPinInfo: onUnfocus...");
	//	_pin.setDisableFocus(false);
	//	_call.setVisible(false);
	//	_address.setVisible(false);
	//}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububPinInfo: buttonPressed...button: " +button);
		if(_pin._phoneNumber == null){
			HububAlert.getInstance().alert("This user has not registered their mobile phone number in their profile...");
		}
		else{
			String dialNumber = _pin._phoneNumber;
			if(!Hubub.HUBUBSIMULATOR && Hubub.HUBUBDEBUG)
				dialNumber = "1770-335-4975";
			HububPhone.getInstance().initiateCall(dialNumber);
		}

	}

	/* OnClickListener Protocol */
	public void onClick(View v) {
		_pin.getMapView().removePinInfo();
		
	}
}
