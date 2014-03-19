package it.unitn.nlpir.itwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unitn.nlpir.itwiki.regex.Pattern;
import it.unitn.nlpir.itwiki.regex.factory.PatternFactory;

public class RelevantFlagger {
	
	public static void main(String[] args) {
		
		String usage = "java it.unitn.nlpir.itwiki.RelevantFlagger"
					 + " [-patterns PATTERNS_PATH] [-candidates CANDIDATES_PATH]"
					 + " [-relevantCandidates RELEVANT_PATH] [-patternlib (java|jregex)]\n\n"
					 + "Set the relevant flag for candidate answers in candidates File.";
		
		String patterns = null;
		String candidates = null;
		String patternlib = "java";
		String relevantCandidates = null;
		
		for (int i = 0; i < args.length; i++) {
			if ("-patterns".equals(args[i])) {
				patterns = args[i + 1];
				i++;
			}
			else if ("-candidates".equals(args[i])) {
				candidates = args[i + 1];
				i++;
			}
			else if ("-relevantCandidates".equals(args[i])) {
				relevantCandidates = args[i + 1];
				i++;
			}
			else if ("-pattern".equals(args[i])) {
				patternlib = args[i + 1];
				i++;
			}
		}
		
		if (candidates == null) {
			System.err.println("-candidates not set\nUsage: " + usage);
			System.exit(-1);
		}
		
		if (patterns == null) {
			System.err.println("-patterns not set\nUsage: " + usage);
			System.exit(-1);
		}
		
		if (relevantCandidates == null) {
			System.err.println("-relevantCandidates not set\nUsage: " + usage);
			System.exit(-1);
		}
		
		final File patternsFile = new File(patterns);
		doesFileExistOrExit(patternsFile);
		
		final File candidatesFile = new File(candidates);
		doesFileExistOrExit(candidatesFile);
		
		final PatternFactory patternFactory = PatternFactory.getFactory(patternlib);
		
		try (				
				BufferedReader in = new BufferedReader(
						new InputStreamReader(
								new FileInputStream(candidatesFile), "UTF-8"));
			
				PrintWriter out = 
						new PrintWriter(relevantCandidates, "UTF-8")) {
			

			Map<String, List<Pattern>> qid2PatternsMap = loadQid2PatternsMap(patternsFile, patternFactory);
			System.out.printf("Patterns loaded.\n");
			
			int lineNum = 0;		
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				String[] linesplit = line.trim().split("\t");
				
				if (linesplit.length != 6) {
					System.err.println("Incorrect format: lineNum: " + lineNum + ", line: " + line);
					System.exit(-1);
				}				
				
				String qid = linesplit[0];			
				String docId = linesplit[1];
				String rank = linesplit[2];
				String score = linesplit[3];
				String text = linesplit[5];
				
				List<Pattern> patternsList = qid2PatternsMap.get(qid);				
				if (patternsList == null) {
					System.err.println("Key error: qid " + qid + " not present");
					System.exit(-1);
				}
			
				boolean relevant = false;
				for (int i = 0;  !relevant && i < patternsList.size(); i++) {
					Pattern pattern = patternsList.get(i);
					//Matcher matcher = pattern.matcher(text);
					//relevant = matcher.find();
					relevant = pattern.search(text);
				}
				
				String outline = String.format("%s\t%s\t%s\t%s\t%s\t%s", qid, docId, rank, score, String.valueOf(relevant), text);
				out.println(outline);			
				
				lineNum++;
			}

			System.out.println("Processed lines: " + lineNum);	
		} catch (IOException e) {
			System.err.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() );			
		}
	}	
	
	private static Map<String, List<Pattern>> loadQid2PatternsMap(File patternsFile, PatternFactory patternFactory) throws IOException {
		assert patternsFile != null; 
		
		Map<String, List<Pattern>> qid2PatternsMap = new HashMap<>();
		try (BufferedReader in = 
				new BufferedReader(
						new FileReader(patternsFile))) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				String[] linesplit = line.trim().split(" ", 2);
				String qid = linesplit[0];
				String regex = linesplit[1];
				
				List<Pattern> patterns = qid2PatternsMap.get(qid);
				if (patterns == null) {
					patterns = new ArrayList<>();
					qid2PatternsMap.put(qid, patterns);
				}
				//Pattern pattern = Pattern.compile(regex);
				//Pattern pattern = new JavaPattern(regex);
				//Pattern pattern = new JRegexPattern(regex);
				Pattern pattern = patternFactory.createPattern(regex);
				patterns.add(pattern);
			}
		}
		
		return qid2PatternsMap;	
	}
	
	private static boolean doesFileExistOrExit(File file) {
		assert file != null;
		
		if (!file.isFile() || !file.canRead()) {
			System.err.println("File '" + file.getAbsolutePath() + "' does not exist or is not redable.");
			System.exit(-1);
		}
		return true;
	}

}
