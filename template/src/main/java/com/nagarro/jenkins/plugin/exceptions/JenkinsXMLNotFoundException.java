package com.nagarro.jenkins.plugin.exceptions;

import java.io.FileNotFoundException;

public class JenkinsXMLNotFoundException extends FileNotFoundException {

    private static final long serialVersionUID = 1L;

    public JenkinsXMLNotFoundException(String message) {
        super(message);
    }

}
