package NLP_PIPELINE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Nlp_Pipeline
{
	static StanfordCoreNLP pipeline;

	public static void init() 
	{
		pipeline = new StanfordCoreNLP("/home/bigtapp/workspace/Streaming_Data/src/main/resources/NLP.properties");
	}

	public static ArrayList<String> findlocations(String text) throws Exception
	{
		GeoParser parser = GeoParserFactory.getDefault("/home/bigtapp/Desktop/IndexDirectory");
		List<ResolvedLocation> resolvedLocations = parser.parse(text);
		ArrayList<String> identified_locations = new ArrayList<String>();
		for (ResolvedLocation resolvedLocation : resolvedLocations)
		{

			identified_locations.add(resolvedLocation.getMatchedName()+" resolved as :"+resolvedLocation.getGeoname().toString());
		}
		return identified_locations;
	}
	public static List<String> facebookusermentionsextract(String str) throws IOException
	{
		String patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(str);
		String result = "";
		List<String> usertags=new ArrayList<String>();
		while (matcher.find()) 
		{
			result = matcher.group();
			usertags.add((matcher.group()).replace("@", "https://www.facebook.com/"));
		}
		return usertags;
	}
	public static ArrayList<String> Email_Extract(String text) 
	{
		Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(text);
		ArrayList<String> results = new ArrayList<String>();
		while (m.find()) 
		{
			results.add(m.group().toString());
		}
		return results;
	}
	public static ArrayList<String> findPhNum(String string2) 
	{
		String line = string2.toLowerCase();
		line = line.replaceAll("[/?.^:,\"\']"," ");
		line = line.replaceAll(" ","  ");
		line = line.concat(" ");
		String regex = "(^|\\s)(-*\\s*\\(?-*\\s*(\\+\\(?|00)?65-*\\s*\\)?-*\\s*)?-*\\s*[986]-*\\s*\\d-*\\s*\\d-*\\s*\\d-*\\s*\\d-*\\s*\\d-*\\s*\\d-*\\s*\\d\\s";
		Set<String> set = new HashSet<String>();
		Matcher m = Pattern.compile(regex).matcher(line);  	
		while (m.find()) 
		{
			set.add(m.group());
		}	
		ArrayList<String> results = new ArrayList<String>();
		Iterator iter = set.iterator();
		while (iter.hasNext()) 
		{
			String newString = ((String) iter.next()).trim();
			results.add(newString);
		}
		
		return results;
	}
	public static String findLemmas(String input) 
	{

		String lemma=null;
		Annotation document = pipeline.process(input);  

		for(CoreMap sentence: document.get(SentencesAnnotation.class))
		{    
			for(CoreLabel token: sentence.get(TokensAnnotation.class))
			{       
				String word = token.get(TextAnnotation.class);      
				lemma = token.get(LemmaAnnotation.class); 
				//System.out.println("lemmatized version :" + lemma);
			}
		}
		return lemma;
	}
	public static List<String> hashtagextract(String str) throws IOException
	{
		Pattern MY_PATTERN = Pattern.compile("#(\\w+|\\W+)");
		Matcher mat = MY_PATTERN.matcher(str);
		List<String> hashtags=new ArrayList<String>();
		while (mat.find()) 
		{
			hashtags.add(mat.group(1));
		}
		return hashtags;
	}
	public static String findSentiment(String text) {

		int mainSentiment = 0;
		if (text != null && text.length() > 0) 
		{
			int longest = 0;
			Annotation annotation = pipeline.process(text);
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class))
			{
				Tree tree = sentence
						.get(SentimentCoreAnnotations.AnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
				String partText = sentence.toString();
				if (partText.length() > longest) 
				{
					mainSentiment = sentiment;
					longest = partText.length();
				}

			}
		}
		String sentiout = "neutral";
		if (mainSentiment==0)
		{
			sentiout= "Very Negative";
		}
		else if (mainSentiment==1)
		{
			sentiout="Negative";
		}else if (mainSentiment==2)
		{
			sentiout="Neutral";
		}
		else if (mainSentiment==3)
		{
			sentiout="Positive";
		}
		else if (mainSentiment==4)
		{
			sentiout="Very Positive";
		}		    

		return sentiout;
	}

	public static Map<String, Temporal> ExtractDate(String text){
		Map<String, Temporal> hm = new HashMap<String, Temporal>();
		Properties props = new Properties();
		AnnotationPipeline pipeline = new AnnotationPipeline();
		pipeline.addAnnotator(new TokenizerAnnotator(false));
		pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
		pipeline.addAnnotator(new POSTaggerAnnotator(false));
		pipeline.addAnnotator(new TimeAnnotator("sutime", props));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");		    
		String formatted = format1.format(cal.getTime());
		Annotation annotation = new Annotation(text);
		annotation.set(CoreAnnotations.DocDateAnnotation.class, formatted);
		pipeline.annotate(annotation);
		//System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));	
		List<CoreMap>timexAnnsAll=annotation.get(TimeAnnotations.TimexAnnotations.class);	
		for (CoreMap cm : timexAnnsAll) 
		{
			List tokens = cm.get(CoreAnnotations.TokensAnnotation.class); 	         
			/* System.out.println(cm + " [from char offset " +
		        				 ((CoreMap) tokens.get(0)).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
		        				 " to " + ((CoreMap) tokens.get(tokens.size() - 1)).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
		        				 " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());*/

			hm.put(cm.toString(), cm.get(TimeExpression.Annotation.class).getTemporal());
		}

		return hm;

	}

	

}
