package tds.decode.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tds.TdsOptions;
import tds.common.Det;
import tds.io.Ciphertext;

/**
 * Decrypt result of Wordcount both with HOM and with TH
 *
 */
public class GrepDecryptor extends Configured implements Tool{
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = this.getConf();  
		Job job = new Job(conf); 
		
		job.setJobName("deterministic decryptor"); 
		job.setJarByClass(GrepDecryptor.class); 
		FileInputFormat.addInputPath(job, new Path(arg0[0])); 
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class); 
		job.setMapperClass(GrepDecryptorMapper.class);
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(LongWritable.class); 
				
		job.setNumReduceTasks(0); 
		
		return job.waitForCompletion(true)?0:1; 		
	}

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new GrepDecryptor(), args); 
	}
}

class GrepDecryptorMapper extends Mapper<Ciphertext, LongWritable, Text, LongWritable>{
	private Det crypto; 
		
	static Logger log = Logger.getLogger(DetDecryptorMapper.class); 	
	
	@Override
	public void setup(Context context){
		
		Configuration conf = context.getConfiguration();
		String key = conf.get(TdsOptions.KEY_OPTION); 
		String fixedIv = conf.get(TdsOptions.IV_OPTION); 
		List<String> params = new ArrayList<String>();
		params.add(key); 
		params.add(fixedIv); 		
			 				
		this.crypto = new Det(); 
		this.crypto.initPrivateParameters(params); 				
	}
	
	/* decrypt the key, keep the value
	 */
	@Override
	public void map(Ciphertext key, LongWritable val, Context context)
			throws IOException, InterruptedException {		
		context.write(new Text(crypto.decryptToText(key.getContent().copyBytes())), val); 		
	}
}