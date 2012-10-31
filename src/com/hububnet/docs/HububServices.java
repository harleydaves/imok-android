package com.hububnet.docs;

import com.hububnet.Hubub;

public class HububServices extends HububXMLDoc{

	public HububServices(){
		super("Services");
		addAttrib("Type", "Serial");
		addAttrib("Tag", "");
		addAttrib("EntityID", super.getEntityID());
		addAttrib("SessionID", super.getSessionID());
		addAttrib("DeviceID", HububCookies.getCookie("DeviceID"));
	}
	
	public String getEntityID(){
		reset();
		return getAttrib("EntityID");
	}
	
	public HububServices setEntityID(String entityID){
		reset();
		addAttrib("EntityID", entityID);
		return this;
	}
	
	public String getSessionID(){
		reset();
		return getAttrib("SessionID");
	}
	
	public String getType(){
		reset();
		return getAttrib("Type");
	}
	
	public HububServices setType(String type){
		reset();
		addAttrib("Type", type);
		return this;
	}
	
	public String getTag(){
		reset();
		return getAttrib("Tag");
	}
	
	public HububServices setTag(String value){
		reset();
		addAttrib("Tag", value);
		return this;
	}
	
	public HububService addServiceCall(String serviceParm){
		reset();
		addElement("Service");
		HububService newService = new HububService(this, currentElement(), serviceParm);
		newService.setEntityID(getEntityID());
		return newService;
	}
	
	public HububService getServices(int index){
		reset();
		if(getElements("Service", index) == null) return null;
		return new HububService(this, currentElement());
	}
	
	public HububServices getServices(){
		reset();
		getElements("Service");
		return this;
	}
	
	public HububService nextService(){
		if(nextElement()){
			return new HububService(this, currentElement());
		}
		return null;
	}

	/****** Testing *******/
	
	public static void mainTest(String[] args){
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("DummyService");
		Hubub.Logger("service: type: " + services.getType());
		services.setType("Foobar");
		services.setTag("Test1");
		Hubub.Logger("services: Tag: " + services.getTag());
		//HububService service1 = services.addServiceCall("DummyService 1");
		Hubub.Logger("services: " + services.toString());
		
		service.setTag("servicetag");
		service.setErrors("Bad mistake!!");
		service.getInputs();
		service.setParm("Amount", "45.36");
		service.setLongParm("LongName", "<SomeTag>This is the long value that can contain anything</SomeTag>");
		service.getOutputs();
		service.setParm("Value", "This is Value");
		service.setLongParm("LongName", "Long output name....");
		service.getInputs();
		Rowset rowset = service.addRowset("TestRowset");
		rowset.addRow();
		rowset.setParm("P1", "V1").setParm("P2", "V2");
		rowset.addRow();
		rowset.setParm("P1", "V11").setParm("P2", "V12");
		
		
		
		rowset = service.getRowset("TestRowset");
		while(rowset.nextRow()){
			Hubub.Logger("P1=" + rowset.getParm("P1") + ", P2=" + rowset.getParm("P2"));
		}
		rowset.addRow().setParm("P1", "V11").setParm("P2", "V12");
		rowset.getRows();
		while(rowset.nextRow()){
			Hubub.Logger("P1=" + rowset.getParm("P1") + ", P2=" + rowset.getParm("P2"));
		}
		Hubub.Logger("services: with Rowset " + services.toString());
		
		service = services.getServices(0);
		Hubub.Logger("service: Name: " + service.getName());
		Hubub.Logger("service: Tag: " + service.getTag());
		Hubub.Logger("service: Errors: " + service.getErrors());
		service.getInputs();
		Hubub.Logger("service: Input: Amount: " + service.getParm("Amount"));
		Hubub.Logger("service: LongInput: LongName: " + service.getLongParm("LongName"));
		service.getOutputs();
		Hubub.Logger("service: Output: Amount: " + service.getParm("Value"));
		Hubub.Logger("service: LongOutput: LongName: " + service.getLongParm("LongName"));
		
		service.zapInputs();
		Hubub.Logger("services: " + services.toString());
		
		HububWebService hws = new HububWebService();
		hws.setFngPrnt("007");
		hws.setPayload(services.toString());
		
		Hubub.Logger("hws: " + hws.toString());
		Hubub.Logger("hsw: FngPrnt: " + hws.getFngPrnt());
		Hubub.Logger("hsw: Payoad: " + hws.getPayload());
		
		HububServices services2 = new HububServices();
		services2.parse(hws.getPayload());
		Hubub.Logger("services2: " + services2.toString());
	
		// New HububService features...
		services = new HububServices();
		service = services.addServiceCall("NewTests");
		service.getInputs();
		Hubub.Logger("Stacksize after getInputs: " + service.stackSize());
		service.setParm("p1", "V1");
		service.setLongParm("LP1", "Long v1");
		Hubub.Logger("Stacksize after setLongInput: " + service.stackSize());
		service.setParm("P2", "V2");
		rowset = service.addRowset("FirstRowset");
		rowset.addRow();
		rowset.setParm("p1", "v1");
		rowset = service.addRowset("SecondRowset");
		rowset.addRow();
		rowset.setParm("p1", "v1in second rowset");
		Hubub.Logger("NewTests Service: " + services.toString());
		
		service.getInputs();
		rowset = service.getRowset("SecondRowset");
		Hubub.Logger("Second Rowset, p1: " + rowset.getParm("p1"));
		
		HububInvoice invoice = new HububInvoice();
		invoice.addItem("5", "0", "4.23", "Blue Flowers");
		invoice.addItem("2", "0", "3.15", "Red Flowers");
		invoice.addItem("1", "0", "5.00", "Potting Soil");
		Hubub.Logger("HububInvoice: " + invoice.toString());
		Hubub.Logger("HububServices: mainTest: invoice again: " +invoice.toString());
		
		
	}
}
