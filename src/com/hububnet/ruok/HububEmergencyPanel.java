package com.hububnet.ruok;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
//import java.util.List;

//import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapController;
//import com.google.android.maps.MapView;
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.R;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.util.Coordinates;
import com.hububnet.util.HububAlert;
import com.hububnet.util.HububBanner;
import com.hububnet.util.HububButton;
import com.hububnet.util.HububButtonListener;
import com.hububnet.util.HububChatSession;
import com.hububnet.util.HububEditText;
import com.hububnet.util.HububGPS;
import com.hububnet.util.HububItemizedOverlay;
import com.hububnet.util.HububLabel;
import com.hububnet.util.HububMapView;
import com.hububnet.util.HububNotificationNote;
import com.hububnet.util.HububNotificationPanel;
import com.hububnet.util.HububOverlayItem;
import com.hububnet.util.HububPhone;
import com.hububnet.util.HububPinNote;
import com.hububnet.util.HububPolicePanel;
import com.hububnet.util.HububProfilePanel;
import com.hububnet.util.HububScrollText;
import com.hububnet.util.HububScrollTextReceiver;
import com.hububnet.util.HububStreamConnector;
import com.hububnet.util.HububStreamHandle;
import com.hububnet.util.HububTextViewListener;
import com.hububnet.util.HububTimer;
import com.hububnet.util.HububWidgett;
import com.hububnet.util.HububWorking;


import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;


