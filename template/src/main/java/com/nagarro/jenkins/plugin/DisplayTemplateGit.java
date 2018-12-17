package com.nagarro.jenkins.plugin;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;

import com.nagarro.jenkins.plugin.exceptions.JenkinsSeedJobFailException;
import com.nagarro.jenkins.plugin.jenkinsJobsTemplates.JenkinsJobs;

public class DisplayTemplateGit {
	private static Logger LOGGER = Logger.getLogger(DisplayTemplateGit.class);
	public JenkinsJobs getJenkinsXmlJob(Map<String, String> jenkinsXmlAndRepoValuesMap, String url, String username,
			String password, String property, String branch, String projectName) throws Exception{
		LOGGER.info("START getJenkinsXmlJob --");
		LOGGER.info("Url received :"+url+"\t Username :"+username+"\t password :"+password+"\t Branch :"+branch+"\t ProjectName :"+projectName);
		//String branch = "master";
		JenkinsJobs jenkinsJobs = null;
		String host = url.substring(0, 24);
		try {
			GitLabApi gitLabApi = GitLabApi.login(ApiVersion.V3, host, username, password, true);
			List<Project> projects = gitLabApi.getProjectApi().getAllProjects();
			LOGGER.info("host:"+host);
			LOGGER.info("size:"+projects.size());
			System.out.println(projects.contains(projectName));
			for (Project project : projects) {
				LOGGER.info("project:"+project.getName());
				if(projectName.equals(project.getName())){
					RepositoryFile repositoryFile = gitLabApi.getRepositoryFileApi().getFile(property, project.getId(), branch);
					LOGGER.info("repositoryFile :\n"+repositoryFile.getFileName());
					byte [] fileInBytes = Base64.decodeBase64(repositoryFile.getContent());
					String file = new String(fileInBytes);
					LOGGER.info("File content:\n"+file);
					FileBuilder fileBuilder = new FileBuilder();
					jenkinsJobs = fileBuilder.jenkinsxmlToJenkinsJob(file);
					LOGGER.info("Jenkins Job "+jenkinsJobs.getJenkinsJob().getBuild());
				}
			}
			jenkinsXmlAndRepoValuesMap.put("repos", url);
			jenkinsXmlAndRepoValuesMap.put("branch_name", branch);
		} catch (GitLabApiException e) {
			LOGGER.info("Unable to connect git repository"+e.getMessage());
			throw new JenkinsSeedJobFailException("Unable to connect with git repository"+e.getMessage());
		}
		LOGGER.info("END getJenkinsXmlJob --");
		return jenkinsJobs;
	}	
	/*public static void main(String[] args) throws GitLabApiException {
		GitLabApi gitLabApi = GitLabApi.login(ApiVersion.V3, "https://git.nagarro.com/", "dscadmin", "ihzj1789!", true);
		List<Project> projects = gitLabApi.getProjectApi().getAllProjects();
		//System.out.println(gitLabApi.getProjectApi().getProject("Administrator","devops-post-commit"));
		for (Project project : projects) {
			System.out.println("Project Name :"+project.getNamespace().getName()+"\t"+project.getName());
			if("devops-post-comit".equals("devops-post-commit")){
				System.out.println("*******************"+project.getId());
			}
		}
	}*/
}