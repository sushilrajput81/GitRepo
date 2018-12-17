package com.nagarro.jenkins.plugin.utility;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJob;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;

public class FetchTagName {
	
	private static Logger LOGGER = Logger.getLogger(FetchTagName.class);
	
	/**
	 * To fetch the tag name from jenkins.xml
	 * @param jenkinsJobs
	 */
	public List<String> getTagNames(JenkinsJobs jenkinsJobs) {//Sushil - Tag name given in jenkins.xml file
		LOGGER.info("START fetchingTagName --");
		List<String> jenkinsValue = null;

		if(jenkinsJobs != null){
			jenkinsValue = new ArrayList<>();
			JenkinsJob jenkinsJob = jenkinsJobs.getJenkinsJob(); 		
			if (null != jenkinsJob.getSonar()) {
				jenkinsValue.add("sonar");
			} 
			if( null != jenkinsJob.getJira()){
				jenkinsValue.add("jira");
			}
			if( null != jenkinsJob.getArtifactory()){
				jenkinsValue.add("artifactory");
			}
			if( null != jenkinsJob.getDockerPlugin()){
				jenkinsValue.add("docker-run");
			}
			if( null != jenkinsJob.getSeleniumPlugin()){
				jenkinsValue.add("selenium-run");
			}
			if(null != jenkinsJob.getPerformancePlugin()){
				jenkinsValue.add("performance-run");
			}
			LOGGER.info("Tag Names size :"+jenkinsValue.size());
		}
		LOGGER.info("END fetchingTagName --");
		return jenkinsValue;
	}
}
