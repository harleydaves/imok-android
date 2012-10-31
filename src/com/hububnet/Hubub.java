package com.hububnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

import com.hububnet.ruok.HububEmergencyPanel;
import com.hububnet.util.HububPhone;
import com.hububnet.util.HububStreamConnector;
import com.hububnet.util.HububStreamHandle;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;


public class Hubub implements HububStreamHandle.Listener{
	private static long GUID;
	private static Hubub _instance;
	private static FileOutputStream LOGSTREAM;
	private HububStreamHandle _handle;
	//private HububMainScreen _mainScreen;

	public static boolean HUBUBSIMULATOR = (Build.MANUFACTURER.equals("unknown"));
	public static boolean HUBUBRELEASE = false;
	public static boolean HUBUBDEBUG = false;
	public static boolean DEVICEDEBUG = true;
	public static String DEBUGLEVEL = "12";	// Indicates level of debugging output to logcat
	public static boolean ALWAYSDEVMAP = false;	// should always use dev google map key
	public static boolean SIGNEDSIM = false;	// Turn on when building a signed version for simulator

	public static String BuildID = "20";
	public static boolean _KeepRunning = true;
	public static int SCROLLBAR_WIDTH = 5;
	public static int STATUSBAR_HEIGHT = 25;

	static String _HububBaseAddressCookie;	// points to the cookie that caches hbubuBaseAddress
	static String _BaseAddressDomain;
	public static String _HububPushStreamPort;
	public static String _HububTranPort;
	public static String _HububRelease;
	private boolean _isFirstMessage = true;
	public static String _DeviceID;
	public static DisplayMetrics _DisplayMetrics;
	public static InputMethodManager _InputMethodMgr;
	public static ArrayList<String> _AuthorizedSims;	// <firstname><lastname> of users
	
	public static int EmailIntent = 100;
	public static int PhoneIntent = 200;
	public static int SelectPicture = 300;


	//public static Hubub _instance;

	private Hubub(){
		this.initializeLogger();
		_AuthorizedSims = new ArrayList<String>();
		_AuthorizedSims.add("DaveSpicer");
		_AuthorizedSims.add("SteveHornyak");
		_AuthorizedSims.add("DougHarris");
		_AuthorizedSims.add("RobSpire");

		TelephonyManager tm = (TelephonyManager) DroidHubub.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		Hubub.Debug("2", "telephoneManager deviceID: " +tm.getDeviceId() +", softwareVersion: " +tm.getDeviceSoftwareVersion());
		//Hubub.Logger("Hubub: Constructor: Secure.ANDROID_ID.toUpperCase(): " +Secure.getString(DroidHubub.getInstance().getContentResolver(), Secure.ANDROID_ID));
		//_DeviceID = Secure.getString(DroidHubub.getInstance().getContentResolver(), Secure.ANDROID_ID).toUpperCase();
		_DeviceID = tm.getDeviceId().toUpperCase();
		//HUBUBSIMULATOR = (_DeviceID.equals("000000000000000"));


		// get Device Manufacturer, Should be 'HTC', 'motorola', or 'unknown'
		Hubub.Debug("2", "Device Manufacturer: " +Build.MANUFACTURER +", Version: " +Build.VERSION.RELEASE);
		
		// Start up the phone handler...
		HububPhone.getInstance();

		// Retrieve Display Metrics
		_DisplayMetrics = new DisplayMetrics();
		DroidHubub.getInstance().getWindowManager().getDefaultDisplay().getMetrics(_DisplayMetrics);

		// Get Input Method Manager
		_InputMethodMgr = (InputMethodManager) DroidHubub.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);

