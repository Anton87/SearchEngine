package it.unitn.nlpir.itwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

public class OldItwikiIndexer {
	
	public static void main(String[] args) {
		
		String itwikiFilepath = "../itwiki-20140127-pages-articles-multistream-paragraphs-with-lists-no-images.txt";
		//String itwikiFilepath = "../itwiki-fragment.txt";
		
		String itwikiIndexDirpath = "index";
		
		System.out.println("Does itwiki corpus exist? " + new File(itwikiFilepath).exists());		
		System.out.println("Does the itwiki index dir exists? " + new File(itwikiIndexDirpath).exists());
		
		try {
			File indexDir = new File(itwikiIndexDirpath);
			Directory index = FSDirectory.open(indexDir);
			ItalianAnalyzer analyzer = new ItalianAnalyzer(Version.LUCENE_46);
			//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
			IndexWriter writer = new IndexWriter(index, iwc);
			
			iwc.setOpenMode(OpenMode.CREATE);
			
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
		
	}

}
