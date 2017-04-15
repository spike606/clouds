package com.mycompany.passwordEntropy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import spark.*;
import static spark.Spark.get;

/**
 * Hello world!
 *
 */
public class PasswordEntropy extends Configured implements Tool 
 
{
     private final static double CHARSET_SIZE = 62;
    
    public static void main(final String[] args){
        get(new Route("/start") {
            @Override
            public Object handle(Request request, Response response) {
                int result = -0;
                try {
                   result = ToolRunner.run(new Configuration(), new PasswordEntropy(), args);
                } catch (Exception ex) {
                    Logger.getLogger(PasswordEntropy.class.getName()).log(Level.SEVERE, null, ex);
                    return "Password entropy job failed! Return code: " + ex.getMessage();
                }                
                return "Password entropy job finished! Return code: " + result;
            }
        });
        get(new Route("/getresult") {
            @Override
            public Object handle(Request request, Response response) {             
                try { 
                    return readFile("/user/outputPasswordEntropySorted/part-r-00000");           
                
                } catch (IOException ex) {
                    Logger.getLogger(PasswordEntropy.class.getName()).log(Level.SEVERE, null, ex);
                    return "Error! Return code: " + ex.getMessage();
                }                
            }
        }); 
    }
    
    private static String readFile(String pathname) throws IOException {

        String result = "";
        try{
                Path pt=new Path(pathname);
                FileSystem fs = FileSystem.get(new Configuration());
                BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(pt)));
                String line;
                line=br.readLine();
                while (line != null){
                        result += line + "<br/>";
                        line=br.readLine();
                }
        }catch(Exception e){
        }
        return result;

    }   

    @Override
    public int run(String[] args) throws Exception {        
        return runJobPasswordEntropy(args);
    }    
    
    private int runJobPasswordEntropy(String[] args) throws Exception{
                // When implementing tool
        Configuration conf = this.getConf();
 
        // Create job
        Job job = new Job(conf, "PasswordSort Job");
        job.setJarByClass(PasswordEntropy.class);
 
        // Setup MapReduce job
        // Do not specify the number of Reducer
        job.setMapperClass(MapEntropy.class);
        job.setReducerClass(ReduceEntropy.class);
 
        // Specify key / value
        job.setMapOutputKeyClass(DoubleWritable.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);        
 
        job.setSortComparatorClass(DoubleComparator.class);
        
        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);
 
        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);
 
        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }      
       
    
    public static class MapEntropy extends Mapper<LongWritable, Text, DoubleWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context)
          throws java.io.IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                value.set(token);
                context.write(new DoubleWritable(getEntropyForPassword(token)), value);
                }
            }
                           
        
        private static double getEntropyForPassword (String password){
            int pwlength = password.length();
               return pwentropy(pwlength);
        }               
        private static double pwentropy(int length){
	        return length*(log2(CHARSET_SIZE));
        }
        private static double log2(double number) { 
        	return Math.log(number)/Math.log(2.0);
        }
    }
    
    public static class ReduceEntropy extends Reducer<DoubleWritable, Text, Text, DoubleWritable> {
        @Override
        public void reduce(DoubleWritable key, Iterable<Text> list, Context context)
          throws java.io.IOException, InterruptedException {
         for (Text value : list) {
          context.write(value,key);
         }
        }
    }
    
     public static class DoubleComparator extends WritableComparator {

     public DoubleComparator() {
         super(DoubleWritable.class);
     }

     @Override
     public int compare(byte[] b1, int s1, int l1,
             byte[] b2, int s2, int l2) {

         Double v1 = ByteBuffer.wrap(b1, s1, l1).getDouble();
         Double v2 = ByteBuffer.wrap(b2, s2, l2).getDouble();

         return v1.compareTo(v2) * (-1);
     }
 }
}
