package com.nagarro.jenkins.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import com.google.common.base.Strings;
import com.nagarro.jenkins.plugin.utility.EmptyAndNullCheckUtillity;
import com.nagarro.jenkins.plugin.utility.TemplateAndPropertyFileHelper;
import com.nagarro.jenkins.plugin.utility.constants.ApplicationConstants;
import com.nagarro.jenkins.plugin.utility.constants.ArtifactoryConstants;
import com.nagarro.jenkins.plugin.utility.constants.DockerConstants;
import com.nagarro.jenkins.plugin.utility.constants.EmailConstants;
import com.nagarro.jenkins.plugin.utility.constants.GitConstants;
import com.nagarro.jenkins.plugin.utility.constants.HTMLPublisherConstants;
import com.nagarro.jenkins.plugin.utility.constants.JiraConstants;
import com.nagarro.jenkins.plugin.utility.constants.MavenConstants;
import com.nagarro.jenkins.plugin.utility.constants.PerformanceConstants;
import com.nagarro.jenkins.plugin.utility.constants.SeleniumConstants;
import com.nagarro.jenkins.plugin.utility.constants.SonarConstants;
import com.nagarro.jenkins.plugin.utility.constants.TestExecutionConstants;

/**
 * Class responsible for making template for the Jobs
 * @author saurabh01
 *
 */
public class PopulateTemplateForJobs {

	TemplateAndPropertyFileHelper templateAndPropertyFileHelper = new TemplateAndPropertyFileHelper();
	static Logger LOGGER = Logger.getLogger(PopulateTemplateForJobs.class);

