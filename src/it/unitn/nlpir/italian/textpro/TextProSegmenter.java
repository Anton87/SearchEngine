package it.unitn.nlpir.italian.textpro;

import it.unitn.limosine.util.SharedModel;
import it.unitn.limosine.util.StreamGobbler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TextProSegmenter extends JCasAnnotator_ImplBase {
	
 	private String txpcommand="textpro.pl"; 	
 	
 	public static final String PARAM_VERBOSE = "verbose";
 	public static final String PARAM_LANGUAGE = "lang";
 	
 	private static final String FIELD_SEPARATOR = "\t";
 	
 	//@ConfigurationParameter(name=PARAM_VERBOSE, defaultValue="false", description="verbose")
 	@ConfigurationParameter(name=PARAM_VERBOSE, defaultValue="false", description="verbose")
 	public boolean verbose;
 	
 	@ConfigurationParameter(name=PARAM_LANGUAGE, defaultValue="ita", description="verbose")
 	public String lang;
 	
 	
 	//ToDo: change with a proper textpro command (using resources and environment variables)
 	private String[] txpparams={"-l", "ita", "-c", "token+tokenstart+tokenend+sentence"};
 	private String txppath;
    String txpencoding = "ISO-8859-1"; // "Cp1252" = Windows Latin 1
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);

		try {

			SharedModel sharedModel = (SharedModel) getContext().getResourceObject("RunTextPro");

			getContext().getLogger().log(Level.INFO,
					"Launching TextPro...");

			txppath = sharedModel.getPath();

			getContext().getLogger().log(Level.INFO, txppath);

			verbose = (boolean) getContext().getConfigParameterValue(PARAM_VERBOSE);
			
			lang = (String) getContext().getConfigParameterValue(PARAM_LANGUAGE);
			System.out.println("lang:" + lang);
			if (!lang.equals("eng") && !lang.equals("ita")) { 
				throw new ResourceInitializationException(
						new Throwable("WARNING! Wrong language parameter. " + 
									  "The value can be 'ita','eng',... "));
			}			
		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}
 	
 	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		
 		//prepare textpro input	
 		String doctxt_utf=jcas.getDocumentText();

 		//run textpro

 		if (doctxt_utf.length()==0) return;
 		
		try {
			String fulltxpcommand = txppath + "/" + txpcommand; 
			List<String> fulltxpcmdline=new ArrayList<String>();
			fulltxpcmdline.add(fulltxpcommand);
			fulltxpcmdline.addAll(Arrays.asList(txpparams));
			ProcessBuilder processBuilder = new ProcessBuilder(fulltxpcmdline);
			
			Map<String, String> env = processBuilder.environment();
			env.put("TEXTPRO", txppath);

			Process process = processBuilder.start();
		
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()), txpencoding);
			PrintWriter writer = new PrintWriter(outputStreamWriter, true);
			
			writer.println(doctxt_utf);
		
			writer.println();
			writer.flush();
			writer.close();        
        
			StreamGobbler outGobbler = new StreamGobbler(process.getInputStream());
			StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream());
			Thread outThread = new Thread(outGobbler);
			Thread errThread = new Thread(errGobbler);
			outThread.start();
			errThread.start();

			outThread.join();
			errThread.join();

			process.waitFor();

        
			for(String out : outGobbler.getOuput()) 
				if (verbose) System.err.println(out.trim());
        
			List<String> output = outGobbler.getOuput();
			List<String> outputErr = errGobbler.getOuput();
        
			for(String out : outputErr) 
				if (verbose) System.err.println(out.trim());
        
			if (verbose) System.err.println("RETURNED:");
			for(String out : output) 
				if (verbose) System.err.println(out.trim());
        
			//parse textpro output
        
			int tokenCount=0;
			int sentenceCount = 0;
			StringBuilder tokenizedSentence = new StringBuilder();
			int tokenSentCount = 0; //sentence-level counter for tokens
			Sentence sentence = new Sentence(jcas);
			
			int lastTokenend = 0;			
     
			for(String out : output) {
				if (verbose) System.out.println("TEXTPRO OUTPUT: " +out);
				String[] txpvals = out.trim().split(FIELD_SEPARATOR);

				// TOKEN
				if (txpvals.length < 4) {
					throw new AnalysisEngineProcessException(
							new IOException("Wrong fields number. Expected 4, got " + txpvals.length));
				}
					
				Token token = new Token(jcas);
				
				int tokenstart = Integer.parseInt(txpvals[1]);
				int tokenend = Integer.parseInt(txpvals[2]);
				
				/** 
				 * TOKENIZATION BUGS: TextPro failed to assign the right token spans
				 */
				if (tokenstart == -1) {
					int j = 0;
					int i = lastTokenend;
					
					for ( ; j < txpvals[0].length(); i++) { 
						char ch = doctxt_utf.charAt(i);
						//System.out.printf("i: %d, ch(i): '%s', j: %d\n", i, ch, j);
						if (ch == txpvals[0].charAt(j)) {
							if (tokenstart == -1) {
								tokenstart = i;
							}
							j++;
						}
					}
					tokenend = i;					
				}
				
				lastTokenend = tokenend;
				
				token.setBegin(tokenstart);
				token.setEnd(tokenend);
				token.addToIndexes();

				tokenCount++;
			
				// SENTENCE			
				if (tokenSentCount==0) {
					sentence = new Sentence(jcas);
					//sentence.setSentenceId(sentenceCount);
					//sentence.setStartToken(token);
					sentence.setBegin(token.getBegin());
					//sentence.setAnnotatorId(getClass().getCanonicalName()+":sentence");
					sentenceCount++;
					tokenizedSentence.setLength(0);
				}
				//tokenizedSentence.append(token.getNormalizedText() + " ");
				tokenSentCount++;
			
				if (txpvals[3].equals("<eos>")) {
					sentence.setEnd(token.getEnd());
					sentence.addToIndexes();

					tokenSentCount = 0; //sentence-level counter for tokens
				}
			}
			       
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
		
}
