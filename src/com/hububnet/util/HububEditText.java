package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.util.HububLabelView.Listener;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.EditText;

public class HububEditText extends EditText{
	private Listener _listener;
	private NewLineListener _newLineListener;
	private NextCharListener _nextCharListener;
	int _cursor = 0;

	public interface Listener{
		public void onUnFocus();
		public void onFocus();
		public void returnEntered(HububEditText editText);
	}

	public interface NewLineListener{
		public void newLineEntered(String newLine);
	}
	
	public interface NextCharListener{
		public void nextCharEntered(String text);
	}

	public HububEditText(Context context) {
		super(context);
		this.setTextColor(Color.BLACK);
		//this.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		this.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_MASK_CLASS);
	}

	public HububEditText(){
		this(DroidHubub.getInstance());
	}
	
	public void reset(){
		_cursor = 0;
	}

	public void setListener(Listener listener){
		_listener = listener;
	}

	public void setNewLineListener(NewLineListener newLineListener){
		_newLineListener = newLineListener;
	}
	
	public void setNextCharListener(NextCharListener nextCharListener){
		_nextCharListener = nextCharListener;
	}

	public String getValue(){
		return this.getText().toString();
	}

	public boolean onKeyDown (int keyCode, KeyEvent event){
		//Hubub.Logger("HububEditText: onKeyDown: keyCode: " +keyCode);
		try{
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				Hubub.Logger("HububEditText: onKeyDown: ENTER...");
				if(_listener != null) _listener.returnEntered(this);
				if(_newLineListener != null){
					Hubub._InputMethodMgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
					String text = getText().toString();
					int textLength = text.length();
					Hubub.Logger("HububEditText: onKeyDown: _cursor: " +_cursor +", text: " +text +" text.length: " +text.length());
					String oText = text.substring(_cursor, textLength);
					if(oText.length() == 0) return true;
					_cursor = textLength+1;
					Hubub.Logger("HububTextView: onKeyPress: oText: " +oText +" oText.length: " +oText.length());
					_newLineListener.newLineEntered(oText);
					super.onKeyDown(keyCode, event);

				}
				return true;
			}
			else if(_nextCharListener != null){
				super.onKeyDown(keyCode, event);
				_nextCharListener.nextCharEntered(this.getValue());
				return false;
			}
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onFocusChanged (boolean focused, int direction, Rect previouslyFocusedRect){
		if(_listener != null){
			if(!focused)
				_listener.onUnFocus();
			else
				_listener.onFocus();
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	public void write(String text){
		String oldText = this.getText().toString();
		String newText = oldText + text +"\n";
		this.setText(newText);
		//_textView.setCursor(20);
		//Element el = _textView.getElement();
		//el.setScrollTop(el.getScrollHeight());
		//_textView.setVerticalScroll(newText.length());
		//_textView.invalidate();
	}


	public void setEditable(boolean editable){
		this.setEnabled(editable);
		this.setFocusableInTouchMode(editable);
		this.setFocusable(editable);
	}
}
