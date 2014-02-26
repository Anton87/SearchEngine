package it.unitn.nlpir.itwiki.regex;

import java.util.regex.Matcher;

public class JavaPattern implements Pattern {
	
	private final java.util.regex.Pattern pattern;
	
	public JavaPattern(String regex) {		
		if (regex == null) {
			throw new NullPointerException("regex is null");
		}
		
		this.pattern = java.util.regex.Pattern.compile(regex);		
	}

	@Override
	public boolean search(String text) {
		if (text == null) { 
			throw new NullPointerException("text is null");
		}
		
		Matcher matcher = this.pattern.matcher(text);
		return matcher.find();
	}

}
