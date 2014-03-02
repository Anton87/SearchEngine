package it.unitn.nlpir.itwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;

public class CandidateGenerator {
	
	private static ClassLoader classLoader = CandidateGenerator.class.getClassLoader();		
	
	public static void main(String[] args)  { 		
		
		String usage = "java it.unitn.nlpir.itwiki.CandidateGenerator"
					 + " [-index INDEX_PATH] [-questions QUESTIONS_PATH]"
					 + " [-similarity SIMILARITY_CLASSPATH] [-maxHits HITS_NUM]"
					 + " [-candidates CANDIDATES_PATH]\n\n"
					 + "Generate candidate answers for questions in the questions File. ";
	
		String index = null;
		String textField = "text";
		String questions = null;
		String candidates = null;
		String similarity = null;
		int maxHits = 10;
		
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			}
			else if ("-maxHits".equals(args[i])) {
				maxHits = Integer.parseInt(args[i + 1]);
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
			
		LuceneRetriever retriever = new LuceneRetriever(maxHits, textField, index);
		
		if (similarity != null) {			
			try {
				Class<?> similarityClass = classLoader.loadClass(similarity);
				Similarity similarityMeasure = (Similarity) similarityClass.newInstance();
				retriever.setSimilarity(similarityMeasure);
			} catch (Exception e) {
				System.out.println("Class '" + similarity + "' loading error: \n" + e.getMessage());
				System.exit(-1);
			}			
		}
		
		try (BufferedReader	in = 
				new BufferedReader(
					new InputStreamReader(
							new FileInputStream(questionsFile), "UTF-8"));			
			PrintWriter out = new PrintWriter(candidates, "UTF-8")) {
			
			int processedQuestions = 0;
			int candidatesRetrieved = 0;
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				String[] lineSplit = line.trim().split(" ", 2);
				String qid = lineSplit[0];
				String qtext = lineSplit[1];
				
				TopDocs hits = retriever.retrieve(qtext);
				ScoreDoc[] scoreDocs = hits.scoreDocs;
				
				for (int i = 0; i < scoreDocs.length; i++) {
					Document doc = retriever.getDocumentById(scoreDocs[i].doc);
					String docId = doc.get("docId");
					String docText = doc.get("text");
					String rankingPos = Integer.toString(i + 1);
					String rankingString = String.valueOf(scoreDocs[i].score);
															
					String outputLine = String.format("%s\t%s\t%s\t%s\t%s\t%s", qid, docId, rankingPos, rankingString, "NONE", docText);
					out.println(outputLine);
				}	
				
				processedQuestions++;
				candidatesRetrieved += scoreDocs.length;
				
				
				if (processedQuestions % 50 == 0) {
					System.out.println("Processed questions: " + processedQuestions);
					System.out.println("Candidates retrieved: " + candidatesRetrieved);
					System.out.println();
				}
			}
			System.out.println("Processed questions: " + processedQuestions);
			System.out.println("Candidates retrieved: " + candidatesRetrieved);
			
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() );
		}
	}

}
