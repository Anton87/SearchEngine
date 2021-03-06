package it.unitn.nlpir.itwiki.segmenter;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import it.unitn.nlpir.italian.textpro.TextProSegmenter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * Split long paragraphs in a wiki corpus in many sentences.
 * The format of the input corpus should be as following: 
 *   <pid>TAB<text>TAB<skip-text>
 * 
 * Support languages:
 *  . Italian (it)
 *  . English (en)
 * 
 * @author Antonio Uva
 *
 */
public class WikiParagraphsSegmenter {

	private static final String FS = "\t"; // fields separator
	/* print a log msg each tot sentences */
	private static final int SENTENCES_NUM_PER_LOG = 10000;
	private static final String SENTENCES_NUM_PER_LOG_OPT = "sentencesNumPerLog";

	private static final String SEGMENTER_OPT = "segmenter";
	private static final String LANGUAGE_OPT = "lang";
	private static final String WIKI_CORPUS_PATH_OPT = "wikiFile";
	private static final String WIKI_CORPUS_SEGMENTED_PATH_OPT = "outputWikiFile";

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("help", false, "Print the help");
		options.addOption(SEGMENTER_OPT, true,
				"The sentence segmenter (e.g. TextPro or OpenNLP");
		options.addOption(LANGUAGE_OPT, true, 
				"The text language (it or en)");
		options.addOption(WIKI_CORPUS_PATH_OPT, true,
				"The path of the file containing wikipedia corpus");
		options.addOption(WIKI_CORPUS_SEGMENTED_PATH_OPT, true, 
				"The path where to save the wikipedia segmented corpus");		
		options.addOption(
				OptionBuilder
				.withLongOpt(SENTENCES_NUM_PER_LOG_OPT)
				.withDescription("The number of sentences to process before printing a log message")
				.hasArg()
				.isRequired(false)
				.create());
		//options.addOption(SENTENCES_NUM_PER_LOG_OPT, true, 
		//		"The number of sentences to process before printing a log message");

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);			
			if (cmd.hasOption("help")) {
				new HelpFormatter().printHelp(
						WikiParagraphsSegmenter.class.getName(), options);
				System.exit(0);
			}
			String language = cmd.getOptionValue(LANGUAGE_OPT);
			if (language == null || language.trim().equals("")) {
				System.err.println("language not specified");
				System.exit(1);
			}
			
			if (!language.equals("en") && !language.equals("it")) {
				System.err.println("Wrong language parameter. The value can be 'it' or 'en'.");
				System.exit(1);
			}

			String segmenterName = cmd.getOptionValue(SEGMENTER_OPT);
			if (segmenterName == null || segmenterName.trim().equals("")) {
				System.err.println("segmenter not specified");
				System.exit(1);
			}

			// Get the it-wiki corpus path
			String wikiFilePath = cmd.getOptionValue(WIKI_CORPUS_PATH_OPT);
			if (wikiFilePath == null || wikiFilePath.trim().equals("")) {
				System.err.println("wikiFile not specified");
				System.exit(1);
			}
			File wikiFile= new File(wikiFilePath);
			if (!wikiFile.exists() || !wikiFile.isFile() || !wikiFile.canRead()) { 
				System.err.println("file does not exist or permission negated: " + wikiFile.getPath());
				System.exit(1);
			}

			// Get the output path where to save the segmented it-wiki corpus
			String outputWikiFilePath = cmd.getOptionValue(WIKI_CORPUS_SEGMENTED_PATH_OPT);
			if (outputWikiFilePath == null || outputWikiFilePath.trim().equals("")) {
				System.err.println("outputWikiFile not specified");
				System.exit(1);
			}
			
			int sentencesNumPerLog = SENTENCES_NUM_PER_LOG;
			String sentencesNumPerLogValue = cmd.getOptionValue(SENTENCES_NUM_PER_LOG_OPT);
			if (sentencesNumPerLogValue != null && !sentencesNumPerLogValue.trim().equals("")) {
				sentencesNumPerLog = Integer.parseInt(sentencesNumPerLogValue);
			}
			
			System.out.println(WikiParagraphsSegmenter.class.getSimpleName() + " [");
			System.out.println("    " + LANGUAGE_OPT + ":  " + language);
			System.out.println("    " + SEGMENTER_OPT + ": " + segmenterName);
			System.out.println("    " + WIKI_CORPUS_PATH_OPT + ": " + wikiFilePath);
			System.out.println("    " + WIKI_CORPUS_SEGMENTED_PATH_OPT + ": " + outputWikiFilePath) ;
			System.out.println("    " + SENTENCES_NUM_PER_LOG_OPT +  ": " + sentencesNumPerLog);
			System.out.println("]");

			AnalysisEngine ssegmenter = null;
			// Use the TextPro segmenter
			if (segmenterName.equals("TextPro")) {
				/*
				 *  Convert lang to TextPro lang value: 
				 *   it -> ita
				 *   en -> eng
				 */
				String textProLang = language.equals("en") ? "eng" : "ita"; 
				ssegmenter = createEngine("desc/TextProSegmenterDescriptor",
						TextProSegmenter.PARAM_VERBOSE, false, 
						TextProSegmenter.PARAM_LANGUAGE, textProLang);
			} 
			// Use the OpenNLP segmenter
			else if (segmenterName.equals("OpenNLP")) {
				ssegmenter = createEngine(OpenNlpSegmenter.class, 
						OpenNlpSegmenter.PARAM_LANGUAGE, language);
			} 
			else {
				System.err.println("Wrong segmenter paramater. The value can be 'TextPro' or 'OpenNLP'.");
				System.exit(1);
			}
			
			int totalLinesNum = 0;
			PrintWriter out = 
					new PrintWriter(
							//new BufferedWriter(new FileWriter(outputWikiFilePath)));
							new FileWriter(outputWikiFilePath));
			String line = null;
			try (BufferedReader in = 
					new BufferedReader(
							new FileReader(wikiFilePath))) {
				
				Date start = new Date(); // record start date
				
				JCas jcas = JCasFactory.createJCas();
				for (line = in.readLine(); line != null; line = in.readLine()) { 
					String[] linesplit = line.split(FS);					
					if (linesplit.length != 3) {
						throw new IOException("Wrong format error: expected 3 fields, got " + linesplit.length);
					}
					String pid = linesplit[0];	// paragraph ID
					String paragraph = linesplit[1];

					jcas.reset();
					jcas.setDocumentLanguage(language);
					jcas.setDocumentText(paragraph);
					ssegmenter.process(jcas);
					int sentenceNum = 0;
					// Write each sentence on a different line
					for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
						String sid = pid + ":" + sentenceNum++; // sentence id
						out.println(sid + FS + sentence.getCoveredText() + FS + linesplit[2]);
					}		
					totalLinesNum++;
					if (totalLinesNum % sentencesNumPerLog == 0) {
						Date now = new Date();
						System.out.printf("Processed %d lines from file: %s... (%d total milliseconds elapsed) \n", 
								totalLinesNum, wikiFilePath, now.getTime() - start.getTime());
					}
				}				
				Date end = new Date();
				System.out.println(end.getTime() - start.getTime() + " total milliseconds elapsed");
			} catch (UIMAException e) { 
				System.err.println("Error while processing line num. " + totalLinesNum + " ... ");
				System.err.println(line);
				System.err.println(totalLinesNum + 1);
			} finally {
				out.close();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UIMAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
