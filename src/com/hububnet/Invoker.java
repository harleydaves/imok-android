package com.hububnet;

import java.io.UnsupportedEncodingException;

import android.content.Intent;
import android.net.Uri;

import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububServices;
import com.hububnet.docs.HububWebService;

import com.hububnet.util.HububAlert;
import com.hububnet.util.HububBanner;
import com.hububnet.util.HububButton;
import com.hububnet.util.HububLogin;
import com.hububnet.util.HububLoginListener;
import com.hububnet.util.HububWorking;

public class Invoker implements HububURLConnectionListener, HububLoginListener, HububAlert.Listener{
	InvokerListener _listener;
	String _sendAsEntityID = null;
	String _phoneNum = null;
	HububURLConnection _urlCon;
	HububLogin _login;
	HububWebService _webService;
	HububServices _services;

	private boolean needsCredentials(HububWebService webService){
		String error = webService.getError();
		if(error == null || error.equals(""))
			return false;
		HububWorking.getInstance().doneWorking();
		if(error.equals("password")){
			String errorMsg = webService.getHeader("ErrorMsg");
			Hubub.Debug("2","errorMsg: " +errorMsg);
			if(errorMsg != null){
				HububAlert.getInstance().removeButton();
				HububAlert.getInstance().alert(errorMsg);
				HububAlert.getInstance().bringToFront();
			}else{
				String iPhoneMinRelease = webService.getHeader("iPhoneMinRelease");
				if(iPhoneMinRelease == null)
					HububCookies.removeCookie("iPhoneMinRelease");
				else{
					HububCookies.setCookie("iPhoneMinRelease", iPhoneMinRelease);
				}
				_login = HububLogin.getInstance();
				_login.setLoginListener(this);
				//_login.setUserIDRequired(webService.getHeader("UserID") != null);
				_login.setUserIDRequired(true);
				_login.setDeviceNameRequired(webService.getHeader("DeviceName") != null);
				_login.show();
			}
		}
		if(error.equals("sid")){
			HububCookies.setCookie("EntityID", webService.getEntityID());
			HububCookies.setCookie("SessionID", webService.getSessionID());
			String expTimeString = webService.getHeader("ExpTime");
			HububCookies.setCookie("ExpireTime", expTimeString);
			HububCookies.setCookie("FirstName", webService.getHeader("FirstName"));
			HububCookies.setCookie("LastName", webService.getHeader("LastName"));
			//Hubub.getInstance().getAndOpenHandle();
			////String deviceID = webService.getHeader("DeviceID");
			////if(deviceID != null) HububCookies.setCookie("DeviceID", deviceID);
			String carrier = webService.getHeader("Carrier");
			HububCookies.setCookie("HububCarrier", carrier);
			//HububBanner.getInstance().setLogo(carrier);
			String carrierName = webService.getHeader("CarrierName");
			if(carrierName != null) HububCookies.setCookie("CarrierName", carrierName);
			long expireTime = Long.parseLong(expTimeString);
			Hubub.Debug("2", "sesssionID Cookie: " +HububCookies.getCookie("SessionID"));
			Hubub.Debug("2", "expireTIme: " +expireTime);
			HububCookies.getInstance().sync();
			////HububLogout.getInstance().setExpireTime(expireTime);
			_login.hide();
			return false;		
		}
		return true;
	}

	public void send(HububServices services, InvokerListener listener){
		Hubub.Debug("2", "listener: " +listener +", services: " +services);
		_listener = listener;
		this.send(services);
	}

