package com.hububnet.util;

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationManager;

public class Coordinates extends Location{
	//double _latitude;
	//double _longitude;
	//float _altitude;

	public Coordinates(double latitude, double longitude, double altitude){
		super(LocationManager.NETWORK_PROVIDER);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setAltitude(altitude);
	}
	
	public Coordinates(){
		super(LocationManager.NETWORK_PROVIDER);
	}
	
	public float distance(Coordinates coords){
		return super.distanceTo(coords);
	}
	
	public GeoPoint getGeoPoint(){
		return new GeoPoint((int)(this.getLatitude() *1e6), (int)(this.getLongitude() *1e6));
	}
	
	

	/*
	public void setLatitude(double latitude){
		_latitude = latitude;
	}

	public void setLongitude(double longitude){
		_longitude = longitude;
	}

	public void setAltitude(float altitude){
		_altitude = altitude;
	}

	public double getLatitude(){
		return _latitude;
	}

	public double getLongitude(){
		return _longitude;
	}

	public float getAltitude(){
		return _altitude;
	}

	// Compute distance in meters between two coordinates...
	
	public float distance(Coordinates coord2){
		double radius = 6378050;// Earth radius in meters at equator 3963.2;	// Earth radius in miles
		double d2r = 3.14159265358979323846/180;	// Degrees to Radians

		//var dLat = (lat2-lat1).toRad();
		double dLat = (coord2._latitude - _latitude) * d2r;
		double sindLatOver2 = Math.sin(dLat/2);

		//var dLon = (lon2-lon1).toRad(); 
		double dLon	 = (coord2._longitude - _longitude) * d2r;
		double sindLonOver2 = Math.sin(dLon/2);

		//var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		double a = sindLatOver2 * sindLatOver2 +

		//Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) * 
		Math.cos(_latitude * d2r) * Math.cos(coord2._latitude * d2r) *

		//Math.sin(dLon/2) * Math.sin(dLon/2); 
		sindLonOver2 * sindLonOver2;

		//var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

		//var d = R * c;
		return (float) (radius * c);


	}
	*/

}
