package edu.umich.aan.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class AANIndexer
{
	AANReader aanReader;
	String outputIndexDir;
	IndexWriter w;
	Analyzer analyzer;
	int docCounter;
	
	public AANIndexer(AANReader aanReader, String outputIndexDir)
	{
		this.aanReader = aanReader;
		this.outputIndexDir = outputIndexDir;
	}
	
	public void index() throws IOException
	{
		//analyzer = new StandardAnalyzer(Version.LUCENE_34);
		//analyzer = new StopAnalyzer(Version.LUCENE_34);
		analyzer = new EnglishAnalyzer(Version.LUCENE_34);
		
		Directory index = new NIOFSDirectory(new File(outputIndexDir));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		w = new IndexWriter(index, config);
	    
		docCounter = 0;
	    List<AANMetadata> aanMetadata = aanReader.getMetadata();
	    for(AANMetadata a : aanMetadata)
	    {
	    	indexDoc(a);
	    }
	    
	    w.close();
	}
	
	
	private void indexDoc(AANMetadata a) throws CorruptIndexException, IOException
	{
		Document doc = new Document();
		System.out.println("Indexing " + ++docCounter + ": " + a.id);
		doc.add(new Field("id", a.id, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("author", a.author, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("title", a.title, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("venue", a.venue, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("year", a.year, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("abstract", aanReader.readAbstract(a), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("fulltext", aanReader.readFullText(a), Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("citation", aanReader.readCitations(a), Field.Store.YES, Field.Index.ANALYZED));
		
		w.addDocument(doc, analyzer);
	}

	public static void main(String[] args) throws IOException
	{
		String aanMetadataFileName = args[0];
		String aanAbstractDirName = args[1];
		String aanFullTextDirName = args[2];
		String aanCitationDirName = args[3];
		String outputIndexDir = args[4];
		
		AANReader aanInfo = new AANReader(aanMetadataFileName, aanAbstractDirName, aanFullTextDirName, aanCitationDirName);
		
		AANIndexer aanIndexer = new AANIndexer(aanInfo, outputIndexDir);
		aanIndexer.index();
	}

}
