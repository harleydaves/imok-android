package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.graphics.Color;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class HububNVPair extends HububWidgett implements HububEditText.Listener{
	HububLabelView _label;
	HububEditText _value;
	boolean _border = false;
	boolean _hasChanged = false;
	String _origValue;
	protected Listener _listener;
	boolean _dropKeyboardOnUnFocus = false;

	public interface Listener{
		public void onUnFocus(HububNVPair nvPair);
	}

	public HububNVPair(){
		super();
		_value = new HububEditText();
		_value.setWidth(150);
		_value.setTextSize(12);
		_value.setHeight(12);
		_value.setEllipsize(TextUtils.TruncateAt.START);
		_value.setHorizontallyScrolling(true);
		_value.setListener(this);
		_value.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_MASK_CLASS);
		//_value.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);

		_label = new HububLabelView();
		_label.setTextColor(Color.BLACK);
		_label.setTextSize(15);
		//_value.getEditField().setListener(this);
		//Hubub.Logger("HububNVPair: constructor: after create _value..");
		//Hubub.Logger("HububNVPair: constructor: after  setMaxSize..");
		this.addHorizWidget(_label);
		this.addHorizWidget(_value);
		//setBorder(true);
		_origValue = "";
		//Hubub.Logger("HububNVPair: constructor: after add(_value)..");
		//setValue("");
		enableEdit(true);
		edit(false);
	}

	public void enableEdit(boolean edit){
		super.enableEdit(edit);
		if(!edit)
			_value.setEditable(false);
	}

	public void reset(){
		this.setValue("");
		//_value.setBackgroundColor(Color.WHITE);
	}

	public void edit(boolean edit){
		//Hubub.Logger("HububNVPair: edit: _enableEdit: " +_enableEdit 
		//		+", edit: " +edit +", current: " +_value.getValue() +", origValue: " +_origValue +", _hasChanged: " +_hasChanged);
		if(!_enableEdit) return;
		if(!edit){
			_hasChanged = (!_origValue.equals(_value.getValue()));
			if(_hasChanged) {
				setValue(_origValue);
			}
		}
		_value.setEditable(edit);

	}

	public boolean hasChanged(){
		return _hasChanged;
	}

	public void setListener(Listener listener){
		_listener = listener;
	}

	public void setNextCharListener(HububEditText.NextCharListener nextCharListener){
		_value.setNextCharListener(nextCharListener);
	}
	
	public void setValue(String text){
		_value.setText(text);
		_origValue = text;
		//setBackgroundColor(Color.WHITE);
		_hasChanged = false;
	}

	public HububEditText getEditText(){
		return _value;
	}


	public String getValue(){
		return _value.getValue();
	}


	public HububNVPair(String label){
		this();
		_label.setText(label + ": ");
	}

	public void setBackgroundColor(int color){
		_value.setBackgroundColor(color);
	}


	public int getAlignX(){
		return _label.getMeasuredWidth();
	}

	public boolean setFocus(){
		return _value.requestFocus();
	}

	public void setDropKeyboardOnUnFocus(boolean dropKeyboardOnUnFocus){
		_dropKeyboardOnUnFocus = dropKeyboardOnUnFocus;
	}


	/* HububEditText.Listener Protocol */
	public void onUnFocus() {
		Hubub.Logger("HububNVPair: onUnFocus...");
		String currentValue = _value.getValue();
		if(!_origValue.equals(currentValue)){
			Hubub.Logger("HububNVPair: onUnfocus: currentValue: " +currentValue);
			_hasChanged = true;
			//setBackgroundColor(Color.YELLOW);
			if(_widgetListener != null) _widgetListener.widgetHasChanged(this);
			invalidate();
		}
		if(_dropKeyboardOnUnFocus) Hubub._InputMethodMgr.hideSoftInputFromWindow(_value.getWindowToken(), 0);
		if(_listener != null) _listener.onUnFocus(this);
	}

	public void onFocus() {
		Hubub.Logger("HububNVPair: onFocus...");
		//Hubub._InputMethodMgr.showSoftInput(_value, 0);
	}

	public void returnEntered(HububEditText editText) {
		ViewGroup parent = (ViewGroup)this.getParent();
		int childCount = parent.getChildCount();
		int nextID = parent.indexOfChild(this) +1;
		View nextView = (nextID >= childCount)?parent.getChildAt(0):parent.getChildAt(nextID);
		_value.clearFocus();
		Hubub.Logger("HububNVPair: returnEntered: childCount: " +childCount +", nextID: " +nextID +", nextView: " +nextView);
		if(nextView instanceof HububWidgett) ((HububWidgett)nextView).setFocus();
		else nextView.requestFocus();
	}


}
