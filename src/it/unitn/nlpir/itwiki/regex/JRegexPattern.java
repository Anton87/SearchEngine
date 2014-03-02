package it.unitn.nlpir.itwiki.regex;

import jregex.Matcher;
import jregex.REFlags;

public class JRegexPattern implements Pattern {
	
	private final jregex.Pattern pattern;
	
	public JRegexPattern(String regex) {
		if (regex == null) {
			throw new NullPointerException("regex is null");
		}
		
		this.pattern = new jregex.Pattern(regex, REFlags.IGNORE_CASE);
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
