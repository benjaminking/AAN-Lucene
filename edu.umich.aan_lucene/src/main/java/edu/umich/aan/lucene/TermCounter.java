package edu.umich.aan.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

public class TermCounter
{
	IndexReader reader;
	IndexSearcher searcher;
	
	Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_34);
	
	public TermCounter(String indexDirName) throws CorruptIndexException, IOException
	{
		reader = IndexReader.open(new NIOFSDirectory(new File(indexDirName)));
		searcher = new IndexSearcher(reader);
	}

	protected TopDocs getHits(String query) throws ParseException, IOException
	{
		Query q = new QueryParser(Version.LUCENE_35, "fulltext", analyzer).parse(query);
		return searcher.search(q, 10);
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException
	{		
		String indexDirName = args[0];
		String termsFile = args[1];
		
		List<String> terms = new ArrayList<String>();
		BufferedReader termReader = new BufferedReader(new InputStreamReader(new FileInputStream(termsFile), "UTF-8"));
		String line;
		while((line = termReader.readLine()) != null)
		{
			terms.add(line);
		}
		
		TermCounter aanSearcher = new TermCounter(indexDirName); 	
		IndexReader reader;
		reader = IndexReader.open(new NIOFSDirectory(new File(indexDirName)));
		
		for(String term : terms)
		{
			ScoreDoc[] sds = aanSearcher.getHits("\"" + term + "\"").scoreDocs;
			if(sds.length < 5)
			{
				if(sds.length > 0)
				{
					System.out.print(term + "\t");
					for(int i=0; i<sds.length; ++i)
						System.out.print("~/research/fuse/aan/aan_lemmatized/" + reader.document(sds[i].doc).getFieldable("id").stringValue() + ".txt ");
					System.out.println();
				}
			}
		}
	}
}
