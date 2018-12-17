package com.nagarro.jenkins.plugin.jenkinsJobsTemplates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * For artifactory plugin in jenkins.xml
 * @author saurabh01
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="jenkins-job")
public class JenkinsJob
{
	@XmlElement(name="template")
    private String template;

	@XmlElement(name="build")
    private Build build;
	
	@XmlElement(name="testExecution")
    private TestExecution testExecution;
	
	@XmlElement(name="sonar")
    private Sonar sonar;
	
	@XmlElement(name="email")
    private Email email;
	
	@XmlElement(name="jira")
    private Jira jira;
	
	@XmlElement(name="artifactory")
    private Artifactory artifactory;
	
	@XmlElement(name="docker-run")
    private DockerPlugin dockerPlugin;
	
	@XmlElement(name="selenium-run")
	private SeleniumPlugin seleniumPlugin;
	
	@XmlElement(name="performance-run")
	private PerformancePlugin performancePlugin;
	
    public TestExecution getTestExecution() {
		return testExecution;
	}

	public void setTestExecution(TestExecution testExecution) {
		this.testExecution = testExecution;
	}

	public SeleniumPlugin getSeleniumPlugin() {
		return seleniumPlugin;
	}

	public void setSeleniumPlugin(SeleniumPlugin seleniumPlugin) {
		this.seleniumPlugin = seleniumPlugin;
	}

	public PerformancePlugin getPerformancePlugin() {
		return performancePlugin;
	}

	public void setPerformancePlugin(PerformancePlugin performancePlugin) {
		this.performancePlugin = performancePlugin;
	}

	public DockerPlugin getDockerPlugin() {
		return dockerPlugin;
	}

	public void setDockerPlugin(DockerPlugin dockerPlugin) {
		this.dockerPlugin = dockerPlugin;
	}

	public Artifactory getArtifactory() {
		return artifactory;
	}

	public void setArtifactory(Artifactory artifactory) {
		this.artifactory = artifactory;
	}

	public String getTemplate () {
        return template;
    }

    public void setTemplate (String template) {
        this.template = template;
    }

	public Build getBuild() {
		return build;
	}

	public void setBuild(Build build) {
		this.build = build;
	}

	public Sonar getSonar() {
		return sonar;
	}

	public void setSonar(Sonar sonar) {
		this.sonar = sonar;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	public Jira getJira() {
		return jira;
	}

	public void setJira(Jira jira) {
		this.jira = jira;
	}
    
}
	