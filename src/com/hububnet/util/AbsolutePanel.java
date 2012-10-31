package com.hububnet.util;

import com.hububnet.DroidHubub;

import android.content.Context;
import android.view.ViewGroup;

@SuppressWarnings("deprecation")
public class AbsolutePanel extends ViewGroup{
	
	public AbsolutePanel()
	{
		super(DroidHubub.getInstance());
	}
	
	public AbsolutePanel(Context context) {
		super(DroidHubub.getInstance());
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
	}

}
