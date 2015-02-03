package util;

import java.util.regex.Pattern;

public class StringUtil {
	
	/** Matches chars in [a-zA-Z] range */
	private static final Pattern NO_ALPHA_CHARS = Pattern.compile("[^a-zA-Z]");
	
	private StringUtil() {};
	
	/**
	 * Remove all characters not in the [a-zA-Z] range.
	 * @param text
	 * @return
	 */
	public static String removeAllNonAlphaChars(String text) { 
		assert text != null;
		
		return NO_ALPHA_CHARS.matcher(text).replaceAll("");		
	}
	
	/**
	 * Remove the extension from a file name.
	 * 
	 * @param text
	 * @return
	 */
	public static String stripExtension(String filename) {
		assert filename != null;
		
		return filename.lastIndexOf(".") != -1 
				? filename.substring(0, filename.lastIndexOf(".")) 
				: filename; 
	}
	
	

}
