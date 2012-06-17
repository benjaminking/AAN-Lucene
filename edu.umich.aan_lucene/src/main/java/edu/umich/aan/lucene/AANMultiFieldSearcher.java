package edu.umich.aan.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public abstract class AANMultiFieldSearcher extends AANSearchUI
{	
	public AANMultiFieldSearcher(String indexDirName) throws CorruptIndexException, IOException
	{
		super(indexDirName);
		// TODO Auto-generated constructor stub
	}

	public Map<String, Float> searchFields(List<String> fields, int numResults, String query) throws CorruptIndexException, IOException, ParseException
	{
		List<TopDocs> results = new ArrayList<TopDocs>();
		for(String field : fields)
		{
			results.add(searchField(field, numResults, query));
		}
		
		return fuseQueryResults(numResults, results);
	}
	
	protected Map<String, Float> fuseQueryResults(int numResults, List<TopDocs> results) throws CorruptIndexException, IOException
	{
		Map<String, MutableFloat> sumOfScores = new HashMap<String, MutableFloat>();
		Map<String, MutableInt> hits = new HashMap<String, MutableInt>();
		
		for(TopDocs td : results)
		{
			for(ScoreDoc doc : td.scoreDocs)
			{
				String id = reader.document(doc.doc).getFieldable("id").stringValue();
				if(sumOfScores.containsKey(id))
					sumOfScores.get(id).add(doc.score);
				else
					sumOfScores.put(id, new MutableFloat(doc.score));
				
				if(hits.containsKey(id))
					hits.get(id).increment();
				else
					hits.put(id, new MutableInt(1));
			}
		}
		
		MapValueComparator comp = new MapValueComparator(sumOfScores);
		TreeMap<String, Float> fusedResults = new TreeMap<String ,Float>(comp);
		for(String id : sumOfScores.keySet())
		{
			fusedResults.put(id, sumOfScores.get(id).floatValue() / hits.get(id).intValue());
		}
		
		Map<String, Float> finalResults = new HashMap<String, Float>();
		int counter = 0;
		for(String id : fusedResults.keySet())
		{
			counter++;
			if(counter <= numResults)
				finalResults.put(id, fusedResults.get(id));
			else
				break;
		}
		
		return finalResults;
	}
	
	private class MapValueComparator implements Comparator<String>
	{
		Map<String, MutableFloat> base;
		public MapValueComparator(Map<String, MutableFloat> base)
		{
			this.base = base;
		}

		public int compare(String o1, String o2)
		{
			int temp = base.get(o2).compareTo(base.get(o1));
			if(temp == 0)
				return o2.compareTo(o1);
			return temp;
		}
		
	}

	protected TopDocs searchField_(String field, int numResults, String query) throws ParseException, IOException
	{
		Query q = new QueryParser(Version.LUCENE_35, field, analyzer).parse(query);
		return searcher.search(q, numResults);
		
		/*String[] tokens = query.split("\\s+");
		SpanTermQuery[] termQueries = new SpanTermQuery[tokens.length];
		int count = 0;
		for(String token : tokens)
		{
			termQueries[count++] = new SpanTermQuery(new Term(field, token));
		}
		SpanNearQuery spanNear = new SpanNearQuery(termQueries, 5, true);
		return searcher.search(spanNear, 2000);*/
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException
	{
		String indexDirName = args[0];
		int numHits = Integer.parseInt(args[1]);
		String query = args[2];
		
		IndexReader reader = IndexReader.open(new NIOFSDirectory(new File(indexDirName)));
		
		float cutoff = 0.0f;
		
		/*AANMultiFieldSearcher aanSearcher = new AANMultiFieldSearcher(indexDirName);
		Map<String, Float> results = aanSearcher.searchFields(numHits, query);	
		
		for(String id : results.keySet())
		{
			if(results.get(id) > cutoff)
			{
				System.out.println(id);
			}
		}*/
	}
}
