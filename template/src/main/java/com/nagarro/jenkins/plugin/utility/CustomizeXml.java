package com.nagarro.jenkins.plugin.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nagarro.jenkins.plugin.exceptions.CustomizeXmlException;
import com.nagarro.jenkins.plugin.utility.constants.ApplicationConstants;
/**
 * To dynamically modify the basic-java-build.xml 
 * @author saurabh01
 *
 */
public class CustomizeXml {

	private final static Logger LOGGER = Logger.getLogger(CustomizeXml.class);
	/**
	 * To delete the plugin from config.xml if jenkins.xml doesn't has value w.r.t plugin
	 * @param tagNames
	 * @return 
	 */
	public String customizeXmlJava(List<String> tagNames, String xml) throws CustomizeXmlException{
		LOGGER.info("START customizeXmlJava --");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		Map<String,List<String>> map = new HashMap<>();
		readingPropertyFileForBasicJava(map);
		LOGGER.info("tagNames size"+tagNames);
		for (String string : tagNames) {
			if(map.containsKey(string)){
				map.remove(string);
			}			
		}
		LOGGER.info(map.size());
		try {
			docFactory.setIgnoringElementContentWhitespace(true);
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(new InputSource(new StringReader(xml)));
			// Get the root element
			Node first = doc.getFirstChild();
			System.out.println("First node of the xml:"+first);
			boolean isOmitted = false;
			if(!map.isEmpty()){
				Set<String> keys= map.keySet();
				if(keys.contains("performance-run") && keys.contains("selenium-run")){
					Node tagToRemove = doc.getElementsByTagName("htmlpublisher.HtmlPublisher").item(0);
					removeNode(tagToRemove);
					isOmitted = true;
				}
				for (String key : keys) {
					if((key.equals("performance-run") || key.equals("selenium-run")) && isOmitted == false)
						removeChildrenPS(map.get(key),doc,key);
					else
						removeChildren(map.get(key),doc);
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			String output = writer.getBuffer().toString();
			LOGGER.info("Customixed xml after deletion of tags"+output);
			return output;
		} catch (ParserConfigurationException pce) {
			LOGGER.info(pce.getMessage());
			throw new CustomizeXmlException(pce.getCause());
		} catch (TransformerException tfe) {
			LOGGER.info(tfe.getMessage());
			throw new CustomizeXmlException(tfe.getCause());
		} catch (IOException ioe) {
			LOGGER.info(ioe.getMessage());
			throw new CustomizeXmlException(ioe.getCause());
		} catch (SAXException sae) {
			LOGGER.info(sae.getMessage());
			throw new CustomizeXmlException(sae.getCause());
		}
	}

	/**
	 * To delete the plugin from config.xml if jenkins.xml doesn't has value w.r.t plugin
	 * @param tagNames
	 * @return 
	 * @throws CustomizeXmlException 
	 */
	public String customizeXmlDotnet(List<String> tagNames, String xml) throws CustomizeXmlException {
		LOGGER.info("START customizeXmlDotnet --");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		Map<String,List<String>> map = new HashMap<>();
		readingPropertyFileForDotnet(map);
		for (String string : tagNames) {
			if(map.containsKey(string)){
				map.remove(string);
			}			
		}
		LOGGER.info("map size: "+map.size());
		try {
			docFactory.setIgnoringElementContentWhitespace(true);
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(new InputSource(new StringReader(xml)));
			// Get the root element
			Node first = doc.getFirstChild();
			LOGGER.info("First node of the xml:"+first);
			boolean isOmitted = false;
			if(!map.isEmpty()){
				Set<String> keys= map.keySet();
				if(keys.contains("performance-run") && keys.contains("selenium-run")){
					Node tagToRemove = doc.getElementsByTagName("htmlpublisher.HtmlPublisher").item(0);
					removeNode(tagToRemove);
					isOmitted = true;
				}
				for (String key : keys) {
					if((key.equals("performance-run") || key.equals("selenium-run")) && isOmitted == false)
						removeChildrenPS(map.get(key),doc,key);
					else
						removeChildren(map.get(key),doc);
				}
			}

			NodeList nodeList= doc.getElementsByTagName("hudson.tasks.BatchFile");
			int length = nodeList.getLength();
			int count = 0;
			for (int i = 0; i < length; i++) {
				Node command = null;
				command = doc.getElementsByTagName("hudson.tasks.BatchFile").item(count);
				NodeList list =  command.getChildNodes();
				LOGGER.info("Command name: "+list.item(1).getTextContent());
				if(list.item(1).getTextContent().contains("COMMAND")){
					LOGGER.info("Node to remove: "+list.item(1).getTextContent());
					removeNode(command);
				}else
					count++;
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			String output = writer.getBuffer().toString();
			LOGGER.info("Customixed xml after deletion of tags"+output);
			return output;
		} catch (ParserConfigurationException pce) {
			throw new CustomizeXmlException(pce.getMessage());
		} catch (TransformerException tfe) {
			throw new CustomizeXmlException(tfe.getMessage());
		} catch (IOException ioe) {
			throw new CustomizeXmlException(ioe.getMessage());
		} catch (SAXException sae) {
			throw new CustomizeXmlException(sae.getMessage());
		}
	}

	private void readingPropertyFileForBasicJava( Map<String,List<String>> properties) throws CustomizeXmlException {
		LOGGER.info("START readingPropertyFileForBasicJava --");
		FileReader reader;
		Properties p = null;
		try {
			String JENKINS_HOME = System.getProperty(ApplicationConstants.JENKINS_HOME);//var/lib/jenkins
			File file = new File(JENKINS_HOME + "/mapJava.properties");
			reader = new FileReader(file);
			p=new Properties(); 
			p.load(reader);
		} catch (FileNotFoundException e) {
			LOGGER.info("mapJava.properties file exception cause :: "+e.getCause());
			throw new CustomizeXmlException(e.getMessage());
		}  catch (IOException e) {
			throw new CustomizeXmlException(e.getMessage());
		}    	

		Set keys =  p.entrySet();
		Iterator<Map.Entry<String, String>> itr = keys.iterator();
		while(itr.hasNext()){
			List<String> values = new ArrayList<>();
			Map.Entry<String, String> entry=(Map.Entry<String, String>)itr.next();  
			LOGGER.info("Key is--"+entry.getKey()+" having value --"+entry.getValue()); 
			String s = null;
			String splittedValues [] = null;
			if(entry.getValue().contains(",")){
				s = entry.getValue();
				splittedValues = s.split(",");
				for (int i = 0; i < splittedValues.length; i++) {
					values.add(splittedValues[i].trim());
				}
			}else	
				values.add(entry.getValue());

			properties.put(entry.getKey(), values);
		}
		LOGGER.info("map size: "+properties.size());
		LOGGER.info("END readingPropertyFileForBasicJava --");
	}

	private void readingPropertyFileForDotnet( Map<String,List<String>> properties) throws CustomizeXmlException {
		LOGGER.info("START readingPropertyFileForDotnet --");
		FileReader reader;
		Properties p = null;
		try {
			String JENKINS_HOME = System.getProperty(ApplicationConstants.JENKINS_HOME);//var/lib/jenkins
			File file = new File(JENKINS_HOME + "/mapDotNet.properties");
			reader = new FileReader(file);
			p=new Properties(); 
			p.load(reader);
		} catch (FileNotFoundException e) {
			LOGGER.info("mapDotNet.properties file exception cause :: "+e.getCause());
			throw new CustomizeXmlException(e.getMessage());
		}  catch (IOException e) {
			throw new CustomizeXmlException(e.getMessage());
		}    	

		Set keys =  p.entrySet();
		Iterator<Map.Entry<String, String>> itr = keys.iterator();
		while(itr.hasNext()){
			List<String> values = new ArrayList<>();
			Map.Entry<String, String> entry=(Map.Entry<String, String>)itr.next();  
			LOGGER.info("Key is--"+entry.getKey()+" having value --"+entry.getValue()); 
			String s = null;
			String splittedValues [] = null;
			if(entry.getValue().contains(",")){
				s = entry.getValue();
				splittedValues = s.split(",");
				for (int i = 0; i < splittedValues.length; i++) {
					values.add(splittedValues[i].trim());
				}
			}else	
				values.add(entry.getValue());

			properties.put(entry.getKey(), values);
		}

		LOGGER.info("map size: "+properties.size());
		LOGGER.info("END readingPropertyFileForDotnet --");
	}

	/**
	 * method to remove the child for performance and selenium
	 * @param value
	 * @param doc
	 */
	private void removeChildrenPS(List<String> values, Document doc, String key) {
		LOGGER.info("START removeChildrenPS --");
		Node tagToRemove = null;
		for (String value : values) {
			if("performance-run".equals(key)){
				tagToRemove = doc.getElementsByTagName(value.trim()).item(1);
			}
			else{
				tagToRemove = doc.getElementsByTagName(value.trim()).item(0);
			}
			LOGGER.info("Removed node-- "+tagToRemove);
			removeNode(tagToRemove);
		}
		LOGGER.info("END removeChildrenPS --");
	}
	/**
	 * For removing the tag and child tags
	 * @param value
	 * @param doc
	 */
	private void removeChildren(List<String> values, Document doc) {
		LOGGER.info("START removeChildren --");
		if(!values.isEmpty()){
			for (String value : values) {
				NodeList tagsToRemove = doc.getElementsByTagName(value.trim());
				int tagCount = tagsToRemove.getLength();
				for (int i = 0; i < tagCount; i++) {
					Node tagToRemove = null;
					tagToRemove = tagsToRemove.item(0);
					LOGGER.info(tagToRemove);					
					removeNode(tagToRemove);
				}
			}
			if(values.contains("hudson.plugins.sonar.MsBuildSQRunnerBegin"))
				removeNode(doc.getElementsByTagName("hudson.tasks.BatchFile").item(2));
		}
		LOGGER.info("END removeChildren --");
	}
	/**
	 * method to remove the child and tag itself
	 * @param tagToRemove
	 */
	private static void removeNode(Node tagToRemove){
		LOGGER.info("START removeNode -- ");
		NodeList list = tagToRemove.getChildNodes();
		int childCount = list.getLength();
		for (int j = 0; j < childCount; j++) {
			tagToRemove.removeChild(list.item(0));
		}
		tagToRemove.getParentNode().removeChild(tagToRemove);
		LOGGER.info("Tag removed -- "+tagToRemove);
		LOGGER.info("END removeNode -- ");
	}
}
