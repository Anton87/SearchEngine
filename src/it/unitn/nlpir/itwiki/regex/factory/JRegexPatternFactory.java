package it.unitn.nlpir.itwiki.regex.factory;

import it.unitn.nlpir.itwiki.regex.JRegexPattern;
import it.unitn.nlpir.itwiki.regex.Pattern;

public class JRegexPatternFactory extends PatternFactory {

	@Override
	public Pattern createPattern(String regex) {
		if (regex == null) {
			throw new NullPointerException("regex is null");
		}
		
		return new JRegexPattern(regex);
	}

}
