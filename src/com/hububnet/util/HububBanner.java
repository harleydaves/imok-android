package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HububBanner extends LinearLayout{
	private static HububBanner _instance;
	private static int LOGOWIDTH = (int) ((float)Hubub.getScaledWidth(80) * ((Build.MANUFACTURER.equals("motorola"))?1:1.2));	// This is what's allocated to logo, does not need to use it all...
	public static int LOGOHEIGHT = (int) ((float)Hubub.getScaledHeight(20) * ((Build.MANUFACTURER.equals("motorola"))?1:1.2));
	private static int SCREENWIDTH = Hubub._DisplayMetrics.widthPixels;
	HububLabel _alertLabel;
	ImageView _imageView;
	TextView _release;

	private HububBanner(Context context) {
		super(context);
		LinearLayout.LayoutParams containerParams
		= new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				0.0F);
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.setBackgroundColor(Color.GRAY);
		this.setLayoutParams(containerParams);
		LinearLayout.LayoutParams lp = (LayoutParams) this.getLayoutParams();

		_imageView = new ImageView(DroidHubub.getInstance());
		try{
			int resid = this.getResources().getIdentifier("andimoklogo", "drawable", "com.hububnet");
			Hubub.Logger("DroidHubub: onCreate: resid: " +resid);
			_imageView.setImageResource(resid);
			_imageView.setLayoutParams(new Gallery.LayoutParams(LOGOWIDTH, LOGOHEIGHT));
			_imageView.setScaleType(ImageView.ScaleType.FIT_START);
			this.addView(_imageView);
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}


		_release = new TextView(DroidHubub.getInstance());
		_release.setText("R" +Hubub._HububRelease);
		_release.setTextColor(Color.BLACK);
		Hubub.Logger("HububBanner: displayWidth: " +Hubub._DisplayMetrics.widthPixels +", imageView Width: " 
				+_imageView.getLayoutParams().width);
		_release.setWidth(Hubub._DisplayMetrics.widthPixels/3);
		_release.setGravity(Gravity.CENTER);		
		this.addView(_release);

		_alertLabel = new HububLabel("ABC: xyz", false);
		_alertLabel.setFontSize(15);
		_alertLabel.setFontColor(Color.BLACK);
		addView(_alertLabel);
		_alertLabel.setVisible(false);
	}


	public static HububBanner getInstance(){
		if(_instance == null)
			_instance = new HububBanner(DroidHubub.getInstance());
		return _instance;
	}

	public HububLabel getAlertLabel(){
		return _alertLabel;
	}

	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		int width = Hubub._DisplayMetrics.widthPixels;
		int height = LOGOHEIGHT;

		// Find out how big everyone wants to be
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(resolveSize(width, widthMeasureSpec),
				resolveSize(height, heightMeasureSpec));

	}

	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		int l = 0;
		int t = 0;
		_imageView.layout(0, 0, _imageView.getMeasuredWidth(), _imageView.getMeasuredHeight());
		l = SCREENWIDTH/2 - _release.getMeasuredWidth()/2;
		_release.layout(l, t, l + _release.getMeasuredWidth(), _release.getMeasuredHeight());
		l = SCREENWIDTH - _alertLabel.getMeasuredWidth();
		_alertLabel.layout(l, t, l+_alertLabel.getMeasuredWidth(), _alertLabel.getMeasuredHeight());
	}


}
