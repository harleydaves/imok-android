package com.hububnet.util;

import com.hububnet.Hubub;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;

public class HububCheckBox extends HububWidgett implements OnClickListener{
	HububLabelView _label;
	HububCheckBoxExt _checkBox;
	private Listener _listener;

	public interface Listener{
		public void onClick(HububCheckBox checkBox);
	}

	public HububCheckBox(){
		super();
		_label = new HububLabelView();
		_checkBox = new HububCheckBoxExt();
		_checkBox.setOnClickListener(this);
		_label.setTextColor(Color.BLACK);
		this.addHorizWidget(_label);
		this.addHorizWidget(_checkBox);
	}

	public HububCheckBox(String label){
		this();
		this.setLabel(label);

	}

	public void setListener(Listener listener){
		_listener = listener;
	}
	
	public void toggle(){
		this._checkBox.toggle();
	}

	public void setLabel(String label){
		_label.setText(label +": ");
	}

	public String getLabel(){
		String label = (String)_label.getText();
		return label.substring(0, label.indexOf(":"));
	}

	public boolean isChecked(){
		return _checkBox.isChecked();
	}

	public boolean hasChanged(){
		return(this.isChecked());
	}

	public String toString(){
		return "_label: " +this.getLabel() +", isChecked: " +_checkBox.isChecked();
	}

	/* OnClickListener Protocol */
	public void onClick(View v) {
		Hubub.Debug("2", "...");
		if(_listener != null) _listener.onClick(this);

	}

}
