package com.github.ltsopensource.core.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeUtil {
	
	public static Object getTypeValue(String str) {
		
		if(checkNumber(str)) {
			return Integer.parseInt(str);
		} else if(checkDouble(str)) {
			return Double.parseDouble(str);
		}
		
		return str;
		
	} 

	public static boolean checkDouble(String str) {
		if (str == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*(\\.?)[0-9]*");
		Matcher matcher = pattern.matcher(str);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	public static boolean checkNumber(String str) {
		if (str == null) {
			return false;
		}
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher matcher = pattern.matcher(str);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

}
