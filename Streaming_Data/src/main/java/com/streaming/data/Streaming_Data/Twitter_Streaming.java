package com.streaming.data.Streaming_Data;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class Twitter_Streaming
{
private static final String topic = "twitter-topic";
	
	public static void run(String consumerKey, String consumerSecret,
			String token, String secret) 
	{

		Properties properties = new Properties();
		properties.put("metadata.broker.list", "localhost:9092");
		properties.put("serializer.class", "kafka.serializer.StringEncoder");
		properties.put("client.id","camus");
		
		ProducerConfig producerConfig = new ProducerConfig(properties);
		kafka.javaapi.producer.Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
		
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(100);

		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
		endpoint.trackTerms(Lists.newArrayList("twitterapi","#gold"));
		
		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

		Client client = new ClientBuilder().hosts(Constants.STREAM_HOST)
				.endpoint(endpoint).authentication(auth)
				.processor(new StringDelimitedProcessor(queue)).build();
		
		
		// Establish a connection
		client.connect();
		
		// Do whatever needs to be done with messages
		for (int msgRead = 0; msgRead < 5; msgRead++)
		{
			
			KeyedMessage<String, String> Data= null;
			System.out.println("Hello");
			try {
				
				Data = new KeyedMessage<String, String>(topic, queue.take());
				//System.out.println("Hello");
				System.out.println(Data);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			producer.send(Data);
		
		
			System.out.println(Data);
			
		}
		producer.close();
		client.stop();
		}

public static void main(String args[]) 
{
	Twitter_Streaming.run("mmkrPknQiNX7AlkDwhADRgAJs","WoR05dPkPYMUGNV52WWShflgFF6kmOdNleTiDdRdpDTzX510wD", "3557086092-LOZOqkjgyYFmimwk4OUP31iqCaaGJID5DQUIfuT", "yPUWhZ5NN8f0le9q13KN01oVMyFpEmhued2HyKyVJbK6E");
}
	

}
