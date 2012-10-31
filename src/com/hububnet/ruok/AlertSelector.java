package com.hububnet.ruok;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.HububTabPanel;
import com.hububnet.docs.HububCookies;
import com.hububnet.util.HububBanner;
import com.hububnet.util.HububButton;
import com.hububnet.util.HububButtonListener;
import com.hububnet.util.HububLabel;
import com.hububnet.util.HububNotificationPanel;
import com.hububnet.util.HububPopup;
import com.hububnet.util.HububPopupScreen;
import com.hububnet.util.HububWidgett;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class AlertSelector extends HububTabPanel implements HububButtonListener{
	HububButton _emergBtn;	// blue
	HububButton _alertBtn;	// red
	HububButton _noticeBtn;	// yellow
	HububButton _notifyBtn;	// orange
	HububLabel _header;
	HububWidgett _topButtons;
	String _buttonPushed;
	private Listener _listener;
	private int _noticeCount = 0;
	private boolean _fourBtns = false;

	private static AlertSelector _instance;
	static String _AlertTitle = "Alert";
	static String _ContinueAlertTitle = "Continue Alert...";
	static int BANNER_HEIGHT = 20;


	public interface Listener{
		public void alertSelected(AlertSelector selector);

	}

	private AlertSelector(){
		super();

		int[] gdColors = {Color.BLACK, Color.LTGRAY};
		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gdColors);
		this.setBackgroundDrawable(gd);
		int displayWidth = Hubub._DisplayMetrics.widthPixels;
		int displayHeight = Hubub._DisplayMetrics.heightPixels;
		HububBanner banner = HububBanner.getInstance();
		banner.measure(displayWidth, displayHeight);
		BANNER_HEIGHT = banner.getMeasuredHeight();
		DroidHubub droidHubub = DroidHubub.getInstance();
		//boolean fourBtns = (droidHubub._NCStatus != null && droidHubub._NCStatus.equals("Act"));

		_hubPanel.setCenterOn(displayWidth/2);
		int usableWidth = displayWidth;
		int usableHeight = displayHeight - BANNER_HEIGHT;

		HububButton.ButtonInfo buttonInfo;

		_header = new HububLabel("IMOK Personal Security", false);
		_header.setFontStyle(Typeface.BOLD);
		_header.setFontColor(Color.WHITE);
		_header.setFontSize(20);

		_emergBtn = new HububButton("", "emergency");
		_alertBtn = new HububButton("Alert", "alert");
		_notifyBtn = new HububButton("", "notify");
		_noticeBtn = new HububButton("Notices (0)", "notices");

		_hubPanel.addVertWidget(_header);
		
		String c_ncStatus = HububCookies.getCookie("NCStatus");
		if(c_ncStatus == null) c_ncStatus = "";
		String c_ncLogoURL = HububCookies.getCookie("NCLogoURL");
		if(c_ncLogoURL == null) c_ncLogoURL = "";
		String c_ncDescription = HububCookies.getCookie("NCDescription");
		if(c_ncDescription == null) c_ncDescription = "";
		String c_acStatus = HububCookies.getCookie("ACStatus");
		if(c_acStatus == null) c_acStatus = "";
		String c_acLogoURL = HububCookies.getCookie("ACLogoURL");
		if(c_acLogoURL == null) c_acLogoURL = "";
		String c_acDescription = HububCookies.getCookie("ACDescription");
		if(c_acDescription == null) c_acDescription = "";
		
		this.positionButton(c_ncStatus, c_ncLogoURL, c_ncDescription, c_acStatus, c_acLogoURL, c_acDescription);

	}
	
	public void reset(){
		this.positionButton("", "", "", "", "", "");
	}
	
	public void positionButton(String ncStatus, String ncLogoURL, String ncDescription, String acStatus, String acLogoURL, String acDescription){
		String c_ncStatus = HububCookies.getCookie("NCStatus");
		String c_ncLogoURL = HububCookies.getCookie("NCLogoURL");
		String c_ncDescription = HububCookies.getCookie("NCDescription");
		String c_acStatus = HububCookies.getCookie("ACStatus");
		String c_acLogoURL = HububCookies.getCookie("ACLogoURL");
		String c_acDescription = HububCookies.getCookie("ACDescription");
		
		if(!ncStatus.equals(c_ncStatus) ||
				!ncLogoURL.equals(c_ncLogoURL) ||
				!ncDescription.equals(c_ncDescription) ||
				!acStatus.equals(c_acStatus) ||
				!acLogoURL.equals(c_acLogoURL) ||
				!acDescription.equals(c_acDescription) ||
				_emergBtn.getParent() == null
				){
			int displayWidth = Hubub._DisplayMetrics.widthPixels;
			int displayHeight = Hubub._DisplayMetrics.heightPixels;
			int usableWidth = displayWidth;
			int usableHeight = displayHeight - BANNER_HEIGHT;
			_hubPanel.removeView(_alertBtn);
			_hubPanel.removeView(_notifyBtn);
			_hubPanel.removeView(_emergBtn);
			_hubPanel.removeView(_noticeBtn);
			
			String footer = "";
			_fourBtns = (ncStatus.equals("Act"));
			
			_alertBtn.setSize((int)(usableWidth*.8), (int)(usableHeight*((!_fourBtns)?0.22:0.10)));
			_alertBtn.setBackgroundColor(Color.BLUE);
			_alertBtn.setFontStyleSize(Typeface.BOLD, Hubub.getScaledWidth((!_fourBtns)?25:15));
			_alertBtn.setGaps((int)(usableHeight*0.05), 0);
			_alertBtn.setRadius(15);
			_alertBtn.setListener(this);
			_hubPanel.addVertWidget(_alertBtn);

			if(_fourBtns){
				_notifyBtn.setSize((int)(usableWidth*.8), (int)(usableHeight*0.22));
				_notifyBtn.setBackgroundColor(0xFFFFA500);	// Orange
				_notifyBtn.setFontStyleSize(Typeface.BOLD, Hubub.getScaledWidth(35));
				_notifyBtn.setGaps((int)(usableHeight*0.05), 0);
				_notifyBtn.setRadius(15);
				_notifyBtn.setListener(this);
				footer = "";
				if(!ncStatus.equals("Act")) footer = "(Not Activated!!)";
				_notifyBtn.updateButtonInfo("", footer, ncLogoURL, ncDescription, 0, 200);
				_hubPanel.addVertWidget(_notifyBtn);
			}
			
			_emergBtn = new HububButton("", "emergency");
			_hubPanel.addVertWidget(_emergBtn);
			_emergBtn.setSize((int)(usableWidth*.8), (int)(usableHeight*((!_fourBtns)?0.32:0.22)));
			_emergBtn.setBackgroundColor(Color.RED);
			_emergBtn.setFontStyleSize(Typeface.BOLD, Hubub.getScaledWidth(25));
			_emergBtn.setGaps((int)(usableHeight*0.05), 0);
			_emergBtn.setRadius(15);
			_emergBtn.setListener(this);
			if(!acStatus.equals("Act")) footer = "(Not Activated!!)";
			//footer = "((TESTING>>>))";
			_emergBtn.updateButtonInfo("Emergency", footer, acLogoURL, acDescription, 0, 100);
			
			_noticeBtn.setSize((int)(usableWidth*.5), (int)(usableHeight*((!_fourBtns)?0.15:0.08)));
			_noticeBtn.setBackgroundColor(Color.YELLOW);
			_noticeBtn.setFontStyleSize(Typeface.BOLD, Hubub.getScaledWidth((!_fourBtns)?15:10));
			_noticeBtn.setGaps((int)(usableHeight*0.05), 0);
			_noticeBtn.setRadius(15);
			_noticeBtn.setListener(this);
			_hubPanel.addVertWidget(_noticeBtn);
			
			// Now cache the latest results for next time...
			HububCookies.setCookie("NCStatus", ncStatus);
			HububCookies.setCookie("NCLogoURL", ncLogoURL);
			HububCookies.setCookie("NCDescription", ncDescription);
			HububCookies.setCookie("ACStatus", acStatus);
			HububCookies.setCookie("ACLogoURL", acLogoURL);
			HububCookies.setCookie("ACDescription", acDescription);
			
			HububCookies.getInstance().sync();
		}
	}

	public static AlertSelector getInstance(){
		if(_instance == null) _instance = new AlertSelector();
		return _instance;
	}

	public void setContinue(boolean continueAlert){
		_alertBtn.setName((continueAlert)?_ContinueAlertTitle:_AlertTitle);
		invalidate();
	}

	public void setListener(Listener listener){
		_listener = listener;
	}

	public void setNoticeCount(int count){
		_noticeCount = count;
		_noticeBtn.setName("Notices (" +_noticeCount +")");
	}

	public int getNoticeCount(){
		return _noticeCount;
	}

	public String getButtonPushed(){
		return _buttonPushed;
	}

	public void show(){
		//_hubPanel.setFocusable(true);
		Hubub.Logger("AlertSelector: show... this.isFocusable: " +this.isFocusable() +", _hubPanel.isFocusable(): " +_hubPanel.isFocusableInTouchMode());
		Hubub.Logger("AlertSelector: show... this.descendantFocusability: " +this.getDescendantFocusability() +", this.isFocusableInTouchMode: " +this.isFocusableInTouchMode());
		//HububEmergencyPanel.getInstance().getTopButtons().setTopPadding(40);
		DroidHubub.getInstance().displayTab(this);
		boolean tookFocus = _alertBtn.setFocus();
		Hubub.Logger("AlertSelector: show... isFocusable: tookFocus: " +tookFocus +", Device in touch mode: " +this.isInTouchMode());
	}

	public void hide(){
		//if(this.getVisibility() == View.VISIBLE)
		if(DroidHubub.isCurrentPanel(this))
			_droidHubub.displayTab(_previousPanel);
	}

	//public boolean isVisible(){
	//	return isVisible();;
	//}

	/* HububButtonListener Protocol */
	public void buttonPressed(HububButton button) {
		Hubub.Logger("HububAlertSelector: buttonPressed: " +button.getTag());
		if(button == _noticeBtn){
			_buttonPushed = "cancel";
			DroidHubub.getInstance().displayTab(HububNotificationPanel.getInstance());
		}
		else if(button == _alertBtn){
			_buttonPushed = "emergency";
			//this.hide();
		}
		else if(button == _emergBtn){
			_buttonPushed = "monitored";
			//this.hide();
		}
		else if(button == _notifyBtn){
			_buttonPushed = "notify";
		}
		if(_listener != null)
			_listener.alertSelected(this);
	}

	@Override
	public void releaseInstance() {
		_instance = null;

	}

}
