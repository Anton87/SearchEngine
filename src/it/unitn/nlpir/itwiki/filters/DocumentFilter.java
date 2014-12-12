package it.unitn.nlpir.itwiki.filters;

import org.apache.lucene.document.Document;


public abstract class DocumentFilter {
	
	public static DocumentFilter newInstance(String docFilter) {
		if (docFilter == null)
			throw new NullPointerException("filter is null");
		
		if (docFilter.equals("none"))  
			return new NoDocumentFilter();
		else if (docFilter.equals("wordsNumLe5"))
			return new ShortDocumentFilter(5); 
			
		System.err.println("WARNING! Wrong docFilter parameter '" + docFilter + '.' + 
						   "Falling back to NoDocumentFilter.");
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
