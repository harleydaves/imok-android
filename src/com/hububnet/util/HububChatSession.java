package com.hububnet.util;

import com.hububnet.Hubub;

public class HububChatSession extends HububWidgett{
	HububScrollTextReceiver _receive;
	HububScrollText _send;
	HububBanner _banner = HububBanner.getInstance();

	public HububChatSession(){
		super();
		_banner.measure(Hubub._DisplayMetrics.widthPixels, Hubub._DisplayMetrics.heightPixels);
		
		_receive = new HububScrollTextReceiver();
		_receive.setFocusable(true);
		_receive.setSize(new Double((Hubub._DisplayMetrics.widthPixels-Hubub.SCROLLBAR_WIDTH)*0.6).intValue(), 
				(int)((Hubub._DisplayMetrics.heightPixels-_banner.getMeasuredHeight() - Hubub.STATUSBAR_HEIGHT )*0.4 -20));
		_receive.setName("Receive: ");
		_receive.enableEdit(false);
		
		//for(int i=0; i<20; i++){
		//	_receive.write("this is a long long long long long line: " +i);
		//}
		
		_send = new HububScrollText();
		_send.setFocusableInTouchMode(true);
		_send.setSize(new Double((Hubub._DisplayMetrics.widthPixels-Hubub.SCROLLBAR_WIDTH)*0.4).intValue(), 
				(int)((Hubub._DisplayMetrics.heightPixels-_banner.getMeasuredHeight() - Hubub.STATUSBAR_HEIGHT -10)*0.4) -20);
		_send.setGaps(2, 0);
		_send.setName("Send: ");
		
		addHorizWidget(_receive);
		addHorizWidget(_send);
		
	}
	
	public void reset(){
		_send.reset();
		_receive.reset();
	}
	
	//protected void onFocus(int direction){
	//	invalidate();
	//	super.onFocus(direction);
	//}

	//protected void onUnfocus(){
	//	invalidate();
	//	super.onUnfocus();
	//}

	
	public HububScrollTextReceiver getReceive(){
		return _receive;
	}
	
	public HububScrollText getSend(){
		return _send;
	}


}
