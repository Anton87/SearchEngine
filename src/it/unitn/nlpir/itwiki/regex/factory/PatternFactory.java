package it.unitn.nlpir.itwiki.regex.factory;

import it.unitn.nlpir.itwiki.regex.Pattern;

public abstract class PatternFactory {
	
	public static PatternFactory getFactory(String factory) { 
		if (factory == null) {
			throw new NullPointerException("factory is null");
		}
		
		if ("java".equals(factory)) {
			PatternFactory pf = new JavaPatternFactory();
			//System.out.println("Returning factory '" + pf.getClass().getSimpleName() + "'");
			return pf;
		}
		else if ("jregex".equals(factory)) {
			PatternFactory pf = new JRegexPatternFactory();
			//System.out.println("Returning factory '" + pf.getClass().getSimpleName() + "'");
			return pf;
		}
		
		System.err.println("No factory with name '" + factory + "' found. Default Java Regex Factory will be returned.");
		return new JRegexPatternFactory();
	}
	
	public abstract Pattern createPattern(String pattern);

}
