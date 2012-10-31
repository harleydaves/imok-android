package com.hububnet.util;

import com.hububnet.DroidHubub;

import android.content.Context;
import android.widget.Gallery;
import android.widget.ImageView;

public class HububImageView extends ImageView{

	public HububImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public HububImageView(){
		super(DroidHubub.getInstance());
	}

	public void setImageSize(int width, int height) {
		this.setLayoutParams(new Gallery.LayoutParams(width, height));
		this.setScaleType(ImageView.ScaleType.FIT_START);
	}

}
