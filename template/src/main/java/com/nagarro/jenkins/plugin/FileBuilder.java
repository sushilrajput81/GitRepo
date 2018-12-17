package com.nagarro.jenkins.plugin;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.nagarro.jenkins.plugin.exceptions.JenkinsSeedJobFailException;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;
import com.nagarro.jenkins.plugin.utility.JobUtility;


public class FileBuilder {
	
	private static Logger LOGGER = Logger.getLogger(JobUtility.class);
	JAXBContext context  = null;
	
	public JenkinsJobs jenkinsxmlToJenkinsJob(String file) throws JenkinsSeedJobFailException {//Sushil - marshalling and unmarshalling for xml file
		JenkinsJobs jenkinsJobs = null;
		try {
			context = JAXBContext.newInstance(JenkinsJobs.class);
			final StringReader reader = new StringReader(file.trim());
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			jenkinsJobs = (JenkinsJobs) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			LOGGER.info("Exception occurred while converting to Pojo"+ e.getMessage());
			throw new JenkinsSeedJobFailException(e.getMessage());
		}
		return jenkinsJobs;
	}
	
	/*public ConfigParams xmlToJava(String file) {
		ConfigParams configParams = null;
		try {
			context = JAXBContext.newInstance(ConfigParams.class);
			final StringReader reader = new StringReader(file);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			configParams = (ConfigParams) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return configParams;
	}*/
	
	/*public Project xmlToJava(String file) {
		Project project = null;
		try {
			context = JAXBContext.newInstance(Project.class);
			final StringReader reader = new StringReader(file);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			project = (Project) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return project;
	}*/

	/*public String xmlToString(String fileName) {
//		Reader reader = new FileReader(new File(fileName));
//		InputStreamReader io = new FileReader(new File(fileName));
		
		InputStream in;
		try {
			in = new FileInputStream(new File(fileName));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line.trim());
			}
			System.out.println("string::::::::" + sb);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void replaceNodes() {
		StreamResult result = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			//step1.
			Document doc = builder.parse(new ByteArrayInputStream(("<?xml version=\"1.0\"?>" + //
                    "<people>" + //
                    "<person><name>First Person Name</name></person>" + //
                    "<person><name>Second Person Name</name></person>" + //
                    "</people>").getBytes()));
			
			//step 2:
			String fragment = "<name>Changed Name</name>";
			Document fragmDocument = builder.parse(new ByteArrayInputStream(fragment.getBytes()));
			
			//Step3:
			Node injectedNode = doc.adoptNode(fragmDocument.getFirstChild());
			
			//step4:
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xPath.compile("//people/person[2]/name");
			System.out.println();
			Element nodeFound = (Element) expr.evaluate(doc, XPathConstants.NODE);
			
			//step5:
			Node parentNode = nodeFound.getParentNode();
			parentNode.removeChild(nodeFound);
			parentNode.appendChild(injectedNode);
			
			DOMSource domSource = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
            result = new StreamResult(new StringWriter());
			transformer.transform(domSource, result);
		} catch (TransformerException | SAXException | 
				IOException | XPathExpressionException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		System.out.println(result.getWriter().toString());
	}*/
	
}
