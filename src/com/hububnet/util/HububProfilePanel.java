package com.hububnet.util;

import java.io.ByteArrayOutputStream;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;

public class HububProfilePanel extends HububTabPanel implements InvokerListener, HububButtonListener, DroidHubub.PicListener{
	private static HububProfilePanel _instance;
	private HububNVPair _name;
	private HububNVPair _address;
	private HububNVPair _phone;
	private HububNVPair _mobilePhone;
	//private HububNVPair _entityType;
	private HububImage _image;
	private HububButton _editButton;
	private HububButton _submitButton;
	private HububButton _updateProfilePic;
	private String _profilePic;
	private HububWidgett _buttons;
	private boolean _hasBeenRetrieved = false;

	private HububProfilePanel(){
		super();
		try{
			ViewGroup.LayoutParams lp = _hubPanel.getLayoutParams();
			lp.width = Hubub._DisplayMetrics.widthPixels;
			_hubPanel.setLayoutParams(lp);
			HububWidgett header = new HububWidgett();
			header.setGaps(15, 0);

			_buttons = new HububWidgett();

			_editButton = new HububButton("Edit", "edit");
			_editButton.setListener(this);


			_submitButton = new HububButton("Submit", "submit");
			_submitButton.setListener(this);

			_image = new HububImage();
			_image.setImageSize(0, Hubub.getScaledHeight(75));

			_buttons.addHorizWidget(_editButton);
			_buttons.addHorizWidget(_submitButton);
			_buttons.setGaps(8, 0);

			header.addHorizWidget(_image);
			header.addHorizWidget(_buttons);
			_hubPanel.addVertWidget(header);
			
			_updateProfilePic = new HububButton("Update Profile Picture","updateProfilePic");
			_updateProfilePic.setVisible(false);
			_updateProfilePic.setListener(this);
			_updateProfilePic.setGaps(8,0);

			_name = new HububNVPair("Name");
			_name.enableEdit(false);
			_name.setGaps(10, 0);

			_address = new HububNVPair("Address");
			//_address.enableEdit(false);
			_phone = new HububNVPair("Contact Phone");

			_mobilePhone = new HububNVPair("Mobile Phone");
			_mobilePhone.setDropKeyboardOnUnFocus(true);

			_hubPanel.addVertWidget(_updateProfilePic);
			_hubPanel.addVertWidget(_name);
			_hubPanel.addVertWidget(_address);
			_hubPanel.addVertWidget(_phone);
			_hubPanel.addVertWidget(_mobilePhone);

			_submitButton.setVisibility(View.INVISIBLE);
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}
	}

	public void onTabSelected(){
		Hubub.Logger("HububProfilePanel: onTabSelected...");
		super.onTabSelected();
		_profilePic = null;
		_mobilePhone.enableEdit(!HububPhone.getInstance().isDerPhoneNum());

		if(_hasBeenRetrieved) return;
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("SetGetEntity");
		services.setTag("Initialize");
		Invoker invoker = new Invoker();
		invoker.send(services, this);
		HububWorking.getInstance().working();
	}

	private void update(){
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("SetGetEntity");
		services.setTag("Update");
		service.getInputs();
		boolean send = false;
		if(_profilePic != null){
			service.setParm("ProfilePic", _profilePic);
			send = true;
		}
		if(_phone.hasChanged()){
			service.setParm("Phone", _phone.getValue());
			send = true;
		}
		if(_address.hasChanged()){
			service.setParm("Address", _address.getValue());
			send = true;
		}
		if(_mobilePhone.hasChanged()){
			String mobilePhone = _mobilePhone.getValue();
			mobilePhone = Hubub.cleanPhoneNumber(mobilePhone);
			if(mobilePhone.length() < 7){
				HububAlert.getInstance().alert("Mobile Phone number must be greater than 6 digits...");
				return;
			}
			service.setParm("MobilePhone", mobilePhone);
			send = true;
			
		}
		if(send){
			Invoker invoker = new Invoker();
			invoker.send(services, this);
			HububWorking.getInstance().working();
		}
		else{
			_editButton.setNameTag("Edit", "edit");
			_submitButton.setVisible(false);
			_updateProfilePic.setVisible(false);
			_hubPanel.cancelEdit();
			_editButton.setFocus();
		}
	}


