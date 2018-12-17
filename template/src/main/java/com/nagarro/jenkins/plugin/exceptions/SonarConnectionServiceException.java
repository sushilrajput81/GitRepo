package com.nagarro.jenkins.plugin.exceptions;

public class SonarConnectionServiceException extends Exception {

    private static final long serialVersionUID = 1L;

    public SonarConnectionServiceException(String message) {
        super(message);
    }

    public SonarConnectionServiceException(String message, Throwable t) {
        super(message, t);
    }

}
