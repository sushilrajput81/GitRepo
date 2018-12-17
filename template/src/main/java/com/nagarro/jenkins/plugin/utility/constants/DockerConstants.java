package com.nagarro.jenkins.plugin.utility.constants;
/**
 *  Contains constants pertain to docker plugin
 * @author saurabh01
 *
 */
public class DockerConstants {
	//Constants for basic-java-build.xml
	public static final String DOCKER_PLUGIN_KEY = "DOCKER_PLUGIN";
	public static final String CLOUD_NAME_KEY = "CLOUD_NAME";
	public static final String IMAGE_NAME_KEY = "IMAGE_NAME";
	public static final String VOLUMES_STRING_KEY = "VOLUMES_STRING";
	public static final String BIND_PORT_KEY = "BIND_PORT";
	public static final String PORTS_KEY = "PORTS";
	public static final String DOCKERHOST_KEY = "DOCKERHOST";
	
	//for pipeline
	public static final String PORT_KEY = "PORT";
	
	//Constants to fetch variable from jenkins.xml
	public static final String DOCKER_PLUGIN_TAG_KEY = "docker.plugin";
	public static final String CLOUD_NAME_TAG_KEY = "docker.cloudName";
	public static final String IMAGE_NAME_TAG_KEY = "image";
	public static final String VOLUMES_STRING_TAG_KEY = "volumesString";
	public static final String BIND_PORT_TAG_KEY = "bindPorts";
	public static final String DOCKERHOST_TAG_KEY = "docker.hostname";
	
	
}
