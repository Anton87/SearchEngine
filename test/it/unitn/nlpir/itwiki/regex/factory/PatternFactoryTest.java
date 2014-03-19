package it.unitn.nlpir.itwiki.regex.factory;

import static org.junit.Assert.*;
import it.unitn.nlpir.itwiki.regex.JRegexPattern;

import org.junit.Test;

public class PatternFactoryTest {

	/**
	 * Test that the PatternFactory class returns the 
	 *  JavaPatternFactory when provided with the string param "java"
	 */
	@Test
	public void testGetJavaFactory() {
		PatternFactory factory = PatternFactory.getFactory("java");
		assertTrue(factory instanceof JavaPatternFactory);
	}
	
	/**
	 * Test that the PatternFactory class returns the
	 * 	JRegexPatternFactory when provided with the string param "jregex"
	 */
	@Test
	public void testGetJRegexFactory() {
		PatternFactory factory = PatternFactory.getFactory("jregex");
		assertTrue(factory instanceof JRegexPatternFactory);
	}

}
