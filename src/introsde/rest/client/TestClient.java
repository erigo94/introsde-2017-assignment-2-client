package introsde.rest.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestClient {

	private static String first_id;
	private static String last_id;
	private static DocumentBuilderFactory domFactory;
	private static DocumentBuilder builder;
	private static XPath xpath;
	private static WebTarget service;
	private static String start;
	private static String request;
	private static String type;
	private static String content;
	private static boolean result;
	private static String xml;
	private static String json;
	private static Document doc;
	private static Response resp;
	private static String measure_id;
	private static String measure_type;
	private static ObjectMapper mapper;
	private static PrintStream printxml;
	private static PrintStream printjson;

	private static String clientServerXmlLog = "out/client-server-xml.log";
	private static String clientServerJsonlLog = "out/client-server-json.log";

	public static void main(String[] args) throws ParserConfigurationException, XPathExpressionException, SAXException,
			IOException, TransformerException {
		initialize();

		// XML Requests
		if (api1Xml()) {
			System.out.println("Request #1: OK");
		} else {
			System.out.println("Request #1: ERROR");
		}
		printResult();

		if (api2XML(first_id)) {
			System.out.println("Request #2: OK");
		} else {
			System.out.println("Request #2: ERROR");
		}
		printResult();

		if (api3XML(first_id)) {
			System.out.println("Request #3: OK");
		} else {
			System.out.println("Request #3: ERROR");
		}
		printResult();
		
		String newPersonId = api4XML();
		printResult();
		
		if (api5XML(newPersonId)) {
			System.out.println("Request #5: OK");
		} else {
			System.out.println("Request #5: ERROR");
		}
		printResult();
		
		String[] activitiesType = api6XML();
		printResult();
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://127.0.1.1:5900/introsde").build();
		// return
		// UriBuilder.fromUri("https://introsde2017-assignment-2-server.herokuapp.com/introsde").build();
	}

	private static void initialize() throws ParserConfigurationException, FileNotFoundException {
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		service = client.target(getBaseURI());
		System.out.println("Server respond at: " + getBaseURI());
		domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		builder = domFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		FileOutputStream filexml = new FileOutputStream(clientServerXmlLog);
		FileOutputStream filejson = new FileOutputStream(clientServerJsonlLog);
		printxml = new PrintStream(filexml);
		printjson = new PrintStream(filejson);

		mapper = new ObjectMapper();
	}

	private static void printResult() throws TransformerException {
		PrintStream stream = null;
		if (type == MediaType.APPLICATION_XML) {
			stream = printxml;
		} else if (type == MediaType.APPLICATION_JSON) {
			stream = printjson;
		}
		stream.print(start + request + " Accept: " + type);
		System.out.print(start + request + " Accept: " + type);

		if (content != null) {
			stream.println(" Content-Type: " + content);
			System.out.println(" Content-Type: " + content);
		} else {
			stream.println();
			System.out.println();
		}
		if (result) {
			stream.println("=> Result: OK");
			System.out.println("=> Result: OK");
		} else {
			stream.println("=> Result: ERROR");
			System.out.println("=> Result: ERROR");
		}
		if (resp != null) {
			stream.println("=> HTTP Status: " + resp.getStatus());
			System.out.println("=> HTTP Status: " + resp.getStatus());
			if (type == MediaType.APPLICATION_XML) {
				String xmlOut = printXML(doc);
				stream.println(xmlOut);
				System.out.println(xmlOut);
			} else if (type == MediaType.APPLICATION_JSON) {
				String jsonOut = printJSON();
				stream.println(jsonOut);
				System.out.println(jsonOut);
			}
		} else {
			stream.println("=> HTTP Status: NO RESPONSE");
			System.out.println("=> HTTP Status: NO RESPONSE");
		}
	}

	private static String printXML(Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		// initialize StreamResult with File object to save to file
		StreamResult result1 = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result1);
		String xmlString = result1.getWriter().toString();
		return xmlString;
	}

	private static String printJSON() {
		Object obj;
		String jsonString = null;
		try {
			if (!json.isEmpty()) {
				obj = mapper.readValue(json, Object.class);
				jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	public static boolean api1Xml() throws SAXException, IOException, XPathExpressionException {
		// GET Request#1 --- GET BASEURL/person
		// Accept: application/xml
		start = "Request #1: GET /";
		request = "person";
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//*");
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes.getLength() > 4)
			result = true;

		// First id
		expr = xpath.compile("//person[1]/idPerson");
		Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		first_id = node.getTextContent();

		// Last id
		expr = xpath.compile("//person[last()]/idPerson");
		node = (Node) expr.evaluate(doc, XPathConstants.NODE);
		last_id = node.getTextContent();
		return result;
	}

	private static boolean api2XML(String id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// GET Request #2 --- GET BASEURL/person/first_id
		// Accept: application/xml
		start = "Request #2: GET /";
		request = "person/" + id;
		type = MediaType.APPLICATION_XML;
		content = null;
		result = false;

		resp = service.path(request).request().accept(type).get();

		xml = resp.readEntity(String.class);
		if (!xml.isEmpty())
			doc = builder.parse(new InputSource(new StringReader(xml)));

		if (resp.getStatus() == 200 || resp.getStatus() == 202)
			result = true;
		return result;
	}

	private static boolean api3XML(String first_id)
			throws SAXException, IOException, XPathExpressionException, TransformerException {
		// PUT Request #3 --- PUT BASEURL/person/first_id
		// Accept: application/xml
		// variable
		start = "Request #3: PUT /";
		request = "person/" + first_id;
		type = MediaType.APPLICATION_XML;
		content = MediaType.APPLICATION_XML;
		result = false;

		String newName = "Massimo";
		String requestBody = "<person>" 
							+ "<idPerson>" + first_id + "</idPerson>"
							+ "<firstname>" + newName + "</firstname>"
							+ "</person>";
		resp = service.path(request).request().accept(type).put(Entity.entity(requestBody, content));
		xml = resp.readEntity(String.class);
		doc = builder.parse(new InputSource(new StringReader(xml)));

		XPathExpression expr = xpath.compile("//firstname");
		String firstname = (String) expr.evaluate(doc, XPathConstants.STRING);
		if (newName.equals(firstname))
			result = true;
		return result;
	}
	
	private static String api4XML() throws SAXException, IOException, XPathExpressionException, TransformerException{	    
		// POST Request #4 --- POST  BASEURL/person
	    // Accept: application/xml
	    //variable
	    start = "Request #4: POST /";
	    request = "person";
	    type = MediaType.APPLICATION_XML;
	    content = MediaType.APPLICATION_XML;
	    result = false;
	    String newPersonId = "";
	    
	    String requestBody = "<person>" + 
					    		"<firstname>Antonio</firstname>" + 
					    		"<lastname>Sierte</lastname>" + 
					    		"<birthdate>1992-10-17</birthdate>" + 
					    		"<activitiesPreference>" + 
					    		"<activity>" + 
					    			"<name>Saxing</name>" + 
						    		"<description>Playing music with Sax</description>" + 
						    		"<idActivityType>1</idActivityType>" + //Social
						    		"<place>City Centre Trento</place>" + 
						    		"<startdate>2017-10-13T10:50:00.0</startdate>" + 
					    		"</activity>" + 
					    		"</activitiesPreference>" + 
	    					"</person>";


	    resp = service.path(request).request().accept(type).post(Entity.entity(requestBody, content));
	    xml = resp.readEntity(String.class);
	    doc = builder.parse(new InputSource(new StringReader(xml)));
	    
	    XPathExpression expr = xpath.compile("//idPerson");
	    newPersonId = (String) expr.evaluate(doc, XPathConstants.STRING);
	    if ((resp.getStatus() == 200 || resp.getStatus() == 201 || resp.getStatus() == 202) && ! newPersonId.isEmpty()) result = true;
	    return newPersonId;
	    
	}
	
	private static boolean api5XML(String id) throws SAXException, IOException, XPathExpressionException, TransformerException{	    
		// DELETE Request #5 --- DELETE  BASEURL/person/id
	    // Accept: application/xml
	    // variable
	    start = "Request #5: DELETE /";
	    request = "person/"+id;
	    type = MediaType.APPLICATION_XML;
	    content = null;
	    doc = null;
	    

	    Response this_resp = service.path(request).request().accept(type).delete();
	    api2XML(id);
	    
	    // reset variable
	    start = "Request #5: DELETE /";
	    request = "person/"+id;
	    type = MediaType.APPLICATION_XML;
	    content = null;
	    result = false;
	    
	    if (resp.getStatus() == 404) result = true;
	    resp = this_resp;
	    return result;
	}
	
	private static String[] api6XML() throws SAXException, IOException, XPathExpressionException, TransformerException{	    
		// GET Request #6 --- GET  BASEURL/activity_types
	    // Accept: application/xml
	    //variable
	    start = "Request #6: GET /";
	    request = "activity_types";
	    type = MediaType.APPLICATION_XML;
	    content = null;
	    result = false;
	    
	    resp = service.path(request).request().accept(type).get();
	    
	    xml = resp.readEntity(String.class);
	    doc = builder.parse(new InputSource(new StringReader(xml)));
	    
	    XPathExpression expr = xpath.compile("//activity_type");
	    NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	    
	    if (nodes.getLength() > 2) result = true;
	    String[] activitiesType= new String[nodes.getLength()];
 	    for (int i = 0; i< nodes.getLength(); i++){
 	    	activitiesType[i]=nodes.item(i).getTextContent();
	    }
 	    return activitiesType;
	}
}