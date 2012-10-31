package com.hububnet.util;

import java.util.ArrayList;

import com.hububnet.ruok.HububEmergencyPanel;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.hububnet.Hubub;

public class HububItemizedOverlay extends ItemizedOverlay<OverlayItem>{
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private OverlayItem _item;
	private Drawable _drawable;

	public HububItemizedOverlay(Drawable arg0) {
		super(boundCenter(arg0));
		_drawable = arg0;
		//super(arg0);
		// TODO Auto-generated constructor stub
	}

	//protected boolean onTap(int index){
	//	HububOverlayItem item = (HububOverlayItem)mOverlays.get(index);
	//	Hubub.Logger("HububItemizedOverlay: onTap: title: " +item.getTitle() +",index: " +index);
	//	item.onSelected(this);
	//	return true;
	//}

	public boolean onTap(GeoPoint p, MapView mapView){
		try{
		Hubub.Logger("HububItemizedOverlay: onTap(int p, MapView mapView)...");
		mapView.removeAllViews();
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		return super.onTap(p, mapView);
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		//return null;
		return mOverlays.get(i);
	}

	public void addItem(OverlayItem item) {
		mOverlays.add(item);
		_item = item;
		populate();
	}
	
	public void removeItem(OverlayItem item){
		mOverlays.remove(item);
	}


	@Override
	public int size() {
		// TODO Auto-generated method stub
		//return 0;
		return mOverlays.size();
	}

}
