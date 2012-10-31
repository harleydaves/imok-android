package com.hububnet.docs;

import com.hububnet.Hubub;

public class HububWebService extends HububXMLDoc{

	public HububWebService(){
		super("HububWebService");
		addElement("Header");
		//HububIDs ids = HububIDs.getInstance();
		//addAttrib("FngPrnt", ids.getDeviceID());
		//addAttrib("SessionID", ids.getSessionID());
		//addAttrib("EntityID", ids.getEntityID());
		addAttrib("FngPrnt", HububCookies.getCookie("DeviceID"));
		addAttrib("SessionID", HububCookies.getCookie("SessionID"));
		addAttrib("EntityID", HububCookies.getCookie("EntityID"));
		if(Hubub.getInstance().isFirstMessage()){
			addAttrib("RealEntityID", HububCookies.getCookie("EntityID"));
		}
		addAttrib("Release", Hubub.getHububRelease());
		pop();
		addElement("Payload");
	}
	
	public void setHeader(String header, String value){
		reset();
		getElements("Header");
		addAttrib(header, value);
	}
	
	public String getHeader(String header){
		reset();
		getElements("Header");
		return getAttrib(header);
	}
	
	public void removeHeader(String header){
		reset();
		getElements("Header");
		removeAttrib(header);
	}
	
	public HububWebService setFngPrnt(String fngprnt){
		reset();
		getElements("Header");
		addAttrib("FngPrnt", fngprnt);
		return this;
	}
	
	public String getFngPrnt(){
		reset();
		getElements("Header");
		return getAttrib("FngPrnt");
	}
	
	public HububWebService setSessionID(String sessionID){
		reset();
		getElements("Header");
		addAttrib("SessionID", sessionID);
		return this;
	}
	
	public String getSessionID(){
		reset();
		getElements("Header");
		return getAttrib("SessionID");
	}
	
	public HububWebService setEntityID(String entityID){
		reset();
		getElements("Header");
		addAttrib("EntityID", entityID);
		return this;
	}
	
	public String getEntityID(){
		reset();
		getElements("Header");
		return getAttrib("EntityID");
	}
	
	public HububWebService setError(String error){
		reset();
		getElements("Header");
		addAttrib("Error", error);
		return this;
	}
	
	public String getError(){
		reset();
		getElements("Header");
		return getAttrib("Error");
	}
	
	public HububWebService setPayload(String payload){
		reset();
		getElements("Payload");
		addText(payload);
		return this;
	}
	
	public String getPayload(){
		reset();
		getElements("Payload");
		return getText();
	}
	
	/*****************/
	public static void mainTest(String[] args){
		
		HububServices services = new HububServices();
		//HububService service = services.addServiceCall("DummyService");
		//HububService service1 = services.addServiceCall("DummyService 1");

		HububWebService hws = new HububWebService();
		hws.setPayload("This is my payload");
		hws.setPayload("THis is my next payload...");
		hws.setPayload(services.toString());
		Hubub.Logger("hws: " + hws.toString());
	}
}
