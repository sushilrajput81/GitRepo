package com.nagarro.jenkins.plugin;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitConnectionRepo {
	
	public void connectToGit(String url) {
		try {
			Repository repo = new FileRepositoryBuilder().setGitDir(new File(url)).build();
			Ref master = repo.getRef("master");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
