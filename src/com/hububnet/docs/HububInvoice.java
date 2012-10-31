package com.hububnet.docs;

import com.hububnet.Hubub;

public class HububInvoice extends HububXMLDoc{

	public HububInvoice(){
		super("HububInvoice");
		addElement("Header");
		addAttrib("InvoiceNumber", "");
		addAttrib("TaxPercent", "7");
		reset();
		addElement("Items");
		reset();
		addElement("SubTotal");
		addAttrib("Amount", "");
		reset();
		addElement("Tip");
		addAttrib("Amount", "");
		reset();
		addElement("Total");
		addAttrib("Amount", "");
	}
		
	public HububInvoice addDate(String month, String day, String year){
		reset().getElements("Header");
		addElement("Date");
		addElement("Month");
		addText(month);
		pop().addElement("Day").addText(day);
		pop().addElement("Year").addText(year);
		return this;
	}
	
	public HububInvoice addItem(String quantity, String discount, String price, String descr){
		reset();
		getElements("Items");
		addElement("Item");
		addAttrib("quantity", quantity);
		addAttrib("discount", discount);
		addAttrib("price", price);
		addAttrib("extended", "100");
		addElement("description");
		addText(descr);
		return this;
	}
	
	
	/************/
	public static void mainTest(String[] args){
		HububInvoice invoice = new HububInvoice();
		invoice.addDate("January", "12", "1947");
		invoice.addItem("1", "20", "50.02", "Blender, high capacity, blue in color");
		invoice.addItem("3", "15", "48.02", "Blender, low capacity, blue in color");
		
		Hubub.Logger("invoice: " + invoice.toString());
		
		HububInvoice newInvoice = new HububInvoice();
		newInvoice.parse(invoice.toString());
		Hubub.Logger("newInvoice: " + newInvoice.toString());
		
	}
}
