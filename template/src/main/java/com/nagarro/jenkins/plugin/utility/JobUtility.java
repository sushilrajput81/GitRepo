package com.nagarro.jenkins.plugin.utility;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.options.Options;
import com.nagarro.jenkins.plugin.JenkinsJobCreator;
import com.nagarro.jenkins.plugin.exceptions.CustomizeXmlException;
import com.nagarro.jenkins.plugin.utility.constants.ApplicationConstants;

public class JobUtility {
	
	private static Logger LOGGER = Logger.getLogger(JobUtility.class);
	
	public static final String DOTNET = "Dotnet";
	
	public static final String PIPELINE = "pipeline";
	
	public void createJobName(String url, StringBuilder newJobName, String vcs, String projectName) {
		LOGGER.info("START createJobName");
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
			newJobName.append(projectName);
		}
		LOGGER.info("END createJobName");
	}
	
	public boolean jobPresence(List<Map<String, String>> jobs, StringBuilder jobCreated) {
		LOGGER.info("START  jobPresence--");
		boolean flag = false;
		if (null != jobs)
			for (Map<String, String> job : jobs) {
				LOGGER.info("jobName:" + job.get("name"));
				if (job.get("name").equals(jobCreated.toString())) {
					flag = true;
					break;
				}
			}
		LOGGER.info("isJobPresent : " + flag);	
		return flag;
	}
	
	/**
	 * To explicitly fail the seed job by passing null
	 * @param httpClient
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void failSeedJob(DefaultHttpClient httpClient){
		LOGGER.info("START failSeedJob");
		try {
			httpClient.execute(null);
		} catch (IOException e) {
			LOGGER.info("Exception occurred while failing seed job");
			e.printStackTrace();
		}
		LOGGER.info("END failSeedJob");
	}
	
	public void disableAllJobs(DefaultHttpClient httpClient,
			StringBuilder newJobName, List<String> tagNames, boolean isDisableJob, String templateName) throws Exception, UnirestException {
		LOGGER.info("START disableAllJobs");
		String tokens[];
		String jobName;
		isDisableJob = true;
		List<Map<String, String>> jobs = getAllJobs();
		if (null != jobs)
			for (Map<String, String> job : jobs) {
				jobName = job.get("name");
				tokens = jobName.split("-");
				LOGGER.info("Job Names: " + newJobName);
				if (null != tokens && tokens[0].equals(newJobName.toString())){
					LOGGER.info("disabling the job: " + newJobName);
					String newConfigXml = disableJob(httpClient, jobName);
					updateNewJob(newConfigXml, jobName,tagNames,isDisableJob, templateName);
				}
			}
		LOGGER.info("END disableAllJobs");
	}
	
	public List<Map<String, String>> getAllJobs() throws UnirestException {
		LOGGER.info("START getAllJobs --");
		String path = "/view/All/api/json";
		List<Map<String, String>> jobs = null;

		Options.refresh();//to restart Unirest.
		LOGGER.info("2:::::::::::::::::::::::::::::::::");//Sushil
		HttpResponse<String> response = doGetToJenkins(path /*+ "?" + qs*/);

		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> fromJson = new Gson().fromJson(response.getBody(), type);

		jobs = (List<Map<String, String>>) fromJson.get("jobs");

		LOGGER.info("END getAllJobs --");
		return jobs;
	}
	
	private HttpResponse<String> doGetToJenkins(String path) throws UnirestException {
		LOGGER.info("START doGetToJenkins --");
		String target = JenkinsJobCreator.JENKINS_HOST_URL + path;
		HttpResponse<String> response = Unirest.get(target)
				.basicAuth(JenkinsJobCreator.SVN_USER_PROP, JenkinsJobCreator.SVN_PASSWORD_PROP).asString();
		LOGGER.info("END doGetToJenkins --");
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private String disableJob(DefaultHttpClient httpClient, String jobName) throws Exception {
		LOGGER.info("START disableJob");
		HttpGet httpGetConfig = new HttpGet(JenkinsJobCreator.JENKINS_HOST_URL + ApplicationConstants.JENKINS_JOB_BASE_PATH + 
				ApplicationConstants.FORWARD_SLASH + jobName + "/config.xml");
		LOGGER.info("URI : "  + httpGetConfig.getURI());
		String configXml = JenkinsJobCreator.toString(httpClient, httpGetConfig);
		JSONObject json = XML.toJSONObject(configXml);
		String newConfigXml = null;
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> fromJson = new Gson().fromJson(json.toString(), type);

		for (Entry<String, Object> map : fromJson.entrySet()) {
			LinkedTreeMap<String, Object> ltm = (LinkedTreeMap<String, Object>) map.getValue();
			LOGGER.info("old xml : "  + XML.toString(fromJson));
			ltm.put("disabled", "true");
			newConfigXml = XML.toString(new JSONObject(fromJson));
			LOGGER.info("new xml : "  + newConfigXml);
		}
		LOGGER.info("END disableJob");
		return newConfigXml;
	}
	
	public void updateNewJob(String configUpdateXml, String newJobName, List<String> tagNames, boolean isDisableJob, String templateName) throws UnirestException, CustomizeXmlException {
		LOGGER.info("START updateNewJob --");
		String path1 = "/job/";
		String path2 = "/config.xml";
		LOGGER.info("path to update: " + path1 + newJobName + path2 +"\n");
		//    	logger.info("final config.xml:::::: " + configUpdateXml);
		postToJenkins(path1 + newJobName + path2, configUpdateXml, tagNames,isDisableJob, templateName);
		LOGGER.info("END updateNewJob --");
	}
	
	private HttpResponse<String> postToJenkins(String path, String xml, List<String> tagNames, boolean isDisableJob, String templateName) throws UnirestException, CustomizeXmlException {
		LOGGER.info("inside postToJenkins() : final path received: " + path);
		LOGGER.info("inside postToJenkins() : final xml received: " + xml);
		if (EmptyAndNullCheckUtillity.isStringNonEmpty(xml)){
			CustomizeXml customize = new CustomizeXml();
			xml = stripNonValidXMLCharacters(xml);
			LOGGER.info("isDisable value: "+isDisableJob+"\t"+"templateName: "+templateName);
			if(EmptyAndNullCheckUtillity.isStringNonEmpty(templateName)){
			if((templateName.contains("java".toLowerCase()) || templateName.contains("java".toUpperCase())) && isDisableJob == false && !(templateName.contains("pipeline".toLowerCase()) || templateName.contains("pipeline".toUpperCase())))
				xml = customize.customizeXmlJava(tagNames, xml); 
			else if(templateName.contains("dotnet".toLowerCase()) || templateName.contains("dotnet".toUpperCase()) && isDisableJob == false)
				xml = customize.customizeXmlDotnet(tagNames, xml);
			}
		}
		LOGGER.info("new xml: " + xml);
		HttpResponse<String> response = Unirest.post(JenkinsJobCreator.JENKINS_HOST_URL + path)
				.basicAuth(JenkinsJobCreator.SVN_USER_PROP, JenkinsJobCreator.SVN_PASSWORD_PROP)
				.header("Content-Type", "application/xml")
				.body(xml).asString();
		return response;
	}
	
	private String stripNonValidXMLCharacters(String in) {      
		LOGGER.info("START stripNonValidXMLCharacters: ");
		StringBuffer out = new StringBuffer(in);
		for (int i = 0; i < out.length(); i++) {
			if(out.charAt(i) == 0x1a) {
				out.setCharAt(i, ' ');
			} else 
				out.setCharAt(i, out.charAt(i));
		}
		LOGGER.info("END stripNonValidXMLCharacters: ");
		return out.toString();
	}
	
	public void createNewJob(String configCreateXml,
			String newJobName,List<String> tagNames, boolean isDisableJob, String templateName) throws CustomizeXmlException {
		LOGGER.info("START createNewJob --");
		String path = "/createItem?name=";
		try {
			postToJenkins(path + newJobName , configCreateXml, tagNames,isDisableJob, templateName);
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		LOGGER.info("END createNewJob --");
	}
	
	public void triggerJenkinsJob(String jobName, List<String> tagNames, String templateName) throws CustomizeXmlException, UnirestException, IOException {
		LOGGER.info("inside job trigger method : newJobName : " + jobName);
		String path1 = "/job/";
		String path2 = "/build?BUILD_USER=";
		String pathToTriggerJob = path1 + jobName + path2 + JenkinsJobCreator.SVN_USER_PROP;
		HttpResponse<String> response = null;
		LOGGER.info("pathToTriggerJob: " + pathToTriggerJob);
		response = postToJenkins(pathToTriggerJob, "",tagNames,false, templateName);
		Unirest.shutdown();
		LOGGER.info("response from triggerJenkinsJob: " + response);
	}
}
