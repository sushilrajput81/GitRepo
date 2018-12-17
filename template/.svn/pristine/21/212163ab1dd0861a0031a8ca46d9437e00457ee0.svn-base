package com.nagarro.jenkins.plugin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path(value = "/pluginApi")
public class CreateJenkinsJobPluginAPI {

	@GET
    public void getUrl(@QueryParam("REPOS") String repoUrl) {
    	System.out.println("inside getValues()");
    	if (null != repoUrl)
    		System.out.println("repoUrl: " + repoUrl);
    }
	
	@GET
	@Path("/getMessage")
	private void getMessage() {
		System.out.println("inside getMessage");
	}
}
