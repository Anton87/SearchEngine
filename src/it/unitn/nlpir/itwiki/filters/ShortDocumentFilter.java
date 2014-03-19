package it.unitn.nlpir.itwiki.filters;

import org.apache.lucene.document.Document;

/**
 * Filter all documents that contain a 
 *  number of words le than the specified in the Constructor.
 *
 */
public class ShortDocumentFilter extends DocumentFilter {
	
	private final int wordsNum;
	
	public ShortDocumentFilter(int wordsNum) {
		if (wordsNum <= 0) {
			throw new IllegalArgumentException("wordsNum must be > 0");
		}
		
		this.wordsNum = wordsNum;		
	}

	@Override
	public boolean isFiltered(Document doc) {
		if (doc == null) { 
			throw new NullPointerException("doc is null");
		}
		
		String text = doc.get("text");
		String[] words = text.split("\\s+");
		return words.length <= this.wordsNum;				
	}

}
