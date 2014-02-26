package it.unitn.nlpir.itwiki.regex.factory;

import it.unitn.nlpir.itwiki.regex.JavaPattern;

import it.unitn.nlpir.itwiki.regex.Pattern;

public class JavaPatternFactory extends PatternFactory {

	@Override
	public Pattern createPattern(String regex) {
		if (regex == null) { 
			throw new NullPointerException("pattern is null");
		}
		
		return new JavaPattern(regex);
	}

}
