package it.unitn.nlpir.itwiki.regex.factory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import util.StringUtil;

public class StringUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testStripFilenameWithExtension() {
		assertEquals(
				"/path/to/file", 
					StringUtil.stripExtension("/path/to/file.txt"));		
	}
	
	@Test
	public void testStringFilenameWithNoExtension() { 
		assertEquals(
				"/path/to/file",
					StringUtil.stripExtension("/path/to/file"));
	}

}
