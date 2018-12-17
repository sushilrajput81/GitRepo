package com.nagarro.jenkins.plugin;

public class EmailData {
	
	private String isEmailToBeSent;
	
	private String[] emailId;

	public String getIsEmailToBeSent() {
		return isEmailToBeSent;
	}

	public void setIsEmailToBeSent(String isEmailToBeSent) {
		this.isEmailToBeSent = isEmailToBeSent;
	}

	public String[] getEmailId() {
		return emailId;
	}

	public void setEmailId(String[] emailId) {
		this.emailId = emailId;
	}
	
}