	public void send(HububServices services){
		_services = services;
		_webService = (_webService == null)?new HububWebService():_webService;
		if(_sendAsEntityID != null){
			_webService.setEntityID(_sendAsEntityID);
		}
		if(_phoneNum != null)
			_webService.setHeader("PhoneNum", _phoneNum);
		_webService.setPayload(services.toString());
		String servicesAsString = _webService.toString();
		Hubub.Debug("2", "servicesAsString: " +servicesAsString);
		byte[] postData = null;
		try {
			postData = servicesAsString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Hubub.Debug("2", "UnsupportedEncodingException: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		String url = (Hubub._HububTranPort.equals("443"))?"https://":"http://";
		url += Hubub.getBaseAddress() +":" +Hubub._HububTranPort +"/HububSrv/HububDispatcher";
		Hubub.Debug("2", "url: " +url);
		_urlCon = new HububURLConnection(url, services, this); 
		_urlCon.setPostData(postData);
		_urlCon.send();
	}

	public void sendAsEntityID(String entityID){
		_sendAsEntityID = entityID;
	}
	
	public void setPhoneNum(String phoneNum){
		_phoneNum = phoneNum;
	}

	/*
	 * InvokerListener Protocol
	 * @see com.hubub.HububURLConnectionListener#connectionDidFail(com.hubub.HububURLConnection)
	 */
	public void connectionDidFail(HububURLConnection urlConnection) {
		// TODO Auto-generated method stub

	}

	public void responseReceived(HububURLConnection urlConnection) {
		String respString = new String(urlConnection.getResponse());
		//Hubub.Debug("2", "respString: " +respString);
		HububWebService webService = new HububWebService();
		webService.parse(respString);
		Hubub.Debug("2", "webService: " +webService.toString());
		HububServices services = new HububServices();
		services.parse(webService.getPayload());
		Hubub.Debug("2", "services: " +services.toString());
		String minRelease = webService.getHeader("MinRelease");
		if(minRelease != null){
			HububAlert.getInstance().setDismissBtn("Upgrade Now...", "upgradeNow");
			HububAlert.getInstance().setListener(this);
			HububAlert.getInstance().alert("This phone will no longer operate and must be " +
					"upgraded to IMOK Release: " +minRelease +" or greater.");
			return;	// Bail out, there's nothing left to do...
		}
		String ccGood = webService.getHeader("CCGood");
		if(ccGood != null){	// must be response to first message
			if(ccGood.equals("No")){
				HububAlert.getInstance().removeButton();
				HububAlert.getInstance().alert("The credit card on file for this account is not valid...");
				return;
			}
			else{
				HububCookies.setCookie("FirstName", webService.getHeader("FirstName"));
				HububCookies.setCookie("LastName", webService.getHeader("LastName"));
				String emailPromo = webService.getHeader("EmailPromo");
				if(emailPromo != null) HububCookies.setCookie("EmailPromo", emailPromo.replaceAll("\\\\n", "\n"));
				HububCookies.getInstance().sync();
			}
		}
		if(!this.needsCredentials(webService)){
			Hubub.Debug("2", "_listener: " +_listener);
			if(_listener != null) _listener.onResponseReceived(services);
		}
	}

	/* HububLoginListener Protocol */
	public void loginInfoEntered(HububLogin login) {
		Hubub.Debug("2", "HububCookies: " +HububCookies.getInstance().toString());
		_webService.setError(login.getPassword());
		if(login.getUserIDRequired()) _webService.setHeader("UserID", login.getUserID());
		if(login.getDeviceNameRequired()) _webService.setHeader("DeviceName", login.getDeviceName());
		String phoneNum = HububCookies.getCookie("PhoneNum");
		if(phoneNum != null) _webService.setHeader("PhoneNum", HububCookies.getCookie("PhoneNum"));
		String hububCarrier = HububCookies.getCookie("HububCarrier");
		if(hububCarrier != null) _webService.setHeader("Carrier", HububCookies.getCookie("HububCarrier"));
		String pushToken = HububCookies.getCookie("PushToken");
		if(pushToken != null) _webService.setHeader("PushToken", HububCookies.getCookie("PushToken"));
		this.send(_services);
		HububWorking.getInstance().working();
		login.hide();
	}

	/* HububAlert.Listener Protocol */
	public void buttonPressed(HububButton button) {
		String tag = button.getTag();
		if(tag.equals("upgradeNow")){
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=com.hububnet"));
			DroidHubub.getInstance().startActivity(intent);
		}
	}

}
