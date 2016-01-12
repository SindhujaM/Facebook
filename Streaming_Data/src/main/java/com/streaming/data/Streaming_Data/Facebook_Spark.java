package com.streaming.data.Streaming_Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;

import NLP_PIPELINE.Nlp_Pipeline;
import edu.stanford.nlp.time.SUTime.Temporal;

public class Facebook_Spark
{
	public static class Facebook implements Serializable
	{
		
		private String message;
		private String id;
		
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) 
		{
			this.message = message;
		}
		public String getId() {
			return id;
		}
		public void setId(String string) 
		{
			this.id = string;
		}
	}
	
	
	
	public static void main(String args[]) throws IOException
	{
		

		 SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("Simple Application");
		 JavaSparkContext jsc = new JavaSparkContext(conf);
		 SQLContext sqlContext = new org.apache.spark.sql.SQLContext(jsc);
		 System.out.println(sqlContext);
		
		 final Properties properties = new Properties();
			{
			try {
				properties.load(new FileInputStream("src/main/resources/Facebook.properties"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			
		 	String base_url=properties.getProperty("base_url");
			String like_page_id=properties.getProperty("like_page_id");
			String feed_limit=properties.getProperty("feed_limit");
			String access_token=properties.getProperty("access_token");
			String get_url=base_url+""+like_page_id+"?fields=feed.limit("+feed_limit+")&access_token="+access_token;
			
			// displaying Final URL
			System.out.println(get_url);
			File file =new File("input.json");
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(getsource(get_url));
			bw.close();
			System.out.print(getsource(get_url));
			
		 
		 System.out.println("=== Data source: JSON Dataset ===");
		 String path = "/home/bigtapp/workspace/Streaming_Data/input.json";
		 
		 //DataFrme 
		 
		 DataFrame df = sqlContext.read().json(path).toDF();
		 df.printSchema();
		 System.out.println(df);
		 df.registerTempTable("facebook");
		 df.save("jsonDataFacebok");
		 System.out.println(df);

	        DataFrame appName1 =df.select("feed").toDF();
	        appName1.save("/home/bigtapp/workspace/Streaming_Data/feed");
	        appName1.printSchema();
	      
	        
	        DataFrame appName2 =appName1.select("feed.data").toDF();
	        System.out.println(appName2);
	        appName2.save("/home/bigtapp/workspace/Streaming_Data/feed_data");
	        appName2.printSchema();
	        
	        

	        DataFrame appName3 = appName2.select("data.message").toDF();
	        appName2.save("/home/bigtapp/workspace/Streaming_Data/message");
	        appName2.printSchema();
	       
	        
		 DataFrame df1 = appName3.sqlContext().sql("SELECT feed FROM facebook");
		 System.out.println(df1);
		
		  List<String> fbMessage =(List<String>) df1.toJavaRDD().map(new Function<Row, String>()
				 {
					public String call(Row r) throws Exception 
					{
						// TODO Auto-generated method stub	
					//System.out.println(	"r data values"+r.getAs(0).toString());
					
					System.out.println("#######################################  NLP STARTS  ####################################");
					GeoParser parser = GeoParserFactory.getDefault(properties.getProperty("GeoParser"));

					List<ResolvedLocation> resolvedLocations = parser.parse(r.getAs(0).toString());
					ArrayList<String> identified_locations = new ArrayList<String>();
					for (ResolvedLocation resolvedLocation : resolvedLocations)
					{
						identified_locations.add(resolvedLocation.getMatchedName()+" resolved as :"+resolvedLocation.getGeoname().toString());
					}

		
					String clavin=identified_locations.toString();
					System.out.println("******************* GEOTAG INFO ************************"); 
					System.out.println(clavin);	
				

			/*		System.out.println("**************  SENTIMENT EXTRACTION IN PROGRESS  ***************");

					String sentiment=Nlp_Pipeline.findSentiment(r.toString());
					System.out.println("***********  Sentiment ********************" +sentiment);*/
					
					System.out.println("***************************** DATE EXTRACTION IN PROGRESS  ******************************");
					Map<String, Temporal> results1 = Nlp_Pipeline.ExtractDate(r.getAs(0).toString());
					
					System.out.println("******************************* EXTRACTED DATE  **************************************");
					System.out.println(results1);
					
					ArrayList<String> phnos = new ArrayList<String>();

					for (String key : results1.keySet())
					{
						phnos.add(results1.get(key).toString());

					}

					System.out.println("*********************************  PHONE NUMBER EXTRACTION IN PROGRESS  **************************");
					
					String phno=phnos.toString();
					System.out.println("********************************* EXTRACTED PHONE NUMBER **********************************");
					System.out.println(phnos);
					
					System.out.println("********************************  EMAIL EXTRACTION STARTS  ****************************");
					ArrayList<String> emailExtract=Nlp_Pipeline.Email_Extract(""+r.getAs(0));
					System.out.println("******************************  EXTRACTED EMAIL ***********************");
					System.out.println(emailExtract);

					
						return null;
					}
			 
				 }).collect();
		 
			jsc.startTime();
			jsc.stop();
			jsc.close();
			System.out.println(jsc);
			
			System.out.println("***********************  ALL TASKS EXECUTED SUCCESSFULLY  ****************************");
		
		 
	}
	private static String getsource(String input) throws IOException
	{
		URL url = new URL(input);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuilder a = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
				a.append(inputLine);
		in.close();
		return a.toString();
	}
	
}