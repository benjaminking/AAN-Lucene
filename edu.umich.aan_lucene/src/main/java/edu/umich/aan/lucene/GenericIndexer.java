package edu.umich.aan.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

public class GenericIndexer
{
	File textDir;
	File indexDir;
	IndexWriter w;
	Analyzer analyzer;
	
	int docCounter;

	public GenericIndexer(File textDir, File indexDir)
	{
		this.textDir = textDir;
		this.indexDir = indexDir;
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
	
	public static void main(String[] args) throws IOException
	{
		File textDir = new File(args[0]);
		File indexDir = new File(args[1]);
		
		GenericIndexer indexer = new GenericIndexer(textDir, indexDir);
		indexer.index();
	}
}
