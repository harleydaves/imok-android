package com.hububnet.util;

import com.hububnet.DroidHubub;
import com.hububnet.Hubub;

import android.content.Intent;

public class HububSendMail implements HububTimer.Listener{
	private static HububSendMail _instance;
	boolean _isActive = false;
	HububTimer _timer;
	Intent _msg;

	private HububSendMail(){
	}

	public static HububSendMail getInstance(){
		if(_instance == null) _instance = new HububSendMail();
		return _instance;
	}

	public synchronized void send(String[] to, String subject, String message){
		try{
			_isActive = true;
			Hubub.Debug("2", "to: " +to +", subject: " +subject +", message: " +message);
			_msg=new Intent(Intent.ACTION_SEND);
			//msg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String[] recipients=to;
			//String[] carbonCopies={"hey.another@recipient.com"};
			_msg.putExtra(Intent.EXTRA_EMAIL, recipients);
			//msg.putExtra(Intent.EXTRA_CC, carbonCopies);
			_msg.putExtra(Intent.EXTRA_TEXT, message);
			_msg.putExtra(Intent.EXTRA_SUBJECT, subject);
			_msg.setType("message/rfc822");
			//msg.setType("text/html"); 
			

			//DroidHubub.getInstance().startActivity(Intent.createChooser(msg, "Choose Your Email App..."));
			//_timer.cancel();
			_timer = new HububTimer(this);
			_timer.schedule(180000);	// Allow email to sit for three minutes then abort...
			DroidHubub.getInstance().startActivityForResult(Intent.createChooser(_msg, "Choose Your Email App..."), Hubub.EmailIntent);

			Hubub.Debug("2", "Ending Send...");
		}catch(Exception e){
			Hubub.Debug("2", Hubub.getStackTrace(e));
		}

	}

	public boolean isActive(){
		return _isActive;
	}

	public void reset(){
		_isActive = false;
	}

	public void cancelTimer(){
		_timer.cancel();
	}

	/* HububTimer.Listener Protocol */
	public void timerExpired(HububTimer timer) {
		//boolean result = DroidHubub.getInstance().stopService(_msg);
		Hubub.Debug("2", "Shutting Down...");
		DroidHubub.getInstance().shutDown();

	}


}
