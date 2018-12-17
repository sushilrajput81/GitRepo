package com.nagarro.jenkins.plugin.utility;

import java.util.Collection;

public class EmptyAndNullCheckUtillity {
	private static boolean status;
	
	public static <T> Boolean isCollectionNonEmpty(Collection<T> var) {
		status = false;
		if (null != var && !var.isEmpty()) {
			status = true;
		}
		return status;
	}
	
	public static Boolean isStringNonEmpty(String string) {
		status = false;
		if (null != string && !string.isEmpty()) {
			status = true;
		}
		return status;
	}

}
