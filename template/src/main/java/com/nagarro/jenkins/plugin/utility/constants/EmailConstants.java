package com.nagarro.jenkins.plugin.utility.constants;
/**
 * Contains constants pertain to email plugin
 * @author saurabh01
 *
 */
public class EmailConstants {
	//Constants for basic-java-build.xml
	public static final String EMAIL_PLUGIN_VERSION_KEY = "EMAIL_PLUGIN_VERSION";
	public static final String DEFAULT_RECIPIENTS_KEY = "DEFAULT_RECIPIENTS";
	public static final String PROJECT_DEFAULT_SUBJECT_KEY = "PROJECT_DEFAULT_SUBJECT";
	public static final String PROJECT_DEFAULT_CONTENT_KEY = "PROJECT_DEFAULT_CONTENT";
	public static final String PROJECT_DEFAULT_REPLYTO_KEY = "PROJECT_DEFAULT_REPLYTO";
	public static final String EMAIL_CONTENT_TYPE_KEY = "EMAIL_CONTENT_TYPE";
	
	//Constants to fetch variable from jenkins.xml
	public static final String EMAIL_SUBJECT_KEY = "subject";
	public static final String EMAIL_BODY_KEY = "body";
	public static final String EMAIL_RECIPIENTS_KEY = "recipients";
	public static final String EMAIL_ATTACH_BUILD_LOG_KEY = "whetherToAttachBuildLog";
}
