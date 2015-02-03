package it.unitn.nlpir.wiki;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Version;

import util.FileUtil;
import util.StringUtil;

public class CandidateGenerator {
	
	private static ClassLoader classLoader = CandidateGenerator.class.getClassLoader();
	
	private static final String FS = "\t"; // field separator
	
	//private final String indexDir;
	//private final String similarity;
	private final boolean filterDuplicates;
	//private final int maxHits;
	private final int numHits;
	
	private final LuceneRetriever retriever;
	private final Map<String, String> text2DocId = new HashMap<>();	// text 2 docId 
	
	/**
	 * Initialize the CandidateGenerator.
	 * 
	 * @param indexDir
	 * @param filterDuplicates
	 * @param analyzer
	 * @param maxHits
	 * @param numHits
	 */
	public CandidateGenerator(String indexDir, boolean filterDuplicates, 
			Analyzer analyzer, int maxHits, int numHits) { 
		this(indexDir, null, filterDuplicates, analyzer, maxHits, numHits);
	}
	
	/**
	 * Initialize the CandidateGenerator.
	 * 
	 * @param indexDir
	 * @param similarity
	 * @param filterDuplicates
	 * @param analyzer
	 * @param maxHits
	 * @param numHits
	 */
	public CandidateGenerator(String indexDir, Similarity similarity, 
			boolean filterDuplicates, Analyzer analyzer, int maxHits, int numHits) {
		checkNotNull(indexDir, "indexDir is null");
		checkArgument(!indexDir.trim().equals(""), "indexDir not specified");
		File indexFile = new File(indexDir);
		checkArgument(indexFile.exists(), indexDir + " does not exist");
		checkArgument(indexFile.isDirectory(), indexDir + " is not a dir");
		checkArgument(indexFile.canRead(), indexDir + " cannot be read. Permission negated");	
		checkNotNull(analyzer, "analyzer is null");
		checkArgument(maxHits > 0, "maxHits must be > 0");
		checkArgument(numHits > 0, "numHits must be > 0");
		checkArgument(numHits <= maxHits, "numHits must be <= 0");
		
		this.numHits = numHits; // number of this to return
		this.filterDuplicates = filterDuplicates; // filter duplicates ? 		
		// insantiate the lucene retriever with specified  analyzer and maxHits num
		retriever = new LuceneRetriever(maxHits, "text", analyzer, indexDir);
		if (similarity != null) {
			// Add our similairty measure to the lucene retriever
			retriever.setSimilarity(similarity);
		}
	}
	
