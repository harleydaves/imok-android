package com.hububnet.docs;

import org.w3c.dom.Element;

import com.hububnet.Hubub;


public class HububService extends HububXMLDoc{

	public HububService(HububXMLDoc doc, Element element){
		super(doc, element);
	}
	
	public HububService(HububXMLDoc doc, Element element, String service){
		super(doc, element);
		addAttrib("Name", service);
		addAttrib("Tag", "");
		addAttrib("EntityID", "");
		addElement("Inputs");
		pop();
		addElement("Outputs");
	}
	
	public HububService setEntityID(String entityID){
		reset();
		addAttrib("EntityID", entityID);
		return this;
	}
	
	public String getEntityID(){
		reset();
		return getAttrib("EntityID");
	}
	
	public String getName(){
		reset();
		return getAttrib("Name");
	}
	
	public String getTag(){
		reset();
		return getAttrib("Tag");		
	}
	
	public HububService setTag(String tag){
		reset();
		addAttrib("Tag", tag);
		return this;
	}
	
	public String getErrors(){
		reset();
		return getAttrib("Errors");
	}
	
	public HububService setErrors(String errors){
		reset();
		addAttrib("Errors", errors);
		return this;
	}
	
	public HububService getInputs(){
		reset();
		getElements("Inputs");
		//Hubub.Debug("2", "_elemStack size: " +_elemStack.size());
		return this;
	}
	
	public HububService getOutputs(){
		reset();
		getElements("Outputs");
		return this;
	}
	
	public String getParm(String parm){
		return getAttrib(parm);
	}
	
	public HububService setParm(String parm, String value){
		addAttrib(parm, value);
		return this;
	}
	
	public String getLongParm(String parm){
		getElements(parm);
		String longValue = getText();
		pop();
		return longValue;
	}
	
	public HububService setLongParm(String parm, String longValue){
		//Hubub.Logger("setLongParm Entry stack size: " + stackSize());
		addElement(parm);
		addText(longValue);
		pop();
		//Hubub.Logger("setLongParm Exit stack size: " + stackSize());
		return this;
	}
	
	public Rowset getRowset(String name){
		getElements("Rowset");
		while(nextElement()){
			if(getAttrib("Name").equals(name)){
				Element rowsetElement = currentElement();
				pop();
				return new Rowset(this, rowsetElement);
			}			
		}
		pop();
		return null;
	}
	
	public Rowset addRowset(String name){
		addElement("Rowset");
		Element rowsetElement = currentElement();
		pop();
		return new Rowset(this, rowsetElement, name);	
	}
	public HububService zapInputs(){
		reset();
		if(removeElement("Inputs") == null) return null;
		return this;
	}
	
	public void resetInputs(){
		zapInputs();
		addElement("Inputs");
	}
		
	public HububService zapOutputs(){
		reset();
		if(removeElement("Outputs") == null) return null;
		return this;
	}
	
	public void resetOutputs(){
		zapOutputs();
		addElement("Outputs");
	}
	
	public String toString(){ // Override toString in super class
		return null;
	}
	
	/***** Testing ******/
	public static void mainTest(String[] args){
		
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("TestCall");
		service.getInputs();
		service.setParm("Dummy", "dummp");
		
		Hubub.Logger("Services: " +services.toString());
	}
}
