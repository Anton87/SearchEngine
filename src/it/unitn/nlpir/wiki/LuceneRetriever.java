package it.unitn.nlpir.wiki;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Search paragraphs indexed by LuceneIndexer.java"
 * 
 * @author antonio
 *
 */
public class LuceneRetriever {
	
	private static final String SEP = "\t";
	
	private int maxtHits;
	private QueryParser parser;
	private IndexSearcher searcher;
	
	public LuceneRetriever(int maxHits, String textField, Analyzer analyzer, String indexDirpath) {
		if (maxHits <= 0) {
			throw new IllegalArgumentException("maxHits must be > 0");
		}
		if (textField == null) {
			throw new NullPointerException("textField is null");
		}
		if (analyzer == null) { 
			throw new NullPointerException("analyzer is null");
		}
		if (indexDirpath == null) {
			throw new NullPointerException("indexDirpath is null");
		}		
		try {
			File indexDir = new File(indexDirpath);
			FSDirectory dir = FSDirectory.open(indexDir);
			IndexReader reader = DirectoryReader.open(dir);
			this.searcher = new IndexSearcher(reader);
						
			// set the new BM25 similarity measure
			// searcher.setSimilarity(new BM25Similarity());
			//ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_46);
			this.maxtHits = maxHits;
			System.err.print("INFORMAZIONI: Setting " + analyzer.getClass().getSimpleName() + " as analyzer... ");
			this.parser = new QueryParser(Version.LUCENE_46, textField, analyzer);
			System.err.println("done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TopDocs retrieve(String query) {
		if (query == null) {
			throw new NullPointerException("query is null");
		}
		
		String processedQuery = query.replaceAll("[^A-Za-z0-9]", " ");
		TopDocs hits = new TopDocs(0, null, 0);
		Query q;
		try {
			q = this.parser.parse(processedQuery);
			// q = this.parser.parse(query);
			hits = this.searcher.search(q, this.maxtHits);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hits;
	}
	
	void setSimilarity(Similarity similarity) {
		if (similarity == null) { 
			throw new NullPointerException("similarity is null");
		}
		System.err.print("INFORMAZIONI: Setting " + similarity.getClass().getSimpleName() + " as similarity... ");
		this.searcher.setSimilarity(similarity);
		System.err.println("done.");
	}
	
	public Document getDocumentById(int id) {
		try {
			return this.searcher.doc(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String usage = "java it.unitn.nlpir.itwiki.LuceneRetriever"
					 + " [-index dir] [-query string]"
					 + " [-maxHits hitsNum] [-similarity class]"
					 + " [-analyzer classpath]\n\n"
					 + "Print the documents satifying the query";		

		int maxHits = 1;
		String query = null;
		String index = "index";
		Similarity similarity = null;
		String analyzerClassname = null;
		
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			} 
			else if ("-query".equals(args[i])) {
				query = args[i + 1];
				i++;
			} 
			else if ("-maxHits".equals(args[i])) {
				maxHits = Integer.parseInt(args[i + 1]);
				if (maxHits <= 0) {
					System.err.println("There must be at least 1 hit");
					System.exit(-1);
				}
				i++;
			}
			else if ("-similarity".equals(args[i])) {
				try {
					similarity = 
							(Similarity) Class.forName(args[i + 1]).newInstance();
				} catch (Exception e) {
					System.err.println("Error while loading similarity class '" + similarity + "'");
					System.exit(-1);
				}
				i++;
			} else if ("analyzer".equals(args[i])) {
				analyzerClassname = args[i + 1];
				i++;
			}
		}
		
		if (query == null) {
			System.err.println("-query not set.\n" + usage);
			System.exit(-1);
		}
		
		final File indexDir = new File(index);
		if (!indexDir.isDirectory()) {
			System.out.println("Directory '" + indexDir.getAbsolutePath() + "' does not exist, please check the path");
			System.exit(-1);
		}
		
		Analyzer analyzer = null;		
		if (analyzerClassname != null) {
			try {
				analyzer = loadAnalyzer(analyzerClassname);
				System.out.println("Loaded " + analyzerClassname + " analyzer");
			} catch (Exception e) {
				System.err.println("The class '" + analyzerClassname + "' does not exist, please check the classpath");
				System.exit(-1);
			}
		} else {
			analyzer = new StandardAnalyzer(Version.LUCENE_46);
		}
					 
		LuceneRetriever retriever = new LuceneRetriever(maxHits, "text", analyzer, index);
		if (similarity != null) { 
			retriever.setSimilarity(similarity);
		}
		
		TopDocs hits = retriever.retrieve(query);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		
		
		for (int i = 0; i < scoreDocs.length; i++) {
			Document doc = retriever.getDocumentById(scoreDocs[i].doc);
			String docId = doc.get("docId");
			String docText = doc.get("text");
			String rankingPos = Integer.toString(i + 1);
			String rankingString = String.valueOf(scoreDocs[i].score);
			
			System.out.println(docId + SEP + docText + SEP + rankingPos + SEP + rankingString);	
		}
	}

	private static Analyzer loadAnalyzer(String className) throws Exception {
		Class<Analyzer> klass = (Class<Analyzer>) Class.forName(className);
		Constructor<Analyzer> ctor = klass.getConstructor(Version.class);
		Analyzer analyzer = ctor.newInstance(Version.LUCENE_46);
		return analyzer;
	}	
	
}
