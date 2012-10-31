package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/*
 * Hubub GPS utility singleton to access/manage GPS hardware
 */

public class HububGPS implements HububTimer.Listener, LocationListener{
	//CLLocationManager _logMgr;
	double _lat;
	double _long;
	double _alt;
	int _updateCount;
	HububTimer _timer;
	boolean _isRunning;
	private static HububGPS _instance;
	Listener _listener = null;

	Criteria _criteria;
	LocationManager _locMgr;
	//boolean _firstRun = true;
	boolean _firstNetwork = true;
	boolean _firstGPS = true;
	LocationProvider _lowProv;	// low accuracy provider
	LocationProvider _highProv;	// high accuracy provider
	LocationProvider _netProv;	// Network provider

	public interface Listener{
		public void coordUpdate(String lati, String longi, String alt);
	}

	private HububGPS(){
		_locMgr = (LocationManager) DroidHubub.getInstance().getSystemService(Context.LOCATION_SERVICE);
		_lat = 38.331659;
		_long = -121.030178;
		_alt = 1;
		_isRunning = false;
		_timer = new HububTimer(this);
		String lat = HububCookies.getCookie("LastLat");
		if(lat != null){
			_lat = Double.parseDouble(lat);
			_long = Double.parseDouble(HububCookies.getCookie("LastLong"));
			_alt = Double.parseDouble(HububCookies.getCookie("LastAlt"));
		}
		
		
		// Define both coarse and fine criteria
		Criteria coarseCrit = new Criteria();
		coarseCrit.setAccuracy(Criteria.ACCURACY_COARSE);
		coarseCrit.setAltitudeRequired(true);
		coarseCrit.setBearingRequired(false);
		coarseCrit.setSpeedRequired(false);
		coarseCrit.setCostAllowed(true);
		coarseCrit.setPowerRequirement(Criteria.POWER_HIGH);
		
		Criteria fineCrit = new Criteria();
		fineCrit.setAccuracy(Criteria.ACCURACY_FINE);
		fineCrit.setAltitudeRequired(true);
		fineCrit.setBearingRequired(false);
		fineCrit.setSpeedRequired(false);
		fineCrit.setCostAllowed(true);
		fineCrit.setPowerRequirement(Criteria.POWER_HIGH);
		
		//_lowProv = _locMgr.getProvider(_locMgr.getBestProvider(coarseCrit, true));
		_lowProv = _locMgr.getProvider(LocationManager.GPS_PROVIDER);
		_netProv = _locMgr.getProvider(LocationManager.NETWORK_PROVIDER);
		//_lowProv = _locMgr.getProvider(LocationManager.NETWORK_PROVIDER);
		//_highProv = _locMgr.getProvider(_locMgr.getBestProvider(fineCrit, true));
	
	}

	public static HububGPS getInstance(){
		if(_instance == null)
			_instance = new HububGPS();
		return _instance;
	}

	public void setListener(Listener listener){
		Hubub.Debug("2", "listener: " +listener);
		_listener = listener;
		//_firstRun = true;
		_firstNetwork = true;
		_firstGPS = true;
	}

	public boolean start(){
		boolean retval = true;
		if(_isRunning) return retval;
		Hubub.Debug("2", "start...");
		//_firstRun = true;
		_firstNetwork = true;
		_firstGPS = true;
		
		// try to get last known location...
		Location lastLoc = null;
		//lastLoc = _locMgr.getLastKnownLocation(_highProv.getName());
		if(lastLoc == null){
			lastLoc = _locMgr.getLastKnownLocation(_lowProv.getName());
			if(lastLoc == null)
				lastLoc = _locMgr.getLastKnownLocation(_netProv.getName());
		}
		if(lastLoc != null){
			_lat = lastLoc.getLatitude();
			_long = lastLoc.getLongitude();
			_alt = lastLoc.getAltitude();
			Hubub.Debug("2", "lastLoc: Provider: " +lastLoc.getProvider() +", lat: " +_lat +", long: " +_long +", alt: " +_alt);
		}

		// Start up the GPS...
		try {
			if(_netProv != null) _locMgr.requestLocationUpdates(_netProv.getName(), 0, 0f, this);
			if(_lowProv != null) _locMgr.requestLocationUpdates(_lowProv.getName(), 0, 0f, this);
			//_locMgr.requestLocationUpdates(_highProv.getName(), 0, 0f, this);
		} catch (Exception e) {
			Hubub.Debug("1", "LocationManager error: " +e.getMessage());
			HububAlert.getInstance().removeButton();
			HububAlert.getInstance().alert("This application requires GPS. Please exit and enable GPS support...");
			retval = false;
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		_updateCount = 0;
		_timer.schedule(0,90000);
		_isRunning = true;


		Hubub.Debug("2", "after requestLocationUpdates...");
		return retval;
	}

	public void stop(){
		if(!_isRunning) return;
		Hubub.Debug("2", "stop...");
		_timer.cancel();
		_isRunning = false;
		_locMgr.removeUpdates(this);
	}

	public String getAlt(){
		return "" +_alt;
	}

	public String getLat(){
		return "" +_lat;
	}

	public String getLong(){
		return "" +_long;
	}

	/* HububTimer.Listener Protocol */
	public void timerExpired(HububTimer timer) {
		//Backlight.enable(true, 255);
		Hubub.Debug("2", " lat: " +_lat +", long: " +_long +", Alt: " +_alt);
		if(_listener != null){
			_listener.coordUpdate(this.getLat(), this.getLong(), this.getAlt());
		}
	}


	/* Android LocationListener Protocol */
	public void onLocationChanged(Location location) {
		//Hubub.Debug("2", "provider: " +location.getProvider() +", hasAccuracy: " +location.hasAccuracy() +", accuracy: " +location.getAccuracy());
		String provider = location.getProvider();
		if(provider.equals(LocationManager.NETWORK_PROVIDER) && !_firstGPS)
			return;
		_lat = location.getLatitude();
		_long = location.getLongitude();
		_alt = location.getAltitude();
		//Hubub.Debug("2", "lat: " +_lat +", long: " +_long +", alt: " +_alt +", hasAltitude: " +location.hasAltitude());
		if(!location.hasAltitude())
			_alt = 0;
		HububCookies.setCookie("LastLat", "" +_lat);
		HububCookies.setCookie("LastLong", "" +_long);
		HububCookies.setCookie("LastAlt", "" +_alt);
		if(_firstGPS){
			if(provider.equals(LocationManager.GPS_PROVIDER)){
				if(_listener != null){
					_listener.coordUpdate(this.getLat(), this.getLong(), this.getAlt());	
					_firstGPS = false;
					_firstNetwork = false;
				}			
			}
		}
		if(_firstNetwork){
			if(provider.equals(LocationManager.NETWORK_PROVIDER)){
				if(_listener != null){
					_listener.coordUpdate(this.getLat(), this.getLong(), this.getAlt());	
					_firstNetwork = false;
				}				
			}
		}
		//if(_firstRun){
			//_locProv.setLocationListener(this, 15, 10, 10);
		//	_firstRun = false;
		//	if(_listener != null){
		//		_listener.coordUpdate(this.getLat(), this.getLong(), this.getAlt());					
		//	}
		//}
		
	}

	public void onProviderDisabled(String provider) {
		Hubub.Debug("2", "Provider: " +provider);
	}

	public void onProviderEnabled(String provider) {
		Hubub.Debug("2", "Provider: " +provider);
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		//Hubub.Debug("2", "Provider: " +provider +", status: " +status +", extras: " +extras.toString());
		
	}
}
