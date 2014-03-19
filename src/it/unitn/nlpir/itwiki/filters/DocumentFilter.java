package it.unitn.nlpir.itwiki.filters;

import org.apache.lucene.document.Document;

public abstract class DocumentFilter {
	
	public static DocumentFilter newInstance(String filter) {
		if (filter == null) { 
			throw new NullPointerException("filter is null");
		}
		
		if ("none".equals(filter)) { 
			DocumentFilter df = new NoDocumentFilter();
			return df;
		}
		else if ("wordsNumLe5".equals(filter)) {
			DocumentFilter df = new ShortDocumentFilter(5); 
			return df;
		}
		System.err.println("WARNING: No DocumentFilter with name '" + filter + "' found. Documents will not be filtered.");
		return new NoDocumentFilter();
	}
	
	/**
	 * Returns whether this document should be filtered.
	 * 
	 * @param doc A document
	 * @return 
	 */
	public abstract boolean isFiltered(Document doc);

}
