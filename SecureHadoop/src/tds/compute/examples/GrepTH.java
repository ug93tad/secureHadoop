package tds.compute.examples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Det;
import tds.common.Rand;
import tds.hom.Searchable;
import tds.io.Ciphertext;
import tds.trustedhw.math.EqualityFunction;
import tds1.encryptor.SearchEncryptor;

/**
 * Similar to WordcountBaseline, but for Grep
 */
public class GrepTH extends Configured implements Tool{
			
	private Searchable se; 
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf(); 						 		
		ICrypto crypto = new Rand(); 	
		crypto.initPrivateParameters(Utils.getSymParams(conf));
		
		//encrypt pattern		
		conf.set(GrepBaselineMapper.PATTERN,
				Utils.bytesToHex(crypto.encryptString(args[0]))); 								 		
		Job job = new Job(conf); 
		job.setJobName("grep exact match - trusted hypervisor"); 
		job.setJarByClass(GrepTH.class); 
		job.setMapperClass(GrepBaselineMapper.class);
		job.setReducerClass(GrepBaselineReducer.class);
		job.setCombinerClass(GrepBaselineReducer.class);
		
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setMapOutputKeyClass(Ciphertext.class);
		job.setMapOutputValueClass(LongWritable.class); 	
		job.setOutputKeyClass(Ciphertext.class);		
		job.setOutputValueClass(LongWritable.class);
				 
		int result =  job.waitForCompletion(true)? 0: 1;	
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.RND); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue());
		return result; 
	}

	public static void main(String[] args) throws Exception{		
		ToolRunner.run(new GrepTH(), args); 
	}
}

class GrepBaselineMapper extends Mapper<NullWritable, Ciphertext, Ciphertext, LongWritable>{
	EqualityFunction crypto;
	static final String PATTERN="keyword";
	private String token; 
	private Det reEncryptor; 
				
	@Override
	public void setup(Context context) throws IOException{
		Configuration conf = context.getConfiguration(); 
				
		crypto = new EqualityFunction(); 			
		crypto.initPrivateParameters(Utils.getSymParams(conf));
		
		token = crypto.decryptToText(Utils.HexToBytes(conf.get(GrepBaselineMapper.PATTERN))); 
	
		reEncryptor = new Det();
		reEncryptor.initPrivateParameters(Utils.getSymParams(conf)); 
		
	}
	
	/* decrypt, then compute
	 */
	@Override
	public void map(NullWritable key, Ciphertext val, Context context) throws IOException, InterruptedException{
		String[] ss = crypto.decryptToText(val.getContent().copyBytes()).split("\\s+"); 
		context.getCounter(TdsOptions.HEEDOOP_COUNTER.RND).increment(1); 
		for (String s: ss)
		//if (Pattern.matches(token, s)){
		if (s.equals(token))
			context.write(new Ciphertext(reEncryptor.encryptString(s)), new LongWritable(1)); 		
	}
}


class GrepBaselineReducer extends Reducer<Ciphertext, LongWritable, Ciphertext, LongWritable>{
	
	@Override
	public void reduce(Ciphertext key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s=0;
		for (LongWritable lw : vals)
			s+=lw.get(); 		
		context.write(key, new LongWritable(s)); 			
	}
}