public class HububEmergencyPanel extends HububTabPanel implements AlertSelector.Listener, HububStreamHandle.Listener,
HububEditText.NewLineListener, InvokerListener, HububTimer.Listener, HububGPS.Listener, HububButtonListener, HububAlert.Listener{
	private static HububEmergencyPanel _instance;
	HububNotificationNote _noteListener;
	AlertSelector _alertSelector;
	boolean _myEmergency;
	HububCookies _cookies = HububCookies.getInstance();
	HububButton _policeFire;
	HububButton _call;
	HububButton _refresh;
	HububButton _end;
	HububWidgett _topButtons;
	HububChatSession _chatSession;
	HububGPS _gps;
	HububScrollText _send;
	HububScrollTextReceiver _receive;
	HububServices _currentServices;
	HububServices _triggerServices;	// The message that triggered the emergency
	HububServices _myAlertServices;	// Remember my alert service so it can be restarted on new alert
	HububStreamHandle _handle;
	String _phoneNumber;
	String _alertFirstName;
	String _alertEntityID;
	Coordinates _alertCoords;
	HububLabel _alertLabel;
	boolean _cameFromMap = false;	// tells me that I am toggling from map to here...
	Hashtable _pinNotes;
	HububMapView _mapView;
	String _type;
	boolean _sendJoined;
	boolean _sessionEnded;

	public static Hashtable<String, Drawable> MarkerPins;
	public static int PINSIZE = 14;

	private HububEmergencyPanel(){
		super();
		try{
			this.setVerticalScrollBarEnabled(false);

			/* Set up and size Pin Markers once so they can be reused quickly... */
			Drawable drawable = this.getResources().getDrawable(R.drawable.redpin);
			drawable.setBounds(0, 0, PINSIZE, PINSIZE);
			MarkerPins = new Hashtable<String, Drawable>();
			MarkerPins.put("Red", drawable);
			drawable = this.getResources().getDrawable(R.drawable.greenpin);
			drawable.setBounds(0, 0, PINSIZE, PINSIZE);
			MarkerPins.put("Green", drawable);
			drawable = this.getResources().getDrawable(R.drawable.purplepin);
			drawable.setBounds(0, 0, PINSIZE, PINSIZE);
			MarkerPins.put("Purple ", drawable);

			_alertSelector = AlertSelector.getInstance();
			_alertLabel = HububBanner.getInstance().getAlertLabel();

			_policeFire = new HububButton("P/F", "pf");
			_policeFire.setFontStyleSize(Typeface.BOLD, 15);
			_policeFire.setTextColor(Color.RED);
			_policeFire.setListener(this);

			_call = new HububButton("Call", "call");
			_call.setFontStyleSize(Typeface.BOLD, 15);
			_call.setListener(this);

			_refresh = new HububButton("Re-Center:", "refresh");
			_refresh.setFontStyleSize(Typeface.BOLD, 15);
			_refresh.setListener(this);

			_end = new HububButton("End", "end");
			_end.setFontStyleSize(Typeface.BOLD, 15);
			_end.setListener(this);

			_topButtons = new HububWidgett();
			_topButtons.addHorizWidget(_policeFire);
			_topButtons.addHorizWidget(_call);
			_topButtons.addHorizWidget(_refresh);
			_topButtons.addHorizWidget(_end);
			_hubPanel.addVertWidget(_topButtons);

			_chatSession = new HububChatSession();
			this.addView(_chatSession);
			_receive = _chatSession.getReceive();
			_send = _chatSession.getSend();
			_send.setFocus();
			_send.getTextView().setNewLineListener(this);

			Hubub.Debug("2", "before new MapView...");
			_mapView = HububMapView.getInstance();
			_mapView.setBuiltInZoomControls(true);
			_mapView.displayZoomControls(false);
			_mapView.setClickable(true);
			//_hubPanel.addVertWidget(_mapView);
			this.addView(_mapView);	// MapView cannot be added to a ScrollView, which is where _hubPanel resides...

			ViewGroup.LayoutParams lp = _mapView.getLayoutParams();
			_chatSession.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
			_topButtons.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
			lp.height = Hubub._DisplayMetrics.heightPixels - _chatSession.getMeasuredHeight() - Hubub.STATUSBAR_HEIGHT
			-HububBanner.getInstance().getMeasuredHeight() - _topButtons.getMeasuredHeight();
			lp.width = Hubub._DisplayMetrics.widthPixels;

			_mapView.moveTo(new Coordinates(34.0, -83, 0));

			_gps = HububGPS.getInstance();
			_alertCoords = new Coordinates(0,0,0);
			_pinNotes = new Hashtable();

			HububPolicePanel policePanel = HububPolicePanel.getInstance(this);
			this.addView(policePanel);
			policePanel.hide();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/*
	 * Begin Utility Functions...
	 */

	private void updateCoords(String lat, String longi, String alt, String entityID){
		Hubub.Debug("2", "_myEmergency: " +_myEmergency);
		try{
			String coordType = "GPS";
			float floatAlt = Float.parseFloat(alt);
			if(floatAlt == 0){
				coordType = "Towers";
				_refresh.setTextColor(Color.RED);
			}
			else
				_refresh.setTextColor(Color.GREEN);
			_refresh.setName("" +coordType);
			_alertCoords.setLatitude(Double.valueOf(lat).doubleValue());
			_alertCoords.setLongitude(Double.valueOf(longi).doubleValue());
			Hubub.Debug("2", "Double: lat: " +_alertCoords.getLatitude() +", long: " +_alertCoords.getLongitude());
			HububPinNote pinNote = _mapView.retrieveAnnotation(entityID, true);
			pinNote.setFirstName(_alertFirstName);
			if(_myEmergency){
				pinNote.setPinColor("Purple");
			}
			pinNote.setCoordinates(_alertCoords);
			pinNote.setAlertCoords(_alertCoords);
			_mapView.addAnnotation(pinNote);
			_mapView.moveTo(_alertCoords);
			pinNote.getMapView().removePinInfo();
			//_mapView.setCenterCoordinate(pinNote.getCoordinates(), true);
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}
	/*
	 * End Utility Functions...
	 */

	public static HububEmergencyPanel getInstance(){
		if(_instance == null) _instance = new HububEmergencyPanel();
		return _instance;
	}

	public void reset(){
		//if(_mapPanel != null)
		//	_mapPanel.reset();
		_chatSession.reset();
		_pinNotes.clear();
	}

	public HububWidgett getTopButtons(){
		return _topButtons;
	}

	public HububChatSession getChatSession(){
		return _chatSession;
	}

	public void setStreamConnection(HububServices services){
		try{
			Hubub.Debug("2", "services: " +services);
			this.reset();	//get ready for next alert session...
			_end.setVisible(true);
			_currentServices = services;
			_triggerServices = services;
			_sendJoined = false;
			_sessionEnded = true;

			services.reset();
			String stream = services.getAttrib("Stream");
			String deviceID = services.getAttrib("DeviceID");
			String entityID = services.getAttrib("EntityID");
			String deviceName = entityID +"/" +deviceID;
			String myDeviceName = HububCookies.getCookie("EntityID") +"/" +HububCookies.getCookie("DeviceID");
			//_sessionEnded = (![[services getAttrib:@"Status"]isEqualToString:@"Act"]);
			_sessionEnded = (!services.getAttrib("Status").equals("Act"));
			services.getServices();
			HububService hubServ = services.nextService();
			hubServ.getInputs();
			//String fName = hubServ.getParm("FirstName");
			String lName = hubServ.getParm("LastName");
			_handle = HububStreamConnector.getStreamHandle(entityID, stream, this);
			_handle.open();
			String exitOrEnd = null;
			if(deviceName.equals(myDeviceName)){	// must be my emergency...
				//Hubub.Debug("2", "my emergency, start listening to GPS...");
				_gps.setListener(this);
				_myEmergency = true;
				_call.setNameTag("Calls", "allowcalls");
				_call.setTextColor(Color.GREEN);
				exitOrEnd = "End";
				_myAlertServices = services;
				_alertSelector.setContinue(true);
			}
			else{
				_myEmergency = false;
				_call.setNameTag("Call", "call");
				_call.setTextColor(Color.GREEN);
				exitOrEnd = "Leave";
				//this.newLineEntered("<joined>");
			}
			_alertLabel.setText(lName +": Alert");
			_end.setName(exitOrEnd);
			_alertLabel.setVisible(true);
			_end.setVisible(true);
			_send.setFocus();
			//HububTimer timer = new HububTimer(this);
			_alertSelector.hide();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void close(){
		Hubub.Debug("2","...");
		_gps.stop();
	}

	public void setNoteListener(HububNotificationNote hububNotificationNote){
		_noteListener = hububNotificationNote;
	}

	public HububMapView getMapView(){
		return _mapView;
	}

	private void kickoffEmergency(String type){
		//_gps.setListener(this);
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("Emergency");
		service.getInputs();
		if(type != null){
			service.setParm("AlarmType", type);
		}
		service.setParm("FirstName", HububCookies.getCookie("FirstName"));
		service.setParm("LastName", HububCookies.getCookie("LastName"));
		service.setParm("PhoneNumber", HububCookies.getCookie("CountryCode") +HububCookies.getCookie("PhoneNum"));
		service.setParm("Lat", _gps.getLat());
		service.setParm("Long", _gps.getLong());
		service.setParm("Alt", _gps.getAlt());

		String servicesString = services.toString();
		services = new HububServices();
		service = services.addServiceCall("RUOK");
		service.setTag("beginEmergency");
		service.getInputs();
		if(type != null) 
			service.setParm("Type", type);
		service.setParm("Services", servicesString);
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		HububWorking.getInstance().working();
		invoker.send(services, this);
	}


	public void onTabSelected(){
		Hubub.Debug("2", "_cameFromMap: " +_cameFromMap
				+" _handle.isOpen(): " +((_handle == null)?false:_handle.isOpen()));
		try{
			//HububBanner.getInstance().setTopButtons(_topButtons);
			//_refresh.setVisibility(View.INVISIBLE);
			//_topButtons.setTopPadding(10);
			if(!_cameFromMap && (_handle == null || !_handle.isOpen())){
				//_topButtons.setTopPadding(40);
				_alertSelector.setListener(this);
				_alertSelector.show();
				//_alertLabel.setVisible(false);
			}
			else{
				_alertLabel.setVisible(true);
				_call.setVisible(true);
				//_topButtons.reDrawThis();
				//HububBanner.getInstance().reDrawThis();
				//this.invalidate();
				//_policeFire.setVisible(true);
			}
			_cameFromMap = false;	// reset
		}catch(Exception e){
			Hubub.Logger("HububEmergencyPanel: onTabSelected: Exception: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
		}

	}

	public void onTabDeSelected(){
		Hubub.Debug("2", "...");
		super.onTabDeSelected();
		_alertLabel.setVisible(false);
	}

	/* AlertSelector.Listener protocol */
	public void alertSelected(AlertSelector selector) {
		try{
			String buttonPushed = selector.getButtonPushed();
			Hubub.Debug("2", "buttonPushed: " +buttonPushed);
			if(buttonPushed.equals("cancel")){
				_alertSelector.hide();
				DroidHubub.getInstance().displayTab(HububNotificationPanel.getInstance());
			}
			//else if(buttonPushed.equals("emergency")){
			else{
				Hubub.Debug("2", "emergency...");
				_alertSelector.hide();
				if(_myAlertServices != null){
					Hubub.getInstance().processEmergency(_myAlertServices);
					return;
				}
				String kickoffParm = null;
				//if(buttonPushed.equals("monitored"))
					kickoffParm = buttonPushed;
				this.kickoffEmergency(kickoffParm);
				_chatSession.reset();
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/* HububStreamHandle.Listener Protool */
	public void messageDeleted(String time, String streamName) {
		// TODO Auto-generated method stub

	}

	public void messageReceived(HububServices services) {
		Hubub.Debug("2", "services: " +services);
		try{
			String tag = services.getTag();
			if(tag != null){
				Hubub.Debug("2", "tag: " +tag +", _myEmergency: " +_myEmergency +", _sessionEnded: " +_sessionEnded);
				if(tag.equals("BeginHistory") || tag.equals("EndHistory")){
					Hubub.Debug("2", "tag: " +tag +", _myEmergency: " +_myEmergency +", _sessionEnded: " +_sessionEnded);
					if(tag.equals("BeginHistory") && !_myEmergency && !_sessionEnded){
						if(_handle.isOpen()){
							this.newLineEntered("<joined>");
						}
						else _sendJoined = true;
					}
					Hubub.Debug("2", "_sendJoined: " +_sendJoined);
					return;
				}
			}
			String myEntityID = HububCookies.getCookie("EntityID");
			String sTime = services.getAttrib("Time");
			String deviceID = services.getAttrib("DeviceID");
			long ltime = Long.parseLong(sTime);
			SimpleDateFormat dtf = new SimpleDateFormat("h:mm aa");
			Date date = new Date(ltime);
			String fDate = dtf.format(date);
			services.getServices();
			HububService hubServ = services.nextService();
			String servName = hubServ.getName();
			String entityID = hubServ.getEntityID();
			hubServ.getInputs();
			if(servName.equals("Emergency")){
				if(_myEmergency){
					_type = hubServ.getParm("AlarmType");
				}
				_chatSession.reset();
				_phoneNumber = hubServ.getParm("PhoneNumber");
				_call.setVisible(_phoneNumber.length() > 0);
				String fname = hubServ.getParm("FirstName");
				//_alertNote.setTitle(fname);
				_alertFirstName = fname;
				String lat = hubServ.getParm("Lat");
				String longi = hubServ.getParm("Long");
				String alt = hubServ.getParm("Alt");
				_alertEntityID = entityID;
				dtf = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
				fDate = dtf.format(date);
				String outString = fname +": BEGIN: " +fDate;
				_receive.write(outString);
				if(lat != null){
					this.updateCoords(lat, longi, alt, _alertEntityID);
				}
				invalidate();
			}
			else if(servName.equals("CoordUpdate")){	// update from person with alert
				String lat = hubServ.getParm("Lat");
				String longi = hubServ.getParm("Long");
				String alt = hubServ.getParm("Alt");
				this.updateCoords(lat, longi, alt, entityID);
			}
			else if(servName.equals("PostMessage")){
				String msg = hubServ.getParm("Msg");
				Hubub.Debug("2", "services: " +services);
				String fname = hubServ.getParm("Fname");
				String lat = hubServ.getParm("Lat");
				if(lat != null){	// Must be someone monitoring the emergency...
					String longi = hubServ.getParm("Long");
					String alt = hubServ.getParm("Alt");
					String dist = hubServ.getParm("Dist");
					String phone = hubServ.getParm("PhoneNumber");
					HububPinNote pinNote = _mapView.retrieveAnnotation(entityID, true);
					Coordinates coord = new Coordinates(0,0,0);
					coord.setLatitude(Double.valueOf(lat).doubleValue());
					coord.setLongitude(Double.valueOf(longi).doubleValue());
					pinNote.setCoordinates(coord);
					pinNote.setPinColor("Purple");
					pinNote.setDist(dist);
					pinNote.setFirstName(fname);
					if(!entityID.equals(myEntityID)){	// If this is someone else...
						pinNote.setPinColor("Green");
						pinNote.setAlt(alt);
						pinNote.setPhoneNumber(phone);
					}
					_mapView.addAnnotation(pinNote);
				}
				String outString = fname +": " +fDate +": " +msg;
				_receive.write(outString);
				if(deviceID.equals(HububCookies.getCookie("DeviceID"))){ // If I'm the sender of this message
					HububWorking.getInstance().doneWorking();
				}
			}
			else if(servName.equals("EndEmergency")){
				String fname = hubServ.getParm("FirstName");
				dtf = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
				fDate = dtf.format(date);
				String outString = fname +": END: " +fDate;
				_receive.write(outString);
				_triggerServices.reset();
				String time = _triggerServices.getAttrib("Time");
				if(_triggerServices.getAttrib("Status") != null){
					_triggerServices.addAttrib("Status", "End");
					if(_noteListener != null)
						_noteListener.updateStatus();
				}
				if(_myEmergency){	// delete Emergency message from all control streams...
					_gps.setListener(null); // Stop automatic updates...
					services = new HububServices();
					String servicesString = services.toString();
					services = new HububServices();
					HububService service = services.addServiceCall("RUOK");
					service.setTag("endEmergency");
					service.getInputs();
					service.setParm("Time", time);
					service.setParm("PhoneNumber", HububPhone.getGlobalPhoneNumber());
					service.setParm("Lat", HububGPS.getInstance().getLat());
					service.setParm("Long", HububGPS.getInstance().getLong());
					if(_type != null){
						service.setParm("Type", _type);
					}
					service.setParm("Services", servicesString);
					Invoker invoker = new Invoker();
					invoker.sendAsEntityID("0");
					invoker.send(services, this);
					_myAlertServices = null;
					_alertSelector.setContinue(false);
					HububWorking.getInstance().doneWorking();
				}
				_end.setVisible(false);
				_handle.close();
				_currentServices = null;
			}
			else if(servName.equals("ExitEmergency")){
				String fname = hubServ.getParm("FirstName");
				dtf = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
				fDate = dtf.format(date);
				String outString = fname +" <left> " +fDate;
				_receive.write(outString);
				if(_myEmergency){

				}
			}
			else if(servName.equals("CallsAllowed")){
				String status = hubServ.getParm("Status");
				String fname = hubServ.getParm("Fname");
				String enabled = (status.equals("Yes"))?"ENABLED CALLS":"DISABLE CALLS";
				String outString = fname +": " +fDate +": " +enabled;
				_receive.write(outString);
				if(!_myEmergency){
					if(status.equals("Yes")){
						_call.setNameTag("Call", "call");
						_call.setTextColor(Color.GREEN);
					}
					else{
						_call.setNameTag("Calls", "nocalls");
						_call.setTextColor(Color.RED);
					}
				}
				else{
					if(status.equals("Yes")){
						_call.setNameTag("Calls", "allowcalls");
						_call.setTextColor(Color.GREEN);
					}
					else{
						_call.setNameTag("Calls", "nocalls");
						_call.setTextColor(Color.RED);
					}
					HububWorking.getInstance().doneWorking();
				}
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}
	
	public void handleStatus(HububStreamHandle handle, String tag){
		Hubub.Debug("2", "tag: " +tag +", _sendJoined: " +_sendJoined);
		if(tag.equals("open")){
			if(_sendJoined){
				this.newLineEntered("<joined>");
				_sendJoined = false;
			}		
		}
		
	}

	/* HububEditText.NewLineListener Protocol */
	public void newLineEntered(String newLine) {
		Hubub.Debug("2", "newLine: " +newLine);
		try{
			if(newLine.length() < 1) return;	// don't send blank lines...
			if(_currentServices == null){
				HububAlert.getInstance().alert("This alert has been ended. Text can no longer be added...");
				return;
			}
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("PostMessage");
			service.getInputs();
			service.setParm("Msg", newLine);
			service.setParm("Fname", HububCookies.getCookie("FirstName"));
			service.setParm("Lname", HububCookies.getCookie("LastName"));
			Hubub.Debug("2", "newLineEntered: " +newLine +" services: " +services);
			if(!_myEmergency){
				String lat = _gps.getLat();
				String longi = _gps.getLong();
				service.setParm("Lat", lat);
				service.setParm("Long", longi);
				service.setParm("Alt", _gps.getAlt());
				Coordinates coord = new Coordinates(0,0,0);
				coord.setLatitude(Double.valueOf(lat).doubleValue());
				coord.setLongitude(Double.valueOf(longi).doubleValue());
				String dist = "" +(_alertCoords.distance(coord) /1609.347); // distance in miles
				int index = 0;
				Hubub.Debug("2", "dist: " +dist);
				if((index = dist.indexOf(".")) >= 0){
					dist = dist.substring(0, index +2);
				}
				service.setParm("Dist", dist);
				service.setParm("PhoneNumber", HububCookies.getCookie("CountryCode") +HububCookies.getCookie("PhoneNum"));
			}
			_handle.writeMsg(services);
			HububWorking.getInstance().working();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		try{
			services.getServices();
			HububService hubServ = services.nextService();
			String servName = hubServ.getName();
			String tag = hubServ.getTag();
			if(servName.equals("RUOK")){
				if(tag.equals("requestMonitoringInfo")){
					hubServ.getOutputs();
					String msg = hubServ.getParm("Msg");
					HububWorking.getInstance().doneWorking();
					HububAlert alert = HububAlert.getInstance();
					//alert.setListener(this);
					alert.alert(msg);
				}
				else{
					HububWorking.getInstance().doneWorking();
					String errors = hubServ.getErrors();
					if(errors != null){
						HububAlert alert = HububAlert.getInstance();
						if(errors.contains("not subscribed")){
							//errors += "Would you like information on Professional Security Monitoring Services?";
							alert.setDismissBtn("No", "no");
							alert.setSecondBtn("Yes, Send me an email...", "yesEmail");
							alert.setListener(this);
						}
						alert.alert(errors);
					}
					else _gps.setListener(this);
				}
			}
			else if(servName.equals("UtilityServices")){
				HububWorking.getInstance().doneWorking();
				HububPolicePanel.getInstance(this).setService(hubServ);
				Hubub.Debug("2", "Police/Fire: " +services);
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/* HububTimer.Listener Protocol */
	public void timerExpired(HububTimer timer) {
		Hubub.Debug("2", "...");
		DroidHubub.getInstance().displayTab(this);
	}

	/* HububGPS.Listener Protocol */
	public void coordUpdate(String lati, String longi, String alt) {
		try{
			if(_handle == null) return;	// catch race condition...
			Hubub.Debug("2", "lati: " +lati +", longi: " +longi +", alt: " +alt);
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("CoordUpdate");
			service.getInputs();
			service.setParm("Lat", lati);
			service.setParm("Long", longi);
			service.setParm("Alt", alt);
			_handle.writeMsg(services);
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "button tag: " +button.getTag());
		if(button == _refresh){
			_mapView.moveTo(_alertCoords);
		}
		else if(button == _call){
			Hubub.Debug("2", "PhoneNumber: " +_phoneNumber);
			if(!_myEmergency){	// Not my emergency
				if(_call.getTag().equals("call") && _phoneNumber.length() > 0){
					String dialNumber = _phoneNumber;
					if(!Hubub.HUBUBSIMULATOR && Hubub.HUBUBDEBUG)
						dialNumber = "1770-335-4975";
					HububPhone.getInstance().initiateCall(dialNumber);
				}
				else{
					HububAlert.getInstance().alert("User not accepting calls at this time...");
				}
			}
			else{
				HububServices services = new HububServices();
				HububService service = services.addServiceCall("CallsAllowed");
				service.getInputs();
				service.setParm("Fname", HububCookies.getCookie("FirstName"));
				service.setParm("Lname", HububCookies.getCookie("LastName"));
				if(_call.getTag().equals("allowcalls")){
					service.setParm("Status", "No");
				}
				else{
					service.setParm("Status", "Yes");
				}
				HububWorking.getInstance().working();
				_handle.writeMsg(services);
			}
		}
		else if(button == _end){
			HububServices services = new HububServices();
			HububService service = services.addServiceCall((_myEmergency)?"EndEmergency":"ExitEmergency");
			service.getInputs();
			service.setParm("FirstName", HububCookies.getCookie("FirstName"));
			_handle.writeMsg(services);
			if(!_myEmergency){
				_alertLabel.setVisible(false);
				DroidHubub.getInstance().displayTab(HububNotificationPanel.getInstance());
				_handle.close();
			}
			else{
				HububWorking.getInstance().working();
			}
		}
		else if(button == _policeFire){
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("UtilityServices");
			service.setTag("Police/Fire");
			service.getInputs();
			service.setParm("Lat", "" +_alertCoords.getLatitude());
			service.setParm("Long", "" +_alertCoords.getLongitude());
			service.setParm("MaxReturns", "1");
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
			HububWorking.getInstance().working();
		}
		else if(button.getTag().equals("yesEmail")){
			Hubub.Debug("2", "HububAlert button pressed: tag: " +button.getTag());
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("RUOK");
			String entityID = service.getEntityID();
			service.setTag("requestMonitoringInfo");
			service.getInputs();
			service.setParm("EntityID", entityID);
			service.setParm("PhoneNumber", HububCookies.getCookie("CountryCode") +HububCookies.getCookie("PhoneNum"));
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
			HububWorking.getInstance().working();
		}
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				if(child instanceof HububPolicePanel){
					child.layout(0, 0, Hubub._DisplayMetrics.widthPixels, _topButtons.getMeasuredHeight() + _chatSession.getMeasuredHeight());
				}
			}

		}
	}


	@Override
	public void releaseInstance() {
		// TODO Auto-generated method stub

	}
}