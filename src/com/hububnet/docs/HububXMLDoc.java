package com.hububnet.docs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.TransformerFactoryConfigurationError;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
//import org.w3c.dom.ls.DOMImplementationLS;
//import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import android.util.Log;

import com.hububnet.Hubub;

public class HububXMLDoc {
	Document _doc = null;
	Stack _elemStack = new Stack();
	NodeList _nl = null;
	int _nli = 0;	// Node List index
	DocumentBuilder _docBuilder;

	protected HububXMLDoc(){
		try {
			_docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			Hubub.Logger("HububXMLDoc: DocuemntBuilderFactory errer: " +e.getMessage());
			e.printStackTrace();
		}
	}

	protected HububXMLDoc(String docType){
		this();
		parse("<" + docType +"/>");
	}

	protected HububXMLDoc(HububXMLDoc doc, Element element){
		_doc = doc._doc; // Used for factory element creation only.
		_elemStack = new Stack();
		_elemStack.push(element);
	}

	protected String getEntityID(){
		return Hubub.getEntityID();
	}

	protected String getSessionID(){
		return Hubub.getSessionID();
	}

	protected HububXMLDoc pop(){
		if(_elemStack.size() > 1) _elemStack.pop();
		return this;
	}

	public int stackSize(){
		return _elemStack.size();
	}

	public HububXMLDoc reset(){
		_elemStack.setSize(1);
		return this;
	}

	protected HububXMLDoc addElement(String element){
		_elemStack.push(((Element)_elemStack.peek()).appendChild(_doc.createElement(element)));
		return this;
	}

	protected HububXMLDoc removeElement(String element){
		if(getElements(element, 0) == null) return null;
		Element current = currentElement();
		pop();
		((Element)_elemStack.peek()).removeChild(current);
		return this;
	}

	public HububXMLDoc addAttrib(String name, String value){
		((Element)_elemStack.peek()).setAttribute(name, value);
		return this;
	}

	public String getAttrib(String name){
		Element current = (Element)_elemStack.peek();
		if(!current.hasAttribute(name)) return null;
		return current.getAttribute(name);
	}

	protected HububXMLDoc removeAttrib(String name){
		((Element)_elemStack.peek()).removeAttribute(name);
		return this;
	}

	protected HububXMLDoc addCDATA(String value){
		CDATASection cdata = _doc.createCDATASection(value);
		((Element)_elemStack.peek()).appendChild(cdata);
		return this;
	}

	protected HububXMLDoc addText(String value){
		Element base = (Element)_elemStack.peek();
		Text text = _doc.createTextNode(value);
		Node firstChild = base.getFirstChild();
		if(firstChild != null){
			base.replaceChild(text, firstChild);
		}
		else{
			base.appendChild(text);
		}
		return this;
	}

	static String getNodeText(Node xmlNode) { // Workaround for GWT Firefox bug
		if(xmlNode == null)
			return "";
		StringBuffer result = new StringBuffer(4096);
		for (Node node = xmlNode.getFirstChild(); node != null; node =
			node.getNextSibling())
			result.append(node.getNodeValue());
		return result.toString();
	}


	protected String getText(){
		//return _elemStack.peek().getFirstChild().getNodeValue();
		return getNodeText((Element)_elemStack.peek());
	}

	protected HububXMLDoc getElements(String name, int index){
		NodeList nl = ((Element)_elemStack.peek()).getElementsByTagName(name);
		if(index > nl.getLength()-1) return null;
		_elemStack.push((Element)nl.item(index));
		return this;
	}

	protected HububXMLDoc getElements(String name){
		_nl = ((Element)_elemStack.peek()).getElementsByTagName(name);
		if(_nl == null) return null;
		_elemStack.push((Element)_nl.item(0));
		_nli = 0;
		return this;
	}

	protected boolean nextElement(){
		if(_nl == null || _nli >= _nl.getLength()){
			_nli = 0;
			_nl = null;
			return false;
		}
		pop();
		_elemStack.push((Element)_nl.item(_nli++));
		return true;
	}

	protected String getTagName(){
		return ((Element)_elemStack.peek()).getTagName();
	}

	protected Element currentElement(){
		return (Element)_elemStack.peek();
	}

