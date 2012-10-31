package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.R;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HububButton extends HububWidgett implements OnClickListener{

	private static final String View = null;
	HububButtonView _buttonView;
	HububButtonListener _listener = null;
	int _widthPadding = 20;
	int _heightPadding = 20;
	boolean _noSizeToFit = false;
	Branding _brand = null;
	ButtonInfo _buttonInfo = null;
	
	public class Branding extends HububWidgett{
		private HububImage _logo;
		private HububLabel _desc;
		
		public Branding(){
			_logo = new HububImage();
			//_logo.setImageURL(logoURL);
			int imageWidth = Hubub.getScaledWidth(70);
			_logo.setImageSize(imageWidth, imageWidth/2);
			_logo.resizeEnabled(false);
			//_logo.reLoadImage();
			
			_desc = new HububLabel();
			//_desc.setText(desc);
			_desc.setFontStyle(Typeface.BOLD);
			_desc.setGaps(5,0);
			this.addHorizWidget(_logo);
			this.addHorizWidget(_desc);
		}
		
		public void setLogoURL(String logURL){
			_logo.setImageURL(logURL);
			_logo.reLoadImage();
			this.reLayoutHoriz();
		}
		
		public void setDesc(String desc){
			_desc.setText(desc);
			this.reLayoutHoriz();
		}
	}
	
	public class ButtonInfo extends HububWidgett{
		public HububLabel _header;
		public Branding _branding;
		public HububLabel _footer;
		
		public ButtonInfo(){
			_header = new HububLabel();
			_header.setFontSize(Hubub.getScaledWidth(25));
			_header.setFontStyle(Typeface.BOLD);
				_branding = new Branding();
			_footer = new HububLabel();
			_footer.setFontSize(Hubub.getScaledWidth(15));
			_footer.setFontStyle(Typeface.BOLD);
			//this.addVertWidget(_footer);
		}
		
		public void update(String header, String footer, String logoURL, String desc){
			this.removeView(_header);
			this.removeView(_footer);
			this.removeView(_branding);
			
			if(header.length() > 0){
				_header.setText(header);
				this.addVertWidget(_header);
			}
			if(logoURL.length() > 0){
				_branding.setLogoURL(logoURL);
				_branding.setDesc(desc);
				this.addVertWidget(_branding);
			}
			if(footer.length() > 0){
				_footer.setText(footer);
				this.addVertWidget(_footer);
			}
		}
	}

	public HububButton(String name, String tag){
		super();
		this.setTag(tag);
		_buttonView = new HububButtonView(DroidHubub.getInstance());
		//_buttonView.setBackgroundColor(Color.MAGENTA);
		_buttonView.setText(name);
		_buttonView.setOnClickListener(this);
		_buttonView.setTextColor(new ColorStateList(
				new int[][] {
						new int[] { android.R.attr.state_focused },
						new int[] {android.R.attr.state_pressed},
						new int[] {android.R.attr.state_selected},
						new int[0],
				}, new int[] {
						//Color.rgb(0, 0, 255),
						Color.WHITE,
						Color.GREEN,
						Color.GREEN,
						Color.BLACK,
				}
		));
		if(Build.MANUFACTURER.equals("motorola") && Build.VERSION.RELEASE.startsWith("2.2"))
			_buttonView.setTextColor(Color.WHITE);

		//_buttonView.setBackgroundResource(resid);
		//Typeface tf = _buttonView.getTypeface();
		//_buttonView.setTypeface(tf, Typeface.BOLD);
		//_buttonView.reDraw();
		this.addVertWidget(_buttonView);
	}
	
	//public ButtonInfo setBranding(String logoURL, String desc){
	//	_buttonInfo = new ButtonInfo(logoURL, desc);
	//	this.addView(_buttonInfo);
	//	_buttonInfo.layout(0, 0, _buttonInfo.getMeasuredWidth(), _buttonInfo.getMeasuredHeight());
	//	return _buttonInfo;
	//}
	
	public void updateButtonInfo(String header, String footer, String logoURL, String desc, int logoWidth, int logoHeight){
		if(_buttonInfo == null){
			_buttonInfo = new ButtonInfo();
			this.addView(_buttonInfo);
		}
		_buttonInfo.update(header, footer, logoURL, desc);
		_buttonInfo.layout(0, 0, _buttonInfo.getMeasuredWidth(), _buttonInfo.getMeasuredHeight());		
	}
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(resolveSize(_buttonView.getMeasuredWidth(), widthMeasureSpec),
				resolveSize(_buttonView.getMeasuredHeight(), heightMeasureSpec));

	}
	
	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		if(_buttonInfo == null) return;
		int l = this.getMeasuredWidth()/2 - _buttonInfo.getMeasuredWidth()/2;
		//int t = _buttonView.getTop() +_buttonView.getMeasuredHeight() - _buttonInfo.getMeasuredHeight() -5;
		int t = this.getMeasuredHeight()/2 - _buttonInfo.getMeasuredHeight()/2;
		
		_buttonInfo.layout(l, t, l+_buttonInfo.getMeasuredWidth(), t+_buttonInfo.getMeasuredHeight());
	}

	public HububButton setName(String name){
		_buttonView.setText(name);
		this.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		return this;
	}

	public void click(){
		_buttonView.performClick();
	}

	public void setTextColor(int color){
		_buttonView.setTextColor(color);
	}
	
	public void setBackgroundResource(int resid){
		_buttonView.setBackgroundResource(resid);
	}

	public HububButton setNameTag(String name, String tag){
		setName(name);
		this.setTag(tag);
		return this;
	}


	public void setBackgroundColor(int color){
		_buttonView.setButtonColor(color);
	}


	public void setSize(int width, int height){
		_buttonView.setSize(width, height);
	}

	public void setHeight(int height){
		ViewGroup.LayoutParams lp = _buttonView.getLayoutParams();
		lp.height = height;
	}

	public void setFontStyle(int style){
		Typeface tf = _buttonView.getTypeface();
		_buttonView.setTypeface(tf, style);
	}


	public void setFontSize(int size){
		_buttonView.setTextSize(size);
	}

	public void setRadius(int radius){
		_buttonView.setRadius(radius);
	}


	public void setFontStyleSize(int style, int size){
		Typeface tf = _buttonView.getTypeface();
		_buttonView.setTypeface(tf, style);
		_buttonView.setTextSize(size);
	}

	public void setListener(HububButtonListener listener){
		_listener = listener;
	}



	public void setSingleLine(boolean setSingleLine){
		_buttonView.setSingleLine(setSingleLine);

	}

	public boolean setFocus(){
		Hubub.Logger("HububButton: setFocus: this.isFocusable: " + this.isFocusable() +", _buttonView.isFocusable: " +_buttonView.isFocusable());
		return _buttonView.requestFocus();
	}



	/* View.OnClickListener Protocol */
	public void onClick(View arg0) {
		Hubub.Logger("HububButton: buttonPressed...tag: " +this.getTag());
		if(_listener != null) _listener.buttonPressed(this);
	}

}