		// Retrieve RUOK Release number...
		PackageManager pm = DroidHubub.getInstance().getPackageManager();
		PackageInfo pi = null;
		try {
			pi = pm.getPackageInfo("com.hububnet", PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		_HububRelease = pi.versionName;
		Hubub.Debug("2", "HububRelease: " +getHububRelease());
	}

	public static Hubub getInstance(){
		if(_instance == null)
			_instance = new Hubub();
		return _instance;
	}

	private void initializeLogger(){
		if(LOGSTREAM == null){
			//FileConnection fconn = null;
			String filePath = "HububLog.txt";
			//if(DeviceInfo.isSimulator()) filePath = "file:///SDCard/BlackBerry/documents/HububLog.log";
			try {
				LOGSTREAM = DroidHubub.getInstance().openFileOutput(filePath, Context.MODE_PRIVATE);
				Log.d("Dave", "Hubub: initializeLogger: just set LOGSTREAM...");
				String fileName = Environment.getExternalStorageDirectory() +"/" +filePath;
				File file = new File(fileName);
				//file.createNewFile();
				// If no exception is thrown, then the URI is valid, but the file may or may not exist.
				//if (!fconn.exists())
				//	fconn.create();  // create the file if it doesn't exist
				//fconn.truncate(0);
				//LOGSTREAM = fconn.openOutputStream();
				//os.write(toString().getBytes());
				//os.close();
				//fconn.close();
			}
			catch (IOException ioe) {
				Hubub.Debug("1", "Could not open file: " +ioe.getMessage());
				Hubub.Logger(Hubub.getStackTrace(ioe));
			}

		}
	}

	public static void Logger(String text){
		Log.d("Dave", text);
	}
	
	public static void Debug(String level, String text){
		if(Hubub.DEBUGLEVEL.contains(level)){
			String outstring = "L" +level +":";
			StackTraceElement[] e = Thread.currentThread().getStackTrace();
			String className = e[3].getClassName();
			outstring += className.substring("com.hububnet.".length(), className.length()) +":";
			outstring += e[3].getMethodName() +":";
			outstring += e[3].getLineNumber() +":" +text +"\n";
			Log.d("Dave", outstring);
		}
	}

	public static String getEntityID(){
		return HububCookies.getCookie("EntityID");
		//return "dummy";
	}

	public static String getSessionID(){
		return HububCookies.getCookie("SessionID");
		//return "dummy";
	}

	public static String getHububRelease(){
		return _HububRelease;
	}


	public static String getStackTrace(Throwable t) {
		String stackTrace = null;

		try {
			StringWriter sw = new StringWriter();

			// print the stack trace using the
			// print writer into the string writer
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);

			// close the writers.
			pw.close();
			sw.close();

			// get the stack trace
			stackTrace = sw.getBuffer().toString();
		} catch (Exception ex) {
		}
		return stackTrace;
	}
	
	public static String getStackTrace(){
		String retval = "";
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		for(int i=3; i< e.length; i++){
			retval += e[i].getClassName() +":" +e[i].getMethodName() +":" +e[i].getLineNumber() +"\n";
		}
		//outstring += e[2].getClassName() +":" +e[2].getMethodName() +":" +e[2].getLineNumber() +": " +text;
		return retval;
	}

	public void getAndOpenHandle(){
		String entityID = HububCookies.getCookie("EntityID");
		//if(entityID == null || entityID.equals("") || (_handle != null && _handle.isOpen())) return;
		if((_handle != null && _handle.isOpen())) return;
		_handle = HububStreamConnector.getStreamHandle(entityID, "Control", this);
	}


	public boolean isFirstMessage(){
		if(_isFirstMessage){
			_isFirstMessage = false;
			return true;
		}
		return false;
	}

	public void setIsFirstMessage(boolean isFirstMessage){
		_isFirstMessage = isFirstMessage;
	}
	
	public static String getFBAppID(){
		String retval = "112926005430451";	// use debug version by default
		if(!Hubub.HUBUBDEBUG)
			retval = "132480326818598";
		return retval;
	}


	public static String getBaseAddress(){
		//Hubub.Logger("Hubub: getBaseAddress...");
		String retval = null;
		if(Hubub.HUBUBSIMULATOR){
			if(Hubub.HUBUBDEBUG){
				//retval = "local.hububnet.net";
				retval = "whitestone.dyndns.biz";
				_HububBaseAddressCookie = "DevHububBaseAddress";
				_HububPushStreamPort = "8080";
				//_HububTranPort = "443";
				_HububTranPort = "8078";
			}
			else{
				retval = "prod.hububnet.net";
				//retval = "whitestone.dyndns.biz";
				_HububBaseAddressCookie = "HububBaseAddress";
				//_HububPushStreamPort = "80";
				_HububPushStreamPort = "8080";
				_HububTranPort = "8078";
				//_HububTranPort = "443";
				_HububPushStreamPort = "80";
				_HububTranPort = "443";
			}
		}
		else{
			if(Hubub.HUBUBDEBUG){
				//retval = "dev.hububnet.net";
				retval = "whitestone.dyndns.biz";
				//retval = "192.168.1.106";
				_HububBaseAddressCookie = "DevHububBaseAddress";
				//_HububTranPort = "443";
				_HububPushStreamPort = "8080";
				_HububTranPort = "8078";
			}
			else{
				retval = "prod.hububnet.net";
				_HububBaseAddressCookie = "HububBaseAddress";
				_HububPushStreamPort = "80";
				_HububTranPort = "443";
				
				//retval = "whitestone.dyndns.biz";
				//_HububPushStreamPort = "8080";
				//_HububTranPort = "8078";
			}

		}
		return retval;
	}

