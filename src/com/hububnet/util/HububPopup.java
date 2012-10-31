package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class HububPopup extends HububWidgett{
	HububPopupScreen _popupScreen;
	LinearLayout _root;
	Context _context = DroidHubub.getInstance();
	int _backgroundColor = Color.YELLOW;
	
	public HububPopup() {
		super();
		_root = new LinearLayout(_context);
		LinearLayout.LayoutParams containerParams
		= new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		_root.setOrientation(LinearLayout.VERTICAL);
		_root.setBackgroundColor(_backgroundColor);
		_root.addView(this);

		_popupScreen = new HububPopupScreen(_context);
		_popupScreen.requestWindowFeature(Window.FEATURE_NO_TITLE);

		_popupScreen.addContentView(_root, containerParams);
		
	}
	
	public void setPosition(int x, int y){
		Hubub.Logger("HububPopup: setPosition: DisplayWidth: " +Hubub._DisplayMetrics.widthPixels +", DisplayHeight: " +Hubub._DisplayMetrics.heightPixels);
		this.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		int width = this.getMeasuredWidth();
		int height = this.getMeasuredHeight();
		int posx = x -Hubub._DisplayMetrics.widthPixels/2 +width/2;
		int posy = y -Hubub._DisplayMetrics.heightPixels/2 +height/2;
		WindowManager.LayoutParams parms = _popupScreen.getWindow().getAttributes();
		//Hubub.Logger("AlertSelector: constructor: parms.horizontalMargin: " +parms.horizontalMargin +", parms.height: " +parms.height);
		parms.x = posx;
		parms.y = posy;
		_popupScreen.getWindow().setAttributes(parms);

	}
	
	public void setTop(double top){
		this.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		//int width = this.getMeasuredWidth();
		int height = this.getMeasuredHeight();
		//int posx = x -Hubub._DisplayMetrics.widthPixels/2 +width/2;
		int posy = (int)(top * Hubub._DisplayMetrics.heightPixels) -Hubub._DisplayMetrics.heightPixels/2 +height/2;
		WindowManager.LayoutParams parms = _popupScreen.getWindow().getAttributes();
		//Hubub.Logger("AlertSelector: constructor: parms.horizontalMargin: " +parms.horizontalMargin +", parms.height: " +parms.height);
		parms.x = 0;
		parms.y = posy;
		_popupScreen.getWindow().setAttributes(parms);		
	}
	
	public HububPopupScreen getPopupScreen(){
		return _popupScreen;
	}
	
	public void setBackgroundColor(int color){
		_root.setBackgroundColor(color);
	}
	
	public void show(){
		_popupScreen.show();
	}
	
	public void hide(){
		_popupScreen.hide();
	}
	
	public void dismiss(){
		_popupScreen.dismiss();
	}

}
