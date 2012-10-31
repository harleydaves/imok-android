package com.hububnet.util;

import com.hububnet.Hubub;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;

public class HububScrollTextReceiver extends HububWidgett{

	HububTextView _textView;
	HububLabel _label;

	public HububScrollTextReceiver(){
		//super();
		_label = new HububLabel();
		_label.setFontStyle(Typeface.BOLD_ITALIC);
		_label.setFontSize(15);
		//_label.setPixelSize(100, 50);
		//_textView = new HububTextView(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
		_textView = new HububTextView();
		_textView.setSelectAllOnFocus (false);
		_textView.setScrollContainer(true);
		_textView.setVerticalScrollBarEnabled(true);
		_textView.setMaxLines(5);
		_textView.setMovementMethod(new ScrollingMovementMethod());
		_textView.setTextColor(Color.BLACK);
		//_textView.setVisibleLines(20);
		addVertWidget(_label);
		addVertWidget(_textView);
		_textView.setTextSize(12);
		//this.setBorder(true);
	}

	public void setName(String name){
		_label.setText(name);
	}

	public void setSize(int width, int height){
		_textView.setWidth(width);
		_label.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		_textView.setHeight(height - _label.getMeasuredHeight());
	}
	
	public void write(String text){
		String oldText = _textView.getText().toString();
		String newText = oldText + text +"\n";
		//_textView.setText(newText);
		_textView.append(text +"\n");
		//_textView.setCursor(20);
		//Element el = _textView.getElement();
		//el.setScrollTop(el.getScrollHeight());
		//_textView.setVerticalScroll(newText.length());
		//_textView.moveCursorToVisibleOffset();
		//_textView.invalidate();
		Spannable tmp = (Spannable) _textView.getText(); 
		Selection.setSelection(tmp, tmp.length()); 

	}
	
	//protected void paint(Graphics graphics){
	//	graphics.drawRect(0, _label.getHeight(), getWidth(), getHeight()-_label.getHeight());
	//	super.paint(graphics);
	//}


	public void reset(){
		_textView.setText("");
	}
	
	public HububTextView getTextViewReceiver(){
		return _textView;
	}
	
	public void setEditable(boolean editable){
		_textView.setEnabled(true);
	}
	
	public void setFocusable(boolean focusable){
		_textView.setEnabled(true);
		_textView.setFocusableInTouchMode(focusable);
		_textView.setFocusable(focusable);

	}


	
	//protected boolean keyChar(char ch, int status, int time){
	//	Hubub.Logger("HububScrollText: keyChar...");
	//	this.invalidate();
	//	return super.keyChar(ch, status, time);
	//}
	
	//protected void onFocus(int direction){
	//	invalidate();
	//	super.onFocus(direction);
	//}

	//protected void onUnfocus(){
	//	invalidate();
	//	super.onUnfocus();
	//}

	//protected boolean navigationMovement(int dx, int dy, int status, int time){
	//	invalidate();
	//	return super.navigationMovement(dx, dy, status, time);
	//}

}
