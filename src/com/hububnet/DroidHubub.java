package com.hububnet;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.util.ByteArrayBuffer;

import com.google.android.maps.MapActivity;

import com.hububnet.reg.HububRegProcess;
import com.hububnet.ruok.AlertSelector;
import com.hububnet.ruok.HububEmergencyPanel;
import com.hububnet.util.HububAlert;
import com.hububnet.util.HububBanner;
import com.hububnet.util.HububButton;
import com.hububnet.util.HububButtonListener;
import com.hububnet.util.HububEmulatorParms;
import com.hububnet.util.HububGPS;
import com.hububnet.util.HububLabel;
import com.hububnet.util.HububLogin;
import com.hububnet.util.HububNotificationPanel;
import com.hububnet.util.HububPhone;
import com.hububnet.util.HububProfilePanel;
import com.hububnet.util.HububRelationshipsPanel;
import com.hububnet.util.HububSendMail;
import com.hububnet.util.HububStreamListener;
import com.hububnet.util.HububUtil;
import com.hububnet.util.HububWidgett;
import com.hububnet.util.HububWorking;
import com.google.android.c2dm.C2DMessaging;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.docs.HububXMLDoc;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DroidHubub extends MapActivity implements HububButtonListener, InvokerListener{
	private LinearLayout root;
	private static DroidHubub _instance;
	//private HububWidgett _container;

	private HububTabPanel _currentPanel;
	private HububProfilePanel _profilePanel;
	public HububNotificationPanel _notificationPanel;
	private HububEmergencyPanel _emergencyPanel;
	private HububRelationshipsPanel _relationshipsPanel;
	private AlertSelector _alertSelector;

	//private static boolean _isRunning = false;

	private PowerManager.WakeLock _wakeLock = null;

	// Option Menu Items
	private static final int OPTION_MENU_ITEM_PROFILE = 0;
	private static final int OPTION_MENU_ITEM_RUOK = 1;
	private static final int OPTION_MENU_ITEM_NOTICES = 2;
	private static final int OPTION_MUNU_ITEM_RELATIONSHIPS = 3;

	private KeyguardManager.KeyguardLock _kl;
	private boolean _intentIsActive = false;

	//public String _NCDescription;
	//public String _NCStatus;
	//public String _NCLogoURL;
	//public String _ACDescription;
	//public String _ACStatus;
	//public String _ACLogoURL;
	PicListener _picListener = null;
	public interface PicListener{
		public void savePic(Bitmap bitmap);
	}
	public void setPicListener(PicListener picListener){
		_picListener = picListener;
	}

	public DroidHubub(){
		super();
		Hubub.Debug("2", "DroidHububConstructor...");
		_instance = this;
	}

	public static DroidHubub getInstance(){
		return _instance;
	}
	
	public void setIntentIsActive(boolean intentIsActive){
		_intentIsActive = intentIsActive;
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try{
			Hubub.Debug("2", "DroidHubub: onCreate...");
			//if(_isRunning) return;
			//_isRunning = true;
			//this.getWindow().setBackgroundDrawable(this.getResources().getDrawable(android.R.color.background_dark));
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			_wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");


			super.onCreate(savedInstanceState);

			KeyguardManager km =  (KeyguardManager)this.getSystemService(Context.KEYGUARD_SERVICE);
			_kl = km.newKeyguardLock("tag");
			//HububXMLDoc.mainTest();
			//System.exit(0);

			// For testing, unregister...
			//C2DMessaging.unregister(this);


			Hubub.getInstance();	// Initialize Hubub
			HububCookies.getInstance();
			Hubub.getBaseAddress();	// Initialize communication constants..

			if(root == null){
				root = new LinearLayout(this);
				root.setOrientation(LinearLayout.VERTICAL);
				root.setBackgroundColor(Color.LTGRAY);
				Hubub.Debug("2", "DroidHubub: onCreate: root.getDescendantFocusability: " +root.getDescendantFocusability());

				HububBanner hububBanner= HububBanner.getInstance();
				root.addView(hububBanner);
			}

			HububGPS.getInstance().start();

			HububWorking working = HububWorking.getInstance();
			working.setTop(0.3);
			Hubub.Debug("2", "DroidHubub: onCreate: Working.measuredWidth: " +working.getMeasuredWidth() +", measuredHeight: " +working.getMeasuredHeight());

			setContentView(root);
			if(!Hubub.HUBUBSIMULATOR && Hubub.SIGNEDSIM){
				HububAlert.getInstance().removeButton();
				HububAlert.getInstance().alert("Your phone is not authorized to run this version, you must install IMOK from Android Market, touch 'Home' to exit...");
				//Thread.sleep(10000);
				//System.exit(0);
			}
			else {
				if(Hubub.HUBUBSIMULATOR && (HububCookies.getCookie("DeviceID") == null)){
					HububEmulatorParms.getInstance().show();
				}
				else{
					this.finishOnCreate();
				}
			}
		} catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
			if(Hubub.HUBUBSIMULATOR) System.exit(0);
		}
	}

	public void finishOnCreate(){
		try{
			Hubub.Debug("2", "...");

			HububServices services = new HububServices();
			HububService service = services.addServiceCall("Login");
			service.getInputs();
			//service.setParm("EntityID", services.getEntityID());
			Invoker invoker = new Invoker();
			String entityID = HububCookies.getCookie("EntityID");
			if(entityID!= null && entityID.length() >0)	// Do this only if this is not a fresh install of the App.
				invoker.sendAsEntityID("0");
			invoker.send(services, this);
			HububWorking.getInstance().working();	
			this.continueInitialization();

		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		Hubub.Debug("2", "DroidHubub: finishOnCreate: end...");
	}

	private void continueInitialization(){
		Hubub.Debug("2", "...");
		_notificationPanel = HububNotificationPanel.getInstance();
		_profilePanel = HububProfilePanel.getInstance();
		_emergencyPanel = HububEmergencyPanel.getInstance();
		_relationshipsPanel = HububRelationshipsPanel.getInstance();
		_alertSelector = AlertSelector.getInstance();
		this.displayTab(_emergencyPanel);

		//HububRegProcess.getInstance().start();
		// Now kick things off...
		//Hubub.getInstance().getAndOpenHandle();

	}

	protected void onRestart(){
		Hubub.Debug("2", "DroidHubub: onRestart...");
		super.onRestart();
	}

	protected void onStart(){
		Hubub.Debug("2", "DroidHubub: onStart...");
		super.onStart();
		HububPhone.getInstance().reset();
		HububSendMail.getInstance().reset();
	}

	protected void onResume(){
		Hubub.Debug("2", "DroidHubub: onResume...");
		super.onResume();
		try{
			// Keep screen bright while running
			_wakeLock.acquire();
			_kl.disableKeyguard();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	protected void onPause(){
		Hubub.Debug("2", "DroicHubub: onPause... isFinishing: " +this.isFinishing());
		super.onPause();

		// release screen brightness lock
		_wakeLock.release();
		_kl.reenableKeyguard();
	}

	public void shutDown(){
		HububCookies.getInstance().sync();
		Hubub.Debug("2", "HububCookies.getInstance().sync()...");
		//HububStreamListener.getInstance().close();
		Hubub.Debug("2", "Called HububStreamListener.getInstance().close()...");
		_emergencyPanel.close();
		//_isRunning = false;
		Hubub.Debug("2", "Here comes System.exit(0)...");
		System.exit(0);		
	}
	
	protected void onUserLeaveHint(){
		super.onUserLeaveHint();
		Hubub.Debug("2", "...");
		boolean anythingActive = (HububPhone.getInstance().isActive() || HububSendMail.getInstance().isActive() || _intentIsActive);
		Hubub.Debug("2","anythingActive: " +anythingActive);
		if(anythingActive) return;

		//System.exit(0);
	}

	protected void onStop(){
		HububPhone phone = HububPhone.getInstance();
		Hubub.Debug("2", "isFinishing: " +this.isFinishing() +", phone.isActive(): " +phone.isActive() +", email.isActive(): " +HububSendMail.getInstance().isActive());
		super.onStop();
		Hubub.Debug("2", "DroidHubub: onStop...after super.onStrop... " );
		boolean anythingActive = (phone.isActive() || HububSendMail.getInstance().isActive() || _intentIsActive);
		Hubub.Debug("2","anythingActive: " +anythingActive);
		if(anythingActive) return;

		Hubub._KeepRunning = false;
		this.shutDown();

		ViewParent vp = null;
		HububBanner banner = HububBanner.getInstance();
		if((vp = banner.getParent()) != null){
			ViewGroup vpvg = (ViewGroup)vp;
			vpvg.removeView(banner); 
		}
		_alertSelector.releaseInstance();
		_emergencyPanel.releaseInstance();
		_profilePanel.releaseInstance();
		_notificationPanel.releaseInstance();
		_relationshipsPanel.releaseInstance();
		_currentPanel = null;
		HububLogin.getInstance().releaseInstance();
		HububCookies.getInstance().releaseInstance();
		HububWorking.getInstance().releaseInstance();
		AlertSelector.getInstance().hide();
		this.finish();
	}

	protected void onDestroy(){
		Hubub.Debug("2", "DroidHubub: onDestroy... isFinishing: " +this.isFinishing());
		super.onDestroy();
	}

	public boolean isFinishing(){
		Hubub.Debug("2", "DroidHubub: isFinishing...");
		return super.isFinishing();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Hubub.Debug("2", "DroidHubub: onKeyDown... keyCode: " +keyCode);
		if (keyCode == KeyEvent.KEYCODE_ENDCALL) {
			Hubub.Debug("2", "DroidHubub: onKeyDown: ENDCALL...");
			this.finish();
			//return true;
		}
		//if(keyCode == KeyEvent.KEYCODE_MENU){
		//	Hubub.Logger("DroidHubub: onKeyDown: MENU...");
		//	if(_currentPanel == _alertSelector){	// Disable menus when AlertSelector is showing...
		//		return true;
		//	}
		//}
		return super.onKeyDown(keyCode, event);
	}
	public void onBackPressed(){
		Hubub.Debug("2", "...");
		super.onBackPressed();
	}

	public void displayTab(HububTabPanel tabPanel){
		Hubub.Debug("2", "DroidHubub: displayTab: _currentPanel: " +_currentPanel +", tabPanel: " +tabPanel);
		//Hubub.Logger(Hubub.getStackTrace());
		if(_currentPanel != null){
			Hubub._InputMethodMgr.hideSoftInputFromWindow(_currentPanel.getWindowToken(), 0);
			_currentPanel.onTabDeSelected();
			root.removeView(_currentPanel);
		}
		if(tabPanel != null){
			root.addView(tabPanel);
			tabPanel.setPreviousPanel(_currentPanel);
			_currentPanel = tabPanel;
			tabPanel.onTabSelected();
		}
		Hubub.Debug("2", "DroidHubub: FInished displayTab: _currentPanel: " +_currentPanel +", tabPanel: " +tabPanel);
	}

	public static boolean isCurrentPanel(HububTabPanel panel){
		return (_instance._currentPanel == panel);
	}


	public HububEmergencyPanel processEmergency(HububServices services){
		HububEmergencyPanel emergPanel = HububEmergencyPanel.getInstance();
		this.displayTab(emergPanel);
		//emergPanel.setStreamConnection(services);
		return emergPanel;
	}

	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		Hubub.Debug("1", "requestCode: " +requestCode +", resultCode: " +resultCode +", data: " +data);
		if(resultCode != Activity.RESULT_OK)
			Hubub.Debug("1", "resultCode NOT OK!!");
		if(requestCode == Hubub.PhoneIntent)
			HububPhone.getInstance().reset();
		else if(requestCode == Hubub.EmailIntent){
			HububSendMail.getInstance().cancelTimer();
			HububSendMail.getInstance().reset();
		}
		if(requestCode == Hubub.SelectPicture){
			if(resultCode == Activity.RESULT_OK){
				byte[] imageBuf = new byte[100000];
				ByteArrayBuffer arrayBuf = new ByteArrayBuffer(400000);
				Uri selectedImage = data.getData();
				InputStream imageStream = null;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
				} catch (FileNotFoundException e) {
					Hubub.Debug("1", "Image could not be opened, Error: " +e.getMessage());
					e.printStackTrace();
				}
				//int bytesRead = 0;
				//while((bytesRead) != -1){
				//	try {
				//		bytesRead = imageStream.read(imageBuf);
				//	} catch (IOException e) {
				//		e.printStackTrace();
				//	}
				//	if(bytesRead != -1) arrayBuf.append(imageBuf, 0, bytesRead);
				//}
				Bitmap bitMap = BitmapFactory.decodeStream(imageStream);
				//ByteArrayOutputStream stream = new ByteArrayOutputStream();
				//bitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				//byte[] byteArray = stream.toByteArray();
				if(_picListener != null)
					_picListener.savePic(bitMap);
			}
			_picListener = null;
			this.setIntentIsActive(false);
		}
	}

	public boolean onCreateOptionsMenu (Menu menu){
		Hubub.Debug("2", "DroidHubub: onCreateOptionsMenu...");
		menu.add(Menu.NONE, this.OPTION_MENU_ITEM_RUOK, Menu.NONE, "IMOK");
		menu.add(Menu.NONE, this.OPTION_MENU_ITEM_PROFILE, Menu.NONE, "Profile");
		menu.add(Menu.NONE, this.OPTION_MENU_ITEM_NOTICES, Menu.NONE, "Notices");
		menu.add(Menu.NONE, this.OPTION_MUNU_ITEM_RELATIONSHIPS, Menu.NONE, "Groups");
		return true;
	}

	public boolean onOptionsItemSelected (MenuItem item){
		Hubub.Debug("2", "DroidHubub: onMenuItemSelcted: item.getTitle(): " +item.getTitle() +", item.getItemID: " +item.getItemId());
		switch (item.getItemId()) {
		case OPTION_MENU_ITEM_NOTICES:
			this.displayTab(_notificationPanel);
			return true;
		case OPTION_MENU_ITEM_PROFILE:
			this.displayTab(_profilePanel);
			return true;
		case OPTION_MENU_ITEM_RUOK:
			this.displayTab(_emergencyPanel);
			return true;
		case OPTION_MUNU_ITEM_RELATIONSHIPS:
			this.displayTab(_relationshipsPanel);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		AlertSelector.getInstance().show();

	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {	// Process the Login Response...
		Hubub.Debug("2", "services: " +services);
		HububWorking.getInstance().doneWorking();
		String entityID = services.getEntityID();
		if(entityID == null) entityID = "";
		String cookieEntityID = HububCookies.getCookie("EntityID");
		if(cookieEntityID == null) cookieEntityID = "";
		Hubub.Debug("2", "entityID: " +entityID +", cookieEntityID: " +cookieEntityID);
		services.getServices();
		HububService hubServ = services.nextService();
		if(entityID.length() == 0 || !entityID.equals(cookieEntityID)){ // Double clutch if first or new user on this phone
			hubServ.resetOutputs();
			services.setEntityID(cookieEntityID);
			Invoker invoker = new Invoker();
			invoker.send(services, this);
			HububWorking.getInstance().working();	
			return;
		}
		HububPhone.getInstance().determinePhoneNumber();
		HububPhone.getInstance().determinePushToken();
		Hubub.getInstance().getAndOpenHandle();
		String numAlerts = HububCookies.getCookie("NumAlerts");
		if(numAlerts == null)
			this.displayTab(_relationshipsPanel);
		else
			this.displayTab(_emergencyPanel);
		hubServ.getOutputs();

		String c_ncStatus = hubServ.getParm("NCStatus");
		if(c_ncStatus == null) c_ncStatus = "";
		String c_ncLogoURL = hubServ.getParm("NCLogoURL");
		if(c_ncLogoURL == null) c_ncLogoURL = "";
		String c_ncDescription = hubServ.getParm("NCDescription");
		if(c_ncDescription == null) c_ncDescription = "";
		String c_acStatus = hubServ.getParm("ACStatus");
		if(c_acStatus == null) c_acStatus = "";
		String c_acLogoURL = hubServ.getParm("ACLogoURL");
		if(c_acLogoURL == null) c_acLogoURL = "";
		String c_acDescription = hubServ.getParm("ACDescription");
		if(c_acDescription == null) c_acDescription = "";

		AlertSelector.getInstance().positionButton(c_ncStatus, c_ncLogoURL, c_ncDescription, 
				c_acStatus, c_acLogoURL, c_acDescription);

		//this.continueInitialization();
	}

	/* MapActivity Protocol */
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}