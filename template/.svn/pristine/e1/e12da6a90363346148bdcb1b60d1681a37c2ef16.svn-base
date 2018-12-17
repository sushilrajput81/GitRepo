package com.nagarro.jenkins.plugin;

import javax.xml.bind.annotation.XmlRootElement;

import hudson.model.ParametersDefinitionProperty;

public class ConfigParams {
	
	private Project project;
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@XmlRootElement(name="project")
	public static class Project
	{
		private Properties properties;
		
		public Properties getProperties ()
		{
			return properties;
		}
		
		public void setProperties (Properties properties)
		{
			this.properties = properties;
		}
		
		public static class Properties
		{
			private ParametersDefinitionProperty parametersDefinitionProperty;
			
			public ParametersDefinitionProperty getParametersDefinitionProperty() {
				return parametersDefinitionProperty;
			}
			
			public void setParametersDefinitionProperty(
					ParametersDefinitionProperty parametersDefinitionProperty) {
				this.parametersDefinitionProperty = parametersDefinitionProperty;
			}
			
		}
	}

}
