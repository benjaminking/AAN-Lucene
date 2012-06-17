package edu.umich.aan.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class PatentIndexer
{
	File textDir;
	File indexDir;
	IndexWriter w;
	Analyzer analyzer;
	
	class MetaData
	{
		public int patentNum;
		public String inventors;
		public String publicationDate;
		public String filingDate;
		public String assignee;
		public String appNumber;
	}
	
	Map<Integer, MetaData> metadataById = new HashMap<Integer, MetaData>();
	
	int docCounter;

	public PatentIndexer(File textDir, File indexDir, File metadataFile) throws IOException
	{
		this.textDir = textDir;
		this.indexDir = indexDir;
		
		readMetadata(metadataFile);
	}
	
	void readMetadata(File metadataFile) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
		String line;
		while((line = reader.readLine()) != null)
		{
			MetaData m = new MetaData();
			String[] tokens = line.split("\\s:::\\s");
			try
			{
				m.patentNum = Integer.parseInt(tokens[0]);
				m.inventors = tokens[1];
				m.publicationDate = tokens[2];
				m.filingDate = tokens[3];
				m.assignee = tokens[4];
				m.appNumber = tokens[5];
				
				metadataById.put(m.patentNum, m);
			}
			catch(NumberFormatException nfe)
			{ }
		}
	}
	
	public void index() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		docCounter = 0;
		analyzer = new EnglishAnalyzer(Version.LUCENE_34);
		
		Directory index = new NIOFSDirectory(indexDir);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		w = new IndexWriter(index, config);
	    
	    for(File doc : textDir.listFiles())
	    {
	    	indexDoc(doc);
	    }
	    
	    w.close();
	}
	
	public void indexDoc(File doc) throws CorruptIndexException, IOException
	{
		Document d = new Document();
		
		BufferedReader reader = new BufferedReader(new FileReader(doc));
		String docText = "";
		String line;
		while((line = reader.readLine()) != null)
		{
			docText += line + "\n";
			System.out.println(docText);
		}
		reader.close();
		
		
		System.out.println("Indexing " + docCounter + " " + doc.getName());
		d.add(new Field("id", doc.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		d.add(new Field("text", docText, Field.Store.YES, Field.Index.ANALYZED));
		
		docCounter++;
		
		w.addDocument(d, analyzer);
	}
}
