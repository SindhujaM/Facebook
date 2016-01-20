package com.streaming.data.Streaming_Data;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import scala.Tuple2;

public class Twitter_Spark
{

	public static void main(String[]args)
	{
		{

			System.out.println( "................kafka produecer sucessfuly start................" );
	      
	        String brokers = "localhost:2181";
	        String group="spark";
	        Map<String, Integer> topicMap = new HashMap<String,Integer>();
	        topicMap.put("twitter-topic",1);
	       
	        JavaStreamingContext spark = new JavaStreamingContext("local[2]", "SparkStream", new  Duration(1200));
	        JavaPairReceiverInputDStream<String, String> messages = KafkaUtils.createStream(spark, brokers, group, topicMap);

	        System.out.println("+++++++++++++++++++ kafka to spark Connection done ++++++++++++++");
	        @SuppressWarnings("serial")
			JavaDStream<String> data = messages.map(new Function<Tuple2<String, String>, String>() 
														{
	                                                    public String call(Tuple2<String, String> Data)
	                                                    {
	                                                        return Data._2();
	                                                    }
														}
					   );
	        
		data.foreachRDD(new Function<JavaRDD<String>, Void>() 
	        	
	       	{
	        	
	        	public Void call(JavaRDD<String> data) throws Exception 
			{
	        	
				
					try {

		    			Mongo mongo = new Mongo("localhost", 27017);
		    			DB db = mongo.getDB("mongodb");
		    			DBCollection collection = db.getCollection("twitter");
		    			
					// TODO Auto-generated method stub
				if(data!=null){
						List<String>result=data.collect();
						//JSONArray tweets = (JSONArray) data.collect();
						System.out.println(""+result);
						
						for(String mongodb : result)
						{
						/*	File f = new File("message.json");
							 {
								FileWriter fw = new FileWriter(f.getAbsoluteFile());
								fw.write(mongodb);
								fw.close();
							 }
						*/
							FileReader reader = new FileReader(mongodb);
							
							JSONParser jsonParser = new JSONParser();
							JSONObject jsonObject = (JSONObject) jsonParser.parse(mongodb);
							JSONObject TempjsonObject = (JSONObject) jsonObject.get("user");
							if (TempjsonObject != null)
								jsonObject = TempjsonObject;
							
								String user = (String)TempjsonObject.get("description");
								
							System.out.println(user);
							
							
							System.out.println(mongodb);
							DBObject dbObject = (DBObject) JSON.parse(mongodb.toString());
						    collection.insert(dbObject);
						}
							System.out.println("DONE");
						}
					
					
					else 
					{
		                System.out.println("Got no data in this window");
		            }
					}
					
					 catch (MongoException e) {
							e.printStackTrace();
						}
									
					return null;
				}
	        	
	        });
	       data.print();
	     //  JavaRDD<String> rdd = data;
	   
	        	spark.start();
		        spark.awaitTermination();
		       
	          
	        	
	        	// public static void main( String[] args ) throws InterruptedException, IOException
	        
	}
	}
}