	public StringWriter populateTemplateNew(String templateName, StringBuilder finalTemplatePath, 
			Properties pluginProperties, String url, Map<String, String> jenkinsXmlMap, 
			StringBuilder newJobName, StringBuilder platform) {
		LOGGER.info("START populateTemplateNew");
		StringBuilder sb = loadTemplate(finalTemplatePath, templateName);
		StringWriter sw = populateTemplate(url, sb , jenkinsXmlMap, pluginProperties, 
				newJobName, templateName, platform);
		//System.out.println("sw: " + sw); // for printing the template in console
		LOGGER.info("executed populateTemplateNew sw:"+sw);
		return sw;
	}
	/*
	 * 
	 * Generating new xml for jenkins
	 */
	public StringBuilder loadTemplate(StringBuilder finalTemplatePath, String templateName) {
		LOGGER.info("START loadTemplate");
		StringBuilder sb = new StringBuilder();
		LOGGER.info("jenkins file path::::: " + finalTemplatePath + templateName);
		InputStream stream = getClass().getResourceAsStream(finalTemplatePath + templateName);
		if (null == stream) {
			LOGGER.info("stream is null:::::::::::::::::::::::::1234");
		} else {
			LOGGER.info("stream :::: " + stream);
			try {
				String content = IOUtils.toString(stream);
				sb.append(content);//raw template
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info("END loadTemplate");
		return sb;
	}

	//make it separate for pipeline's jenkinsfile and job
	public StringWriter populateTemplate(String url, StringBuilder sb, Map<String, String> jenkinsXmlMap, 
			Properties prop, StringBuilder jobName, String templateName, StringBuilder platform) {
		LOGGER.info("START populateTemplate --");
		Velocity.init();
		VelocityContext context = new VelocityContext();
		String buildType = jenkinsXmlMap.get(ApplicationConstants.BUILD_TYPE);
		if (ApplicationConstants.PIPELINE.toLowerCase().equals(buildType)) {
			LOGGER.info("pipeline job");
			context.put("JOB_NAME", jobName);
			populateContextFromPropertyFileForPipelineJobs(context, prop, url);
			populateContextFromJenkinsXmlOrPropertyFileForPipelineJobs(context, prop, jenkinsXmlMap);
			if (!platform.toString().isEmpty() && ApplicationConstants.DOTNET.equals(platform.toString()))
				populateContextForDotnetPipelineJenkinsFile(context, prop);
		} else {
			populateMiscValues(context, url, jobName, prop, templateName, jenkinsXmlMap);
			populateContextForMavenProject(context, prop);
			populateContextFromPropertyFileForFreestyleJobs(context, prop);
			populateContextFromJenkinsXmlOrPropertyFileForFreestyleJobs(context, prop, jenkinsXmlMap);
			if (!platform.toString().isEmpty() && ApplicationConstants.DOTNET.equals(platform.toString()))
				populateForDotnet(context, prop, jenkinsXmlMap);
		}
		StringWriter writer = new StringWriter();
		Velocity.evaluate(context, writer, "basic_java_template", sb.toString());
		LOGGER.info("END populateTemplate --");
		return writer;
	}

	private void populateContextForMavenProject(VelocityContext context,
			Properties prop) {
		LOGGER.info("START populateContextForMavenProject --");
		context.put(MavenConstants.MAVEN_VERSION2_KEY, prop.getProperty("maven.version2"));
		context.put(MavenConstants.MAVEN_GROUP_ID_KEY, prop.getProperty("maven.rootModule.groupId"));
		context.put(MavenConstants.MAVEN_ARTIFACT_ID_KEY, prop.getProperty("maven.rootModule.artifactId"));
		context.put(MavenConstants.MAVEN_VALIDATION_LEVEL_KEY, prop.getProperty("maven.validationLevel"));

		//artifactory-details
		context.put(ArtifactoryConstants.ARTIFACTORY_VERSION_KEY, prop.getProperty("artifactory.version"));
		context.put(ArtifactoryConstants.ARTIFACTORY_NAME_KEY, prop.getProperty("artifactory.name"));
		context.put(ArtifactoryConstants.ARTIFACTORY_URL_KEY, prop.getProperty("artifactory.url"));
		//context.put("ARTIFACTORY_KEY", prop.getProperty("artifactory.keyFromSelect"));
		context.put(ArtifactoryConstants.EXCLUDE_PATTERNS_KEY, prop.getProperty("artifactory.excludePatterns"));

		context.put(ArtifactoryConstants.AGGREGATION_BUILD_STATUS_KEY, prop.getProperty("artifactory.aggregationBuildStatus"));
		context.put("SONAR_PROPERTY_FILE_LOCATION", prop.getProperty("sonar.propertyFile.location"));
		LOGGER.info("END populateContextForMavenProject --");
	}

	private void populateForDotnet(VelocityContext context, Properties prop, Map<String, String> jenkinsXmlMap) {
		LOGGER.info("START populateForDotnet --");
		//for dotnet
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.JOB_SCHEDULE))
			context.put(ApplicationConstants.JOB_SCHEDULE_TOKEN, jenkinsXmlMap.get(ApplicationConstants.JOB_SCHEDULE));
		else 
			context.put(ApplicationConstants.JOB_SCHEDULE_TOKEN, prop.getProperty(ApplicationConstants.JOB_SCHEDULE_KEY_DOTNET));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.SOLUTION_FILE))
			context.put(ApplicationConstants.COMMAND_TOKEN, prop.getProperty("nuget.exe.path")+" restore "+jenkinsXmlMap.get(ApplicationConstants.SOLUTION_FILE));
		else 
			context.put(ApplicationConstants.COMMAND_TOKEN, prop.getProperty("batchfile.command"));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY))
			context.put(SonarConstants.SONAR_PROJECT_VERSION_KEY, jenkinsXmlMap.get(SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY));
		else
			context.put(SonarConstants.SONAR_PROJECT_VERSION_KEY, "$"+SonarConstants.BUILD_NUMBER);
		if (!Strings.isNullOrEmpty(jenkinsXmlMap.get(ApplicationConstants.CMD_LINE_ARGS)))
			context.put("CMD_LINE_ARGS", jenkinsXmlMap.get(ApplicationConstants.CMD_LINE_ARGS));
		else
			context.put("CMD_LINE_ARGS", "");
		
		context.put(SonarConstants.SONAR_VERSION_KEY, prop.getProperty("sonar.version"));
		context.put(SonarConstants.SONAR_INSTALLATION_NAME_KEY, prop.getProperty("sonar.installation.name"));

		context.put("SONAR_COMMAND", getCommand2Value(prop.getProperty("batchfile.command2.prefix"), jenkinsXmlMap.get(ApplicationConstants.SOLUTION_FILE), prop.getProperty("batchfile.command2.suffix")));
		context.put("JDK_DOTNET", prop.getProperty("project.jdk.dotnet"));
		context.put("LABEL_EXPR_DOTNET", prop.getProperty("project.assignedNode.dotnet"));//common

		context.put("MSBUILD_NAME", prop.getProperty("msbuild.name"));
		context.put("MSBUILD_FILE", jenkinsXmlMap.get(ApplicationConstants.SOLUTION_FILE));
		context.put("MSBUILD_VERSION", prop.getProperty("msbuild.plugin.version"));

		// for jira integration
		context.put(JiraConstants.JIRA_PLUGIN_VERSION_KEY, prop.getProperty(JiraConstants.JIRA_PLUGIN_VERSION_TAG_KEY));
		context.put(JiraConstants.REST_API_URL_KEY, prop.getProperty(JiraConstants.REST_API_URL_TAG_KEY));
		context.put(JiraConstants.USERNAME_KEY, prop.getProperty(JiraConstants.USERNAME_TAG_KEY));
		context.put(JiraConstants.PASSWORD_KEY, prop.getProperty(JiraConstants.PASSWORD_TAG_KEY));
		context.put(JiraConstants.JQL_KEY, "");
		context.put(JiraConstants.PROJECTNAME_KEY, jenkinsXmlMap.get(JiraConstants.PROJECT_KEY));
		context.put(JiraConstants.TICKETID_KEY, jenkinsXmlMap.get(JiraConstants.ID_KEY));
		context.put(JiraConstants.COMMENT_KEY, jenkinsXmlMap.get(JiraConstants.COMMENT_TAG_KEY));

		// for test Execution
		if(!Strings.isNullOrEmpty(jenkinsXmlMap.get("testExcectuionPresent"))){
			setTestExecutionParams(context, prop, jenkinsXmlMap);
		}
		addArguments(context, prop, jenkinsXmlMap);
		LOGGER.info("END populateForDotnet --");
	}

	private void addArguments(VelocityContext context, Properties prop,Map<String, String> jenkinsXmlMap) {
		LOGGER.info("START addArguments -- ");
		StringBuilder args = new StringBuilder("");
		if(Strings.isNullOrEmpty(jenkinsXmlMap.get(TestExecutionConstants.UNIT_TEST_COMMAND_TAG_KEY)) && !Strings.isNullOrEmpty(jenkinsXmlMap.get("testExcectuionPresent"))){
			String unitTest = jenkinsXmlMap.get(TestExecutionConstants.UNIT_TEST_TOOL_TAG_KEY);
			if("mstest".equalsIgnoreCase(unitTest))
				args.append(prop.getProperty("SonarMstestArgs"));
			else if("Nunit".equalsIgnoreCase(unitTest))
				args.append(prop.getProperty("SonarNunitArgs"));
			else if("Vstest".equalsIgnoreCase(unitTest))
				args.append(prop.getProperty("SonarVstestArgs"));
			args.append(" ");
		}
		if(!Strings.isNullOrEmpty(jenkinsXmlMap.get(SonarConstants.SONAR_ADDITIONAL_ARGUMENTS_TAG_KEY)))
			args.append(jenkinsXmlMap.get(SonarConstants.SONAR_ADDITIONAL_ARGUMENTS_TAG_KEY));
		LOGGER.info("args value -- "+String.valueOf(args));
		LOGGER.info("END addArguments -- ");
		context.put(SonarConstants.SONAR_ADDITIONAL_ARGUMENTS_KEY, String.valueOf(args));
	}
	
	private void setTestExecutionParams(VelocityContext context, Properties prop, Map<String, String> jenkinsXmlMap) {
		String rmDir = "rmdir "+prop.getProperty(TestExecutionConstants.OUTPUT_DIR)+" "+prop.getProperty("VstestOutputDir")+" /s /q";
		String mkDir = "mkdir "+prop.getProperty(TestExecutionConstants.OUTPUT_DIR);
		String unitTestCommand = jenkinsXmlMap.get(TestExecutionConstants.UNIT_TEST_COMMAND_TAG_KEY);
		LOGGER.info("START setTestExecutionParams rmDir, mkDir, unitTestCommand--"+rmDir+"\t"+mkDir+"\t"+unitTestCommand);
		if(Strings.isNullOrEmpty(unitTestCommand)){
			String coverage = prop.getProperty("OpencoverPath");
			String coverageValue = jenkinsXmlMap.get(TestExecutionConstants.COVERAGE_TAG_KEY);
			if(!Strings.isNullOrEmpty(coverageValue)){
				LOGGER.info("START coverageValue --"+coverageValue);
				if(!Strings.isNullOrEmpty(prop.getProperty(coverageValue))){
					LOGGER.info("isNullOrEmpty coverageValue --"+prop.getProperty(coverageValue));
					coverage = prop.getProperty(coverageValue);
				}
			}
			String openCoverArgs = null;
			if(Strings.isNullOrEmpty(jenkinsXmlMap.get(TestExecutionConstants.OPEN_COVER_ARGS_TAG_KEY))){
				LOGGER.info("isNullOrEmpty openCoverArgs --"+prop.getProperty(TestExecutionConstants.OPEN_COVER_SUFFIX));
				openCoverArgs = prop.getProperty(TestExecutionConstants.OPEN_COVER_SUFFIX);
			}
			else{
				LOGGER.info("isNullOrEmpty else openCoverArgs --"+jenkinsXmlMap.get(TestExecutionConstants.OPEN_COVER_ARGS_TAG_KEY));
				openCoverArgs = jenkinsXmlMap.get(TestExecutionConstants.OPEN_COVER_ARGS_TAG_KEY);
			}
				
			LOGGER.info("START openCoverArgs --"+openCoverArgs);
			String unitTest = jenkinsXmlMap.get(TestExecutionConstants.UNIT_TEST_TOOL_TAG_KEY);
			if("mstest".equalsIgnoreCase(unitTest)){
				StringBuilder command = new StringBuilder(rmDir+"\n"+mkDir+"\n");
				LOGGER.info("START populateForDotnet msTest--");
				String commandPrefix = coverage+prop.getProperty(TestExecutionConstants.OPEN_COVER_PREFIX)
				+" -target:"+prop.getProperty(TestExecutionConstants.MS_TEST_PATH)+" -targetargs:\"";
				String middle = null;
				String dllPath =  jenkinsXmlMap.get(TestExecutionConstants.DLL_PATH_TAG_KEY);
				LOGGER.info("dllPath value: "+dllPath);
				if(!Strings.isNullOrEmpty(dllPath)){
					if(dllPath.contains(",")){
						String [] paths = dllPath.split(",");
						middle = getPath(paths,unitTest);
					}else
						middle = "/testcontainer:"+"\\\""+dllPath+"\\\"";
				}
				String suffix = prop.getProperty(TestExecutionConstants.MS_TEST_SUFFIX)+" "
						+openCoverArgs+" "+prop.getProperty(TestExecutionConstants.OPEN_COVER_REPORT_FILE);
				
				command.append(commandPrefix).append(middle).append(suffix);
				context.put(TestExecutionConstants.TEST_EXECUTION_COMMAND, String.valueOf(command));
			}else if("Nunit".equalsIgnoreCase(unitTest)){
				LOGGER.info("START populateForDotnet Nunit--");
				StringBuilder command = new StringBuilder(rmDir+"\n"+mkDir+"\n");
				String commandPrefix = coverage+prop.getProperty(TestExecutionConstants.OPEN_COVER_PREFIX)+" -target:"
						+prop.getProperty(TestExecutionConstants.N_UNIT_PATH)+" -targetargs:\"";
				String middle = null;
				String dllPath =  jenkinsXmlMap.get(TestExecutionConstants.DLL_PATH_TAG_KEY);
				LOGGER.info("dllPath value: "+dllPath);
				if(!Strings.isNullOrEmpty(dllPath)){
					if(dllPath.contains(",")){
						String [] paths = dllPath.split(",");
						middle = getPath(paths,unitTest);
					}else
						middle = "\\\""+dllPath+"\\\"";
				}
				String suffix = prop.getProperty(TestExecutionConstants.N_UNIT_SUFFIX)+" "
						+openCoverArgs+" "+prop.getProperty(TestExecutionConstants.OPEN_COVER_REPORT_FILE);
				
				command.append(commandPrefix).append(middle).append(suffix);
				context.put(TestExecutionConstants.TEST_EXECUTION_COMMAND, String.valueOf(command));
			}else if("Vstest".equalsIgnoreCase(unitTest)){
				LOGGER.info("START populateForDotnet Vstest--");
				StringBuilder command = new StringBuilder(rmDir+"\n"+mkDir+"\n");
				String commandPrefix = coverage+prop.getProperty(TestExecutionConstants.OPEN_COVER_PREFIX)+" -target:"
						+prop.getProperty(TestExecutionConstants.VS_TEST_PATH)+" -targetargs:\"";
				String middle = null;
				String dllPath =  jenkinsXmlMap.get(TestExecutionConstants.DLL_PATH_TAG_KEY);
				LOGGER.info("dllPath value: "+dllPath);
				if(!Strings.isNullOrEmpty(dllPath)){
					if(dllPath.contains(",")){
						String [] paths = dllPath.split(",");
						middle = getPath(paths,unitTest);
					}else
						middle = "\\\""+dllPath+"\\\"";
				}
				String suffix = prop.getProperty(TestExecutionConstants.VS_TEST_SUFFIX)+" "
						+openCoverArgs+" "+prop.getProperty(TestExecutionConstants.OPEN_COVER_REPORT_FILE);
				
				command.append(commandPrefix).append(middle).append(suffix);
				context.put(TestExecutionConstants.TEST_EXECUTION_COMMAND, String.valueOf(command));
			}
		}else
			context.put(TestExecutionConstants.TEST_EXECUTION_COMMAND, unitTestCommand);
		
	}
	private String getCommand2Value(String prefix, String solutionFile, String suffix) {
		return prefix+" "+solutionFile+" "+suffix;
	}

	private void populateContextForDotnetPipelineJenkinsFile(
			VelocityContext context, Properties prop) {
		LOGGER.info("START populateContextForDotnetPipelineJenkinsFile --");
		context.put("JDK_DOTNET", prop.getProperty("project.jdk.dotnet"));
		context.put("LABEL_EXPR_DOTNET", prop.getProperty("project.assignedNode.dotnet"));//common
		context.put("NUGET_PATH", prop.getProperty("env.nuget.path"));
		context.put("SLN_FILENAME", prop.getProperty("env.sln.filename"));
		context.put("MSBUILD_PATH", prop.getProperty("env.msbuild.path"));
		context.put("COVERAGE_FILE_NAME", prop.getProperty("env.coverage.filename"));
		context.put("SONAR_SCANNER_EXE", prop.getProperty("env.sonar.scanner.exe"));
		context.put("SONAR_SCANNER_PATH", prop.getProperty("env.sonar.scanner.path"));
		context.put("SONAR_PROJECT_KEY", prop.getProperty("env.sonar.project.key"));
		context.put("SONAR_PROJECT_NAME", prop.getProperty("env.sonar.project.name"));
		context.put("VS_REPORT_PATH", prop.getProperty("env.vs.report.path"));
		context.put("VS_REPORT_FILENAME", prop.getProperty("env.vs.report.filename"));
		context.put("OC_REPORT_PATH", prop.getProperty("env.oc.report.path"));
		context.put("OC_REPORT_FILENAME", prop.getProperty("env.oc.report.filename"));
		LOGGER.info("END populateContextForDotnetPipelineJenkinsFile --");
	}

	private void populateMiscValues(VelocityContext context, String url, StringBuilder jobName, 
			Properties prop, String templateName, Map<String, String> jenkinsXmlMap) {
		LOGGER.info("START populateMiscValues --");
		StringBuilder desc = new StringBuilder();
		desc.append("&#60;html&#62;").append("&#60;b&#62;").append(ApplicationConstants.DESC1 + "&#60;br&#62;").append("&#60;&#47;b&#62;").
		append(ApplicationConstants.DESC2 + jobName + "&#60;br&#62;").
		append(ApplicationConstants.DESC3 + prop.getProperty("jenkins.job.plugin.name") +
				ApplicationConstants.COMMA).
		append(ApplicationConstants.DESC4 + prop.getProperty("jenkins.job.plugin.version") +
				"&#60;br&#62;").append("&#60;b&#62;").
		append(ApplicationConstants.DESC5 + jenkinsXmlMap.get("lastAuthor") + "&#60;br&#62;").append("&#60;&#47;b&#62;").
		append(ApplicationConstants.DESC6 + prop.getProperty("jenkins.hostname") + 
				"&#60;br&#62;").
		append(ApplicationConstants.DESC7 + url+" "+ "&#60;br&#62;").
		append(ApplicationConstants.DESC8 /*repo id*/ + "&#60;br&#62;").
		append(ApplicationConstants.DESC9 /*branch id*/+ "&#60;br&#62;").append("&#60;b&#62;").
		append(ApplicationConstants.DESC10 + jenkinsXmlMap.get("committedRevision") + "&#60;br&#62;").append("&#60;&#47;b&#62;").
		append(ApplicationConstants.DESC11 + new Date() + "&#60;br&#62;").
		append(ApplicationConstants.DESC12 + templateName + "&#60;br&#62;").append("&#60;&#47;html&#62;");
		LOGGER.info("job description :::::::: " + desc);
		context.put("REPO", url);
		context.put("DESC", desc);
		//if sonar values present
		if (null != jenkinsXmlMap.get("areSonarValuesPresent")){
			context.put("SONAR_SHELL_COMMAND", populateSonarShellCommand(prop, jenkinsXmlMap));
			populateSonarValues(prop,jenkinsXmlMap,context);
		}
		LOGGER.info("END populateMiscValues --");	
	}

	private void populateSonarValues(Properties prop, Map<String, String> jenkinsXmlMap, VelocityContext context) {
		//for basic java
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY))
			context.put(SonarConstants.BUILD_NUMBER, jenkinsXmlMap.get(SonarConstants.SONAR_PROJECT_VERSION_TAG_KEY));
		else
			context.put(SonarConstants.BUILD_NUMBER, prop.getProperty("sonar.projectVersion"));

		if(templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, SonarConstants.SONAR_SOURCES_KEY)){
			context.put(SonarConstants.WORKSPACE, jenkinsXmlMap.get(SonarConstants.SONAR_SOURCES_KEY));
		}
		if(templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, SonarConstants.SONAR_BINARIES_NEW_KEY)){
			context.put(SonarConstants.BINARIES, jenkinsXmlMap.get(SonarConstants.SONAR_BINARIES_NEW_KEY));
		}else
			context.put(SonarConstants.BINARIES, prop.getProperty("sonar.projectBinaries"));
	}


	private String populateSonarShellCommand(Properties prop,
			Map<String, String> jenkinsXmlMap) {
		String sonarBranch;
		String sonarSources;
		StringBuilder sonarCmd = new StringBuilder();

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.SONAR_BRANCH_NAME))
			sonarBranch = jenkinsXmlMap.get(ApplicationConstants.SONAR_BRANCH_NAME);
		else 
			sonarBranch = prop.getProperty(ApplicationConstants.SONAR_BRANCH_KEY); 

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.SONAR_SOURCES))
			sonarSources = jenkinsXmlMap.get(ApplicationConstants.SONAR_SOURCES);
		else 
			sonarSources = prop.getProperty(ApplicationConstants.SONAR_SOURCES_KEY);

		//creating sonar cmd
		sonarCmd.append(prop.getProperty(ApplicationConstants.SONAR_SHELL_DEFAULT_CMD_MVN_KEY));
		sonarCmd.replace(sonarCmd.indexOf("${MAVEN_HOME}"), sonarCmd.indexOf("${MAVEN_HOME}") + 
				"${MAVEN_HOME}".length(), prop.getProperty("maven.home"));
		sonarCmd.replace(sonarCmd.indexOf("${SONAR_BRANCH}"), sonarCmd.indexOf("${SONAR_BRANCH}") + 
				"${SONAR_BRANCH}".length(), sonarBranch);
		sonarCmd.replace(sonarCmd.indexOf("${SONAR_URL}"), sonarCmd.indexOf("${SONAR_URL}") + 
				"${SONAR_URL}".length(), prop.getProperty("sonar.url"));

		if (EmptyAndNullCheckUtillity.isStringNonEmpty(prop.getProperty(
				ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD1_MVN_KEY))) {
			sonarCmd.append(" ").append(prop.getProperty(
					ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD1_MVN_KEY));
			sonarCmd.replace(sonarCmd.indexOf("$SONAR_PROJECT_KEY"), sonarCmd.indexOf("$SONAR_PROJECT_KEY") + 
					"$SONAR_PROJECT_KEY".length(), prop.getProperty(ApplicationConstants.SONAR_PROJECTKEY_KEY));
		}

		if (EmptyAndNullCheckUtillity.isStringNonEmpty(prop.getProperty(
				ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD2_MVN_KEY))) {
			sonarCmd.append(" ").append(prop.getProperty(
					ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD2_MVN_KEY));
			sonarCmd.replace(sonarCmd.indexOf("$SONAR_SOURCES"), sonarCmd.indexOf("$SONAR_SOURCES") + 
					"$SONAR_SOURCES".length(), sonarSources);
		}

		if (EmptyAndNullCheckUtillity.isStringNonEmpty(prop.getProperty(
				ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD3_MVN_KEY))) {
			sonarCmd.append(" ").append(prop.getProperty(
					ApplicationConstants.SONAR_SHELL_ADDITIONAL_CMD3_MVN_KEY));
			sonarCmd.replace(sonarCmd.indexOf("$SONAR_PROJECT_NAME"), sonarCmd.indexOf("$SONAR_PROJECT_NAME") + 
					"$SONAR_PROJECT_NAME".length(), prop.getProperty(ApplicationConstants.SONAR_PROJECTNAME_KEY));
		} 

		LOGGER.info("final sonar command::::::" + sonarCmd);
		return sonarCmd.toString();
	}

	private void populateContextFromPropertyFileForFreestyleJobs(VelocityContext context,
			Properties prop) {
		LOGGER.info("START populateContextFromPropertyFileForFreestyleJobs");
		context.put(ApplicationConstants.DAYS_TO_KEEP, prop.getProperty(ApplicationConstants.DAYS_TO_KEEP_KEY));
		context.put(ApplicationConstants.NUM_TO_KEEP, prop.getProperty(ApplicationConstants.NUM_TO_KEEP_KEY));
		context.put(ApplicationConstants.ARTIFACT_DAYS_TO_KEEP, prop.getProperty(ApplicationConstants.ARTIFACT_DAYS_TO_KEEP_KEY));
		context.put(ApplicationConstants.ARTIFACT_NUM_TO_KEEP, prop.getProperty(ApplicationConstants.ARTIFACT_NUM_TO_KEEP_KEY));
		context.put(ApplicationConstants.SCM_VERSION, prop.getProperty(ApplicationConstants.SCM_VERSION_KEY));
		context.put(ApplicationConstants.CREDS_ID, prop.getProperty(ApplicationConstants.CREDS_ID_KEY));
		context.put(ApplicationConstants.LOCAL, prop.getProperty(ApplicationConstants.LOCAL_KEY));
		context.put(ApplicationConstants.DEPTH_OPTION, prop.getProperty(ApplicationConstants.DEPTH_OPTION_KEY));
		context.put(ApplicationConstants.JDK, prop.getProperty(ApplicationConstants.JDK_KEY));

		//maven
		context.put(ApplicationConstants.MAVEN_NAME, prop.getProperty(ApplicationConstants.MAVEN_NAME_KEY));
		context.put(ApplicationConstants.POM, prop.getProperty(ApplicationConstants.POM_KEY));
		context.put(ApplicationConstants.PATH_SETTINGS_XML, prop.getProperty(ApplicationConstants.PATH_SETTINGS_XML_KEY));

		//ant
		context.put("ANT_VERSION", prop.getProperty("ant.version"));
		context.put("ANT_TARGET", prop.getProperty("ant.targets"));
		context.put("ANT_NAME", prop.getProperty("ant.antName"));

		//email
		context.put(EmailConstants.EMAIL_PLUGIN_VERSION_KEY, prop.getProperty("email.plugin.version"));
		context.put(EmailConstants.PROJECT_DEFAULT_SUBJECT_KEY, prop.getProperty("email.subject"));
		context.put(EmailConstants.PROJECT_DEFAULT_CONTENT_KEY, prop.getProperty("email.body"));
		context.put(EmailConstants.PROJECT_DEFAULT_REPLYTO_KEY, prop.getProperty("email.replyTo"));
		context.put(EmailConstants.EMAIL_CONTENT_TYPE_KEY, prop.getProperty("email.content.type"));

		//jira
		context.put(JiraConstants.JIRA_PLUGIN_VERSION_KEY, prop.getProperty(JiraConstants.JIRA_PLUGIN_VERSION_TAG_KEY));
		context.put(JiraConstants.REST_API_URL_KEY, prop.getProperty(JiraConstants.REST_API_URL_TAG_KEY));
		context.put(JiraConstants.USERNAME_KEY, prop.getProperty(JiraConstants.USERNAME_TAG_KEY));
		context.put(JiraConstants.PASSWORD_KEY, prop.getProperty(JiraConstants.PASSWORD_TAG_KEY));
		LOGGER.info("END populateContextFromPropertyFileForFreestyleJobs");
	}

	private void populateContextFromJenkinsXmlOrPropertyFileForFreestyleJobs(VelocityContext context,
			Properties prop, Map<String, String> jenkinsXmlMap) {
		LOGGER.info("START populateContextFromJenkinsXmlOrPropertyFileForFreestyleJobs");
		TemplateAndPropertyFileHelper templateAndPropertyFileHelper = new TemplateAndPropertyFileHelper();
		//for maven
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.COMMAND))
			context.put(ApplicationConstants.COMMAND_TOKEN, jenkinsXmlMap.get(ApplicationConstants.COMMAND));
		else 
			context.put(ApplicationConstants.COMMAND_TOKEN, prop.getProperty(ApplicationConstants.COMMAND_MAVEN_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.SHELL_COMMAND))
			context.put(ApplicationConstants.SHELL_COMMAND_TOKEN, jenkinsXmlMap.get(ApplicationConstants.SHELL_COMMAND));
		else 
			context.put(ApplicationConstants.SHELL_COMMAND_TOKEN, prop.getProperty(ApplicationConstants.SHELL_COMMAND_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.LABEL_EXPR))
			context.put(ApplicationConstants.LABEL_EXPR_TOKEN, jenkinsXmlMap.get(ApplicationConstants.LABEL_EXPR));
		else 
			context.put(ApplicationConstants.LABEL_EXPR_TOKEN, prop.getProperty(ApplicationConstants.LABEL_EXPR_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.JOB_SCHEDULE))
			context.put(ApplicationConstants.JOB_SCHEDULE_TOKEN, jenkinsXmlMap.get(ApplicationConstants.JOB_SCHEDULE));
		else 
			context.put(ApplicationConstants.JOB_SCHEDULE_TOKEN, prop.getProperty(ApplicationConstants.JOB_SCHEDULE_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, EmailConstants.EMAIL_SUBJECT_KEY))
			context.put(EmailConstants.PROJECT_DEFAULT_SUBJECT_KEY, jenkinsXmlMap.get(EmailConstants.EMAIL_SUBJECT_KEY));
		else 
			context.put(EmailConstants.PROJECT_DEFAULT_SUBJECT_KEY, prop.getProperty(EmailConstants.EMAIL_SUBJECT_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, EmailConstants.EMAIL_BODY_KEY))
			context.put(EmailConstants.PROJECT_DEFAULT_CONTENT_KEY, jenkinsXmlMap.get(EmailConstants.EMAIL_BODY_KEY));
		else 
			context.put(EmailConstants.PROJECT_DEFAULT_CONTENT_KEY, prop.getProperty(EmailConstants.EMAIL_BODY_KEY));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.ARTIFACTORY_REPO))
			context.put("ARTIFACTORY_KEY", jenkinsXmlMap.get(ApplicationConstants.ARTIFACTORY_REPO));
		else
			context.put("ARTIFACTORY_KEY", prop.getProperty("artifactory.keyFromSelect"));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, EmailConstants.EMAIL_RECIPIENTS_KEY)){
			context.put(EmailConstants.DEFAULT_RECIPIENTS_KEY, jenkinsXmlMap.get(EmailConstants.EMAIL_RECIPIENTS_KEY));
		}
		else
			context.put(EmailConstants.DEFAULT_RECIPIENTS_KEY, prop.getProperty("extendedEmailPublisher.recipientList"));

		// Putting jira values, as it is not yet decided weather values will be coming from jenkins.xml or properties
		context.put(JiraConstants.JQL_KEY, "");
		context.put(JiraConstants.PROJECTNAME_KEY, jenkinsXmlMap.get(JiraConstants.PROJECT_KEY));
		context.put(JiraConstants.TICKETID_KEY, jenkinsXmlMap.get(JiraConstants.ID_KEY));
		context.put(JiraConstants.COMMENT_KEY, jenkinsXmlMap.get(JiraConstants.COMMENT_TAG_KEY));

		// Docker plugin
		context.put(DockerConstants.DOCKER_PLUGIN_KEY, prop.getProperty(DockerConstants.DOCKER_PLUGIN_TAG_KEY));
		context.put(DockerConstants.CLOUD_NAME_KEY, prop.getProperty(DockerConstants.CLOUD_NAME_TAG_KEY));
		context.put(DockerConstants.IMAGE_NAME_KEY, jenkinsXmlMap.get(DockerConstants.IMAGE_NAME_TAG_KEY));
		context.put(DockerConstants.VOLUMES_STRING_KEY, jenkinsXmlMap.get(DockerConstants.VOLUMES_STRING_TAG_KEY));
		context.put(DockerConstants.BIND_PORT_KEY, jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY));
		if(jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY) != null)
			context.put(DockerConstants.PORTS_KEY, jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY).replace(ApplicationConstants.COLON, ApplicationConstants.HYPHEN+ApplicationConstants.RIGHT_BRAKET));
		context.put(DockerConstants.DOCKERHOST_KEY, prop.getProperty(DockerConstants.DOCKERHOST_TAG_KEY));
		// Selenium plugin
		context.put(SeleniumConstants.SELENIUM_TARGETS_KEY, prop.getProperty(SeleniumConstants.SELENIUM_TARGETS_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_MAVEN_NAME_KEY, prop.getProperty(SeleniumConstants.SELENIUM_MAVEN_NAME_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_HOSTNAME_KEY, prop.getProperty(SeleniumConstants.SELENIUM_HOSTNAME_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_MAVEN_TEST_RESULT_KEY, prop.getProperty(SeleniumConstants.SELENIUM_MAVEN_TEST_RESULT_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_POM_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_POM_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_PORT_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_PORT_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_CONTEXT_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_CONTEXT_TAG_KEY));

		// Performance plugin
		context.put(PerformanceConstants.PERFORMANCE_TARGETS_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_TARGETS_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_MAVEN_NAME_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_MAVEN_NAME_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_HOSTNAME_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_HOSTNAME_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_MAVEN_TEST_RESULT_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_MAVEN_TEST_RESULT_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_POM_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_POM_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_PORT_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_PORT_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_CONTEXT_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_CONTEXT_TAG_KEY));

		// HTML Publisher plugin
		context.put(HTMLPublisherConstants.HTML_PUBLISHER_KEY, prop.getProperty(HTMLPublisherConstants.HTML_PUBLISHER_TAG_KEY));
		context.put(HTMLPublisherConstants.P_REPORT_NAME_KEY, prop.getProperty(HTMLPublisherConstants.P_REPORT_NAME_TAG_KEY));
		context.put(HTMLPublisherConstants.P_REPORT_FILES_KEY, prop.getProperty(HTMLPublisherConstants.P_REPORT_FILES_TAG_KEY));
		context.put(HTMLPublisherConstants.S_REPORT_NAME_KEY, prop.getProperty(HTMLPublisherConstants.S_REPORT_NAME_TAG_KEY));
		context.put(HTMLPublisherConstants.S_REPORT_FILES_KEY, prop.getProperty(HTMLPublisherConstants.S_REPORT_FILES_TAG_KEY));

		//for git		
		context.put(GitConstants.GIT_VERSION_KEY, prop.getProperty(GitConstants.GIT_VERSION));
		context.put(GitConstants.CONFIG_VERSION_KEY, "2");
		if(jenkinsXmlMap.get(GitConstants.REPOS_XML_KEY) != null){
			context.put(GitConstants.REPO_KEY, jenkinsXmlMap.get(GitConstants.REPOS_XML_KEY));
		}
		context.put(GitConstants.BRANCH_SPEC_NAME_KEY, jenkinsXmlMap.get(GitConstants.BRANCH_NAME_KEY));
		context.put(GitConstants.CREDS_ID_KEY, prop.getProperty(ApplicationConstants.CREDS_ID_KEY));
		context.put(GitConstants.GIT_TOOL_KEY, "Default");
		context.put(GitConstants.SUB_MOD_CFG_CLS_KEY, "list");
		context.put(GitConstants.DO_GEN_SUB_MOD_CONFIG_KEY, "false");
		LOGGER.info("END populateContextFromJenkinsXmlOrPropertyFileForFreestyleJobs");
	}

	private void populateContextFromPropertyFileForPipelineJobs(VelocityContext context,
			Properties prop, String url) {
		LOGGER.info("Inside populateContextFromPropertyFileForPipelineJobs");
		context.put(ApplicationConstants.LABEL_EXPR_TOKEN, prop.getProperty(ApplicationConstants.LABEL_EXPR_KEY));//u/p
		context.put(ApplicationConstants.JDK, prop.getProperty(ApplicationConstants.JDK_KEY));
		context.put("SVN_CREDS_ID", prop.getProperty(ApplicationConstants.CREDS_ID_KEY));
		context.put("MAVEN_PATH", prop.getProperty("maven.version"));
		context.put("ANT_PATH", prop.getProperty("ant.path"));
		context.put("SONAR_URL", prop.getProperty("sonar.url"));
		context.put("BUILDTOOL", prop.getProperty("buildtool"));
		context.put("TARGET", prop.getProperty("ant.targets"));
		context.put("SCM", prop.getProperty("scm"));
		String scm = prop.getProperty("scm");
		if (EmptyAndNullCheckUtillity.isStringNonEmpty(scm)) {
			if (scm.equals("SVN"))
				context.put("SVN_REPO", url);
			else if (scm.equals("GIT"))
				context.put("GIT_REPO", url);
		}

		context.put("GIT_CREDS_ID", "");
		context.put("GIT_REPO", "");
		context.put("GIT_BRANCH", "");

		//w.r.t. pipeline job
		context.put("WORKFLOW_JOB_VERSION", prop.getProperty("workflow.job.version"));
		context.put("BLUE_OCEAN_REST_VERSION", prop.getProperty("blueocean.rest.version"));
		context.put("OPENSHIFT_SYNC_VERSION", prop.getProperty("openshift.sync.version"));
		context.put("WORKFLOW_CPS_VERSION", prop.getProperty("workflow.cps.version"));
		context.put("SCRIPTPATH", prop.getProperty("scriptpath"));
		context.put("SONAR_BUILD_HOME", prop.getProperty("sonar.build.home"));
		context.put("SONAR_INTEGRATION", prop.getProperty("sonar.integration"));

		//email
		context.put(EmailConstants.EMAIL_PLUGIN_VERSION_KEY, prop.getProperty("email.plugin.version"));
		context.put(EmailConstants.DEFAULT_RECIPIENTS_KEY, prop.getProperty("extendedEmailPublisher.recipientList"));
		context.put(EmailConstants.PROJECT_DEFAULT_SUBJECT_KEY, prop.getProperty("email.subject"));
		context.put(EmailConstants.PROJECT_DEFAULT_CONTENT_KEY, prop.getProperty("email.body"));
		context.put(EmailConstants.PROJECT_DEFAULT_REPLYTO_KEY, prop.getProperty("email.replyTo"));
		context.put(EmailConstants.EMAIL_CONTENT_TYPE_KEY, prop.getProperty("email.content.type"));

		//jira
		context.put(JiraConstants.JIRA_PLUGIN_VERSION_KEY, prop.getProperty("jira.plugin.version"));
		context.put(JiraConstants.REST_API_URL_KEY, prop.getProperty("jira.restApiUrl"));
		context.put(JiraConstants.USERNAME_KEY, prop.getProperty("jira.username"));
		context.put(JiraConstants.PASSWORD_KEY, prop.getProperty("jira.password"));
		context.put(JiraConstants.JIRA_SITE_KEY,prop.getProperty("jira.site"));
		//context.put("ARTIFACTORY_REPO", prop.getProperty("artifactory.keyFromSelect"));
		//logger.info("ARTIFACTORY_REPO value:"+ prop.getProperty("artifactory.keyFromSelect"));
		//System.out.println("JIRA INTEGRATION PLUGIN TEST");
	}

	private void populateContextFromJenkinsXmlOrPropertyFileForPipelineJobs(VelocityContext context,
			Properties prop, Map<String, String> jenkinsXmlMap) {
		//git_branch, email_id
		//for maven
		LOGGER.info("START populateContextFromJenkinsXmlOrPropertyFileForPipelineJobs");
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.COMMAND))
			context.put(ApplicationConstants.COMMAND_TOKEN, jenkinsXmlMap.get(ApplicationConstants.COMMAND));
		else 
			context.put(ApplicationConstants.COMMAND_TOKEN, prop.getProperty(ApplicationConstants.COMMAND_MAVEN_KEY));

		//sonar
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.SONAR_BRANCH_NAME))
			context.put(ApplicationConstants.SONAR_BRANCH, jenkinsXmlMap.get(ApplicationConstants.SONAR_BRANCH_NAME));
		else 
			context.put(ApplicationConstants.SONAR_BRANCH, prop.getProperty(ApplicationConstants.SONAR_BRANCH_KEY));

		/*if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.PROJECTNAME))
				context.put(ApplicationConstants.PROJECTNAME, jenkinsXmlMap.get(ApplicationConstants.PROJECTNAME));*/
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.ARTIFACTORY_REPO))
			context.put("ARTIFACTORY_REPO", jenkinsXmlMap.get(ApplicationConstants.ARTIFACTORY_REPO));
		else
			context.put("ARTIFACTORY_REPO", prop.getProperty("artifactory.keyFromSelect"));

		LOGGER.info("artifactory.name value:"+prop.getProperty("artifactory.name"));
		context.put(ArtifactoryConstants.ARTIFACTORY_NAME_KEY, prop.getProperty("artifactory.name"));

		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.ISSUEID))
			context.put("ISSUE_ID", jenkinsXmlMap.get(ApplicationConstants.ISSUEID));
		if (templateAndPropertyFileHelper.fetchFromMap(jenkinsXmlMap, ApplicationConstants.COMMENT))
			context.put("COMMENT", jenkinsXmlMap.get(ApplicationConstants.COMMENT));

		// java pipeline code for docker, performance and selenium
		// Docker plugin
		LOGGER.info("docker.image value:"+jenkinsXmlMap.get("image"));
		context.put(DockerConstants.CLOUD_NAME_KEY, prop.getProperty(DockerConstants.CLOUD_NAME_TAG_KEY));
		context.put(DockerConstants.IMAGE_NAME_KEY, jenkinsXmlMap.get(DockerConstants.IMAGE_NAME_TAG_KEY));
		context.put(DockerConstants.VOLUMES_STRING_KEY, jenkinsXmlMap.get(DockerConstants.VOLUMES_STRING_TAG_KEY));
		context.put(DockerConstants.BIND_PORT_KEY, jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY));		
		if(jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY) != null)
			context.put(DockerConstants.PORT_KEY, jenkinsXmlMap.get(DockerConstants.BIND_PORT_TAG_KEY).substring(0, 4));
		// Selenium plugin
		context.put(SeleniumConstants.SELENIUM_TARGETS_KEY, prop.getProperty(SeleniumConstants.SELENIUM_TARGETS_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_HOSTNAME_KEY, prop.getProperty(SeleniumConstants.SELENIUM_HOSTNAME_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_MAVEN_TEST_RESULT_KEY, prop.getProperty(SeleniumConstants.SELENIUM_MAVEN_TEST_RESULT_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_POM_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_POM_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_PORT_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_PORT_TAG_KEY));
		context.put(SeleniumConstants.SELENIUM_CONTEXT_KEY, jenkinsXmlMap.get(SeleniumConstants.SELENIUM_CONTEXT_TAG_KEY));


		// Performance plugin
		context.put(PerformanceConstants.PERFORMANCE_TARGETS_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_TARGETS_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_HOSTNAME_KEY, prop.getProperty(PerformanceConstants.PERFORMANCE_HOSTNAME_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_POM_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_POM_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_PORT_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_PORT_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_CONTEXT_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_CONTEXT_TAG_KEY));
		context.put(PerformanceConstants.PERFORMANCE_MAVEN_TEST_RESULT_KEY, jenkinsXmlMap.get(PerformanceConstants.PERFORMANCE_MAVEN_TEST_RESULT_TAG_KEY));


		context.put("SELENIUM_FILENAME", prop.getProperty("html.publisher.selenium.reportFiles"));
		context.put("SELENIUM_REPORTNAME", prop.getProperty("html.publisher.selenium.reportName"));
		context.put("PERFORMANCE_FILENAME", prop.getProperty("html.publisher.performance.reportFiles"));
		context.put("PERFORMANCE_REPORTNAME",prop.getProperty("html.publisher.performance.reportName"));
		LOGGER.info("END populateContextFromJenkinsXmlOrPropertyFileForPipelineJobs");
	}			

	private String getPath(String[] args,String unitTest) {
		LOGGER.info("START getPath--");
		StringBuilder sb = new StringBuilder();
		for (String string : args) {
			if("mstest".equalsIgnoreCase(unitTest))
				sb.append("/testcontainer:");
			sb.append("\\\""+string+"\\\"  ");
			sb.deleteCharAt(sb.length()-1);
		}
		LOGGER.info("END getPath--"+String.valueOf(sb));
		return String.valueOf(sb);
	}
}