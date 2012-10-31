package com.hububnet.util;

import com.hububnet.DroidHubub;

import android.graphics.Color;
import android.graphics.Typeface;

public class HububLabel extends HububWidgett{
	HububLabelView _labelView;

	public HububLabel(){
		super();
		_labelView = new HububLabelView(DroidHubub.getInstance());
		_labelView.setTextColor(Color.BLACK);
		_labelView.setSingleLine(true);
		this.addVertWidget(_labelView);
	}
	
	public HububLabel(String name){
		this();
		_labelView.setText(name + ": ");
	}
	
	public HububLabel(String name, boolean colon){
		this();
		_labelView.setText(name +((colon)?":":""));
	}

	
	public void setText(String text){
		_labelView.setText(text);
	}
	
	public String getText(){
		return _labelView.getText().toString();
	}
	
	public String getLabel(){
		return (String) _labelView.getText();
	}
	
	public void setFontColor(int color){
		_labelView.setTextColor(color);
	}
	
	public void setFontStyle(int style){
		Typeface tf = _labelView.getTypeface();
		_labelView.setTypeface(tf, style);
	}
	
	public void setFontSize(int size){
		_labelView.setTextSize(size);
	}


}
