package com.hububnet;

import org.xml.sax.SAXParseException;

public interface HububURLConnectionListener{
	public void responseReceived(HububURLConnection urlConnection);
	
	public void connectionDidFail(HububURLConnection urlConnection);

}
