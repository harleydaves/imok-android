package com.hububnet.reg;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;

//import com.hubub.client.utils.HububCookies;
//import com.hubub.client.utils.HububEULA;
//import com.hubub.client.utils.HububSystem;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.docs.Rowset;
import com.hububnet.ruok.AlertSelector;
import com.hububnet.util.HububAlert;
import com.hububnet.util.HububButton;
import com.hububnet.util.HububChoiceBox;
import com.hububnet.util.HububDatePicker;
import com.hububnet.util.HububGPS;
import com.hububnet.util.HububImage;
import com.hububnet.util.HububLabel;
import com.hububnet.util.HububNVPair;
import com.hububnet.util.HububPasswordBox;
import com.hububnet.util.HububPhone;
import com.hububnet.util.HububWidgett;
import com.hububnet.util.HububWorking;

// Manages the user registration process on the phone...
public class HububRegProcess implements HububRegDialog.Listener, InvokerListener, DroidHubub.PicListener, View.OnKeyListener{
	private static HububRegProcess _instance;
	private HububRegDialog _lastDialog;
	private String _lastDialogName;
	private ProgressDialog _spinner;
	private static String DEFAULTEULA = "http://dl.dropbox.com/u/32826835/IMOKNet/RUOKEULA.htm";
	private HububNVPair _firstName;
	private HububNVPair _lastName;
	private HububNVPair _userID;
	private HububNVPair _email;
	private HububNVPair _repeatEmail;
	private HububNVPair _phoneNum;
	private HububChoiceBox _gender;
	private HububDatePicker _dob;
	private HububNVPair _tempPwd;
	private HububPasswordBox _permPwd;
	private HububPasswordBox _repeatPwd;
	private HububNVPair _promoCode;
	private HububNVPair _NCAuthID;
	private boolean _isActive = false;
	private Listener _listener;
	private boolean _justDoPhone;
	private boolean _selectAlarmDealer;

	// Collected info
	private Branding _colPartnerChoice;
	private String _colFirstName;
	private String _colLastName;
	private int _colBirthDay;
	private int _colBirthMonth;
	private int _colBirthYear;
	private String _colGender;
	private String _colUserID;
	private String _colEmail;
	//private String _colCountryCode;
	//private String _colPhoneNum;
	private String _colTempPwd;
	private String _colPermPwd;
	private String _colPromoCode;
	private String _colProfilePic;
	private String _colEntityID;
	private String _colNCAuthID;
	private String _colNCID;

	private String _prevPhoneNum;

	public interface Listener{
		public void setCredentials(String userid, String password);
		public void facebookSelected(String promoOwnerID, String promoCode, String carrier, 
				String regUser, String carrierID, String NCAuthID, String NCID, String cCode, String mobilePhone);
	}

	private void updatePhoneNum(String phoneNum){
		if(!phoneNum.equals(_prevPhoneNum)){
			HububCookies.setCookie("PhoneNum", phoneNum);
			if(_justDoPhone){
				HububServices services = new HububServices();
				HububService service = services.addServiceCall("SetGetEntity");
				service.getInputs();
				service.setParm("CCode", HububCookies.getCookie("CountryCode"));// value:[HububCookies getCookie:@"CountryCode"]];
				service.setParm("MobilePhone", phoneNum);
				Invoker invoker = new Invoker();
				HububWorking.getInstance().working();
				invoker.send(services, this);
			}
		}
		HububCookies.getInstance().sync();

	}

