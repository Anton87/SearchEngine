package it.unitn.nlpir.itwiki.filters;

import org.apache.lucene.document.Document;


/**
 * Do not filter any document.
 *
 */
public class NoDocumentFilter extends DocumentFilter {

	@Override
	public boolean isFiltered(Document doc) {
		if (doc == null) {
			throw new NullPointerException("doc is null");
		}
		
		return false;
	}

}
