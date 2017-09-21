package gov.usdot.cv.common.util;

import com.deleidos.rtws.commons.config.RtwsConfig;

public class PropertyLocator {
	
	private PropertyLocator() {
		// All invocation through static methods.
	}
	
	public static int getInt(String key, int defaultValue) {
		String value = System.getProperty(key);
		if (value != null) {
			return Integer.parseInt(value);
		}
		return RtwsConfig.getInstance().getInt(key, defaultValue);
	}
	
	public static String getString(String key, String defaultValue) {
		String value = getString(key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}
	
	public static String getString(String key) {
		String value = System.getProperty(key);
		if (value == null) {
			value = RtwsConfig.getInstance().getString(key);
		}
		return value;
	}
	
}