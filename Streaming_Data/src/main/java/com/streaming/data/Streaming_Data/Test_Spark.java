package com.streaming.data.Streaming_Data;

	import java.io.Serializable;
	 import java.util.Arrays;
	 import java.util.List;
	 
	 import org.apache.spark.SparkConf;
	 import org.apache.spark.api.java.JavaRDD;
	 import org.apache.spark.api.java.JavaSparkContext;
	import org.apache.spark.api.java.function.Function;
	 
	 import org.apache.spark.sql.DataFrame;
	 import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
public class Test_Spark
{
	public static class Person implements Serializable {
		        private String name;
		        private int age;
		    
		        public String getName() {
		          return name;
		        }
		    
		        public void setName(String name) {
		          this.name = name;
		        }
		    
		        public int getAge() {
		          return age;
		        }
		    
		        public void setAge(int age) {
		          this.age = age;
		        }
		     }
	public static void main(String[] args) throws Exception
	{
		SparkConf sparkConf = new SparkConf().setMaster("local[2]").setAppName("JavaSparkSQL");
		        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
		       SQLContext sqlContext = new SQLContext(ctx);
		    
		        System.out.println("=== Data source: RDD ===");
		       // Load a text file and convert each line to a Java Bean.
		        JavaRDD<Person> people = ctx.textFile("/home/bigtapp/Downloads/spark-master/examples/src/main/resources/people.txt").map(
		         new Function<String, Person>() {
		            public Person call(String line) {
		             String[] parts = line.split(",");
		   
		              Person person = new Person();
		             person.setName(parts[0]);
		             person.setAge(Integer.parseInt(parts[1].trim()));
		   
		              return person;
		            }
		         });
		  
		      // Apply a schema to an RDD of Java Beans and register it as a table.
		        DataFrame schemaPeople = sqlContext.createDataFrame(people, Person.class);
		       schemaPeople.registerTempTable("people");
		    
		        // SQL can be run over RDDs that have been registered as tables.
		        DataFrame teenagers = sqlContext.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19");
		   
		       // The results of SQL queries are DataFrames and support all the normal RDD operations.
		        // The columns of a row in the result can be accessed by ordinal.
		       List<String> teenagerNames = teenagers.toJavaRDD().map(new Function<Row, String>() {
		          public String call(Row row) {
		            return "Name: " + row.getString(0);
		          }
		        }).collect();
		        for (String name: teenagerNames) {
		          System.out.println(name);
		        }
		  
		      
		      System.out.println("=== Data source: JSON Dataset ===");
		      // A JSON dataset is pointed by path.
		      // The path can be either a single text file or a directory storing text files.
		     String path = "/home/bigtapp/Downloads/spark-master/examples/src/main/resources/people.json";
		      // Create a DataFrame from the file(s) pointed by path
		       DataFrame peopleFromJsonFile = sqlContext.read().json(path);
		   
		      // Because the schema of a JSON dataset is automatically inferred, to write queries,
		       // it is better to take a look at what is the schema.
		       peopleFromJsonFile.printSchema();
		       // The schema of people is ...
		       // root
		      //  |-- age: IntegerType
		       //  |-- name: StringType
		   
		       // Register this DataFrame as a table.
		      peopleFromJsonFile.registerTempTable("people");
		  
		      // SQL statements can be run by using the sql methods provided by sqlContext.
		     DataFrame teenagers3 = sqlContext.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19");
		  
		      // The results of SQL queries are DataFrame and support all the normal RDD operations.
		      // The columns of a row in the result can be accessed by ordinal.
		       teenagerNames = teenagers3.toJavaRDD().map(new Function<Row, String>() {
		         public String call(Row row) { return "Name: " + row.getString(0); }
		      }).collect();
		      for (String name: teenagerNames) {
		         System.out.println(name);
		       }
		 
		      // Alternatively, a DataFrame can be created for a JSON dataset represented by
		      // a RDD[String] storing one JSON object per string.
		       List<String> jsonData = Arrays.asList(
		            "{\"name\":\"Yin\",\"address\":{\"city\":\"Columbus\",\"state\":\"Ohio\"}}");
		      JavaRDD<String> anotherPeopleRDD = ctx.parallelize(jsonData);
		       DataFrame peopleFromJsonRDD = sqlContext.read().json(anotherPeopleRDD.rdd());
		   
		       // Take a look at the schema of this new DataFrame.
		     peopleFromJsonRDD.printSchema();
		       // The schema of anotherPeople is ...
		       // root
		       //  |-- address: StructType
		       //  |    |-- city: StringType
		       //  |    |-- state: StringType
		       //  |-- name: StringType
		   
		       peopleFromJsonRDD.registerTempTable("people2");
		   
		       DataFrame peopleWithCity = sqlContext.sql("SELECT name, address.city FROM people2");
		       List<String> nameAndCity = peopleWithCity.toJavaRDD().map(new Function<Row, String>() {
		         public String call(Row row) {
		           return "Name: " + row.getString(0) + ", City: " + row.getString(1);
		         }
		       }).collect();
		       for (String name: nameAndCity) {
		         System.out.println(name);
		       }
		   
		       ctx.stop();
		    }
		
	}

