package com.hububnet.util;

import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;

public class HububStreamHandle implements InvokerListener{
	String _entityID;
	String _stream;
	String _myEntityID;
	String _myDeviceID;
	HububStreamConnector _connector;
	Listener _listener;
	boolean _isOpen;
	
	static int OPENCOUNT = 0;
	
	public interface Listener{
		public void messageReceived(HububServices services);
		public void messageDeleted(String time, String streamName);
		public void handleStatus(HububStreamHandle handle, String tag);
	}
	
	public HububStreamHandle(String entityID, String stream, String myEntityID,
			String myDeviceID, HububStreamConnector connector, Listener listener){
		Hubub.Logger("HububStreamHandle: Constructor: entityID: " +entityID +", stream: " +stream +", myEntityID: " +myEntityID +", myDeviceID: " +myDeviceID);
		_entityID = entityID;
		_stream = stream;
		_myEntityID = myEntityID;
		_myDeviceID = myDeviceID;
		_connector = connector;
		_listener = listener;
		_isOpen = false;
	}
	
	public void open(){
		Hubub.Logger("HububStreamHandle: open...");
		_isOpen = true;
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("ControlStream");
		service.setTag("open");
		service.getInputs();
		service.setParm("StreamName", _entityID +"/" +_stream);
		service.setParm("EntityName", _myEntityID +"/" +_myDeviceID);
		if(OPENCOUNT == 0)
			OPENCOUNT = 1;
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);
	}
	
	public void close(){
		_connector.closeStreamName(_entityID +"/" +_stream);
		_isOpen = false;
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("ControlStream");
		service.setTag("close");
		service.getInputs();
		service.setParm("StreamName", _entityID +"/" +_stream);
		service.setParm("EntityName", _myEntityID +"/" +_myDeviceID);
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);
	}
	
	public void writeMsg(HububServices sendServices){
		if(!_isOpen)return;
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("ControlStream");
		service.setTag("writeMsg");
		service.getInputs();
		service.setParm("StreamName", _entityID +"/" +_stream);
		String servString = sendServices.toString();
		service.setParm("Msg", servString);
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);
		//HububWorking.getInstance().working();
	}
	
	public void deleteMsg(String time, String streamName){
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("ControlStream");
		service.setTag("deleteMsg");
		service.getInputs();
		service.setParm("StreamName", streamName);
		service.setParm("Time", time);
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);
		HububWorking.getInstance().working();
	}
	
	public boolean isOpen(){
		return _isOpen;
	}
	
	public void setIsOpen(boolean isOpen){
		Hubub.Logger("HububStreamHandle: setIsOpen: isOpen: " +isOpen);
		_isOpen = isOpen;
	}
	
	public void receiveMsg(HububServices receiveMsg){
		Hubub.Logger("HububSteamHandle: receiveMsg: receiveMsg: " +receiveMsg);
		if(_listener != null) _listener.messageReceived(receiveMsg);
	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		services.getServices();
		HububService hubServ = services.nextService();
		String tag = hubServ.getTag();
		if(tag.equals("open")){
			_isOpen = true;
		}
		else if(tag.equals("close")){
			_isOpen = false;
		}
		else if(tag.equals("writeMsg")){
			//HububWorking.getInstance().doneWorking();
		}
		else if(tag.equals("deleteMsg")){
			HububWorking.getInstance().doneWorking();
			hubServ.getInputs();
			String streamName = hubServ.getParm("StreamName");
			String time = hubServ.getParm("Time");
			if(_listener != null)
				_listener.messageDeleted(time, streamName);
		}
		if(_listener != null){
			_listener.handleStatus(this, tag);
		}
	}

}
