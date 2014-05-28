package examples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import crypto.Det;
import crypto.Rand;
import crypto.SearchToken;
import crypto.Searchable;

import tds.Utils;
import tds1.encryptor.SearchEncryptor;

/**
 * Similar to WordcountBaseline, but for Grep
 */
public class GrepBaseline extends Configured implements Tool{
			
	private Searchable se; 
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf(); 		
		System.out.format("LD_LIBRARY_PATH = %s\n",System.getenv("LD_LIBRARY_PATH"));
		System.out.format("java.library.path = %s\n",System.getProperty("java.library.path")); 
				 
		
		//create new token and set in in the configuration
		//so that all workers can see
		se = new Searchable(); 
		se.init(conf.get("key")); 
		conf.set(GrepBaselineMapper.PATTERN,args[0]); 		
		
		Job job = new Job(conf); 
		job.setJobName("grep exact match - baseline"); 
		job.setJarByClass(GrepBaseline.class); 
		job.setMapperClass(GrepBaselineMapper.class);
		job.setReducerClass(GrepBaselineReducer.class);
		job.setCombinerClass(GrepBaselineCombiner.class);
		
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class); 	
		job.setOutputKeyClass(BytesWritable.class);		
		job.setOutputValueClass(BytesWritable.class);
				 
		return job.waitForCompletion(true)? 0: 1;		
	}

	public static void main(String[] args) throws Exception{		
		ToolRunner.run(new GrepBaseline(), args); 
	}
}

class GrepBaselineMapper extends Mapper<BytesWritable, BytesWritable, Text, LongWritable>{
	Rand crypto;
	static final String PATTERN="keyword";
	private Pattern pattern;
			
	@Override
	public void setup(Context context){
		Configuration conf = context.getConfiguration(); 
		crypto = new Rand(); 			
		crypto.init(conf.get("key"));
		
		pattern = Pattern.compile(conf.get(PATTERN));
	}
	
	/* decrypt, then compute
	 */
	@Override
	public void map(BytesWritable key, BytesWritable val, Context context) throws IOException, InterruptedException{
		byte[] iv = key.copyBytes(); 
		byte[] ct = val.copyBytes(); 
		//String[] ss = new String(crypto.decrypt_word_rnd(ct, iv)).split("\\s+");
		
		String text = new String(crypto.decrypt_word_rnd(ct, iv));
	    Matcher matcher = pattern.matcher(text);
	    
	    while (matcher.find()) {
	      context.write(new Text(matcher.group(0)), new LongWritable(1));
	    }	    		
	}
}

class GrepBaselineCombiner extends Reducer<Text, LongWritable, Text, LongWritable>{
	@Override 
	public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException{
		long s =0;
		for (LongWritable lw : values)
			s+=lw.get(); 
		context.write(key, new LongWritable(s)); 
	}
}

class GrepBaselineReducer extends Reducer<Text, LongWritable, BytesWritable, BytesWritable>{
	static final int AES_BLOCK_SIZE=16; 
	Rand crypto;
	
	@Override
	public void setup(Context context){
		Configuration conf = context.getConfiguration(); 
		crypto = new Rand(); 			
		crypto.init(conf.get("key")); 
	}
	
	@Override
	public void reduce(Text key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s=0;
		for (LongWritable lw : vals)
			s+=lw.get(); 
		String pt = key.toString()+" "+s; 
		byte[] iv = crypto.randomBytes(AES_BLOCK_SIZE);
		byte[] ct = crypto.encrypt_word_rnd(pt, iv); 
		context.write(new BytesWritable(iv), new BytesWritable(ct)); 			
	}
}
