package com.hububnet.docs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.util.HububEmulatorParms;
import com.hububnet.util.HububPhone;

public class HububCookies extends HububXMLDoc{
	private static HububCookies _instance;
	String _filePath = "HububCookies.txt";
	DroidHubub _droidHubub;
	//private static String HUBUBCOOKIEFILE = "HububCookies.txt";
	FileOutputStream _fos;
	FileInputStream _fis;

	private HububCookies(){
		super("HububCookies");
		_droidHubub = DroidHubub.getInstance();
		addAttrib("ExpireTime", "");
		addAttrib("SessionID", "");
		addAttrib("EntityID", "");
		addAttrib("BuildID", Hubub.BuildID);
		addAttrib("PushToken", "");
		//FlashCookies.removeCookie("HububCookies");
		//String hububCookies = Cookies.getCookie("HububCookies");
		Hubub.Logger("HububCookies: Constructor: asString: " +this.toString() +" Length: " +this.toString().length());
		//File file = _droidHubub.getFileStreamPath(HUBUBCOOKIEFILE);

		//FileConnection fconn = null;
		byte[] readBuf = new byte[2000];
		String hububCookies = "";
		try {
			//_filePath = "file:///store/home/user/HububCookies.txt";
			//if(Hubub.HUBUBSIMULATOR) 
			//	_filePath = "file:///SDCard/BlackBerry/documents/HububCookies.txt";
			//fconn = (FileConnection)Connector.open(_filePath, Connector.READ_WRITE);
			// If no exception is thrown, then the URI is valid, but the file may or may not exist.
			//if (!fconn.exists())
			//	fconn.create();  // create the file if it doesn't exist
			Hubub.Logger("HububCookies: Constructor: setting _fos...");
			//_fos = _droidHubub.getBaseContext().openFileOutput(_filePath, Context.MODE_PRIVATE);
			//_fos.write("abc".getBytes());
			Hubub.Logger("HububCookies: Constructor: closing _fos...");
			//_fos.close();
			Hubub.Logger("HububCookies: Constructor: opening _fis...");
			_fis = _droidHubub.getBaseContext().openFileInput(_filePath);
			//InputStream is = fconn.openInputStream();
			Hubub.Logger("HububCookies: Constructor: reading _fis...");
			int bytesRead = _fis.read(readBuf);
			if(bytesRead >= 0){
				hububCookies = new String(readBuf, 0, bytesRead);
			}
			Hubub.Logger("HububCookies: Constructor: hububCookies: " +hububCookies +" Length: " +hububCookies.length());
			Hubub.Logger("HububCookies: Constructor: closing _fis...");
			_fis.close();
			//fconn.close();
		}
		catch (IOException ioe) {
			Hubub.Logger("HububCookies: Constructor: Could no open file: " +ioe.getMessage());
			//Hubub.Logger(Hubub.getStackTrace(ioe));
		}
		catch (Exception e){
			Hubub.Logger("HububCookies: Constructor: Exception: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));

		}
		//String hububCookies = FlashCookies.getCookie("HububCookies");
		if(hububCookies.equals("")){
			Hubub.Logger("HububCookies: cookies: ''...");
			sync();
		}
		else{
			parse(hububCookies);
			String buildID = this.getAttrib("BuildID");
			if(buildID == null || !buildID.equals(Hubub.BuildID)){
				this.addAttrib("EntityID", "");
				this.addAttrib("BuildID", Hubub.BuildID);
			}
		}
		//addAttrib("PushToken", "" +Hubub._DeviceID);

		if(!Hubub.HUBUBSIMULATOR){	// If simulator, defer to Emulator parms...
			String devID = "Roid" + ":" +Hubub._DeviceID;
			addAttrib("DeviceID", devID);
		}

		String emailPromo = this.getAttrib("EmailPromo");
		if(emailPromo == null){
			this.addAttrib("EmailPromo", "Hi, I have joined IMOKSecurity.net and would like to add you to my Alert Group but was not able to find you on the system. \n\n" +
					"IMOK provides me with a global personal security service on my Smartphone and " +
					"you can join at IMOKSecurity.net.  They currently support the Android (android.imoksecurity.net) and iPhone (iphone.imoksecurity.net). \n\n" +
					"Let me know if you sign up.\n\n" +

			"Thanks,\n");
		}

		//String phoneNum = "Nothing";
		//String derivedPhoneNum = "";
		//Hubub.Debug("2", "HububCookies: Constructor: get phoneNum...");
		//try{
		//	derivedPhoneNum = ((TelephonyManager) _droidHubub.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		//	if(derivedPhoneNum == null) derivedPhoneNum = "";
		//	derivedPhoneNum = Hubub.cleanPhoneNumber(derivedPhoneNum);
		//	if(derivedPhoneNum.length() > 4){
		//		phoneNum = derivedPhoneNum;
		//		this.addAttrib("DerivedPhoneNum", derivedPhoneNum);
		//	}
		//	else{
		//		this.removeAttrib("DerivedPhoneNum");
		//	}
		//}
		//catch(Exception e){
		//	Hubub.Debug("1", "TelephoneManager Error: " +e.getMessage());
		//	Hubub.Logger(Hubub.getStackTrace(e));
		//}

		if(getAttrib("CountryCode") == null) addAttrib("CountryCode", "");
		if(getAttrib("PhoneNum") == null) addAttrib("PhoneNum", "");
		if(getAttrib("DerPhoneNum") == null) addAttrib("DerPhoneNum", "");

		//Hubub.Debug("2", "derivedPhoneNum: " +derivedPhoneNum);
		////if(!phoneNum.startsWith("1"))
		////	phoneNum = HububPhone.getCountryCode() +Hubub.cleanPhoneNumber(phoneNum);
		//String countryCode = HububPhone.getCountryCode();
		//Hubub.Debug("2", "phoneNumber: " +phoneNum +", Country Code: " +countryCode);
		//addAttrib("PhoneNum", phoneNum);
		//addAttrib("CountryCode", countryCode);
		Hubub.Debug("2", "hububCookies: " +this.toString());
		sync();
	}

	
	// Reset cookies to 'factory install' state... Useful when a new user is using a phone from a previous user
	public HububCookies reset(){
		Hubub.Debug("2", "cookies: " +this);
		try {
			_fos = _droidHubub.getBaseContext().openFileOutput(_filePath, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Hubub.Debug("2","HububCookies: sync: write _fos...");
		try {
			_fos.write(new byte[0]);	// Truncate the cookies file by writing zero bytes...
			Hubub.Debug("2", "HububCookies: sync: close _fos...");
			_fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String simDeviceID = this.getAttrib("SimDeviceID");
		_instance = null;
		HububCookies cookies = getInstance();
		if(simDeviceID != null){
			cookies.addAttrib("DeviceID", simDeviceID);
			cookies.addAttrib("SimDeviceID", simDeviceID);
		}
		Hubub.Debug("2", "cookies: " +cookies);
		return cookies;

	}

	public static HububCookies getInstance(){		
		if(_instance == null) _instance = new HububCookies();
		return _instance;
	}

	public static String getCookie(String cookie){
		return getInstance().getAttrib(cookie);
	}

	public static void setCookie(String cookie, String value){
		getInstance().addAttrib(cookie, value);
	}

	public static void removeCookie(String cookie){
		getInstance().removeAttrib(cookie);
	}

	public void sync(){
		//long month = 60*60*24*365*1000/12; // average month in milliseconds
		//Date date = new Date();
		//date = new Date(date.getTime() + month);
		//Cookies.setCookie("HububCookies", toString(), date);
		//FileConnection fconn = null;
		Hubub.Logger("HububCookies: sync: cookies: " +this.toString());
		try {
			//fconn = (FileConnection)Connector.open(_filePath, Connector.READ_WRITE);
			// If no exception is thrown, then the URI is valid, but the file may or may not exist.
			//if (!fconn.exists())
			//	fconn.create();  // create the file if it doesn't exist
			//fconn.truncate(0);
			//OutputStream os = fconn.openOutputStream();
			Hubub.Logger("HububCookies: sync: open _fos...");
			_fos = _droidHubub.getBaseContext().openFileOutput(_filePath, Context.MODE_PRIVATE);
			Hubub.Logger("HububCookies: sync: write _fos...");
			_fos.write(toString().getBytes());
			Hubub.Logger("HububCookies: sync: close _fos...");
			_fos.close();
			//fconn.close();
		}
		catch (IOException ioe) {
			Hubub.Logger("HububCookies: sync: Could no open file: " +ioe.getMessage());
			ioe.printStackTrace();
		}

		//FlashCookies.setCookie("HububCookies", toString());

	}
	public void releaseInstance() {
		_instance = null;

	}

}
