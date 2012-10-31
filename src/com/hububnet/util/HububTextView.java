package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.content.Context;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class HububTextView extends TextView implements KeyListener{
	HububTextViewListener _textViewListener = null;
	int _cursor = 0;

	public HububTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public HububTextView(){
		super(DroidHubub.getInstance());
	}
	
	public void setTextViewListener(HububTextViewListener textViewListener){
		_textViewListener = textViewListener;
	}

	
	/* KeyListener Protocol */

	public void clearMetaKeyState(View view, Editable content, int states) {
		// TODO Auto-generated method stub
		
	}

	public boolean onKeyDown(View view, Editable itext, int keyCode,
			KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER){
			String text = (String)getText();
			int textLength = text.length();
			Hubub.Logger("HububTextView: onKeyDown: text: " +text +" text.length: " +text.length());
			String oText = text.substring(_cursor, textLength);
			_cursor = textLength+1;
			Hubub.Logger("HububTextView: onKeyPress: oText: " +oText +" oText.length: " +oText.length());
			if(_textViewListener != null) _textViewListener.newLineEntered(oText);
			return true;
		}
		return false;
	}

	public boolean onKeyOther(View view, Editable text, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
