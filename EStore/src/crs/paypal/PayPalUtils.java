package crs.paypal;

import java.util.HashMap;
import java.util.Map;

public class PayPalUtils {
	private static String defaultKeyValueSeparator = ":";
	private static String defaultRecordSeparator = "|";

	public static String mapToString(HashMap<String, String> customFieldMap) {
		return mapToString(customFieldMap, defaultKeyValueSeparator, defaultRecordSeparator);
	}



	public static String mapToString(Map<String, String> pMap, String pKeyValueSeparator, String pRecordSeparator)
	{
		StringBuffer delimitedStringBuf = new StringBuffer();
		for (Map.Entry<String, String> entry : pMap.entrySet()) {
			delimitedStringBuf.append((String)entry.getKey());
			delimitedStringBuf.append(pKeyValueSeparator);
			delimitedStringBuf.append((String)entry.getValue());
			delimitedStringBuf.append(pRecordSeparator);
		}
		String delimitedString = delimitedStringBuf.toString();

		if (delimitedString.endsWith(pRecordSeparator)) {
			delimitedString = delimitedString.substring(0, delimitedString.length() - 1);
		}
		return delimitedString;
	}
}
