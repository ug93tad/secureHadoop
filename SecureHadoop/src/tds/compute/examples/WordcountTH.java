package tds.compute.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Det;
import tds.common.Rand;
import tds.io.Ciphertext;


public class WordcountTH extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Wordcount with TH"); 
		job.setJarByClass(WordcountTH.class); 
		job.setMapperClass(WCBaselineMapper.class);		
		job.setCombinerClass(WCBaselineReducer.class); 
		job.setReducerClass(WCBaselineReducer.class); 
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setMapOutputKeyClass(Ciphertext.class);
		job.setMapOutputValueClass(LongWritable.class); 
				
		
		job.setOutputKeyClass(Ciphertext.class);
		job.setOutputValueClass(LongWritable.class); 					
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
				
		
		int result =  job.waitForCompletion(true)?0:1;
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.RND); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue()); 
		return result; 
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new WordcountTH(), args);
	}
}

class WCBaselineMapper extends Mapper<NullWritable, Ciphertext, Ciphertext, LongWritable>{
	ICrypto crypto, reEncryptor;
	
	@Override
	public void setup(Context context){				
		Configuration conf = context.getConfiguration(); 	
		crypto = new Rand(); 	
		crypto.initPrivateParameters(Utils.getSymParams(conf));
			
		reEncryptor = new Det();
		reEncryptor.initPrivateParameters(Utils.getSymParams(conf)); 
	}
	
	/* decrypt, then re-encrypt
	 */
	@Override
	public void map(NullWritable key, Ciphertext val, Context context) throws IOException, InterruptedException{
		
		String[] ss = crypto.decryptToText(val.getContent().copyBytes()).split("\\s+");
		context.getCounter(TdsOptions.HEEDOOP_COUNTER.RND).increment(1); 
		for (String word : ss) {
			Ciphertext out = new Ciphertext(reEncryptor.encryptString(word));
			context.write(out, new LongWritable(1));
		}
	}
}


	class WCBaselineReducer extends Reducer<Ciphertext, LongWritable, Ciphertext, LongWritable>{	
	
	@Override
	public void reduce(Ciphertext key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s=0;
		for (LongWritable lw : vals)
			s+=lw.get(); 		
		context.write(key, new LongWritable(s)); 			
	}
}
