package com.nagarro.jenkins.plugin.utility;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * Class made to encrypt and decrypt the properties file content
 * @author saurabh01
 *
 */
public class PasswordDecryption {

	static final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
	static final String password = "saurabh";
	
	/**
	 * To initialize the encryptor with password, it will initialize only once 
	 */
	static public void initialize(){
		if(!encryptor.isInitialized()){
			encryptor.setPassword(password);
			encryptor.initialize();
		}
	}

	/**
	 * This method will decrypt password which will come in "ENC(*)" form
	 * @param password
	 * @return
	 */
	public static String decryptPassword(String password){
		return encryptor.decrypt(password);
	}

	/**
	 * To encrypt the text that we want to put in properties file
	 * @param password
	 * @return
	 */
	public static String encryptPassword(String text){
		return encryptor.encrypt(text);
	}
	
	/*public static void main(String[] args) {
		initialize();
		String s= encryptor.encrypt("ihzj1789!");
		String result = encryptor.decrypt(s);
		System.out.println(s);
		if("ihzj1789!".equals(result))
			System.out.println("Correct");
	}*/
}