	public HububXMLDoc parse(String input){
		//_doc = XMLParser.parse(input);
		try {
			_doc = _docBuilder.parse(new ByteArrayInputStream(input.getBytes("UTF-8")));
		} catch (SAXException e) {
			Hubub.Logger("HububXMLDoc: parse: SAXException: input: " +input +" Error: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
			//e.printStackTrace();
		} catch (IOException e) {
			Hubub.Logger("HububXMLDoc: parse: IOException: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));
			//e.printStackTrace();
		}
		_elemStack = new Stack();
		_elemStack.push(_doc.getDocumentElement());
		return this;
	}

	public String toString(){
		StringBuffer buf = new StringBuffer();
		this.unParse(_doc, buf);
		return buf.toString();

	}

	private void unParse(Node node, StringBuffer buf){
		try{
			//Hubub.Logger("HububXMLDoc: unParse...");
			short nodeType = node.getNodeType();
			String nodeName = node.getNodeName();
			if(nodeType == Node.TEXT_NODE){
				buf.append(node.getNodeValue().replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
				return;
			}
			if(nodeType == Node.DOCUMENT_NODE){
				buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				int numChi = node.getChildNodes().getLength();
				for(int i=0; i<numChi; i++){
					unParse(node.getChildNodes().item(i), buf);
				}
				return;
			}
			else{
				buf.append("<").append(nodeName);
				int numNodes = node.getAttributes().getLength();
				for(int i=0; i<numNodes; i++){
					Attr attr = (Attr)node.getAttributes().item(i);
					buf.append(" " +attr.getName() +"=\"");
					String attVal = attr.getValue().replaceAll("&", "&amp;")
					.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
					buf.append(attVal +"\"");
				}
			}
			if(node.hasChildNodes()){
				buf.append(">");
				int numChi = node.getChildNodes().getLength();
				for(int i=0; i<numChi; i++){
					unParse(node.getChildNodes().item(i), buf);
				}
				buf.append("</" +nodeName +">");
			}
			else {
				buf.append("/>");
			}
		}catch(Exception e){
			Hubub.Logger("hububXMLDoc: unParse: exception: buf: " +buf.toString());
			Hubub.Logger(Hubub.getStackTrace(e));
			//System.exit(0);
		}
		return;
	}

	// This is the unparse code supported in 2.2 and greater.  Wrote my own unparse (above) to support 2.1 and greater
	// Keep this code around in case we want it when we shift to min relase 2.2

	/*
	public String xxtoString(){
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(_doc);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String xmlString = result.getWriter().toString();
		System.out.println(xmlString);	
		return xmlString;

	}
	*/

	/*
	private static String getStringFromNode(Node root, int depth) throws IOException {


		StringBuilder result = new StringBuilder();
		//result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		if (root.getNodeType() == 3)
			result.append(root.getNodeValue());
		else {
			if (root.getNodeType() != 9) {
				StringBuffer attrs = new StringBuffer();
				for (int k = 0; k < root.getAttributes().getLength(); ++k) {
					attrs.append(" ").append(
							root.getAttributes().item(k).getNodeName()).append(
							"=\"");
					String strng = root.getAttributes().item(k).getNodeValue();
					strng = strng.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
					attrs.append(strng).append("\"");
				}
				result.append("<").append(root.getNodeName()).append("")
				.append(attrs).append(">");
			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			}

			NodeList nodes = root.getChildNodes();
			int j = nodes.getLength();
			for (int i = 0; i < j; i++) {
				Node node = nodes.item(i);
				String nodeString = getStringFromNode(node, depth+1);
				if(node.getNodeType() == Node.TEXT_NODE){
					nodeString = nodeString.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
					Hubub.Logger("getStringFromNode: depth: " +depth +", nodeString: " +nodeString);
					result.append(nodeString);			
				}
			}

			if (root.getNodeType() != 9) {
				if(j == 0){
					result = new StringBuilder(result.substring(0, result.length() - 1));
					result.append("/>");
				}
				else
					result.append("</").append(root.getNodeName()).append(">");
			}
		}
		return result.toString();
	}
	*/

	/*****************/

	public static void mainTest(){
		Hubub.Logger("HububXMLDoc: mainTest...");

		HububXMLDoc doc = new HububXMLDoc("HububInvoice");
		doc.addElement("Level2");
		doc.addElement("Level3");
		doc.addAttrib("Att1", "Val1");
		doc.pop();
		doc.addAttrib("Att2", "Val2");
		doc.addElement("level3.2");
		doc.addAttrib("Att3", "Val3");
		//doc.addCDATA("This is the value");
		doc.addText("<AnotherElem>valuevalue</AnotherElem>");
		doc.addText("This is the NEW value");
		Hubub.Logger("asString: " + doc.toString());

		Hubub.Logger("getText: " + doc.getText());
		Hubub.Logger("Get attrib Att2= " + doc.getAttrib("Att2"));
		Hubub.Logger("Get attrib Att3= " + doc.getAttrib("Att3"));

		HububXMLDoc doc1 = new HububXMLDoc().parse(doc.toString());
		//doc1.parse(doc.toString());


		Hubub.Logger("Doc1 asString: " + doc1.toString());
		Hubub.Logger("Doc1 getTag: " + doc1.getTagName());
		doc.pop();

		for(int i=0; doc.getElements("*", i) != null ; i++){
			Hubub.Logger("Tag name: " + doc.getTagName());
			doc.pop();
		}
		doc.reset();
		for(int i=0; doc.getElements("*", i) != null ; i++){
			Hubub.Logger("Tag name: " + doc.getTagName());
			doc.pop();
		}
		doc.reset();
		Hubub.Logger("Doc getTag: " + doc.getTagName());


		HububServices services = new HububServices();
		HububService service= services.addServiceCall("Dummy");
		service.getInputs();
		service.setParm("Inp1", "<\"<<\"");
		service.setParm("Imp2", "Val2");

		HububServices services2 = new HububServices();
		HububService service2 = services2.addServiceCall("Services2");
		service2.getInputs();
		service2.setParm("Inp3", "\"<>\">>\"&");

		service.setParm("IInp3", services2.toString());

		HububWebService webservice = new HububWebService();
		webservice.setPayload(services.toString());

		String serial1 = webservice.toString();
		HububWebService webservice1 = new HububWebService();
		webservice1.parse(serial1);

		String serial2 = webservice1.toString();

		Hubub.Logger("\nserial1: " +serial1 +"\n\nserial2: " +serial2 +"\n\nTest Passed: " + (serial1.equals(serial2)));
		services = new HububServices();
		services.parse(webservice1.getPayload());
		services.getServices();
		service = services.nextService();
		service.getInputs();
		String IInp3 = service.getParm("IInp3");


		Hubub.Logger("\nIInp3: " +IInp3);
		services = new HububServices();
		services.parse(IInp3);
		services.getServices();
		service = services.nextService();
		service.getInputs();
		String Inp3 = service.getParm("Inp3");

		Hubub.Logger("Inp3: " +Inp3);

		//org.w3c.dom.DOMImplementationSourceList foo = null;
		//Hubub.Logger("HububXMLDoc: webservice: " +webservice.toString());

	}

}
