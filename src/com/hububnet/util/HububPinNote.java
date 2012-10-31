package com.hububnet.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

import com.hububnet.ruok.HububEmergencyPanel;

public class HububPinNote extends HububWidgett implements InvokerListener, OnClickListener{//, HububOverlayItem.Listener {
	Coordinates _coordinates = new Coordinates(0,0,0);
	GeoPoint _geoPoint = _coordinates.getGeoPoint();
	Coordinates _alertCoords;
	String _firstName;
	String _entityID;
	String _phoneNumber;
	String _dist;
	String _alt;
	String _pinColor = "";

	HububMapView _mapView;
	HububPinInfo _pinInfo;
	boolean _disableFocus = false;
	String _address;	// Street Addr
	String _address2;	// City, State Country Code
	HububOverlayItem _item;
	HububItemizedOverlay _overlay;
	private static MapView.LayoutParams _MapParams;
	HububImageView _image;


	public HububPinNote(){
		super();
		try{
			Hubub.Logger("HububPinNote: Constructor...");
			_mapView = HububEmergencyPanel.getInstance().getMapView();
			_image = new HububImageView();
			_image.setOnClickListener(this);
			this.addView(_image);

			this.setPinColor("Red");
			_pinInfo = new HububPinInfo(this);
			_MapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
					ViewGroup.LayoutParams.WRAP_CONTENT,
					this.getPoint(),
					0,
					0,
					MapView.LayoutParams.BOTTOM_CENTER);
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}
	
	public HububMapView getMapView(){
		return _mapView;
	}

	//public HububOverlayItem getItem(){
	//	return _item;
	//}

	//public void setDisableFocus(boolean disableFocus){
	//	_disableFocus = disableFocus;
	//}

	public void setAlertCoords(Coordinates alertCoords){
		_alertCoords = alertCoords;
	}

	public void setPinColor(String color){
		if(color.equals(_pinColor)) return;
		int resid = this.getResources().getIdentifier(color.toLowerCase() +"pin", "drawable", "com.hububnet");
		_image.setImageResource(resid);
		int size = Hubub.getScaledHeight(14);
		_image.setImageSize(size, size);
		_pinColor = color;
	}
	
	public String getPinColor(){
		return _pinColor;
	}

	//public void setOverlay(HububItemizedOverlay overlay){
	//	_overlay = overlay;
	//}

	public void setCoordinates(Coordinates coordinates){
		_coordinates = coordinates;
	}

	public Coordinates getCoordinates(){
		return _coordinates;
	}


	public void setFirstName(String firstName){
		_firstName = firstName;
		_pinInfo.setFirstName(firstName);
	}

	public String getFirstName(){
		return _firstName;
	}

	public void setEntityID(String entityID){
		_entityID = entityID;
	}

	public String getEntityID(){
		return _entityID;
	}

	public void setPhoneNumber(String phoneNumber){
		_phoneNumber = phoneNumber;
	}

	public String getPhoneNumber(){
		return _phoneNumber;
	}

	public void setDist(String dist){
		_dist = dist;
	}

	public String getDist(){
		return _dist;
	}

	public void setAlt(String alt){
		_alt = alt;
	}

	public String getAlt(){
		return _alt;
	}

	public GeoPoint getPoint(){
		return this._coordinates.getGeoPoint();
	}


	public String toString(){
		return ("firstName: " +_firstName +", pinColor: " +_pinColor +" entityID: " +_entityID +" phoneNumber: " +_phoneNumber
				+", dist: " +_dist +", coords: " +_coordinates);

	}

	/* HububBitmapField.Listener Protocol */
	//public void gainedFocus(int direction){
	//	Hubub.Logger("HububPinNote: gainedFocus... toString: " +toString());
		//if(_pinInfo.getManager() != null) return;	// already has focus...
		//int scale = Fixed32.div(_img.getHeight(), 25);
		//EncodedImage img = _img.scaleImage32(scale, scale);
		//_bitmap.setImage(img);
		//_mapPanel.setPinWithFocus(this);
		//_mapView.add(_pinInfo);
		//_pinInfo.reDrawThis();
	//	invalidate();
	//}

	//public void lostFocus() {
	//	Hubub.Logger("HububPinNote: lostFocus...");
		//if(_disableFocus || _pinInfo.getManager() == null) return;	// already lost focus...
		//int scale = Fixed32.div(_img.getHeight(), 15);
		//EncodedImage img = _img.scaleImage32(scale, scale);
		//_bitmap.setImage(img);
		//_mapPanel.setPinWithFocus(null);
		//_mapView.delete(_pinInfo);
		//_pinInfo.reset();
	//	invalidate();
	//}

	/* OnClickListener Protocol */
	public void onClick(View v) {
		Hubub.Logger("HububPinNote: onClick... firstName: " +_firstName +", point: " +this.getPoint());
		_mapView.removePinInfo();
		if(_dist == null){
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("UtilityServices");
			service.setTag("ReverseGeocode");
			service.getInputs();
			service.setParm("Lat", "" +this._coordinates.getLatitude());
			service.setParm("Long", "" +this._coordinates.getLongitude());
			Invoker invoker = new Invoker();
			invoker.sendAsEntityID("0");
			invoker.send(services, this);
			HububWorking.getInstance().working();
		}
		else{
			//_pinInfo.prepareToDisplay();
			_pinInfo.addCallButton();
			_mapView.addView(_pinInfo, _MapParams);

		}
	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		try{
			services.getServices();
			HububService hubServ = services.nextService();
			if(hubServ.getName().equals("UtilityServices")){
				//_pinInfo.prepareToDisplay();
				_mapView.addView(_pinInfo, _MapParams);
				//_pinInfo.addCallButton();
				hubServ.getOutputs();
				_pinInfo.addAddress(hubServ.getParm("Addr"), "");
				HububWorking.getInstance().doneWorking();
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}	
	}

}


