package com.hububnet.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.AsyncFacebookRunner;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.reg.HububRegProcess;

//import android.R;
import com.hububnet.R;
import android.graphics.Color;
import android.graphics.Typeface;
import android.R.drawable;
import android.os.Bundle;
import android.view.View;

public class HububLogin extends HububPopup implements HububPasswordBoxListener, 
	HububButtonListener, DialogListener, RequestListener, InvokerListener, HububRegProcess.Listener{
	private HububNVPair _userid;
	private HububNVPair _deviceName;
	private HububPasswordBox _passwordBox;
	private String _password;
	private HububLabel _header;
	private HububLabel _textBox;
	private HububLoginListener _listener = null;
	private int _attempts = 0;
	private boolean _userIDRequired = false;
	private boolean _deviceNameRequired = false;
	private HububButton _dismissBtn;
	private static HububLogin _instance;
	private Facebook _facebook;
	private AsyncFacebookRunner _asyncRunner;
	private HububButton _FBLogin;
	
	private String _promoOwnerID;
	private String _carrierID;
	private String _promoCode;
	private String _regUser;
	private String _carrier;
	private String _NCAuthID;
	private String _NCID;
	private String _cCode;
	private String _MobilePhone;

	private HububLogin(){
		super();
		_facebook = new Facebook(Hubub.getFBAppID());
		this.setPadding(8);
		//this.setBorder(true);
		setBackgroundColor(Color.LTGRAY);
		_userid = new HububNVPair("Userid");

		_deviceName = new HububNVPair("Device Name");
		_passwordBox = new HububPasswordBox("Password");
		_passwordBox.setPasswordBoxListener(this);
		_passwordBox.setDropKeyboardOnUnFocus(true);
		_header = new HububLabel("Connecting to IMOK");
		_header.setFontSize(20);
		_header.setFontStyle(Typeface.BOLD_ITALIC);
		_header.setAlignX(100);

		_textBox = new HububLabel();
		_textBox.setText("Please enter your...");
		_textBox.setAlignX(100);

		_dismissBtn = new HububButton("Enter ", "dismiss");
		_dismissBtn.setListener(this);
		//_dismissBtn.setGaps(20, 0);

		_FBLogin = new HububButton("", "fblogin");
		_FBLogin.setListener(this);
		_FBLogin.setBackgroundResource(R.drawable.fbconnect);

		addVertWidget(_header);
		addVertWidget(_FBLogin);
		addVertWidget(_textBox);
		addVertWidget(_deviceName);
		addVertWidget(_userid);
		addVertWidget(_passwordBox);
		addVertWidget(_dismissBtn);
		this.edit();
	}

	public static HububLogin getInstance(){
		if(_instance == null) _instance = new HububLogin();
		return _instance;
	}

	public void setUserIDRequired(boolean required){
		_userIDRequired = required;
	}

	public boolean getUserIDRequired(){
		return _userIDRequired;
	}

	public void setDeviceNameRequired(boolean required){
		_deviceNameRequired = required;
		//_deviceName.setVisible(required);
	}

	public boolean getDeviceNameRequired(){
		return _deviceNameRequired;
	}

	public void resetAttempts(){
		_attempts = 0;
	}

	public void setLoginListener(HububLoginListener listener){
		_listener = listener;
	}

	public void show(){
		Hubub.Debug("2", "_deviceNameRequired: " +_deviceNameRequired +" _userIDRequired: " +_userIDRequired +", _listener: " +_listener);
		Hubub.Debug("2", "this.getDescendantFocusability: " +this.getDescendantFocusability());
		
		_promoOwnerID = null;
		_promoCode = null;
		_carrier = null;
		_regUser = null;
		_carrierID = null;
		
		HububRegProcess regProcess = HububRegProcess.getInstance();
		regProcess.setListener(this);
		regProcess.start();
		_deviceName.reset();
		_userid.reset();
		_passwordBox.reset();
		_deviceName.setValue("MyAndroid...");
		if(1==1) return;
		String FBAccessToken = HububCookies.getCookie("FBAccessToken");
		String entityID = HububCookies.getCookie("EntityID");
		Hubub.Debug("2", "FBAccessToken: " +FBAccessToken +", entityID: " +entityID);
		String iPhoneMinRelease = HububCookies.getCookie("iPhoneMinRelease");
		if((iPhoneMinRelease != null && !iPhoneMinRelease.equals("2.112")) &&
				(entityID == null || entityID.length() == 0 || _deviceNameRequired)){
			_FBLogin.setVisible(true);
			_textBox.setText("Or, Please enter your...");
		}
		else{
			_FBLogin.setVisible(false);
			_textBox.setText("Please enter your...");
			if(FBAccessToken != null){
				this.buttonPressed(_FBLogin);
				return;
			}
		}
		String headerTxt = "Connecting to IMOK";
		//String carrierName = HububCookies.getCookie("CarrierName");
		//if(carrierName != null && !carrierName.equals("")){
		//	headerTxt += " to " +carrierName;
		//}
		headerTxt += ":";
		_header.setText(headerTxt);
		_deviceName.setVisible(false);
		_userid.setVisible(_userIDRequired);
		this.edit();
		//_userid.enableEdit(false);
		//_userid.setValue("This is a userid");
		super.show();
		//if(_deviceNameRequired){
		//	_deviceName.setFocus();
		//}
		//else 
		if(_userIDRequired){
			Hubub.Debug("2", "going to set focus to _userid...");
			_userid.setFocus();
		}
		else{
			Hubub.Debug("2", "going to set focus to _passwordBox...");
			_passwordBox.setFocus();
		}
		invalidate();
	}

	public void setText(String text){
		_textBox.setText(text);
	}

	public String getPassword(){
		return _password;
	}

	public String getUserID(){
		return _userid.getValue();
	}

	public String getDeviceName(){
		return _deviceName.getValue();
	}

	public int getAttempts(){
		return _attempts;
	}



	/* HububPasswordBoxListener Protocol */


	public void passwordEntered(HububPasswordBox passwordBox) {
		Hubub.Debug("2", "password: " +_passwordBox.getValue() +"_listener: " +_listener);
		_password = _passwordBox.getValue();
		if(_password.length() == 0) return;
		if(!_userid.getValue().startsWith("_"))	// if this is not a FB login...
				HububCookies.removeCookie("FBAccessToken");
		_passwordBox.setValue("");
		if(_deviceNameRequired && getDeviceName().length() < 1){
			HububAlert.getInstance().alert("New devices must be given a name...");
			//_deviceName.setFocus();
			return;
		}
		_attempts ++;
		if(_listener != null) _listener.loginInfoEntered(this);		
	}

	public void releaseInstance() {
		_instance = null;

	}


	/* HububButtonListener Protocol */

	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "buttonPressed...tag: " +button.getTag());
		if(button == _dismissBtn)
			this.passwordEntered(_passwordBox);
		else if(button == _FBLogin){
			this.hide();
			_facebook.authorize(DroidHubub.getInstance(), new String[] {"email", "read_stream",
				"publish_stream", "user_about_me", "user_birthday"}, -1, this);
		}
	}

	/* Facebook DialogListener Protocol */

	public void onCancel() {
		Hubub.Debug("2", "HububLogin: onCandel...");
		this.buttonPressed(_FBLogin);

	}

	public void onComplete(Bundle values) {
		String accessToken = _facebook.getAccessToken();
		Hubub.Debug("2", "HububLogin: onComplete...accessToken: " +accessToken);
		_asyncRunner = new AsyncFacebookRunner(_facebook);
		_asyncRunner.request("me", this);
	}

	public void onError(DialogError e) {
		Hubub.Debug("2", "HububLogin: onError...");
		this.buttonPressed(_FBLogin);
	}

	public void onFacebookError(FacebookError e) {
		Hubub.Debug("2", "HububLogin: onFaceBookError...");
		this.buttonPressed(_FBLogin);

	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		Hubub.Debug("2", "services: " +services);
		HububWorking.getInstance().doneWorking();
		services.getServices();
		HububService service = services.nextService();
		service.getInputs();
		String userid = service.getParm("Userid");
		String accessToken = service.getParm("AccessToken");
		service.getOutputs();
		String entityID = service.getParm("EntityID");
		Hubub.Debug("2", "entityID: " +entityID +", userid: " +userid);
		if(entityID.equals("-1")){
			try {
				_facebook.logout(DroidHubub.getInstance().getApplicationContext());
			} catch (MalformedURLException e) {
				Hubub.Debug("2", "Malformed URL: msg: " +e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Hubub.Debug("2", "IOException: msg: " +e.getMessage());
				e.printStackTrace();
			}
		}
		else{
			HububCookies.setCookie("FBAccessToken", accessToken);
		}
		_userIDRequired = true;

		// Now go ahead and log in (if you can...)
		_deviceName.setValue("MyAndroid...");
		_userid.setValue(userid);
		_passwordBox.setValue("_");
		this.passwordEntered(_passwordBox);
	}

	/* AsyncFacebookRunner.RequestListener Protocol */
	public void onComplete(String response, Object state) {
		Hubub.Debug("2", "AsyncFaceboolRunner: response: " +response.toString());
		try{
			String accessToken = _facebook.getAccessToken();
			JSONObject json = Util.parseJson(response);
			String userid = json.getString("id");
			userid = "_FB." +userid;
			Hubub.Debug("2", "accessToken: " +accessToken +", userID: " +userid);
			HububCookies.setCookie("FBAccessToken", accessToken);

			// Now have server update this user from FB
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("FBRegisterLogin");
			service.getInputs();
			service.setParm("Userid", userid);
			service.setParm("AccessToken", accessToken);
			
			if(_regUser != null) service.setParm("RegUser", _regUser);
			if(_promoOwnerID != null) service.setParm("PromoOwnerID", _promoOwnerID);
			if(_carrier != null) service.setParm("Carrier", _carrier);
			if(_promoCode != null) service.setParm("PromoCode", _promoCode);
			if(_carrierID != null) service.setParm("CarrierID", _carrierID);
			if(_NCAuthID != null) service.setParm("NCAuthID", _NCAuthID);
			if(_NCID != null) service.setParm("NCID", _NCID);
			if(_cCode != null) service.setParm("CCode", _cCode);
			if(_MobilePhone != null) service.setParm("MobilePhone", _MobilePhone);
			
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			//HububWorking.getInstance().working();
			invoker.send(services, this);
			
		}catch(Exception e){
			Hubub.Debug("1", "Exception: " +e.getMessage());
			e.printStackTrace();
		} catch (FacebookError e) {
			Hubub.Debug("1", "Error: " +e.getMessage());
			e.printStackTrace();
		}
		
	}

	public void onFacebookError(FacebookError e, Object state) {
		Hubub.Debug("2", "AsyncFaceboolRunner...");
		
	}

	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		Hubub.Debug("2", "AsyncFaceboolRunner...");
		
	}

	public void onIOException(IOException e, Object state) {
		Hubub.Debug("2", "AsyncFaceboolRunner...");
		
	}

	public void onMalformedURLException(MalformedURLException e, Object state) {
		Hubub.Debug("2", "AsyncFaceboolRunner...");
		
	}

	/* HububRegProcess.Listener Protocol */
	public void setCredentials(String userid, String password) {
		Hubub.Debug("2", "userid: " +userid +", password: " +password);
		_userid.setValue(userid);
		_passwordBox.setValue(password);
		this.passwordEntered(_passwordBox);
	}

	public void facebookSelected(String promoOwnerID, String promoCode,
			String carrier, String regUser, String carrierID, String NCAuthID, String NCID, String cCode, String mobilePhone) {
		_promoOwnerID = promoOwnerID;
		_promoCode = promoCode;
		_carrier = carrier;
		_regUser = regUser;
		_carrierID = carrierID;
		_NCAuthID = NCAuthID;
		_NCID = NCID;
		_cCode = cCode;
		_MobilePhone = mobilePhone;
		this.buttonPressed(_FBLogin);
	}

}
