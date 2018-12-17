package com.nagarro.jenkins.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nagarro.jenkins.plugin.ParamsJson.Actions;
import com.nagarro.jenkins.plugin.ParamsJson.Actions.Parameters;
import com.nagarro.jenkins.plugin.exceptions.EmailPropertyNotFoundException;
import com.nagarro.jenkins.plugin.exceptions.JenkinsSeedJobFailException;
import com.nagarro.jenkins.plugin.exceptions.JiraPropertyNotFoundException;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;
import com.nagarro.jenkins.plugin.utility.FetchTagName;
import com.nagarro.jenkins.plugin.utility.JobUtility;
import com.nagarro.jenkins.plugin.utility.TemplateAndPropertyFileHelper;
import com.nagarro.jenkins.plugin.utility.constants.ApplicationConstants;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

/**
 * @author Sonal Kesarwani.
 */
public class JenkinsJobCreator extends Builder implements SimpleBuildStep {

	public static final String JENKINS_URL_PARAM = "REPOS";
	public static String JENKINS_HOST_URL;
	public static String JENKINS_SEED_JOB;
	public static String SVN_USER_PROP;
	public static String SVN_PASSWORD_PROP;
	public static String JENKINS_USER_PROP;
	public static String JENKINS_PASSWORD_PROP;
	public final String name;
	boolean isDisableJob;
	boolean isPipeline;

	private final static Logger LOGGER = Logger.getLogger(JenkinsJobCreator.class);

	private static String errMsg = ApplicationConstants.ERR_MSG_PREFIX;

	@DataBoundConstructor
	public JenkinsJobCreator(String name) {
		this.name = name;
	}

	/**
	 * We'll use this from the {@code config.jelly}.
	 */
	public String getName() {
		return name;
	}

