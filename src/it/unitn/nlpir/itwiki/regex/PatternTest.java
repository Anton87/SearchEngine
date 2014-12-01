package it.unitn.nlpir.itwiki.regex;

import static org.junit.Assert.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jregex.Matcher;

import org.junit.Test;

public class PatternTest {
	
	private final static String CONDITIONAL_REGEX = "(?(?=A)B|C)";
	
	@Test
	public void testPerlRegexWithJRegexPattern() {
		jregex.Pattern pattern = new jregex.Pattern(CONDITIONAL_REGEX);
		Matcher m = pattern.matcher("AB");
		assertTrue(m.find());		
	}
	
	@Test(expected=PatternSyntaxException.class)
	public void testPerlRegexWithJavaPattern() { 
		Pattern pattern = Pattern.compile(CONDITIONAL_REGEX);
		java.util.regex.Matcher m = pattern.matcher("AB");
	}

}
