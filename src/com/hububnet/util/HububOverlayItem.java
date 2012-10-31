package com.hububnet.util;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.hububnet.Hubub;

public class HububOverlayItem extends OverlayItem{
	HububOverlayItem.Listener _listener;

	public HububOverlayItem(GeoPoint arg0, String arg1, String arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}
	
	public interface Listener{
		public void onSelected(HububOverlayItem item);
	}
	
	public void setListener(HububOverlayItem.Listener listener){
		_listener = listener;
	}
	
	public void onSelected(HububItemizedOverlay overlay){
		Hubub.Logger("HububOverlayItem: onSelected...");
		if(_listener != null) _listener.onSelected(this);
	}

}
