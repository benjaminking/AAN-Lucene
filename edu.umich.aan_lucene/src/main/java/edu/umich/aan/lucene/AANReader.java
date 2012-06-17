package edu.umich.aan.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AANReader
{
	String aanMetadataFileName;
	String aanAbstractDirName;
	String aanFullTextDirName;
	String aanCitationDirName;
	
	Pattern idLinePattern, authorLinePattern, titleLinePattern, venueLinePattern, yearLinePattern;
	
	List<AANMetadata> aanMetadata = new ArrayList<AANMetadata>();
	
	public AANReader(String aanMetadataFileName, String aanAbstractDirName, String aanFullTextDirName, String aanCitationDirName) throws IOException
	{
		this.aanMetadataFileName = aanMetadataFileName;
		this.aanAbstractDirName = aanAbstractDirName;
		this.aanFullTextDirName = aanFullTextDirName;
		this.aanCitationDirName = aanCitationDirName;
		
		idLinePattern = Pattern.compile("^id\\s*=\\s*\\{(.*)\\}");
		authorLinePattern = Pattern.compile("^author\\s*=\\s*\\{(.*)\\}");
		titleLinePattern = Pattern.compile("^title\\s*=\\s*\\{(.*)\\}");
		venueLinePattern = Pattern.compile("^venue\\s*=\\s*\\{(.*)\\}");
		yearLinePattern = Pattern.compile("^year\\s*=\\s*\\{(.*)\\}");
		
		readMetadata();
	}

	private void readMetadata() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(aanMetadataFileName));
		AANMetadata currentMetadata = new AANMetadata();
		
		String line;
		while((line = reader.readLine()) != null)
		{
			Matcher m;
			if(line.matches("^\\s*$"))
			{
				aanMetadata.add(currentMetadata);
				currentMetadata = new AANMetadata();
			}
			if((m = idLinePattern.matcher(line)).matches())
			{
				currentMetadata.id = m.group(1);
				continue;
			}
			if((m = authorLinePattern.matcher(line)).matches())
			{
				currentMetadata.author = m.group(1);
				continue;
			}
			if((m = titleLinePattern.matcher(line)).matches())
			{
				currentMetadata.title = m.group(1);
				continue;
			}
			if((m = venueLinePattern.matcher(line)).matches())
			{
				currentMetadata.venue = m.group(1);
				continue;
			}
			if((m = yearLinePattern.matcher(line)).matches())
			{
				currentMetadata.year = m.group(1);
				continue;
			}
		}
	}
	
	public List<AANMetadata> getMetadata()
	{
		return aanMetadata;
	}
	
	public String readAbstract(AANMetadata aanMetadata)
	{
		File abstractFile = new File(aanAbstractDirName + File.separator + aanMetadata.id + ".txt");
		if(abstractFile.exists())
		{
			try
			{
				FileInputStream stream = new FileInputStream(abstractFile);
				try {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					/* Instead of using default, pass in a decoder. */
					return Charset.defaultCharset().decode(bb).toString();
				}
				finally {
					stream.close();
				}
			}
			catch(IOException ioe)
			{
				return "";
			}

		}
		else
		{
			return "";
		}
	}
	
	public String readFullText(AANMetadata aanMetadata)
	{
		File fullTextFile = new File(aanFullTextDirName + File.separator + aanMetadata.id + ".txt");
		if(fullTextFile.exists())
		{
			try
			{
				FileInputStream stream = new FileInputStream(fullTextFile);
				try {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					/* Instead of using default, pass in a decoder. */
					return Charset.defaultCharset().decode(bb).toString();
				}
				finally {
					stream.close();
				}
			}
			catch(IOException ioe)
			{
				return "";
			}

		}
		else
		{
			return "";
		}
	}
	
	public String readCitations(AANMetadata aanMetadata)
	{
		File citationFile = new File(aanCitationDirName + File.separator + aanMetadata.id + ".CS");
		if(citationFile.exists())
		{
			try
			{
				FileInputStream stream = new FileInputStream(citationFile);
				try {
					FileChannel fc = stream.getChannel();
					MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
					String result = Charset.defaultCharset().decode(bb).toString();
					
					result = result.replaceAll("<\\.*?>", "");
					result = result.replaceAll("\\-{2,}", "");
					return result;
				}
				finally {
					stream.close();
				}
			}
			catch(IOException ioe)
			{
				return "";
			}
		}
		else
		{
			return "";
		}
	}
}
