package com.nagarro.jenkins.plugin.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jasypt.properties.EncryptableProperties;

import com.google.common.base.Strings;
import com.nagarro.jenkins.plugin.exceptions.EmailPropertyNotFoundException;
import com.nagarro.jenkins.plugin.exceptions.JenkinsJobNotCreatedException;
import com.nagarro.jenkins.plugin.exceptions.JiraPropertyNotFoundException;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Artifactory;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Build;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.DockerPlugin;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Email;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJob;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Jira;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.PerformancePlugin;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Property;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.SeleniumPlugin;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.SolutionFile;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.Sonar;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.TestExecution;
import com.nagarro.jenkins.plugin.utility.constants.ApplicationConstants;
import com.nagarro.jenkins.plugin.utility.constants.EmailConstants;
import com.nagarro.jenkins.plugin.utility.constants.SonarConstants;
import com.nagarro.jenkins.plugin.utility.constants.TestExecutionConstants;

public class TemplateAndPropertyFileHelper {

	private final static Logger LOGGER = Logger.getLogger(TemplateAndPropertyFileHelper.class);
	
	/**
	 * To read jenkins.properties from plugin 
	 * @return
	 * @throws IOException 
	 */
	public Properties loadSaveJenkinsPropertyFile() throws IOException {
		LOGGER.info("jenkins home- PasswordDecryption - Before initialize");
		PasswordDecryption.initialize();
		LOGGER.info("jenkins home- PasswordDecryption - After initialize");
		//Properties prop = new EncryptableProperties(PasswordDecryption.encryptor);
		Properties prop = new Properties();
		LOGGER.info("jenkins home- Password encryptor");
		InputStream is = null;
		//String JENKINS_HOME = System.getProperty(ApplicationConstants.JENKINS_HOME);//var/lib/jenkins
		String JENKINS_HOME = "C:\\Program Files (x86)\\Jenkins";//Sushil
		LOGGER.info("jenkins home"+JENKINS_HOME.toString());
		final File folder = new File(JENKINS_HOME);
		File file = new File(JENKINS_HOME + "/jenkins.properties");
		//LOGGER.info("jenkins home"+JENKINS_HOME.toString());
		LOGGER.info("jenkins file folder"+folder.getName());
		Boolean fileExists = false;
		File existingFile = null;
		String propertyFileContent;
		BufferedWriter output = null;
		try {
			for (final File fileEntry : folder.listFiles()) {
				if (!fileEntry.isDirectory() && fileEntry.getName().equals("jenkins.properties")) {
					fileExists = true;
					existingFile = fileEntry;
					break;
				}
			}

			if (fileExists) {
				LOGGER.info("jenkins.properties file exists");
				//read
				is = new FileInputStream(existingFile);
				prop.load(is);
			} else {
				//create property file on Jenkins
				InputStream stream = getClass().getResourceAsStream("plugin.properties");
				propertyFileContent = IOUtils.toString(stream);
				LOGGER.info("content:::::::: " + propertyFileContent);
				LOGGER.info("creating new property file on jenkins home directory: " + JENKINS_HOME);
				output = new BufferedWriter(new FileWriter(file));
				if (null != propertyFileContent)
					output.write(propertyFileContent);
				prop.load(stream);
			}
		} finally {
			try {
				if (null != output)
					output.close();
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return prop;
	}

	public String inferPathAndTemplateNameToLoadAndJobName(String templateNameFromJenkinsXml, 
			StringBuilder finalTemplatePath, String url, StringBuilder newJobName, String vcs, 
			StringBuilder platform) {
		String templateName = null;
		String platformString = null;
		String action = null;
		String basic = null;
		String [] tokens = templateNameFromJenkinsXml.split("-");
		if (null != tokens) {
			if (ApplicationConstants.BASIC.equalsIgnoreCase(tokens[0]))
				basic = tokens[0].toLowerCase();

			if (ApplicationConstants.JAVA.equalsIgnoreCase(tokens[1])) {
				platformString = ApplicationConstants.JAVA;
			} else if (ApplicationConstants.DOTNET.equalsIgnoreCase(tokens[1])) {
				platformString = ApplicationConstants.DOTNET;
			} else if (ApplicationConstants.IOS.equalsIgnoreCase(tokens[1])) {
				platformString = ApplicationConstants.IOS;
			}

			if (tokens.length < 3) {
				action = ApplicationConstants.BUILD;
			} else {
				if (ApplicationConstants.DEPLOY.equalsIgnoreCase(tokens[2])) {
					action = ApplicationConstants.DEPLOY;
				} else if (ApplicationConstants.DOCKER.equalsIgnoreCase(tokens[2])) {
					action = ApplicationConstants.DOCKER;
					if (null != tokens[3] && ApplicationConstants.DEPLOY.equalsIgnoreCase(tokens[3]))
						action = ApplicationConstants.DOCKER + ApplicationConstants.HYPHEN + ApplicationConstants.DEPLOY;
					else 
						action = ApplicationConstants.DOCKER + ApplicationConstants.HYPHEN + ApplicationConstants.BUILD;
				} else if (ApplicationConstants.PIPELINE.equalsIgnoreCase(tokens[2])) {
					action = ApplicationConstants.PIPELINE;
				}
			}
			if (EmptyAndNullCheckUtillity.isStringNonEmpty(basic) && EmptyAndNullCheckUtillity.
					isStringNonEmpty(action) && EmptyAndNullCheckUtillity.isStringNonEmpty(platformString)) {
				templateName = basic + ApplicationConstants.HYPHEN + platformString.toLowerCase() +
						ApplicationConstants.HYPHEN + action.toLowerCase() + ApplicationConstants.DOT + 
						ApplicationConstants.XML;
				finalTemplatePath.append(ApplicationConstants.FORWARD_SLASH +
						platformString.toLowerCase() + ApplicationConstants.FORWARD_SLASH);
			}
			LOGGER.info("-----------finalTemplatePath----------------:" + finalTemplatePath);
			//			createJobName(action, url, newJobName, vcs);
			newJobName.append(ApplicationConstants.HYPHEN).append(action.toLowerCase());
			platform.append(platformString);
		}
		LOGGER.info("templateName to be used:" + templateName);//basic-java-build.xml
		LOGGER.info("job name :::::::" + newJobName); //com.nagarro.DevOps.poc.sampleTestProject-build
		return templateName;
	}

	/*private void createJobName(String action, String url,
			StringBuilder newJobName, String vcs) {
		int count = 0;
		String [] tokens = url.split(ApplicationConstants.FORWARD_SLASH);
		newJobName.append(ApplicationConstants.JOB_NAME_BEGINNING).append(ApplicationConstants.DOT);
		if (ApplicationConstants.SVN.equals(vcs)) {
			for (String token : tokens) {
				count++;
				if (ApplicationConstants.SVN.equals(token))
					break;
			}
			while (count < tokens.length)
				newJobName.append(tokens[count++]).append(ApplicationConstants.DOT);
			newJobName.replace(newJobName.length() - 1, newJobName.length(), ApplicationConstants.BLANK_SPACE);
		} else if (ApplicationConstants.GIT.equals(vcs)) {

		}
		newJobName.append(ApplicationConstants.HYPHEN).append(action.toLowerCase());
	}*/

	/**
	 * To read data from jenkins.xml, fields present in xml and making map on the basis of values given in tags 
	 * @param jenkinsJobs
	 * @param jenkinsXmlMap
	 * @return
	 * @throws Exception 
	 */
	public String populateValuesFromJenkinsXml(JenkinsJobs jenkinsJobs, Map<String, String> jenkinsXmlMap) throws Exception {
		JenkinsJob jenkinsJob = jenkinsJobs.getJenkinsJob();
		String templateNameFromJenkinsXMl = null;
		if (null != jenkinsJob) {
			templateNameFromJenkinsXMl = jenkinsJob.getTemplate();
			LOGGER.info("property: " + "template" + "::::" + "value: " + templateNameFromJenkinsXMl);
			LOGGER.info("values from jenkins.xml: ");
			
			if (null != jenkinsJob.getBuild()) {
				LOGGER.info("property: " + "build" + "::::" + "value: " + jenkinsJob.getBuild());
				populateMapWithBuildValues(jenkinsJob.getBuild(), jenkinsXmlMap, templateNameFromJenkinsXMl);//Sushil getting content from xml on the basis of tags
			}else if(jenkinsJob.getBuild() == null){
				throw new Exception("Build tag is not defined");
			}
			if(templateNameFromJenkinsXMl.equals("BASIC-DOTNET")){
				if (null != jenkinsJob.getTestExecution()) {
					populateMapWithTestExecutionValues(jenkinsJob.getTestExecution(), jenkinsXmlMap);
					jenkinsXmlMap.put("testExcectuionPresent", "yes");
				}
			}
			if (null != jenkinsJob.getSonar()) {
				LOGGER.info("property: " + "sonar" + "::::" + "value: " + jenkinsJob.getSonar());
				jenkinsXmlMap.put("areSonarValuesPresent", ApplicationConstants.Y);
				populateMapWithSonarValues(jenkinsJob.getSonar(), jenkinsXmlMap);
			} 
			if (null != jenkinsJob.getEmail()) {
				populateMapWithEmailValues(jenkinsJob.getEmail(), jenkinsXmlMap);
			} 
			if(null  != jenkinsJob.getJira() ){
				populateMapWithJiraValues(jenkinsJob.getJira(), jenkinsXmlMap);
			}
			if(null  != jenkinsJob.getArtifactory()){
				populateMapWithArtifactoryValues(jenkinsJob.getArtifactory(), jenkinsXmlMap);
			}
			if( null  != jenkinsJob.getDockerPlugin()){
				populateMapWithDockerPuginValues(jenkinsJob.getDockerPlugin(), jenkinsXmlMap);
			}
			if( null  != jenkinsJob.getSeleniumPlugin()){
				populateMapWithSeleniumPluginValues(jenkinsJob.getSeleniumPlugin(), jenkinsXmlMap);
			}
			if(null  != jenkinsJob.getPerformancePlugin()){
				populateMapWithPerformancePluginValues(jenkinsJob.getPerformancePlugin(), jenkinsXmlMap);
			}
		}
		return templateNameFromJenkinsXMl;
	}
	
	/**
	 * To read testExecution tag and its properties
	 * @param testExecution
	 * @param jenkinsXmlMap
	 * @throws Exception 
	 */
	private void populateMapWithTestExecutionValues(TestExecution testExecution, Map<String, String> jenkinsXmlMap) throws Exception{
		String name;
		String value;
		boolean isDllPresent = false;
		boolean isUnitTestToolPresent = false;
		boolean isUnitTestCommand = false;
		if(testExecution.getProperty() != null){
			for (Property property : testExecution.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if("pathtotestdlls".equals(name)){
					isDllPresent = true;
					if(Strings.isNullOrEmpty(value))
						throw new Exception("Please provide dll's path in pathtotestdlls tag");
					else
						jenkinsXmlMap.put(TestExecutionConstants.DLL_PATH_TAG_KEY, value);
				}else if("UnitTestTool".equals(name)){
					isUnitTestToolPresent = true;
					if(Strings.isNullOrEmpty(value))
						throw new Exception("Please provide test tool(mstest/vstest/nunit) in UnitTestTool tag");
					else
					jenkinsXmlMap.put(TestExecutionConstants.UNIT_TEST_TOOL_TAG_KEY, value);
				}else if("coverage".equals(name)){
					jenkinsXmlMap.put(TestExecutionConstants.COVERAGE_TAG_KEY, value);
				}else if("extraargumentsforopencover".equals(name)){
					jenkinsXmlMap.put(TestExecutionConstants.OPEN_COVER_ARGS_TAG_KEY, value);
				}else if("extraargumentsforunittests".equals(name)){
					jenkinsXmlMap.put(TestExecutionConstants.UNIT_TEST_ARGS_TAG_KEY, value);
				}else if("UnitTestCommand".equals(name)){
					isUnitTestCommand = true;
					jenkinsXmlMap.put(TestExecutionConstants.UNIT_TEST_COMMAND_TAG_KEY, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
			
			if(isDllPresent == false && isUnitTestCommand == false)
				throw new Exception("Please provide pathtotestdlls tag");
			if(isUnitTestToolPresent == false && isUnitTestCommand == false)
				throw new Exception("Please provide UnitTestTool tag");
		}else
			throw new Exception("Please provide TestExecution tags: pathtotestdlls and UnitTestTool with properties");
	}
	
	/**
	 * To read performance tag and its properties
	 * @param performancePlugin
	 * @param jenkinsXmlMap
	 */
	private void populateMapWithPerformancePluginValues(PerformancePlugin performancePlugin, Map<String, String> jenkinsXmlMap) {
		String name;
		String value;
		if(performancePlugin.getProperty() != null){
			for (Property property : performancePlugin.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if("p-pom".equals(name)){
					jenkinsXmlMap.put("p-pom", value);
				}else if("p-port".equals(name)){
					jenkinsXmlMap.put("p-port", value);
				}else if("p-context".equals(name)){
					jenkinsXmlMap.put("p-context", value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}
	}
	/**
	 * To read selenium tag and its properties
	 * @param seleniumPlugin
	 * @param jenkinsXmlMap
	 */
	private void populateMapWithSeleniumPluginValues(SeleniumPlugin seleniumPlugin, Map<String, String> jenkinsXmlMap) {
		String name;
		String value;
		if(seleniumPlugin.getProperty() != null){
			for (Property property : seleniumPlugin.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if("s-pom".equals(name)){
					jenkinsXmlMap.put("s-pom", value);
				}else if("s-port".equals(name)){
					jenkinsXmlMap.put("s-port", value);
				}else if("s-context".equals(name)){
					jenkinsXmlMap.put("s-context", value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}
	}
	/**
	 * To read docker tag and its properties
	 * @param dockerPlugin
	 * @param jenkinsXmlMap
	 */
	private void populateMapWithDockerPuginValues(DockerPlugin dockerPlugin, Map<String, String> jenkinsXmlMap) {
		String name;
		String value;
		if(dockerPlugin.getProperty() != null){
			for (Property property : dockerPlugin.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if("image".equals(name)){
					jenkinsXmlMap.put("image", value);
				}else if("volumesString".equals(name)){
					jenkinsXmlMap.put("volumesString", value);
				}else if("bindPorts".equals(name)){
					jenkinsXmlMap.put("bindPorts", value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}
	}
	
	/**
	 * To read Artifactory tag and its properties
	 * @param artifactory
	 * @param jenkinsXmlMap
	 */
	private void populateMapWithArtifactoryValues(Artifactory artifactory, Map<String, String> jenkinsXmlMap) {
		String name;
		String value;
		if(artifactory.getProperty() != null){
			for (Property property : artifactory.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if(ApplicationConstants.ARTIFACTORY_REPO.equals(name)){
					jenkinsXmlMap.put(ApplicationConstants.ARTIFACTORY_REPO, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}
	}
	
	/**
	 * To read jira tag and its properties
	 * @param jira
	 * @param jenkinsXmlMap
	 * @throws JiraPropertyNotFoundException
	 */
	private void populateMapWithJiraValues(Jira jira, Map<String, String> jenkinsXmlMap) throws JiraPropertyNotFoundException {
		String name;
		String value;
		int count = 0;
		if(jira.getProperty() != null){
			for (Property property : jira.getProperty()) {
				name = property.getName();
				value = property.getValue()[0];
				if(ApplicationConstants.COMMENT.equals(name)){
					count++;
					jenkinsXmlMap.put(ApplicationConstants.COMMENT, value);
				}else if(ApplicationConstants.PROJECTNAME.equals(name)){
					count++;
					jenkinsXmlMap.put(ApplicationConstants.PROJECTNAME, value);
				}else if(ApplicationConstants.ISSUEID.equals(name)){
					count++;
					jenkinsXmlMap.put(ApplicationConstants.ISSUEID, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}else{
			LOGGER.info("No properties for Jira tag");
			throw new JiraPropertyNotFoundException("Please provide mandatory properties for jira tag");
		}
		if(count < 3)
			throw new JiraPropertyNotFoundException("All mandatory fields are not given");
	}
	
	/**
	 * To read build tag and its properties
	 * @param build
	 * @param jenkinsXmlMap
	 * @param templateName 
	 * @throws Exception 
	 */
	private void populateMapWithBuildValues(Build build,
			Map<String, String> jenkinsXmlMap, String templateName) throws Exception {
		boolean isSolutionFile = false;
		boolean isBuildTypePresent = false;
		String name;
		String value;
		if(build.getProperty() != null){
			for (Property p : build.getProperty()) {
				name = p.getName();
				value = p.getValue()[0];
				if (ApplicationConstants.COMMAND.equals(name)) {
					jenkinsXmlMap.put(ApplicationConstants.COMMAND, value);
				} else if (ApplicationConstants.SHELL_COMMAND.equals(name)) {
					jenkinsXmlMap.put(ApplicationConstants.SHELL_COMMAND, value);
				} else if (ApplicationConstants.LABEL_EXPR.equals(name)) {
					jenkinsXmlMap.put(ApplicationConstants.LABEL_EXPR, value);
				} else if (ApplicationConstants.JOB_SCHEDULE.equals(name))  {
					jenkinsXmlMap.put(ApplicationConstants.JOB_SCHEDULE, value);
				} else if (ApplicationConstants.GIT_BRANCH_NAME.equals(name)) {
					jenkinsXmlMap.put(ApplicationConstants.GIT_BRANCH_NAME, value);
				}else if ("BASIC-DOTNET".equals(templateName) && ApplicationConstants.SOLUTION_FILE.equals(name)) {
					if(!Strings.isNullOrEmpty(value)){
						isSolutionFile = true;
						jenkinsXmlMap.put(ApplicationConstants.SOLUTION_FILE, value);
					}else
						throw new Exception("Please provide solutionFile in jenkins.xml");
				}else if(ApplicationConstants.BUILD_TYPE.equals(name)){
					isBuildTypePresent = true;
					if(!Strings.isNullOrEmpty(value)) {
						LOGGER.info("inside1: " + "buildType" + "::::" + "value: " + value);
						jenkinsXmlMap.put(ApplicationConstants.BUILD_TYPE, value);
					}else if(Strings.isNullOrEmpty(value) && "BASIC-DOTNET".equals(templateName)){
						LOGGER.info("inside2: " + "buildType" + "::::" + "value: " + value);
						jenkinsXmlMap.put(ApplicationConstants.BUILD_TYPE, value);
					}else
						throw new Exception("Please provide build type in jenkins.xml");
				}else if (ApplicationConstants.CMD_LINE_ARGS.equals(name)) {
					jenkinsXmlMap.put(ApplicationConstants.CMD_LINE_ARGS, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}
		}		
		if(!isSolutionFile && templateName.equals("BASIC-DOTNET")){
			LOGGER.info("isSolutionFile:"+isSolutionFile);
			throw new Exception("Please provide solutionFile tag in jenkins.xml");
		}
		if(!isBuildTypePresent && templateName.equals("BASIC-JAVA")){
			LOGGER.info("isBuildTypePresent:"+isBuildTypePresent);
			throw new Exception("Please provide buildType tag in jenkins.xml");
		}
	}
	
	/**
	 * To read sonar tags and its properties 
	 * @param sonar
	 * @param jenkinsXmlMap
	 */
	private void populateMapWithSonarValues(Sonar sonar,
			Map<String, String> jenkinsXmlMap) {
		String name;
		String value;
		if(sonar.getProperty() != null){
			for (Property p : sonar.getProperty()) {
				name = p.getName();
				value = p.getValue()[0];
				if (ApplicationConstants.SONAR_BRANCH_NAME.equals(name))  {
					jenkinsXmlMap.put(ApplicationConstants.SONAR_BRANCH_NAME, value);
				}else if (SonarConstants.SONAR_SOURCES_KEY.equals(name)) {
					jenkinsXmlMap.put(SonarConstants.SONAR_SOURCES_KEY, value);
				}else if (SonarConstants.SONAR_BINARIES_NEW_KEY.equals(name)) {
					jenkinsXmlMap.put(SonarConstants.SONAR_BINARIES_NEW_KEY, value);
				}else if (SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY.equals(name)) {
					jenkinsXmlMap.put(SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY, value);
				}else if (SonarConstants.SONAR_ADDITIONAL_ARGUMENTS_TAG_KEY.equals(name)) {
					jenkinsXmlMap.put(SonarConstants.SONAR_ADDITIONAL_ARGUMENTS_TAG_KEY, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}	
		}
	}

	/**
	 * To read email tag and its properties
	 * @param email
	 * @param jenkinsXmlMap
	 * @throws EmailPropertyNotFoundException
	 */
	private void populateMapWithEmailValues(Email email,
			Map<String, String> jenkinsXmlMap) throws EmailPropertyNotFoundException{
		String name;
		String value;
		if(email.getProperty() != null){
			for (Property p : email.getProperty()) {
				name = p.getName();
				value = p.getValue()[0];
				if (EmailConstants.EMAIL_SUBJECT_KEY.equals(name)) {
					jenkinsXmlMap.put(EmailConstants.EMAIL_SUBJECT_KEY, value);
				} else if (EmailConstants.EMAIL_BODY_KEY.equals(name)) {
					jenkinsXmlMap.put(EmailConstants.EMAIL_BODY_KEY, value);
				} else if (EmailConstants.EMAIL_ATTACH_BUILD_LOG_KEY.equals(name)) {
					jenkinsXmlMap.put(EmailConstants.EMAIL_ATTACH_BUILD_LOG_KEY, value);
				} else if (EmailConstants.EMAIL_RECIPIENTS_KEY.equals(name)) {
					jenkinsXmlMap.put(EmailConstants.EMAIL_RECIPIENTS_KEY, value);
				}
				LOGGER.info("property: " + name + "::::" + "value: " + value);
			}	
		}/*else{
			logger.info("No properties for Email tag");
			throw new EmailPropertyNotFoundException("Please provide mandatory properties for email tag");
		}*/	
	}

	public Boolean fetchFromMap(Map<String, String> jenkinsXmlMap, String key) {
		Boolean isKeyPresent = false;
		if (EmptyAndNullCheckUtillity.isStringNonEmpty(jenkinsXmlMap.get(key))) {
			isKeyPresent = true;
		}
		return isKeyPresent;
	}
	
	/*private void mandatoryFieldsCheck(String templateName, Map<String, String> map){
		if(templateName.equals("Dot"))
	}*/

}