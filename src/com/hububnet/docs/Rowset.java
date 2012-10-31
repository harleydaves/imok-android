package com.hububnet.docs;

import org.w3c.dom.Element;

public class Rowset extends HububXMLDoc{

	Element _currentRow = null;

	
	public Rowset(HububXMLDoc doc, Element element){
		super(doc, element);
		getElements("Row");
	}
	
	public Rowset(HububXMLDoc doc, Element element, String name){
		super(doc, element);
		addAttrib("Name", name);
		addAttrib("Size", "0");
	}
	
	public String getName(){
		reset();
		return getAttrib("Name");
	}

	public int size(){
		reset();
		return Integer.parseInt(getAttrib("Size"));
	}
	
	public Rowset addRow(){
		reset();
		int size = Integer.parseInt(getAttrib("Size")) + 1;
		addAttrib("Size", "" + size);
		addElement("Row");
		return this;
	}
	
	public Rowset getRows(){
		reset();
		getElements("Row");
		return this;
	}
	
	public boolean nextRow(){
		return nextElement();
	}
	
	public Rowset setParm(String parm, String value){
		addAttrib(parm, value);
		return this;
	}
	
	public String getParm(String parm){
		return getAttrib(parm);
	}
}
