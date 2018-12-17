package com.nagarro.jenkins.plugin.jenkinsJobsTemplates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * For JenkinsJobs in jenkins.xml
 * @author saurabh01
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="jenkins-jobs")
public class JenkinsJobs {

	@XmlElement(name="jenkins-job")
    private JenkinsJob jenkinsJob;

	public JenkinsJob getJenkinsJob() {
		return jenkinsJob;
	}

	public void setJenkinsJob(JenkinsJob jenkinsJob) {
		this.jenkinsJob = jenkinsJob;
	}
		
}
