package edu.umich.aan.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class GenericSearcher
{
	IndexReader reader;
	IndexSearcher searcher;
	
	Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_34);
	
	public GenericSearcher(String indexDirName) throws CorruptIndexException, IOException
	{
		reader = IndexReader.open(new NIOFSDirectory(new File(indexDirName)));
		searcher = new IndexSearcher(reader);
	}

	protected TopDocs search(int numResults, String query) throws ParseException, IOException
	{
		Query q = new QueryParser(Version.LUCENE_35, "text", analyzer).parse(query);
		return searcher.search(q, numResults);
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException
	{
		if(args.length != 3)
		{
			System.out.println("Usage: ");
			System.out.println("GenericSearcher [index dir name] [num results] [query]");
			System.exit(1);
		}
		
		String indexDirName = args[0];
		int numResults = Integer.parseInt(args[1]);
		String query = args[2];
		
		IndexReader reader;
		reader = IndexReader.open(new NIOFSDirectory(new File(indexDirName)));
		
		GenericSearcher searcher = new GenericSearcher(indexDirName); 	
		
		for(ScoreDoc sd : searcher.search(numResults, query).scoreDocs)
		{
			System.out.println(reader.document(sd.doc).getFieldable("id").stringValue());
		}
	}
}
