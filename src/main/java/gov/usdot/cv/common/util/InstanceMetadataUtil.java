package gov.usdot.cv.common.util;

public class InstanceMetadataUtil {
	
	private InstanceMetadataUtil() {
		// All invocation through static methods.
	}
	
	public static int getNodeNumber(String fqdn, String domain) {
		if (fqdn == null || domain == null) return -1;
		String subdomain = fqdn.replace(domain, "").trim();
		if (subdomain.length() == 0) return -1;

		int start = (subdomain.charAt(subdomain.length() - 1) == '.') ? 
			subdomain.length() - 2 : subdomain.length() - 1;
		
		String num = "";
		for (int i = start; i > 0; i--) {
			char ch = subdomain.charAt(i);
			if (! Character.isDigit(ch)) {
				break;
			}
			
			num = ch + num;
		}
		
		if (num.length() == 0) return 1;
		return Integer.parseInt(num);
	}
	
	public static int getNodeNumber() {
		String fqdn = PropertyLocator.getString("RTWS_FQDN");
		String domain = PropertyLocator.getString("RTWS_DOMAIN");
		return getNodeNumber(fqdn, domain);
	}
	
}