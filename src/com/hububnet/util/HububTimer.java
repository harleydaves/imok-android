package com.hububnet.util;

import java.util.Timer;
import java.util.TimerTask;

import com.hububnet.DroidHubub;


public class HububTimer{
	Listener _listener;
	String _tag;
	Task _task;
	Timer _timer;
	
	
	public interface Listener{
		public void timerExpired(HububTimer timer);
	}
	
	private class Task extends TimerTask{
		Listener _listener;
		HububTimer _timer;
		
		public Task(Listener listener, HububTimer timer){
			super();
			_listener = listener;
			_timer = timer;
		}
		
		public void run(){
			if(_listener != null){
				//final HububTimer me = this;
				Runnable runnable = new Runnable(){
					public void run(){
						_listener.timerExpired(_timer);
					}
				};
				//Application.getApplication().invokeLater(runnable);
				DroidHubub.getInstance().runOnUiThread(runnable);

				//_listener.timerExpired(_timer);
			}
		}
	}

	public HububTimer(Listener listener){
		_timer = new Timer();
		_task = new Task(listener, this);
	}
	
	public void schedule(long delay){
		//if(_timer != null)_timer.cancel();
		_timer.schedule(_task, delay);
	}
	
	public void schedule(long delay, long period){
		_timer.schedule(_task, delay, period);
	}
	
	public void cancel(){
		if(_timer != null) _timer.cancel();
	}
	public void setTag(String tag){
		_tag = tag;
	}
	
	public String getTag(){
		return _tag;
	}
}