	public static HububProfilePanel getInstance(){
		if(_instance == null)
			_instance = new HububProfilePanel();
		return _instance;
	}

	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububProfilePanel: butonPressed: tag: " +button.getTag());
		String tag = button.getTag();
		Hubub._InputMethodMgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
		if(button == _editButton){
			if(tag.equals("edit")){
				_editButton.setNameTag("Cancel Edit", "cancel");
				//_editButton.sizeToFit();
				_hubPanel.edit();
				_submitButton.setVisible(true);
				_updateProfilePic.setVisible(true);
				_profilePic = null;
				//invalidate();
			}
			else if(tag.equals("cancel")){
				button.setNameTag("Edit", "edit");
				_hubPanel.cancelEdit();
				_submitButton.setVisibility(View.INVISIBLE);
				_updateProfilePic.setVisible(false);
				_profilePic = null;
				_editButton.setFocus();
				//invalidate();
			}
		}
		else if(button == _submitButton){
			Hubub.Logger("HububProfilePanel: buttonPressed: submitButton tag: " +button.getTag());
			this.update();
		}
		else if(button == _updateProfilePic){
			DroidHubub.getInstance().setIntentIsActive(true);
			DroidHubub.getInstance().setPicListener(this);
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			DroidHubub.getInstance().startActivityForResult(photoPickerIntent, Hubub.SelectPicture); 

		}
	}

	@Override
	public void releaseInstance() {
		_instance = null;

	}

	/* HububInvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		Hubub.Logger("HububProfilePanel: onResponseReceived: services: " +services);
		HububWorking.getInstance().doneWorking();
		_hasBeenRetrieved = true;
		services.getServices();
		HububService hubServ = services.nextService();
		hubServ.getOutputs();
		_name.setValue(hubServ.getParm("Fname") +" " +hubServ.getParm("Lname"));
		_address.setValue(hubServ.getParm("Address"));
		_phone.setValue(hubServ.getParm("Phone"));
		_mobilePhone.setValue(hubServ.getParm("MobilePhone"));
		//HububCookies.setCookie("PhoneNum", hubServ.getParm("MobilePhone"));
		_image.setImagePath("profile." +HububCookies.getCookie("EntityID"));
		_image.reLoadImage();
		//HububCookies.getInstance().sync();
		if(services.getTag().equals("Update")){
			_editButton.setNameTag("Edit", "edit");
			_submitButton.setVisible(false);
			this._updateProfilePic.setVisible(false);
			_hubPanel.cancelEdit();
			//invalidate();
		}
		_editButton.setFocus();
		//invalidate();
	}
	
	/* DroidHubub.PicListener Protocol */
	public void savePic(Bitmap bitmap) {
		int bw = bitmap.getWidth();
		int bh = bitmap.getHeight();
		Hubub.Debug("2", "bitmap.width: " +bw +", bitmap.height: " +bh);
		// Crop to a square of the center of the picture...
		int tSize, tX, tY = 0;
		if(bw > bh){
			tSize = bh;
			tX = bw/2 - tSize/2;
			tY = 0;
		}
		else{
			tSize = bw;
			tX = 0;
			tY = bh/2 - tSize/2;
		}
		bitmap = Bitmap.createBitmap(bitmap, tX, tY, tSize, tSize);
		Hubub.Debug("2", "bitmap.width: " +bitmap.getWidth() +", bitmap.height: " +bitmap.getHeight());
		float scalefactor = 100f/(float)(bitmap.getHeight());
		int scaledHeight = (int) (bitmap.getHeight()*scalefactor);
		int scaledWidth = (int) (bitmap.getWidth()*scalefactor);
		bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);
		Hubub.Debug("2", "bitmap.width: " +bitmap.getWidth() +", bitmap.height: " +bitmap.getHeight());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		_profilePic = Base64.encodeToString(byteArray, Base64.DEFAULT);
		Hubub.Debug("2", "byteArray.length: " +byteArray.length +", _colProfilePic.length(): " +_profilePic.length());
	}
}
