package tk.qsjia.hostseditor.util;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: SJia
 * Date: 13-1-9
 * Time: 下午3:06
 * To change this template use File | Settings | File Templates.
 */
public class StringUtils {
	private StringUtils() {

	}

	/**
	 * 常量定义
	 */
	public static final String URL = "^http[s]?:\\/\\/([\\w-]+\\.)+[\\w-]+([-+\\w./?%*&=#]*)?$";
	public static final String IP = "^(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)){3}$";
	public static final String HOST = "^(([a-zA-Z0-9]+-?)*[a-zA-Z0-9]+\\.)+[a-zA-Z]{2,4}$";
	public static final String INTEGER = "^\\d+$";
	public static final String ALPHABET = "^[A-Za-z]$";

	public static boolean isUrl(String str) {
		return validateString(str, URL);
	}

	public static boolean isIP(String str) {
		return validateString(str, IP);
	}

	public static boolean isHost(String str) {
		return validateString(str, HOST);
	}

	public static boolean isInteger(String str) {
		return validateString(str, INTEGER);
	}

	public static boolean isAlphabet(String str) {
		return validateString(str, ALPHABET);
	}

	public static boolean validateString(String str, String regexString) {
		Pattern pattern = Pattern.compile(regexString);
		return pattern.matcher(str).matches();
	}
}
