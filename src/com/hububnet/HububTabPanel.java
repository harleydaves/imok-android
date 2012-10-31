package com.hububnet;

import com.hububnet.util.HububPanel;
import com.hububnet.util.HububWidgett;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public abstract class HububTabPanel extends HububWidgett{
	protected HububPanel _hubPanel;
	protected HububTabPanel _previousPanel;
	protected DroidHubub _droidHubub;
	protected ScrollView _scrollView;
	//ed static HububTabPanel _instance;

	public HububTabPanel(Context context) {
		super(context);
		_droidHubub =DroidHubub.getInstance();
		//this.setFocusable(true);
		//this.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		//this.setFocusableInTouchMode(true);
	}
	
	public HububTabPanel(){
		this(DroidHubub.getInstance());
		_hubPanel = new HububPanel();
		_scrollView = new ScrollView(DroidHubub.getInstance());
		_scrollView.addView(_hubPanel);
		_scrollView.setScrollbarFadingEnabled(false);
		//this.setScrollbarFadingEnabled(false);
		this.addView(_scrollView);
	}
	
	//public HububTabPanel(int defStyle) {
	//	super(DroidHubub.getInstance(), null, defStyle);
	//	_hubPanel = new HububPanel();
	//	this.addView(_hubPanel);
	//}
	
	protected void setPreviousPanel(HububTabPanel previousPanel){
		_previousPanel = previousPanel;
	}
	
	// Called whenever this tab is selected.  This super class purposely does nothing.
	public void onTabSelected(){
		Hubub.Logger("HububTabPanel: onTabSelected...");
		//Hubub._InputMethodMgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}
	
	// Called whenever this tab is de-selected.  This super class purposely does nothing.
	public void onTabDeSelected(){
		Hubub.Logger("HububTabPanel: onTabDeSelected...");
	}
	
	public abstract void releaseInstance();

}
