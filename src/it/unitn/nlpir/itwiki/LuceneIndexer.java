package it.unitn.nlpir.itwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndexer {
	
	public static void main(String[] args) {
		
		String usage = "java it.unitn.nlpir.itwiki.LuceneIndexer"
					 + " [-index INDEX_PATH] [-doc DOC_PATH]\n\n"
					 + "This indexes the documents in DOC_PATH creating a Lucene index"
					 + "in INDEX_PATH that can be searched with LuceneRetriever";
		
		String indexPath = "index";
		String docPath = null;
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-doc".equals(args[i])) {
				docPath = args[i + 1];
				i++;
			}
		}
		
		if (docPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(-1);
		}
		
		final File docFile = new File(docPath);
		if (!docFile.isFile() || !docFile.canRead()) {
			System.out.println("File '" + docFile.getAbsolutePath() + "' does not exist or is not redable, please check the path");
			System.exit(-1);
		}
		
		Date start = new Date();
		
		
		//String itwikiFilepath = "../itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.txt";
		//String itwikiFilepath = "../itwiki-fragment.txt";
		
		//String itwikiIndexDirpath = "index";
		
		///System.out.println("Does itwiki corpus exist? " + new File(itwikiFilepath).exists());		
		//System.out.println("Does the itwiki index dir exists? " + new File(itwikiIndexDirpath).exists());
		
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			
			Directory indexDir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new ItalianAnalyzer(Version.LUCENE_46);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			
			
			//File indexDir = new File(itwikiIndexDirpath);
			//Directory index = FSDirectory.open(indexDir);
			//ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_46);
			//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			//IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			
			// Create a new index in the directory, removing any
			// previously indexed documents
						
			iwc.setOpenMode(OpenMode.CREATE);			
			
			// Optional: for better indexing performance,
			// we increase the RAM buffer.
			iwc.setRAMBufferSizeMB(256.0);
			
			IndexWriter writer = new IndexWriter(indexDir, iwc);
			indexDoc(writer, docFile);
			
			writer.close();
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage() );
		}
	}
			
	/**
	 * Indexes the given file using the given writer.
	 * 
	 * @param writer
	 * @param docFile
	 */
	private static void indexDoc(IndexWriter writer, File docFile) throws IOException  {
		assert writer != null;
		assert docFile != null;
		
		try (BufferedReader br = 
				new BufferedReader(
						new InputStreamReader(
								new FileInputStream(docFile), "UTF-8"))) {
			int  lineNum = 0;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String[] linesplit = line.split("\t");
				
				if (linesplit.length != 3) {
					throw new IOException("Format error at line num: " + lineNum);
				}
				String docId = linesplit[0];
				String text = linesplit[1];
				
				// make a new empty document
				Document doc = new Document();
				
				Field docIdField = new StringField("docId", docId, Field.Store.YES);
				doc.add(docIdField);
				
				Field textField = new TextField("text", text, Field.Store.YES);
				doc.add(textField);
				
				writer.updateDocument(new Term("docId", docId), doc);
				
				lineNum++;
			}
		}
		/*
		
		 
		
	

			
			BufferedReader br = 
					new  BufferedReader(
							new InputStreamReader(
									new FileInputStream(itwikiFilepath), "UTF-8"));
			
			String docId = null;
			String text = null;
			int lineNum = 0;
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				
				String[] lineSplit = line.split("\t");
				
				if (lineSplit.length != 3) {
					System.out.printf("line num %d does not respect format: <docId> <tab> <text> <tab> <SKIP_STUFF>\n", lineNum++);
					continue;
				}
				
				docId = lineSplit[0];
				text = lineSplit[1];
				
				// make a new, empty document
				Document doc = new Document();
				
				Field docIdField = new StringField("docId", docId, Field.Store.YES);
				doc.add(docIdField);
				
				Field textField = new TextField("text", text, Field.Store.YES);
				doc.add(textField);
				
				//writer.addDocument(doc);
				writer.updateDocument(new Term("docId", docId), doc);		
				
				lineNum++;
			}			
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		*/
	}

}
