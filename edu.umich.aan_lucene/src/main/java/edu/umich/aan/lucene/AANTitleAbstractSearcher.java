package edu.umich.aan.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.TopDocs;

public class AANTitleAbstractSearcher extends AANMultiFieldSearcher
{
	List<String> fields = new ArrayList<String>();

	public AANTitleAbstractSearcher(String indexDirName) throws CorruptIndexException, IOException
	{
		super(indexDirName);
		fields.add("abstract");
		fields.add("title");
	}
	
	public Map<String, Float> search(int numResults, String query) throws CorruptIndexException, IOException, ParseException
	{
		return super.searchFields(fields, numResults, query);
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException
	{
		String indexDirName = args[0];
		int numHits = Integer.parseInt(args[1]);
		String query = args[2];
		
		AANTitleAbstractSearcher aanSearcher = new AANTitleAbstractSearcher(indexDirName);
		
		for(String id : aanSearcher.search(numHits, query).keySet())
		{
			System.out.println(id);
		}
	}

}
