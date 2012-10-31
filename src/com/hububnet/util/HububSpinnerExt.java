package com.hububnet.util;


import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.content.DialogInterface;
import android.widget.Spinner;

public class HububSpinnerExt extends Spinner{
	Listener _listener;
	
	public interface Listener{
		public void onItemSelected(int itemIndex, HububSpinnerExt spinnerExt);
	}
	
	
	public HububSpinnerExt(){
		super(DroidHubub.getInstance());
	}
	
	public void setListener(Listener listener){
		_listener = listener;
	}
	
	
	public void onClick(DialogInterface dialog, int which){
		Hubub.Debug("2", "dialog: " +dialog +", which: " +which);
		if(_listener != null){
			_listener.onItemSelected(which, this);
		}
		super.onClick(dialog, which);
	}

}