	private boolean shouldWeAskForPhone(){
		Hubub.Debug("2", "...");
		String retval = "";
		HububAlert alert = HububAlert.getInstance();
		String countryCode = HububPhone.getCountryCode();
		if(countryCode == null || countryCode.length() == 0){
			alert.removeButton();
			alert.alert("Unable to determine the country code for this phone and cannot proceed...MCC: " 
					+DroidHubub.getInstance().getResources().getConfiguration().mcc);
			return false;
		}
		String prevCountryCode = HububCookies.getCookie("CountryCode");
		String phoneNum = HububCookies.getCookie("PhoneNum");
		_prevPhoneNum = phoneNum;
		String derPhoneNum = "";
		try{
			derPhoneNum = ((TelephonyManager) DroidHubub.getInstance().
					getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		}catch(Exception e){
			derPhoneNum = "";
			Hubub.Debug("1", "TelephoneManager Error: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));

		}

		// Testing area...
		//derPhoneNum = "";
		//countryCode = "44";

		String prevDerPhoneNum = HububCookies.getCookie("DerPhoneNum");

		HububCookies.setCookie("CountryCode", countryCode);
		HububCookies.setCookie("DerPhoneNum", derPhoneNum);
		if(derPhoneNum.length() > 0){
			//if(!derPhoneNum.equals(_prevPhoneNum))
			this.updatePhoneNum(derPhoneNum);
			return false;
		}
		else{
			if(!countryCode.equals(prevCountryCode)){// ask for number...
				retval = "New country code since last time IMOK was activated...";
			}
			if(retval.length() == 0 && prevDerPhoneNum.length() > 0){ // couldn't derive PN and prev derived PN exists
				retval = "Can't derive phone number...";
			}
			if(retval.length() == 0 && phoneNum.length() == 0){ // couldn't derive PN and prev derived PN exists
				retval = "Can't derive phone number...";
			}			
		}
		return (retval.length() > 0);
	}

	private void getPartners(){
		_spinner.show();
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("SetGetSystem");
		service.getInputs();
		service.setParm("Carrier", "imok");
		service.setParm("Lat", HububGPS.getInstance().getLat());// value:[[HububGPS getInstance] getLat]];
		service.setParm("Long", HububGPS.getInstance().getLong());//  value:[[HububGPS getInstance] getLong]];

		// for testing...
		//[service setParm:@"Lat" value:@"51.531814"];
		//[service setParm:@"Long" value:@"-0.126343"];

		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);

	}


	private HububRegProcess(){
		super();
		_justDoPhone = false;
		_selectAlarmDealer = false;
		_spinner = new ProgressDialog(DroidHubub.getInstance());
		_spinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		_spinner.setMessage("Working...");
	}

	public static HububRegProcess getInstance(){
		if(_instance == null) _instance= new HububRegProcess();
		return _instance;
	}

	public void setListener(Listener listener){
		_listener = listener;
	}

	public void start(){
		_colEntityID = HububCookies.getCookie("_colEntityID");
		_colUserID = HububCookies.getCookie("_colUserID");
		_colFirstName = HububCookies.getCookie("_colFirstName");
		if(_colUserID == null){
			AlertSelector.getInstance().reset();
			HububCookies.getInstance().reset();
			this.displayDialog("IMOKID");
		}
		else{
			this.displayDialog("Password");
		}
		//this.displayDialog("Picture");
	}

	public void selectAlarmDealer(){
		_selectAlarmDealer = true;
		this.getPartners();
	}

	public void justDoPhone(){
		_justDoPhone = true;
		if(this.shouldWeAskForPhone()){
			this.displayDialog("PhoneNum");
		}
	}


	private HububRegDialog displayDialog(String dialogName){
		HububRegDialog dialog = new HububRegDialog();
		dialog.setCancelable(false);
		dialog.setTag(dialogName);

		if(dialogName.equals("PhoneNum")){
			dialog.setLButton("", "");
			dialog.setRButton("Enter", "enter");
			dialog.show();
			dialog.setTopText("Please provide your <i>Mobile Phone Number</i>, <b>without the Country Code</b>. " +
			"Leading zeros you enter will be deleted.<br><br>");
			HububWidgett widget = dialog.getWidgets();
			_phoneNum = new HububNVPair("Phone Number");
			_phoneNum.setDropKeyboardOnUnFocus(true);
			//_phoneNum.setValue(HububCookies.getCookie("PhoneNum"));
			widget.addVertWidget(_phoneNum);
			widget.align();
			widget.sizeToFit();
			widget.edit();	
			_phoneNum.setFocus();
		}

		if(dialogName.equals("IMOKorFacebook")){
			dialog.setLButton("Yes", "yes");
			dialog.setRButton("No", "no");
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RegProcess/Do%20you%20have%20a%20Facebook%20ID.htm");
		}

		else if(dialogName.equals("EULA")){
			dialog.setLButton("Accept", "accept");
			dialog.setRButton("Decline", "decline");
			dialog.setTopText("You must read and <b>ACCEPT</b> the following <i>Terms and Conditions</i> to continue...");
			//dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RUOKEULA.htm");
		}

		else if(dialogName.equals("PromoCode")){
			_colPromoCode = null;
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br>If you've been given a Promo Code, enter it here and press <i><b>Continue</b></i>. Otherwise just press <i><b>Continue</b></i>...<br><br>");
			HububWidgett widget = dialog.getWidgets();
			_promoCode = new HububNVPair("Promo Code");
			_promoCode.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_promoCode);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_promoCode.setFocus();
		}

		else if(dialogName.equals("NCAuthID")){
			_colNCAuthID = null;
			_colNCID = null;
			String desc = this._colPartnerChoice.getDesc();
			dialog.setLButton("Back", "back");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br><br><b>" +desc +"</b> requires an <b>Authorization Code</b> to proceed.<br><br>" +
					"Please enter it here and press <i><b>Continue</b></i>. " +
			"Otherwise just press <i><b>Back</b></i> to start over...<br><br>");
			HububWidgett widget = dialog.getWidgets();
			_NCAuthID = new HububNVPair("Authorization Code");
			_NCAuthID.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_NCAuthID);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_NCAuthID.setFocus();
		}

		else if(dialogName.equals("IMOKID")){
			dialog.setLButton("Yes", "yes");
			dialog.setRButton("No", "no");
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RegProcess/IMOKID.htm");
		}

		else if(dialogName.equals("DidYouRegWithFB")){
			dialog.setLButton("Yes", "yes");
			dialog.setRButton("No", "no");
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RegProcess/Did%20You%20Register%20with%20Facebook.htm");
		}

		else if(dialogName.equals("EnterUseridPW")){
			_userID = new HububNVPair("UserID");
			_userID.setDropKeyboardOnUnFocus(true);
			this._permPwd = new HububPasswordBox("Password");
			_permPwd.setDropKeyboardOnUnFocus(true);
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br><br>Enter your IMOK <b>Userid</b> and <b>Password</b>...<br><br>");
			HububWidgett widget = dialog.getWidgets();
			widget.addVertWidget(_userID);
			widget.addVertWidget(_permPwd);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_userID.setFocus();
		}

		else if(dialogName.equals("ChoosePartner")){
			this._colPartnerChoice = null;
			dialog.setLButton("", "");
			dialog.setRButton("None", "none");
			dialog.setTopText("If you are working with an IMOK partner, please select that partner from the list below. " +
			"Otherwise, select <i><b>None</b></i><br>");
		}

		else if(dialogName.equals("ConfirmPartner")){
			dialog.setLButton("Confirm", "confirm");
			dialog.setRButton("Choose Again", "choose");
			dialog.showButtons();
		}

		else if(dialogName.equals("RegSteps")){
			dialog.setLButton("Use Facebook ID", "usefacebook");
			dialog.setRButton("Continue", "continue");
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RegProcess/RegSteps.htm");
		}

		else if(dialogName.equals("Name")){
			_colFirstName = null;
			_colLastName = null;
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br><br>Please provide your <i><b>First Name</b></i> and <i><b>Last Name</b></i>:<br>");
			HububWidgett widget = dialog.getWidgets();
			_firstName = new HububNVPair("First Name");
			_firstName.setDropKeyboardOnUnFocus(true);
			_lastName = new HububNVPair("Last Name");
			_lastName.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_firstName);
			widget.addVertWidget(_lastName);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_firstName.setFocus();
		}

		else if(dialogName.equals("GenderDOB")){
			_colBirthDay = 0;
			_colBirthMonth = 0;
			_colBirthYear = 0;
			_colGender = null;
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br>Please enter your <i><b>Gender</b></i> and <i><b>Date of Birth</b></i>:<br>");
			HububWidgett widget = dialog.getWidgets();
			_gender = new HububChoiceBox();
			_gender.addItem("Gender", "gender");
			_gender.addItem("Male", "Male");
			_gender.addItem("Female", "Female");

			//DatePickerDialog dpDialog = new DatePickerDialog(DroidHubub.getInstance(), null, 2000, 1, 20);
			_dob = new HububDatePicker(DroidHubub.getInstance());
			_dob.setOnKeyListener(this);

			widget.addVertWidget(_gender);
			widget.addVertWidget(_dob);
			widget.align();
			widget.sizeToFit();
			widget.edit();
		}

		else if(dialogName.equals("Picture")){
			_colProfilePic = null;
			dialog.setLButton("Yes", "yes");
			dialog.setRButton("No", "no");
			dialog.setTopText("<br><br>Would you like to upload a profile picture for your account?<br><br>");
			dialog.showButtons();
		}

		else if(dialogName.equals("UserID")){
			_colUserID = null;
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br><br>Please enter the <i><b>UserID</b></i> you would like to use for your IMOK account, the UserID is NOT case sensitive:<br><br>");
			HububWidgett widget = dialog.getWidgets();
			_userID = new HububNVPair("UserID");
			_userID.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_userID);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_userID.setFocus();
		}
		else if(dialogName.equals("EmailAddress")){
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("<br><br>Please enter the <i><b>Email Address</b></i> you would like to use with your IMOK account:<br><br>");
			HububWidgett widget = dialog.getWidgets();
			_email = new HububNVPair("Email Address");
			_email.setDropKeyboardOnUnFocus(true);
			_repeatEmail = new HububNVPair("Repeat Email");
			_repeatEmail.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_email);
			widget.addVertWidget(_repeatEmail);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_email.setFocus();
		}

		else if(dialogName.equals("PasswordInstruct")){
			dialog.setLButton("", "");
			dialog.setRButton("Exit", "exit");
			dialog.loadURL("http://dl.dropbox.com/u/32826835/IMOKNet/RegProcess/PasswordInstruct.htm");

		}

		else if(dialogName.equals("Password")){
			_colTempPwd = null;
			_colPermPwd = null;
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.setTopText("Welcome back <b>" + _colFirstName +"</b>:<br>" +
					"Now enter the password you received in the email into the <i><b>Temp Password</b></i> box below " +
			"and then enter your <i><b>Permanent Password</b></i>. Passwords must be at least 6 characters...");
			HububWidgett widget = dialog.getWidgets();
			_tempPwd = new HububNVPair("Temp Password");
			_tempPwd.setDropKeyboardOnUnFocus(true);
			_permPwd = new HububPasswordBox("Perm Password");
			_permPwd.setDropKeyboardOnUnFocus(true);
			//_permPwd.setGaps(5,0);
			//_repeatPwd = new HububPasswordBox("Repeat Password");
			//_repeatPwd.setDropKeyboardOnUnFocus(true);
			widget.addVertWidget(_tempPwd);
			widget.addVertWidget(_permPwd);
			//widget.addVertWidget(_repeatPwd);
			widget.align();
			widget.sizeToFit();
			widget.edit();
			_tempPwd.setFocus();
		}

		else if(dialogName.equals("PasswordUpdated")){
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.showButtons();
		}

		else if(dialogName.equals("Error")){
			dialog.setLButton("", "");
			dialog.setRButton("Continue", "continue");
			dialog.showButtons();
		}

		dialog.setListener(this);
		dialog.show();
		if(_lastDialog != null){
			_lastDialog.dismiss();
			_lastDialogName = _lastDialog.getTag();
		}
		_lastDialog = dialog;
		return dialog;
	}


	public void brandingCallback(Branding branding){
		Hubub.Debug("2", "branding: " +branding);
		_colPartnerChoice = branding;
		HububRegDialog dialog = this.displayDialog("ConfirmPartner");
		dialog.setTopText("<br>You have selected: <br><br>" +
				"<b>" +branding.getDesc() +"</b><br><br>" +
		"If this is correct, press <b>Confirm</b>, otherwise <b>Choose Again</b>");
		//String eula = branding.getEULAURL();
		//if(eula == null) eula = DEFAULTEULA;
		//HububRegDialog dialog = this.displayDialog("EULA");
		//dialog.loadURL(eula);
	}

	/* HububRegDialog.Listener Protocol... process choices here..............................................
	 ***********************************************************************************************************/
	public void dialogAction(HububRegDialog regDialog, Object selectedObject) {
		String tag = regDialog.getTag();
		Hubub.Debug("2", "regDialog.getTag: " +tag + ", selectedObject: " +selectedObject);

		if(tag.equals("EULA")){
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("decline")){
				HububAlert.getInstance().alert("Your registration can not continue until you accept our terms and conditions...");
				return;
			}
			String relationship = this._colPartnerChoice.getRelationship();
			if(relationship.equals("NotificationCenter")){
				this.displayDialog("NCAuthID");
				return;
			}
			this.displayDialog("PromoCode");
		}

		else if(tag.equals("PhoneNum")){
			String phoneNum = Hubub.cleanPhoneNumber(_phoneNum.getValue());
			Hubub.Debug("2", "phoneNum: " +phoneNum);
			if(phoneNum.length() < 7){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText("<br><b>Sorry, the phone number length must be greater than 6 digits...</b>");
				return;
			}
			else{
				this.updatePhoneNum(phoneNum);
				if(_justDoPhone){
					regDialog.dismiss();
				}
				else this.getPartners();
			}
		}

		else if(tag.equals("PromoCode")){
			_colPromoCode = _promoCode.getValue();
			if(_colPromoCode.length() == 0){
				this.displayDialog("IMOKorFacebook");
				return;
			}
			_spinner.show();
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("ValidatePromoCode");
			service.getInputs();
			service.setParm("PromoCode", _colPromoCode);
			service.setParm("PromoOwnerID", _colPartnerChoice.getEntityID());
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
		}

		else if(tag.equals("NCAuthID")){
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("back")){
				_colNCID = null;
				_colNCAuthID = null;
				this.displayDialog("IMOKID");
				return;
			}
			_colNCAuthID = _NCAuthID.getValue();
			if(_colNCAuthID.length() == 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText("<br><br>Sorry, you must provide an <b>Authorization Code</b>...");
				return;

			}
			_colNCID = this._colPartnerChoice.getEntityID();
			_spinner.show();
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("ValidateNCAuthID");
			service.getInputs();
			service.setParm("NCID", _colNCID);
			service.setParm("NCAuthID", _colNCAuthID);
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);

		}

		else if(tag.equals("IMOKID")){
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("no")){
				if(this.shouldWeAskForPhone()){
					this.displayDialog("PhoneNum");
				}
				else this.getPartners();
			}
			else{
				this.displayDialog("DidYouRegWithFB");
			}

		}

		else if(tag.equals("EnterUseridPW")){
			String errors = "";
			String userID = _userID.getValue();
			String password = _permPwd.getValue();
			if(userID.length() == 0){
				errors += "<br><br><b>Sorry, you must enter a UserID...</b>";
			}
			if(password.length() == 0){
				errors += "<br><br><b>Sorry, you must enter a Password...</b>";

			}
			if(errors.length() > 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText(errors);
			}
			else{
				if(_listener != null){
					_spinner.show();
					this._lastDialog.dismiss();
					_listener.setCredentials(userID, password);
					_spinner.dismiss();
				}
			}
		}

		else if(tag.equals("DidYouRegWithFB")){
			Hubub.Debug("2", "tag: " +tag +", begin login with FB...");
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("no")){
				this.displayDialog("EnterUseridPW");
			}
			else{
				if(_listener != null){
					_lastDialog.dismiss();
					_listener.facebookSelected(null, null, null, null, null, null, null, null, null);
				}
			}
			Hubub.Debug("2", "tag: " +tag +", end login with FB...");

		}

		else if(tag.equals("ChoosePartner")){
			HububRegDialog dialog = this.displayDialog("EULA");
			dialog.loadURL(DEFAULTEULA);
		}

		else if(tag.equals("ConfirmPartner")){
			HububButton button = (HububButton)selectedObject;
			String buttonTag = button.getTag();
			if(buttonTag.equals("confirm")){
				HububRegDialog dialog = this.displayDialog("EULA");
				String eula = this._colPartnerChoice.getEULAURL();
				if(eula == null) eula = DEFAULTEULA;
				dialog.loadURL(eula);

			}
			else{
				this.getPartners();
			}
		}

		else if(tag.equals("IMOKorFacebook")){
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("no")){
				this.displayDialog("RegSteps");
			}
			else{
				if(_listener != null){
					Hubub.Debug("2", "Begin Registration using FB...");
					_lastDialog.dismiss();
					String promoCode = _colPromoCode;
					if(promoCode == null || promoCode.length() == 0) promoCode = null;
					String regUser = "Yes";
					String promoOwnerID = _colPartnerChoice.getEntityID();
					String carrier = _colPartnerChoice.getCarrier();
					String carrierID = _colPartnerChoice.getCarrierID();
					_listener.facebookSelected(promoOwnerID, promoCode, carrier, regUser, carrierID, _colNCAuthID, _colNCID,
							HububCookies.getCookie("CountryCode"), HububCookies.getCookie("PhoneNum"));
					Hubub.Debug("2", "End Registration using FB...");
				}
			}
		}

		else if(tag.equals("RegSteps")){
			HububButton button = (HububButton)selectedObject;
			if(button.getTag().equals("continue")){
				//this.displayDialog("RegSteps");
				this.displayDialog("Name");
			}
			else{
				this.displayDialog("IMOKorFacebook");
			}

		}

		else if(tag.equals("Name")){
			_colFirstName = _firstName.getValue();
			_colLastName = _lastName.getValue();
			if(_colFirstName.length() == 0 || _colLastName.length() == 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText("<b>Sorry, you must provide both a First and Last Name...</b>");
			}
			else{
				this.displayDialog("GenderDOB");
			}
		}

		else if(tag.equals("GenderDOB")){
			String error = "";
			_dob.clearFocus();
			_colGender = _gender.getCurrentTag();
			_colBirthDay = _dob.getDayOfMonth();
			_colBirthMonth = _dob.getMonth();
			_colBirthYear = _dob.getYear();
			Calendar cal = Calendar.getInstance();
			cal.set(_colBirthYear, _colBirthMonth, _colBirthDay);
			long bTime = cal.getTime().getTime();
			long currentTime = new Date().getTime();
			long threeYears = 1000L*60L*60L*24L*365L*3L;
			Hubub.Debug("2", "choice: " +_colGender +", Day: " +_colBirthDay +", Month: " +_colBirthMonth +", Year: " +_colBirthYear
					+", bTime: " +bTime +", currentTime: " +currentTime +", threeYears: " +threeYears);
			if(_colGender.equals("gender")){
				error += "<p><b>Sorry, You must specify gender...<br></b></p>";
			}
			if((currentTime - bTime) < threeYears){
				error += "<p><b>Sorry, the minimum age is 3 years...</b></p>";
			}
			if(error.length() > 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText(error);
			}
			else{
				_colBirthMonth++;	// Add one to make January have a value of 1...
				this.displayDialog("Picture");
			}
		}

		else if(tag.equals("Picture")){
			String buttonTag = ((HububButton)selectedObject).getTag();
			if(buttonTag.equals("no")){
				this.displayDialog("UserID");
				return;
			}
			this.displayDialog("UserID");
			DroidHubub.getInstance().setIntentIsActive(true);
			DroidHubub.getInstance().setPicListener(this);
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			DroidHubub.getInstance().startActivityForResult(photoPickerIntent, Hubub.SelectPicture); 
		}

		else if(tag.equals("UserID")){
			this._colUserID = _userID.getValue();
			if(_colUserID.length() == 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText("<p><b>Sorry, you must provide a UserID...</b></p>");
				return;
			}
			_spinner.show();
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("ValidateUserid");
			service.getInputs();
			service.setParm("UserID", _colUserID);
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
		}

		else if(tag.equals("EmailAddress")){
			_colEmail = _email.getValue();
			String repeatEmail = _repeatEmail.getValue();
			String errors = "";
			if(_colEmail.length() == 0){
				errors += "<br><b>Sorry, you must provide an email address...</b>";
			}
			else if(!repeatEmail.equals(_colEmail)){
				errors += "<br><b>Sorry, the two email addresses do not match...</b>";
			}
			if(errors.length() > 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText(errors);
				return;
			}
			//this.displayDialog("PasswordInstruct");
			_spinner.show();
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("ValidateEmail");
			service.getInputs();
			service.setParm("Email", this._colEmail);
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);


		}

		else if(tag.equals("PasswordInstruct")){
			Hubub.Debug("2", "tag: " +tag);
			DroidHubub.getInstance().shutDown();
		}

		else if(tag.equals("Password")){
			_colTempPwd = _tempPwd.getValue();
			_colPermPwd = _permPwd.getValue();
			//String repeatPwd = _repeatPwd.getValue();
			String error = "";
			if(_colTempPwd.length()<6){
				error += "<br><br><b>Sorry, temporary password must be at least 6 characters...</b>";

			}
			if(_colPermPwd.length() < 6){
				error += "<br><br><b>Sorry, permanent password must be at least 6 characters...</b>";
			}
			else{
				if(_colPermPwd.startsWith("hubub")){
					error += "<br><b>Sorry, you cannot begin your permanent password with those characters...</b>";
				}
				//else if(!_colPermPwd.equals(repeatPwd)){
				//	error += "<br><br><b>Sorry, the permanent password you entered was not repeated correctly. <br><br>Try again...</b>";
				//}
			}
			if(error.length() != 0){
				HububRegDialog dialog = this.displayDialog("Error");
				dialog.setTopText(error);
				return;
			}
			// Update password on server...
			_spinner.show();
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Register");
			service.setTag("UpdatePassword");
			service.getInputs();
			service.setParm("EntityID", _colEntityID);
			service.setParm("UserID", _colUserID);
			service.setParm("Password", _colPermPwd);
			service.setParm("OldPassword", _colTempPwd);
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
		}

		else if(tag.equals("PasswordUpdated")){
			Hubub.Debug("2", "tag: " +tag);
			_lastDialog.dismiss();
			if(_listener != null){
				_listener.setCredentials(_colUserID, this._colPermPwd);
			}
		}

		else if(tag.equals("Error")){
			this.displayDialog(_lastDialogName);
		}
	}
	/***********************************************************************/

	public void reset(){
		_isActive = false;
	}

	public boolean isActive(){
		return _isActive;
	}

	/******************************* InvokerListener Protocol... ***************************************************/
	public void onResponseReceived(HububServices services) {
		Hubub.Debug("2", "services: " +services);
		_spinner.dismiss();
		services.getServices();
		HububService hubServ = services.nextService();
		String serviceName = hubServ.getName();
		if(serviceName.equals("SetGetSystem")){
			hubServ.getOutputs();
			Rowset rowset = hubServ.getRowset("Dealers");
			if(rowset == null){
				Hubub.Debug("1", "rowset not here, shouldn't happen...");
				return;
			}

			HububRegDialog dialog = this.displayDialog("ChoosePartner");
			LinearLayout innerContent = dialog.getInnerContent();
			HububWidgett widgett = new HububWidgett();
			innerContent.addView(widgett);
			while(rowset.nextRow()){
				String desc = rowset.getParm("Description");
				String eulaURL = rowset.getParm("EULAURL");
				String logoURL = rowset.getParm("LogoURL");
				String relationship = rowset.getParm("Relationship");
				String entityID = rowset.getParm("EntityID");
				String carrier = rowset.getParm("Carrier");
				String carrierID = rowset.getParm("CarrierID");
				String dealer = rowset.getParm("Dealer");
				if(_selectAlarmDealer){	// Don't include NotificationCenters when selecting dealer..
					if(relationship.equals("NotificationCenter")) continue;
				}
				Hubub.Debug("2", "desc: " +desc +", EULAURL: " +eulaURL 
						+", Carrier: " +carrier +", CarrierID: " +carrierID +", Dealer: " +dealer +", EntityID: " +entityID);
				Branding branding = new Branding();
				branding.setBrandingCallback(this);
				branding.setGaps(3, 0);
				branding.setAlignX(200);
				if(dealer.equals("ruok")){
					this._colPartnerChoice = branding;
				}
				else{
					widgett.addVertWidget(branding);
				}
				//innerContent.addView(branding);
				branding.setLogoURL(logoURL);
				branding.setDesc(desc);
				branding.setRelationship(relationship);
				branding.setEntityID(entityID);
				branding.setCarrier(carrier);
				branding.setCarrierID(carrierID);
				branding.setEULAURL(eulaURL);
			}
		}
		else if(serviceName.equals("SetGetEntity")){
			HububWorking.getInstance().doneWorking();
		}
		else if(serviceName.equals("Register")){
			String tag = hubServ.getTag();
			if(tag.equals("ValidatePromoCode")){
				hubServ.getOutputs();
				String errors = hubServ.getParm("Errors");
				if(errors != null && errors.length() > 0){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText("<br><b>Promo Code Error: <br>" +errors +"</b>");
				}else{
					//if(_colPhoneNum.length() < 6){
					//	this.displayDialog("PhoneNum");
					//}
					//else{
					this.displayDialog("IMOKorFacebook");
					//}
				}
			}
			else if(tag.equals("ValidateNCAuthID")){
				hubServ.getOutputs();
				String errors = hubServ.getParm("Errors");
				if(errors != null && errors.length() > 0){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText("<br>Authorization Code Error: <br><br><b>" +errors +"</b>");
					return;
				}
				this.displayDialog("PromoCode");

			}
			else if(tag.equals("ValidateUserid")){
				hubServ.getOutputs();
				String userid = hubServ.getParm("UserID");
				if(userid != null){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText("<br><br><b>Sorry, that UserID is taken, you will need to choose another...</b>");
					return;
				}
				this.displayDialog("EmailAddress");
			}
			else if(tag.equals("ValidateEmail")){
				hubServ.getOutputs();
				String email = hubServ.getParm("Email");
				if(email != null){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText("<br><br><b>Sorry, that email address is already in the system, you will need to choose another...</b>");
					return;
				}
				Hubub.Debug("2", "_colUserID: " +_colUserID);
				_spinner.show();
				HububServices newServices = new HububServices();
				HububService service = newServices.addServiceCall("Register");
				service.setTag("register");
				service.getInputs();
				service.setParm("CarrierID", _colPartnerChoice.getCarrierID());
				service.setParm("PromoOwnerID", _colPartnerChoice.getEntityID());
				service.setParm("Fname", this._colFirstName);
				service.setParm("Lname", this._colLastName);
				service.setParm("Userid", this._colUserID);
				//service.setParm("Passwd", _regPasswd.getValue());
				service.setParm("Email", this._colEmail);
				service.setParm("Sex", this._colGender);
				service.setParm("BMonth", "" +this._colBirthMonth);
				service.setParm("BDay", "" +this._colBirthDay);
				service.setParm("BYear", "" +this._colBirthYear);
				service.setParm("DeviceID", HububCookies.getCookie("DeviceID"));
				service.setParm("Carrier", this._colPartnerChoice.getCarrier());
				//service.setParm("PhoneNum", this._colPhoneNum);
				if(_colNCID != null){
					service.setParm("NCID", _colNCID);
					service.setParm("NCAuthID", _colNCAuthID);
				}
				if(_colProfilePic != null){
					service.setParm("ProfilePic", _colProfilePic);
				}
				String promoCode = this._colPromoCode;
				if(promoCode.length() > 0){
					service.setParm("PromoCode", promoCode);
				}
				service.setParm("CCode", HububCookies.getCookie("CountryCode"));
				service.setParm("MobilePhone", HububCookies.getCookie("PhoneNum"));

				Invoker invoker = new Invoker();
				invoker.sendAsEntityID("0");
				invoker.send(newServices, this);

			}
			else if(tag.equals("register")){
				String errors = hubServ.getErrors();
				Hubub.Debug("2", "Register: register: errors: " +errors);
				if(errors.length() > 0){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText(errors);
					return;	
				}
				hubServ.getOutputs();
				String entityID = hubServ.getParm("EntityID");

				// Set these cookies so they are available on return...
				HububCookies.setCookie("_colEntityID", entityID);
				HububCookies.setCookie("_colUserID", _colUserID);
				HububCookies.setCookie("_colFirstName", _colFirstName);
				this.displayDialog("PasswordInstruct");
			}
			else if(tag.equals("UpdatePassword")){
				String errors = hubServ.getErrors();
				Hubub.Debug("2", "Register: register: errors: " +errors);
				if(errors.length() > 0){
					HububRegDialog dialog = this.displayDialog("Error");
					dialog.setTopText(errors);
					return;	
				}
				HububCookies.removeCookie("_colEntityID");
				HububCookies.removeCookie("_colUserID");
				HububCookies.removeCookie("_colFirstName");
				HububCookies.getInstance().sync();
				HububRegDialog dialog = this.displayDialog("PasswordUpdated");
				dialog.setTopText("<br><br>Welcome " +_colFirstName +":<br><br>Your permanent password is now in place. " +
						"When you are ready to set up your alert group press " +
				"<b>Continue</b><br>");
			}
		}
	}

	// Inner Classes ******************************************************************************************
	private class Branding extends HububWidgett{
		private HububImage _logo;
		private HububLabel _desc;
		private String _eulaURL;
		private String _entityID;
		private String _carrier;
		private String _carrierID;
		private String _relationship;
		private boolean _scrolled;
		private HububRegProcess _callback;

		public Branding(){
			_logo = new HububImage();
			//_logo.setImageURL(logoURL);
			int imageWidth = Hubub.getScaledWidth(70);
			_logo.setImageSize(imageWidth, imageWidth/2);
			_logo.resizeEnabled(false);
			//_logo.reLoadImage();

			_desc = new HububLabel();
			//_desc.setText(desc);
			_desc.setFontStyle(Typeface.BOLD);
			_desc.setGaps(5,0);
			this.addHorizWidget(_logo);
			this.addHorizWidget(_desc);
		}

		public void setBrandingCallback(HububRegProcess callback){
			_callback = callback;
		}

		public void setLogoURL(String logURL){
			_logo.setImageURL(logURL);
			_logo.reLoadImage();
			this.reLayoutHoriz();
		}

		public void setDesc(String desc){
			_desc.setText(desc);
			this.reLayoutHoriz();
		}

		public String getDesc(){
			return _desc.getText();
		}

		public void setEULAURL(String eulaURL){
			_eulaURL = eulaURL;
		}

		public String getEULAURL(){
			return _eulaURL;
		}

		public void setEntityID(String entityID){
			_entityID = entityID;
		}

		public String getEntityID(){
			return _entityID;
		}

		public void setCarrier(String carrier){
			_carrier = carrier;
		}

		public String getCarrier(){
			return _carrier;
		}

		public void setCarrierID(String carrierID){
			_carrierID = carrierID;
		}

		public String getCarrierID(){
			return _carrierID;
		}

		public void setRelationship(String relationship){
			_relationship = relationship;
		}

		public String getRelationship(){
			return _relationship;
		}

		public String toString(){
			return (
					"entityID: " +_entityID +", desc: " +_desc.getText() +", relationship: " +_relationship +
					", _eulaURL: " +_eulaURL);
		}

		public boolean onTouchEvent (MotionEvent event){
			int action = event.getAction();
			Hubub.Debug("2", "action: " +action + ", event: " +event);
			if(action == 2)
				_scrolled = true;
			else if((action == MotionEvent.ACTION_UP)){
				Hubub.Debug("2", "fire callback...");
				if(_callback != null) _callback.brandingCallback(this);
			}
			return true;

		}

		public boolean onInterceptTouchEvent (MotionEvent event){
			int action = event.getAction();
			Hubub.Debug("2", "action: " +action + ", event: " +event);
			if(action == MotionEvent.ACTION_DOWN){
				_scrolled = false;
			}
			return true;
		}
	}

	/*DroidHubub.PicListener Protocol */
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
		_colProfilePic = Base64.encodeToString(byteArray, Base64.DEFAULT);
		Hubub.Debug("2", "byteArray.length: " +byteArray.length +", _colProfilePic.length(): " +_colProfilePic.length());
	}

	/* View.OnKeyListener Protocol */
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Hubub.Debug("2", "keyCode: " +keyCode);
		return false;
	}


	public boolean onKeyDown(int arg0, KeyEvent arg1) {
		Hubub.Debug("2", "arg0: " +arg0);
		return false;
	}

	public boolean onKeyLongPress(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onKeyMultiple(int arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onKeyUp(int arg0, KeyEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
