package com.nagarro.jenkins.plugin.exceptions;

public class JenkinsJobNotCreatedException extends JenkinsServiceException {

    private static final long serialVersionUID = 1L;

    public JenkinsJobNotCreatedException(String message) {
        super(message);
    }

}
