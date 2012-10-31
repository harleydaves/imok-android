package com.hububnet.util;

import java.util.Enumeration;
import java.util.Hashtable;

import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.docs.Rowset;


public class HububStreamConnector implements InvokerListener, HububTimer.Listener{

	String _ownerEntityID;
	String _streamName;
	String _myEntityID;
	String _myDeviceID;
	Hashtable _handles;
	HububTimer _timer;
	HububTimer _reOpenTimer;
	HububStreamListener _streamListener;
	
	static HububStreamConnector _instance;
	static boolean _hasConnected = false;
	
	private HububStreamConnector(){
		_handles = new Hashtable(2);
		_myEntityID = HububCookies.getCookie("EntityID");
		_myDeviceID = HububCookies.getCookie("DeviceID");
		_timer = new HububTimer(this);
		_reOpenTimer = new HububTimer(this);
	}
	
	public static HububStreamConnector getInstance(){
		if(_instance == null)
			_instance = new HububStreamConnector();
		return _instance;
	}
	
	public void connect(){
		_streamListener = HububStreamListener.getInstance();
	}
	
	public void reOpen(){
		Hubub.Logger("HububStreamConnector: reOpen...");
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("ControlStream");
		service.setTag("reOpen");
		service.getInputs();
		service.setParm("EntityName", HububCookies.getCookie("EntityID") +"/" +_myDeviceID);
		Rowset rowset = service.addRowset("Streams");
		Enumeration e = _handles.keys();
		String streamName;
		while(e.hasMoreElements()){
			streamName = (String)_handles.get(e.nextElement());
			rowset.addRow();
			rowset.setParm("StreamName", streamName);
		}
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);
		
	}
	
	public Hashtable getHandles(){
		return _handles;
	}
	
	public String getMyEntityID(){
		return _myEntityID;
	}
	
	public String getMyDeviceID(){
		return _myDeviceID;
	}
	
	public static HububStreamHandle getStreamHandle(String entityID, String stream, HububStreamHandle.Listener listener){
		HububStreamHandle retval = null;
		HububStreamConnector connector = HububStreamConnector.getInstance();
		String streamName = entityID +"/" +stream;
		retval = (HububStreamHandle)connector.getHandles().get(streamName);
		if(retval == null){
			retval = new HububStreamHandle(entityID, stream, _instance.getMyEntityID(),
					_instance.getMyDeviceID(), _instance, listener);
			_instance.getHandles().put(streamName, retval);
		}
		if(!_hasConnected){
			_instance.connect();
			_hasConnected = true;
			retval.setIsOpen(true);
		}
		return retval;
	}
	
	public void closeStreamName(String streamName){
		if(_handles != null)
			_handles.remove(streamName);
	}
	public void close(){
		if(_streamListener != null) _streamListener.close();
		_hasConnected = false;
	}
	
	public void processStreamMessage(HububServices services){
		Hubub.Logger("HububStreamConnector: processStreamMessage: services: " +services.toString());
		String streamName = services.getAttrib("StreamName");
		if(streamName != null){
			HububStreamHandle streamHandle = (HububStreamHandle) _handles.get(streamName);
			if(streamHandle != null) streamHandle.receiveMsg(services);
			else
				Hubub.Logger("Execption! HububStreamConnector: processStreamMessage: streamHanlde is NULL!!...");
		}
	}
	
	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		String error = services.getAttrib("Error");
		_timer.cancel();
		services.getServices();
		HububService service = services.nextService();
		String tag = service.getTag();
		if(error != null && error.equals("-1003")){
			Hubub.Logger("HububStreamConnector: onResponseReceived: error: " +error);
			_reOpenTimer.cancel();
			_reOpenTimer.schedule(30000);
		}
		else{
			if(tag.equals("readQueue")){
				String noop = services.getAttrib("Noop");
				service.getOutputs();
				String servString = service.getParm("Services");
				if(servString != null && noop == null){
					HububServices returnServices = new HububServices();
					returnServices.parse(servString);
					String streamName = returnServices.getAttrib("StreamName");
					if(streamName != null){
						HububStreamHandle streamHandle = (HububStreamHandle) _handles.get(streamName);
						streamHandle.receiveMsg(returnServices);
					}
				}
			}
			else if(tag.equals("reOpen")){
				_reOpenTimer.cancel();
			}
			this.connect();
		}
	}

	/* HububTimer.Listener Protocol */
	public void timerExpired(HububTimer timer) {
		if(timer == _timer){
			_timer.cancel();
			this.connect();
		}
		else if(timer == _reOpenTimer){
			this.reOpen();
		}
	}
	

}
