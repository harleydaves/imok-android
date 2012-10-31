package com.hububnet.util;

import com.hububnet.DroidHubub;

import android.content.Context;
import android.graphics.Rect;
import android.widget.TextView;

public class HububLabelView extends TextView{
	private Listener _listener;
	
	public interface Listener{
		public void onUnFocus();
	}

	public HububLabelView(Context context) {
		super(context);
	}
	
	public HububLabelView(){
		this(DroidHubub.getInstance());
	}
	
	public void setListener(Listener listener){
		_listener = listener;
	}
	
	protected void onFocusChanged (boolean focused, int direction, Rect previouslyFocusedRect){
		if(!focused){
			if(_listener != null) _listener.onUnFocus();
		}
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

}
