package com.hububnet.util;

import java.util.Hashtable;
import java.util.List;

import com.hububnet.ruok.HububEmergencyPanel;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.R;

public class HububMapView extends MapView implements OnClickListener{
	MapController _mc;
	List<Overlay> _mapOverlays;
	//HububItemizedOverlay _overlay;
	Hashtable<String, HububPinNote> _pinNotes;
	private static MapView.LayoutParams _MapParams;
	private static HububMapView _instance;
	private static String _MapViewKey;

	public HububMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	private HububMapView(String mapViewKey){
		super(DroidHubub.getInstance(), mapViewKey);
		this.setOnClickListener(this);
		_pinNotes = new Hashtable<String, HububPinNote>();
		_mc = this.getController();
		_mc.setZoom(Hubub.getScaledHeight(6));
		_mapOverlays = this.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.purplepin);

		//_overlay = new HububItemizedOverlay(drawable);	// Overlay needs a default pin which is stupid...
		//_mapOverlays.add(_overlay);
		drawable.setBounds(0, 0, 15, 15);

		_MapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT,
				//this.getPoint(),
				new GeoPoint(0, 0),
				0,
				0,
				MapView.LayoutParams.BOTTOM_CENTER);

	}

	//private static String DEVKEY = "05Zjkv6FGSRIDX4KwpFj_kWafwj0Yh0CC20ykxA";
	private static String DEVKEY = "05Zjkv6FGSRJDPYMyCdGmPTq82eX8zdDOvI7ATg";
	private static String PRODKEY = "05Zjkv6FGSRKwd5jq8CyRYRBccU3pP8R5cMMKgA";
	public static HububMapView getInstance(){
		if(_instance == null) {
			if(Hubub.SIGNEDSIM) _MapViewKey = PRODKEY;
			else{
				if(Hubub.ALWAYSDEVMAP) _MapViewKey = DEVKEY;
				else _MapViewKey = (!Hubub.HUBUBSIMULATOR && !Hubub.HUBUBDEBUG)?PRODKEY:DEVKEY;
			}
			Hubub.Debug("2", "_MapViewKey: " +_MapViewKey);
			_instance = new HububMapView(_MapViewKey);
		}
		return _instance;
	}

	public void moveTo(Coordinates coords){
		_mc.setCenter(coords.getGeoPoint());
	}

	public HububPinNote retrieveAnnotation(String entityID, boolean remove){
		HububPinNote pinNote = null;
		if((pinNote = _pinNotes.get(entityID)) == null){
			pinNote = new HububPinNote();
			pinNote.setEntityID(entityID);
			_pinNotes.put(entityID, pinNote);
		}
		return pinNote;
	}

	public void addAnnotation(HububPinNote pinNote){
		Hubub.Debug("2", "...");
		//_overlay.addItem(pinNote.getItem());
		//pinNote.setOverlay(_overlay);
		MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 
				ViewGroup.LayoutParams.WRAP_CONTENT,
				//this.getPoint(),
				pinNote.getPoint(),
				0,
				0,
				MapView.LayoutParams.BOTTOM_CENTER);

		this.removeView(pinNote);
		this.addView(pinNote, mapParams);
		pinNote.setPinColor(pinNote.getPinColor());
	}

	public void onClick(View v) {
		Hubub.Logger("HububMapView: onClick: view: " +v);
	}

	public void removePinInfo(){
		int count = getChildCount();
		for(int i=0; i<count; i++){
			View child = this.getChildAt(i);
			if(child instanceof HububPinInfo){
				this.removeView(child);
			}
		}
	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);	
		try{
			//Hubub.Logger("HububMapView: onLayout: left: " +left +", top: " +top +", right: " +right +", bottom: " +bottom);
			int count = getChildCount();
			Hubub.Logger("HububMapView: onLayout: childCount: " +count);
			for(int i=0; i<count; i++){
				View child = this.getChildAt(i);
				Hubub.Logger("HububMapView: onLayout: child: " +child);
				if((child.getVisibility() != View.GONE) && (child instanceof HububPinInfo)){
					HububPinInfo pinInfo = (HububPinInfo)child;
					GeoPoint geoPoint = pinInfo.getPin().getPoint();
					Point point = new Point();
					point = this.getProjection().toPixels(geoPoint, point);
					int x,y = 0;	// placement Coordinates

					// Now do Quadrant Switching...
					int pinX = point.x;
					int pinY = point.y;
					if(pinX > this.getMeasuredWidth()/2){ 		// Pin on right half
						x = pinX - pinInfo.getMeasuredWidth();
						if(pinY > this.getMeasuredHeight()/2){ 	// Pin on lower right
							y = pinY - pinInfo.getMeasuredHeight();
						}
						else{							// Pin on upper right
							y = pinY;
						}
					}
					else{	// Pin on left half
						x = pinX;
						if(pinY > this.getMeasuredHeight()/2){	// Pin on lower left
							y = pinY - pinInfo.getMeasuredHeight();					
						}
						else{							// Pin on upper left
							y = pinY;
						}
					}

					//Hubub.Logger("HububMapView: onLayout: geoPoint: " +geoPoint +", point: " +point +", x: " +x +", y: " +y);
					//Hubub.Logger("HububMapView: onLayout: child: " +child +", childleft: " +child.getLeft() +", childtop: " +child.getTop());
					pinInfo.layout(x, y,
							x + pinInfo.getMeasuredWidth(),
							y + pinInfo.getMeasuredHeight());

				}
			}
			for(int i=0; i<count; i++){
				View child = this.getChildAt(i);
				if((child.getVisibility() != View.GONE) && (child instanceof HububPinNote)){
					HububPinNote pinNote = (HububPinNote)child;
					GeoPoint geoPoint = pinNote.getPoint();
					Point point = new Point();
					point = this.getProjection().toPixels(geoPoint, point);
					int x,y = 0;	// placement Coordinates

					int sideSize = pinNote.getMeasuredHeight();
					//Hubub.Logger("HububMapView: onLayout: PinNotes: sideSize: " +sideSize +", child: " +child);
					x = point.x - sideSize/2;
					y = point.y - sideSize/2;
					pinNote.layout(x, y, x+sideSize, y+sideSize);
					//pinNote.bringToFront();
				}
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

}