	@Override
	public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
		LOGGER.info("START inside perform -- ");
		try {
			postToJenkins(listener);
			PropertyConfigurator.configure("log4j.properties");
		}catch (UnirestException e) {
			LOGGER.info("Exception occurred due to -- "+e.getMessage());
		} 
		LOGGER.info("END inside perform method --");
	}

	@SuppressWarnings("deprecation")
	private void postToJenkins(TaskListener listener) throws UnirestException {
		LOGGER.info("START inside postToJenkins --");
		DefaultHttpClient httpClient = null;
		DisplayTemplate dt;
		DisplayTemplateGit dtg;
		JenkinsJobs jenkinsJobs = null;
		Map <String, String> jenkinsXmlAndRepoValuesMap;
		String templateNameFromJenkinsXMl = null;
		String templateName = null;
		Properties pluginProperties;
		TemplateAndPropertyFileHelper templateAndPropertyFileHelper;
		PopulateTemplateForJobs populateTemplatePipeline;
		String url = null;
		String buildType;
		String vcs;
		StringBuilder newJobName = new StringBuilder();
		StringBuilder finalTemplatePath = new StringBuilder();
		StringBuilder platform = new StringBuilder(); 
		Boolean isJobPresent = false;
		StringWriter configCreateUpdate;
		StringWriter jenkinsFileTemplate;
		List<Map<String, String>> jobs;
		List<String> tagNames = null;
		List<String> actionParameters = null;
		FetchTagName fetchTagName = null;
		JobUtility jobUtility = null;
		String projectName = null;
		isDisableJob = false;
		try {
			templateAndPropertyFileHelper = new TemplateAndPropertyFileHelper();
			LOGGER.info("jenkins before plugin properties");
			
			pluginProperties = templateAndPropertyFileHelper.loadSaveJenkinsPropertyFile();// loading properties to connect jenkins server, username and password 
			LOGGER.info("jenkins after plugin properties");
			initializeJenkinsConstants(pluginProperties);
			testAllValues();
			httpClient = establishHttpConnectionToJenkins(pluginProperties);
			actionParameters = getUrlParamFromJenkinsSeedJob(httpClient);// 
			url = actionParameters.get(0);
			projectName = extractProjectName(url);
			if (null != url) {
				jenkinsXmlAndRepoValuesMap = new HashMap<>();
				fetchTagName = new FetchTagName();
				jobUtility = new JobUtility();
				vcs = inferVCS(url);
				if (ApplicationConstants.SVN.equals(vcs)) {
					dt = new DisplayTemplate();
					jenkinsJobs = dt.getJenkinsJob(jenkinsXmlAndRepoValuesMap, url, 
							SVN_USER_PROP, SVN_PASSWORD_PROP, pluginProperties.getProperty("jenkins.filename"));//Sushil -loading jenkins.xml with all jenkins Jobs 
				} else if(ApplicationConstants.GIT.equals(vcs)){
					LOGGER.info("url: " + actionParameters.get(0)+"\t"+actionParameters.get(1));
					//GitConnectionRepo git;
					dtg = new DisplayTemplateGit();
					jenkinsJobs = dtg.getJenkinsXmlJob(jenkinsXmlAndRepoValuesMap, url, 
							pluginProperties.getProperty("jenkins.username"), pluginProperties.getProperty("jenkins.password"), 
							pluginProperties.getProperty("jenkins.filename"),actionParameters.get(1),projectName);	
				}
				tagNames = fetchTagName.getTagNames(jenkinsJobs);// Sushil - for fetching tags names from jenkins.xml 
				jobUtility.createJobName(url, newJobName, vcs,projectName);
				if (null != jenkinsJobs) {
					
					
					
					//Sushil - After creating all required things like initializing and starting jenkins properly for a particular project and job.
					// Now load jenkins.xml file
					
					templateNameFromJenkinsXMl = templateAndPropertyFileHelper.
							populateValuesFromJenkinsXml(jenkinsJobs, jenkinsXmlAndRepoValuesMap);//loaded values from jenkins.xml - 
					buildType = jenkinsXmlAndRepoValuesMap.get(ApplicationConstants.BUILD_TYPE);

					
					
					
					
					finalTemplatePath.append(ApplicationConstants.FORWARD_SLASH + ApplicationConstants.TEMPLATE);
					//template name to load and creating path from where to load the template.
					templateName = templateAndPropertyFileHelper.inferPathAndTemplateNameToLoadAndJobName(
							templateNameFromJenkinsXMl, finalTemplatePath, url, newJobName, vcs, platform);

					if (ApplicationConstants.PIPELINE.equalsIgnoreCase(buildType)) {
						finalTemplatePath.append(buildType + ApplicationConstants.FORWARD_SLASH);
					} else {
						if (!platform.toString().isEmpty() && !(
								ApplicationConstants.DOTNET.equals(platform.toString()) || ApplicationConstants.IOS.equals(platform.toString())))
							finalTemplatePath.append(buildType + ApplicationConstants.FORWARD_SLASH);
						finalTemplatePath.append(vcs + ApplicationConstants.FORWARD_SLASH);
					}
					LOGGER.info("finalTemplatePath::::::::::" + finalTemplatePath);
					populateTemplatePipeline = new PopulateTemplateForJobs(); 
					configCreateUpdate = populateTemplatePipeline.populateTemplateNew(templateName, 
							finalTemplatePath, pluginProperties, url, jenkinsXmlAndRepoValuesMap, newJobName, platform);
					jobs = jobUtility.getAllJobs();
					isJobPresent = jobUtility.jobPresence(jobs, newJobName);
					if (!isJobPresent) {
						LOGGER.info("creating a new job: " + newJobName);
						jobUtility.createNewJob(configCreateUpdate.toString(), newJobName.toString(),tagNames, isDisableJob, templateName);
						listener.getLogger().println(ApplicationConstants.GENERATED_JOB_MSG+newJobName.toString());
						if (ApplicationConstants.PIPELINE.equalsIgnoreCase(buildType)) {
							jenkinsFileTemplate = createAndPopulateTemplateForPipelineJenkinsFile(templateName, finalTemplatePath,
									populateTemplatePipeline, pluginProperties, url, jenkinsXmlAndRepoValuesMap, newJobName, 
									platform);
							createJenkinsFileForPipelineJob(newJobName.toString(), jenkinsFileTemplate, pluginProperties);
							LOGGER.info("created file::::::::::::::");
						}
					} else {
						listener.getLogger().println(ApplicationConstants.UPDATED_JOB_MSG+newJobName.toString());
						LOGGER.info("updating the job: " + newJobName);
						jobUtility.updateNewJob(configCreateUpdate.toString(), newJobName.toString(),tagNames,isDisableJob, templateName);
					}
					jobUtility.triggerJenkinsJob(newJobName.toString(), tagNames, templateName);
				} else {
					LOGGER.info("disabling job");
					jobUtility.disableAllJobs(httpClient, newJobName,tagNames, isDisableJob, templateName);
				}
			} else 
				LOGGER.info("url is null");
		} catch (UnirestException e) {
			LOGGER.info(errMsg+e.getMessage());
			listener.getLogger().println(errMsg+e.getMessage());
			jobUtility.failSeedJob(httpClient);
		}catch (JenkinsSeedJobFailException exp){
			LOGGER.info(errMsg+exp.getMessage());
			listener.getLogger().println(errMsg+exp.getMessage());
			jobUtility.failSeedJob(httpClient);
		}catch (EmailPropertyNotFoundException exp){
			LOGGER.info(errMsg+exp.getMessage());
			listener.getLogger().println(errMsg+exp.getMessage());
			jobUtility.failSeedJob(httpClient);
		}catch (JiraPropertyNotFoundException exp){
			LOGGER.info(errMsg+exp.getMessage());
			listener.getLogger().println(errMsg+exp.getMessage());
			jobUtility.failSeedJob(httpClient);
		}catch (Exception exp) {
			LOGGER.info(errMsg+exp.getMessage());
			listener.getLogger().println(errMsg+exp.getMessage());
			jobUtility.failSeedJob(httpClient);
		} finally {
			//errMsg = "";
			if (null != httpClient){
				LOGGER.info("In finally, Shutting down the Jenkins-job-creator");
				httpClient.getConnectionManager().shutdown();
				httpClient.close();
			}
		}
		LOGGER.info("END inside postToJenkins --");
	}

	/**
	 * To extract project name
	 * @param url
	 * @return
	 */
	private String extractProjectName(String url) {
		LOGGER.info("START extractProjectName --");
		String result = "";
		String[] splittedUrl = url.split("/");	
		String projectName = splittedUrl[splittedUrl.length-1];
		LOGGER.info(projectName);
		result = projectName.substring(0,projectName.length()-4);
		LOGGER.info("END extractProjectName --");
		return result;
	}

	private void testAllValues() {
		LOGGER.info("all values::::");
		LOGGER.info("JENKINS_HOST_URL::::" + JENKINS_HOST_URL);
		LOGGER.info("JENKINS_SEED_JOB::::" + JENKINS_SEED_JOB);
		LOGGER.info("SVN_USER_PROP::::" + SVN_USER_PROP);
		LOGGER.info("SVN_PASSWORD_PROP::::" + SVN_PASSWORD_PROP);
		LOGGER.info("JENKINS_USER_PROP::::" + JENKINS_USER_PROP);
		LOGGER.info("JENKINS_PASSWORD_PROP::::" + JENKINS_PASSWORD_PROP);
	}

	private void initializeJenkinsConstants(Properties pluginProperties) {
		LOGGER.info("START initializeJenkinsConstants");
		JENKINS_HOST_URL = ApplicationConstants.HTTP + pluginProperties.getProperty("jenkins.hostname") +
				ApplicationConstants.COLON + pluginProperties.getProperty("jenkins.portNumber");
		JENKINS_SEED_JOB = pluginProperties.getProperty("jenkins.seedjob.name");
		SVN_USER_PROP = pluginProperties.getProperty("svn.username");
		SVN_PASSWORD_PROP = pluginProperties.getProperty("svn.password"); 
		JENKINS_USER_PROP = pluginProperties.getProperty("jenkins.username");
		LOGGER.info("Jenkins user name"+JENKINS_USER_PROP.toString());//updated by sushil
		JENKINS_PASSWORD_PROP = pluginProperties.getProperty("jenkins.password");
		LOGGER.info("Jenkins user passwd"+JENKINS_PASSWORD_PROP.toString());
		LOGGER.info("START initializeJenkinsConstants");
	}

	/*private void deleteJenkinsJob(String jobName) throws UnirestException {
		 String TEST_XML_STRING =
			        "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";
		 JSONObject jo = XML.toJSONObject(TEST_XML_STRING);
		 String str = jo.toString(4);
		 logger.info(str);
	}*/

	private StringWriter createAndPopulateTemplateForPipelineJenkinsFile(
			String templateName, StringBuilder finalTemplatePath, PopulateTemplateForJobs populateTemplatePipeline, 
			Properties pluginProperties, String url, Map<String, String> jenkinsXmlMap, 
			StringBuilder newJobName, StringBuilder platform) {
		LOGGER.info("START createAndPopulateTemplateForPipelineJenkinsFile");
		StringWriter templateJenkinsFile;
		StringBuilder tempNameForJenkinsFile = new StringBuilder();
		tempNameForJenkinsFile.append(templateName.substring(0, templateName.indexOf(
				ApplicationConstants.DOT + ApplicationConstants.XML)));
		tempNameForJenkinsFile.append(ApplicationConstants.HYPHEN).
		append(ApplicationConstants.JENKINS_FILE);
		LOGGER.info("template name for jenkins file::::" + tempNameForJenkinsFile);
		finalTemplatePath.append(ApplicationConstants.JENKINS_FILE + 
				ApplicationConstants.FORWARD_SLASH);
		LOGGER.info("finalTemplatePath for jenkinsfile1234:::::::" + finalTemplatePath);
		templateJenkinsFile = populateTemplatePipeline.populateTemplateNew(tempNameForJenkinsFile.toString(), 
				finalTemplatePath, pluginProperties, url, jenkinsXmlMap, newJobName, platform);
		LOGGER.info("templateJenkinsFile:::::: " + templateJenkinsFile);
		LOGGER.info("END createAndPopulateTemplateForPipelineJenkinsFile");
		return templateJenkinsFile;
	}

	private String inferVCS(String url) {
		LOGGER.info("START inferVCS --");
		String vcs = null;
		if (url.contains("/svn/"))
			vcs = ApplicationConstants.SVN;
		else if (url.contains(".git"))
			vcs = ApplicationConstants.GIT;//https://git.nagarro.com/root/Test-project-123.git
		LOGGER.info("vcs:" + vcs);
		LOGGER.info("END inferVCS --");
		return vcs;
	}

	private List<String> getUrlParamFromJenkinsSeedJob(DefaultHttpClient httpClient) throws JsonSyntaxException, Exception {
		LOGGER.info("START getUrlParamFromJenkinsSeedJob --");
		List<String> url = null;
		boolean isReposParamPresent = false;
		HttpGet httpGetConfigBuild = new HttpGet(JENKINS_HOST_URL + "/job/" + JENKINS_SEED_JOB + "/api/json?pretty=true");
		String buildInfo = toString(httpClient, httpGetConfigBuild);

		BuildJson buildJson = new Gson().fromJson(buildInfo, BuildJson.class);
		String currBuildNum = null;
		if (null != buildJson && null != buildJson.getLastBuild()) 
			currBuildNum = buildJson.getLastBuild().getNumber();
		else 
			LOGGER.info(" buildJson or buildJson.getLastBuild is null");
		LOGGER.info("current build number: " + currBuildNum);

		if (null != currBuildNum) {
			HttpGet httpGetConfigParams = new HttpGet(JENKINS_HOST_URL + "/job/" + JENKINS_SEED_JOB + "/" + currBuildNum + "/api/json?pretty=true");
			ParamsJson paramsJson = new Gson().fromJson(
					toString(httpClient, httpGetConfigParams), ParamsJson.class);
			url = new ArrayList<>(2);
			for (Actions actions : paramsJson.getActions()) {
				LOGGER.info("parameters size :"+actions.getParameters().length);
				if (null != actions.getParameters() && actions.getParameters().length > 0) 
					for (Parameters parameter : actions.getParameters()) {
						if (null != parameter) {
							if(JENKINS_URL_PARAM.equals(parameter.getName()))
								url.add(0,parameter.getValue());
							if("GIT_BRANCH".equals(parameter.getName()))
								url.add(1,parameter.getValue());
							LOGGER.info("name : " + parameter.getName() + ", value : " + url);
							isReposParamPresent = true;
						}
					}
				if (isReposParamPresent)
					break;
			}
		} else 
			LOGGER.info("build is null");
		LOGGER.info("END getUrlParamFromJenkinsSeedJob --");
		return url;
	}

	@SuppressWarnings("deprecation")
	private DefaultHttpClient establishHttpConnectionToJenkins(Properties pluginProperties) {
		LOGGER.info("START establishHttpConnectionToJenkins --");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		LOGGER.info("path for creds: " + pluginProperties.getProperty("jenkins.hostname") + 
				ApplicationConstants.COLON + pluginProperties.getProperty("jenkins.portNumber"));
		//        httpClient.getCredentialsProvider().setCredentials(new AuthScope("10.127.127.74:8080", 8080),
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(
				pluginProperties.getProperty("jenkins.hostname") + ApplicationConstants.COLON + 
				pluginProperties.getProperty("jenkins.portNumber"), 
				Integer.parseInt(pluginProperties.getProperty("jenkins.portNumber"))),
				new UsernamePasswordCredentials(JENKINS_USER_PROP, JENKINS_PASSWORD_PROP));
		LOGGER.info("END establishHttpConnectionToJenkins --");
		return httpClient;
	}

	private void createJenkinsFileForPipelineJob(String newJobName, StringWriter sw, Properties pluginProperties) throws Exception {
		LOGGER.info("creating JenkinsFile on jenkins::::::::::::::");
		LOGGER.info("jenkins file content ::::::::" + sw.toString());
		String JENKINS_HOME = System.getProperty("JENKINS_HOME");//var/lib/jenkins
		Boolean isFolderPresent = false;
		Boolean isFolderCreatedNew = false;
		String path1 = JENKINS_HOME + "/jobs/"+newJobName;
		String path2 = "workspace@script";
		String jenkinsFileName = pluginProperties.getProperty("scriptpath");
		final File workspaceFolder = new File(path1);
		for (final File dirEntry : workspaceFolder.listFiles()) {
			if (dirEntry.isDirectory() && dirEntry.getName().equals(path2)) {
				isFolderPresent = true;
				File projectFolder = new File(path1 + "/" + path2);
				for (final File fileEntry : projectFolder.listFiles()) {
					if (!fileEntry.isDirectory() && fileEntry.getName().equals(jenkinsFileName)) {
						//file exists: overwrite
						fileEntry.delete();
						break;
					}
				}
			}
		}

		if (!isFolderPresent)
			isFolderCreatedNew = new File(path1 + "/" + path2 + "/").mkdirs();

		LOGGER.info("value::::::::" + isFolderCreatedNew);
		if (isFolderCreatedNew || isFolderPresent) {
			File jenkinsFileNew = new File(path1 + "/" + path2 + "/" + jenkinsFileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(jenkinsFileNew));
			bw.write(sw.toString());
			bw.close();
		}
	}

	public static String toString(DefaultHttpClient client, 
			HttpRequestBase request) throws Exception {
		LOGGER.info("START toString --");
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		LOGGER.info("client --"+client+"\t request"+request);
		String basicAuth = Base64.getEncoder().encodeToString((JenkinsJobCreator.JENKINS_USER_PROP + ":" + JenkinsJobCreator.JENKINS_PASSWORD_PROP).getBytes(StandardCharsets.UTF_8));
		request.addHeader("Authorization", "Basic " + basicAuth);
		String responseBody = client.execute(request, responseHandler);
		LOGGER.info(responseBody + "\n");
		LOGGER.info("END toString --");
		return responseBody;
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	/**
	 * Descriptor for {@link JenkinsJobCreator}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use {@code transient}.
		 */
		private boolean useFrench;

		/**
		 * In order to load the persisted global configuration, you have to 
		 * call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}

		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 *
		 * @param value
		 *      This parameter receives the value that the user has typed.
		 * @return
		 *      Indicates the outcome of the validation. This is sent to the browser.
		 *      <p>
		 *      Note that returning {@link FormValidation#error(String)} does not
		 *      prevent the form from being saved. It just means that a message
		 *      will be displayed to the user. 
		 */
		public FormValidation doCheckName(@QueryParameter String value)
				throws IOException, ServletException {
			if (value.length() == 0)
				return FormValidation.error("Please set a name");
			if (value.length() < 4)
				return FormValidation.warning("Isn't the name too short?");
			return FormValidation.ok();
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types 
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "Create jenkins job";
		}

		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			useFrench = formData.getBoolean("useFrench");
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this, like setUseFrench)
			save();
			return super.configure(req);
		}

		/**
		 * This method returns true if the global configuration says we should speak French.
		 *
		 * The method name is bit awkward because global.jelly calls this method to determine
		 * the initial state of the checkbox by the naming convention.
		 */
		public boolean getUseFrench() {
			return useFrench;
		}
	}
}

