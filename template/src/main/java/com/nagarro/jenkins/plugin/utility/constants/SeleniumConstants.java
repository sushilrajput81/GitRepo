package com.nagarro.jenkins.plugin.utility.constants;
/**
 * Contains constants pertain to Selenium plugin
 * @author saurabh01
 *
 */
public class SeleniumConstants {
	//Constants for basic-java-build.xml
	public static final String SELENIUM_TARGETS_KEY = "SELENIUM_TARGETS";
	public static final String SELENIUM_MAVEN_NAME_KEY = "SELENIUM_MAVEN_NAME";
	public static final String SELENIUM_HOSTNAME_KEY = "SELENIUM_HOSTNAME";
	public static final String SELENIUM_MAVEN_TEST_RESULT_KEY = "SELENIUM_MAVEN_TEST_RESULT";
	public static final String SELENIUM_POM_KEY = "SELENIUM_POM";
	public static final String SELENIUM_PORT_KEY = "SELENIUM_PORT";
	public static final String SELENIUM_CONTEXT_KEY = "SELENIUM_CONTEXT";
	
	//Constants to fetch variable from jenkins.xml
	public static final String SELENIUM_TARGETS_TAG_KEY = "selenium.targets";
	public static final String SELENIUM_MAVEN_NAME_TAG_KEY = "selenium.mavenName";
	public static final String SELENIUM_HOSTNAME_TAG_KEY = "selenium.hostname";
	public static final String SELENIUM_MAVEN_TEST_RESULT_TAG_KEY = "selenium.maven.test.failure.ignore";
	public static final String SELENIUM_POM_TAG_KEY = "s-pom";
	public static final String SELENIUM_PORT_TAG_KEY = "s-port";
	public static final String SELENIUM_CONTEXT_TAG_KEY = "s-context";
}