	public static void removeBaseAddressCookie(){
		HububCookies.removeCookie(_HububBaseAddressCookie);
	}

	public static String getHububDispatcher(){
		Hubub.Debug("2", "...");
		return Hubub.getHububBaseAddress() +"HububSrv/Dispatcher";
	}

	public static String getHububBaseAddress(){
		Hubub.Debug("2", " _BaseAddressDomain: " +_BaseAddressDomain);
		String retval = HububCookies.getCookie(_HububBaseAddressCookie);
		Hubub.Debug("2", " _HububBaseAddressCookie: " +_HububBaseAddressCookie +" retval: " +retval);
		if(retval == null || retval.equals("")){
			/*
			 * Dervive URLs we will need from forwarded registered subdomains.
			 */
			int rc = -1;
			//HttpConnection httpConn = null;
			//InputStream is = null;
			//OutputStream os = null;
			String respString = null;
			retval = Hubub.getBaseAddress() +":" + _HububTranPort +"/";
			HububCookies.setCookie(_HububBaseAddressCookie, retval);
			HububCookies.getInstance().sync();
		}
		return retval;
	}

	public static String getHububBaseURL(){
		return Hubub.getHububBaseAddress() + "HububSrv/";
	}

	public static String cleanPhoneNumber(String inNumber){
		String retval = "";
		for(int i=0; i<inNumber.length(); i++){
			String digit = inNumber.substring(i, i+1);
			if(digit.equals("0") || digit.equals("1") || digit.equals("2") || digit.equals("3") || digit.equals("4")
					|| digit.equals("5") || digit.equals("6") || digit.equals("7") || digit.equals("8") || digit.equals("9")){
				retval += digit;
			}
		}
		// strip leading zeros...
		for(int i=0; i<retval.length(); i++){
			if(retval.substring(0,1).equals("0")){
				if(retval.length() == 1) retval = "";
				else 
					retval = retval.substring(1);
				i--;
			}
			else
				break;
		}
		return retval;
	}

	public static int getScaledWidth(int width){
		return (int)((width/320.0) *Hubub._DisplayMetrics.widthPixels);
	}

	public static int getScaledHeight(int height){
		return (int)((height/480.0) *Hubub._DisplayMetrics.heightPixels);
	}

	public void deleteFromStream(String timer, String streamName){
		_handle.deleteMsg(timer, streamName);
	}

	public HububEmergencyPanel processEmergency(HububServices services){
		HububEmergencyPanel emergPanel = HububEmergencyPanel.getInstance();
		emergPanel.setStreamConnection(services);
		DroidHubub.getInstance().displayTab(emergPanel);
		return emergPanel;
	}


	/* HububStreamHandle.Listener Protocol */
	public void messageDeleted(String time, String streamName) {
		Hubub.Debug("2", "...");

	}

	public void messageReceived(HububServices services) {
		Hubub.Debug("2", "services: " +services.toString());		
		String tag = services.getTag();
		services.getServices();
		HububService hubServ = services.nextService();
		String servName = hubServ.getName();
		hubServ.getInputs();
		if(servName.equals("Emergency")){
			DroidHubub.getInstance()._notificationPanel.setStreamConnection(services);
		}
		else if(servName.equals("ControlStream")){
			try{
				//if([tag isEqualToString:@"Delete"]){
				//	[_notificationController deleteNotice:[hubServ getParm:@"DeleteTimer"]];
				//	//return;
				//}
				if(tag.equals("Delete")){
					DroidHubub.getInstance()._notificationPanel.deleteNotice(hubServ.getParm("DeleteTimer"));
				}
				else if(tag.equals("BeginHistory"))
					DroidHubub.getInstance()._notificationPanel.setProcessingHistory(true);
				else if(tag.equals("EndHistory"))
					DroidHubub.getInstance()._notificationPanel.setProcessingHistory(false);
			}catch(Exception e){
				Hubub.Debug("1", "Exception: " +e.getMessage());
				Hubub.Logger(Hubub.getStackTrace(e));
			}
		}
	}

	public void handleStatus(HububStreamHandle handle, String tag){
	}
}
