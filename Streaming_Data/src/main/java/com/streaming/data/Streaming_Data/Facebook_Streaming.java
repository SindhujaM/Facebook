package com.streaming.data.Streaming_Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class Facebook_Streaming 
{
	private static final String topic = "facebook";
	
	 public void fbrun(String base_url,String like_page_id,String feed_limit,String access_token) throws InterruptedException, IOException
	
	{
		String get_url=base_url+""+like_page_id+"?fields=feed.limit("+feed_limit+")&access_token="+access_token;
		
		// displaying Final URL
		System.out.println(get_url);
		
		// Calling get source method
	//	String facebookData =(getsource(get_url));
		//System.out.println(facebookData);
	
	
	
		
	Properties properties = new Properties();
	properties.put("metadata.broker.list", "localhost:9092");
	properties.put("serializer.class", "kafka.serializer.StringEncoder");
	properties.put("client.id","camus");
	
	ProducerConfig fbConfig = new ProducerConfig(properties);
	final kafka.javaapi.producer.Producer<String, String> Producer = new kafka.javaapi.producer.Producer<String, String>(fbConfig);

	//	LinkedBlockingQueue<String> fbqueue = new LinkedBlockingQueue<String>(1200);
	//	fbqueue.put(facebookData.toString());	
		
		KeyedMessage<String, String> message = new KeyedMessage<String, String>(topic, getsource(get_url));
		Producer.send(message);
	
		//System.out.println(facebookData);
		System.out.println(message);
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
	
	public static void main(String args[]) throws IOException, InterruptedException
	{
		Facebook_Streaming fb = new Facebook_Streaming();
		fb.fbrun("https://graph.facebook.com/","317523414926816","50","CAACEdEose0cBAGZBKVZCH1wzWyEgrUCe77MqEmigD7WpIEDBvt5xsAUBWdqy7FPMHC2UBtLuwuiHHBxjfwOocMcPK0ZBvhII4T20V3vl7QC4uKjsbZB7dc6vUcfwefJrQX2SDoZAWLPiNF9sSKGo8pLdWFAUwLgV4ZBAP9pVmaBhOeApAkMRjgb7ghwYtZBCEruE9ZBSJ8Cj4AZDZD");
		
	}
}