	/**
	 * Generate the candidates for the questions in the questions file.
	 * 	Save the candidates in the candidates file.
	 *  
	 * @param questionsFile The file containing the questions
	 * @param candidatesFile The file where to save the  generate candidates
	 */
	public void generateCanididates(String questionsFile, String candidatesFile) {
		checkNotNull(questionsFile, "questionsFile is null");
		checkArgument(!questionsFile.trim().equals(""), "questionsFile not specified");
		// check that the questions file does exist and can be read
		checkArgument(new File(questionsFile).exists(), questionsFile + " does not exist");
		checkArgument(new File(questionsFile).isFile(), questionsFile + " is not a file");
		checkArgument(new File(questionsFile).canRead(), questionsFile + " cannot be read. Permission negated");
		
		checkNotNull(candidatesFile, "candidatesFile is null");
		checkArgument(!candidatesFile.trim().equals(""), "candidatesFile not specified");
		
		String duplicateCandidatesFile = StringUtil.stripExtension(candidatesFile) + ".duplicates.txt";
		System.out.println("duplicateCandidatesFile. " + duplicateCandidatesFile);
		Date start = new Date();
		
		PrintWriter out = null;
		PrintWriter outDup = null;
		try (BufferedReader	in = 
				new BufferedReader(
					new InputStreamReader(
							new FileInputStream(questionsFile), "UTF-8"))) {
			
			// Create parent dir for candidates file (if doesn't exist)
			FileUtil.createParentDir(candidatesFile);
			out = new PrintWriter(
					new WriterOutputStream(
							new FileWriter(candidatesFile), "UTF-8"));
			outDup = new PrintWriter(
					new WriterOutputStream(
							new FileWriter(duplicateCandidatesFile), "UTF-8"));
			
			int processedQuestionsNum = 0;
			int candidatesRetrievedNum = 0;
			/** Keep track of all (normalized) documents text retrieved for each Query
			 *   (used to check that candidates with duplicate text are inserted)
			 */			
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				String[] lineSplit = line.trim().split(" ", 2);
				String qid = lineSplit[0];
				String query = lineSplit[1]; // The question to process
				
				TopDocs hits = retriever.retrieve(query);
				ScoreDoc[] scoreDocs = hits.scoreDocs;
				
				int collectedDocsNum = 0; // Number of collected documents ( no duplicates )
			
				for (int i = 0; i < scoreDocs.length; i++) { 
					Document doc = retriever.getDocumentById(scoreDocs[i].doc);
					String docId = doc.get("docId");
					String docText = doc.get("text");
					String rankingPos = Integer.toString(i + 1);
					String rankingString = String.valueOf(scoreDocs[i].score);
					
					/* Remove all chars not in range [a-zA-Z] from docText */ 	
					if (!filterDuplicates || !isDuplicate(doc)) {
						// Print candidate if not a duplicate
						out.println(qid + FS + docId + FS + rankingPos + FS + rankingString + FS + "none" + FS + docText);
						collectedDocsNum++;
					} else {
						// Print duplicate doc on the duplicates file
						System.out.printf("Question n. %s: Skipping document '%s', duplicate of '%s'\n", qid, docId, getOriginalDocId(doc));
						outDup.println(qid + FS + docId + FS + rankingPos + FS + rankingString + FS + "none" + FS + docText);
					}
					
					if (collectedDocsNum >= numHits) break;			
				}
				
				processedQuestionsNum++;
				candidatesRetrievedNum += scoreDocs.length;

				newQueryProcessed(); // performs some clean-up operations 
							
				if (processedQuestionsNum % 50 == 0) {
					System.out.printf("Processed %d questions\n", processedQuestionsNum);
					System.out.printf("Candidates %d retrieved (%d milliseconds)\n", candidatesRetrievedNum, new Date().getTime() - start.getTime());
					System.out.println();
				}
			}
			System.out.println("Processed questions: " + processedQuestionsNum);
			System.out.println("Candidates retrieved: " + candidatesRetrievedNum);
			
			out.close();
			outDup.close();
		} catch (IOException e) {
			System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() );
			e.printStackTrace();
		} 
	}
	
	private void newQueryProcessed() { 
		text2DocId.clear();
	}
	
	/**
	 * Returns true if a document is a duplicate.
	 * 
	 * @param doc A document
	 * @return
	 */
	public boolean isDuplicate(Document doc) { 
		String text = StringUtil.removeAllNonAlphaChars(doc.get("text"));
		if (text2DocId.containsKey(text)) { 
			return true;
		}
		//System.out.println("new docId: "  + doc.get("docId"));
		text2DocId.put(text, doc.get("docId"));
		return false;
	}
	
	/**
	 * Get the docId of the original document stored in text2DocId map
	 * 	which has the same text of this document 
	 * @param text
	 * @return
	 */
	public String getOriginalDocId(Document doc) {
		assert doc != null;
		
		String text = StringUtil.removeAllNonAlphaChars(doc.get("text"));		
		String docId = text2DocId.get(text);
				
		assert docId == null;	
		return docId;
	}
	
	public static void main(String[] args)  { 		
		
		String usage = "java it.unitn.nlpir.itwiki.CandidateGenerator"
					 + " [-index dir] [-questions file] "
					 + " [-similarity classname] "
					 + " [-analyzer classname] "
					 + " [-filterDuplicates true|false] "
					 + " [-maxHits int] "
					 + " [-numHits int] "
					 + " [-candidates file] \n\n"
					 + "Generate candidate answers for questions in the questions File. ";
	
		String index = null;
		String textField = "text";
		String questions = null;
		String candidates = null;
		String similarity = null;
		boolean filterDuplicates = true;
		String analyzerClassname = null;
		/** The maximum number of documents to search for candidate generations. 
		 *  Only first maxHits candidates are retrieved by lucene.  
		 */
		int maxHits = 10;
		/**
		 * Number of hits to return. Keep in mind that that number of this returned
		 *  can be less the numHits if the first maxHits candidates retrieved contains
		 *  lot of duplicates.
		 */
		int numHits = 10;
		
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			}
			else if ("-maxHits".equals(args[i])) {
				maxHits = Integer.parseInt(args[i + 1]);
				i++;
			} 
			else if ("-numHits".equals(args[i])) {
				numHits = Integer.parseInt(args[i + 1]);
				i++;
			}
			else if ("-questions".equals(args[i])) {
				questions = args[i + 1];
				i++;
			}
			else if ("-similarity".equals(args[i])) {
				similarity = args[i + 1];
				i++;
			}
			else if ("-candidates".equals(args[i])) {
				candidates = args[i + 1];
				i++;
			} else if ("-analyzer".equals(args[i])) {
				analyzerClassname = args[i + 1];
				i++;
			} else if ("-filterDuplicates".equals(args[i])) {
				filterDuplicates = Boolean.parseBoolean(args[i + 1]);
				i++;
			}
		}
			
		if (index == null)  {
			System.err.println("-index not set.\nUsage: " + usage);
			System.exit(-1);
		}		
		
		if (questions == null) {
			System.err.println("-questions not set.\nUsage: " + usage);
			System.exit(-1);
		}
		
		if (candidates == null) { 
			System.err.println("-candidates not set.\nUsage: " + usage);
			System.exit(-1);
		}
			
		final File questionsFile = new File(questions);
		if (!questionsFile.isFile() || !questionsFile.canRead()) {
			System.err.println("File '" + questionsFile.getAbsolutePath() + "' does not exist or is not redable.");
			System.exit(-1);
		}	
		
		System.out.println(CandidateGenerator.class.getSimpleName() + " [");
		System.out.println("    -index: " + index);
		System.out.println("    -numHits: " + numHits);
		System.out.println("    -maxHits: " + maxHits);
		System.out.println("    -analyzer: " + analyzerClassname);
		System.out.println("    -similarity: " + similarity);
		System.out.println("    -questions: " + questions);
		System.out.println("    -candidates: " + candidates);
		System.out.println("]");
		
		Analyzer analyzer = null;
		if (analyzerClassname != null) {
			try {
				System.err.println("INFORMAZIONI: Loading " + analyzerClassname + "... ");
				analyzer = loadAnalyzer(analyzerClassname);
			} catch (Exception e) {
				System.err.println("Analyzer " + analyzerClassname + " does not exist. Please, check the class path");
				System.exit(-1);
			}
		} else {
			System.err.println("Analyzer is null: using StandardAnalyzer.");
			analyzer = new StandardAnalyzer(Version.LUCENE_46);
		}
		
		Similarity similarityMeasure = null;
		if (similarity != null) {			
			try {
				Class<?> similarityClass = classLoader.loadClass(similarity);
				System.err.println("INFORMAZIONI: Loading " + similarity + "... ");
				similarityMeasure = (Similarity) similarityClass.newInstance();
			} catch (Exception e) {
				System.err.println("Class '" + similarity + "' loading error: \n" + e.getMessage());
				System.exit(-1);
			}			
		}
		
		new CandidateGenerator(index, similarityMeasure, filterDuplicates, analyzer, maxHits, numHits)
			.generateCanididates(questions, candidates);
	}

	/**
	 * Load the specified Analyzer. 
	 * 
	 * @param className The analyzer class name
	 * @return
	 * @throws Exception
	 */
	private static Analyzer loadAnalyzer(String className) throws Exception {
		Class<Analyzer> klass = (Class<Analyzer>) Class.forName(className);
		Constructor<Analyzer> ctor = klass.getConstructor(Version.class);
		Analyzer analyzer = ctor.newInstance(Version.LUCENE_46);
		return analyzer;
	}
	
	

}
