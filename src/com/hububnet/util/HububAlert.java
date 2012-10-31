package com.hububnet.util;

import com.hububnet.Hubub;
import com.hububnet.docs.HububCookies;

import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

public class HububAlert extends HububPopup implements HububButtonListener{

	private static HububAlert _instance;
	HububLabel _heading;
	HububLabelView _text;
	HububButton _dismissBtn;
	HububButton _secondButton;
	private Listener _listener;
	private boolean _removeButton = false;
	
	public interface Listener{
		public void buttonPressed(HububButton button);
	}
	
	protected HububAlert(){
		super();
		String heading = HububCookies.getCookie("CarrierName");
		if(heading == null)
			heading = "Hubub";
		heading = "IMOK";
		heading += " - ";
		_heading = new HububLabel(heading +"Alert");
		_heading.setFontStyle(Typeface.BOLD_ITALIC);
		_heading.setAlignX(Hubub._DisplayMetrics.widthPixels/2);
		
		_text = new HububLabelView();
		_text.setTextColor(Color.BLACK);

		_dismissBtn = new HububButton("Dismiss", "dismiss");
		_dismissBtn.setGaps(5, 0);
		_dismissBtn.setListener(this);
		
		_secondButton = new HububButton("Second Button", "second");
		_secondButton.setGaps(5, 0);
		_secondButton.setListener(this);
		_secondButton.setVisible(false);

		addVertWidget(_heading);
		addVertWidget(_text);
		addVertWidget(_secondButton);
		addVertWidget(_dismissBtn);
		
		//this.setPadding(8);
	}
	
	public static HububAlert getInstance(){
		if(_instance == null) _instance = new HububAlert();
		return _instance;
	}
	
	public void alert(String text){
		_text.setText(text);
		_dismissBtn.setVisible(!_removeButton);
		show();
		//this.reDrawThis();
	}
	
	public void setListener(Listener listener){
		_listener = listener;
	}
	
	public void setDismissBtn(String name, String tag){
		_dismissBtn.setNameTag(name, tag);
	}
	
	public void setSecondBtn(String name, String tag){
		_secondButton.setNameTag(name, tag);
		_secondButton.setVisible(true);
	}
	
	public HububAlert removeButton(){
		_removeButton = true;
		return this;
	}
	
	/*
	 * HububButtonListener Protocol...
	 * @see com.hubub.utils.HububButtonListener#buttonPressed(com.hubub.utils.HububButton)
	 */
	public void buttonPressed(HububButton button) {
		Hubub.Debug("2", "button.tag: " +button.getTag());
		if(_listener != null) _listener.buttonPressed(button);
		hide();
		_listener = null;
		_secondButton.setVisible(false);
		_dismissBtn.setNameTag("Dismiss", "dismiss");
	}
}
