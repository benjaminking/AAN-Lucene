package edu.umich.aan.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.TopDocs;

public class PRFAANSearcher extends AANMultiFieldSearcher
{
	public PRFAANSearcher(String indexDirName) throws CorruptIndexException, IOException
	{
		super(indexDirName);
	}
	
	public Map<String, Float> search(int numHits, Map<String, Float> weightedFeatures) throws CorruptIndexException, IOException, ParseException
	{
		String query = "";
		for(Entry<String, Float> entry : weightedFeatures.entrySet())
		{
			query += "\"" + entry.getKey() + "\"^" + entry.getValue().toString() + " ";
		}
		
		List<TopDocs> topDocs = new ArrayList<TopDocs>();
		
		topDocs.add(searchField_("title", numHits, query));
		topDocs.add(searchField_("abstract", numHits, query));
		topDocs.add(searchField_("fulltext", numHits, query));
		
		return fuseQueryResults(numHits, topDocs);
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException
	{
		String indexDirName = args[0];
		int numHits = Integer.parseInt(args[1]);
		String featureNameFile = args[2];
		
		Map<String, Float> terms = new HashMap<String, Float>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(featureNameFile)));
		String line;
		int termCutoff = 50;
		while((line = reader.readLine()) != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(line);
			String term = tokenizer.nextToken("\t");
			Float weight = Float.parseFloat(tokenizer.nextToken("\t"));
			terms.put(term, weight);
			if(terms.size() >= termCutoff)
				break;
		}
		reader.close();
		
		PRFAANSearcher aanSearcher = new PRFAANSearcher(indexDirName);
		Map<String, Float> results = aanSearcher.search(numHits, terms);	
		
		float cutoff = 0.005f;
		
		for(String id : results.keySet())
		{
			if(results.get(id) > cutoff)
			{
				System.out.println(id);
			}
		}
	}
}
