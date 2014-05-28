package tds.compute.examples;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tds.io.Ciphertext;

/**
 * @author dinhtta
 * wordcount for data generated by DetEncryptor
 * 
 * -D encryption_mode=line/word
 */
public class Wordcount extends Configured implements Tool{

	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Word count"); 
		job.setJarByClass(Wordcount.class); 
		job.setMapperClass(WordcountMapper.class); 
		job.setReducerClass(WordcountReducer.class);
		job.setCombinerClass(WordcountReducer.class); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapOutputKeyClass(Ciphertext.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(Ciphertext.class);
		job.setOutputValueClass(LongWritable.class); 
		
		FileInputFormat.addInputPath(job, new Path(arg0[0]));
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		return job.waitForCompletion(true) ? 0:1; 
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new Wordcount(), args); 
	}
}

class WordcountMapper extends Mapper<NullWritable, Ciphertext, Ciphertext, LongWritable>{
	static Logger log = Logger.getLogger(WordcountMapper.class); 
			
	@Override
	public void map(NullWritable key, Ciphertext val, Context context) throws IOException, InterruptedException{		
		context.write(val, new LongWritable(1));		
	}		
}

class WordcountReducer extends Reducer<Ciphertext,LongWritable, Ciphertext, LongWritable>{
	@Override
	public void reduce(Ciphertext key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s =0;
		for (LongWritable val : vals)
			s+=val.get(); 
		context.write(key, new LongWritable(s)); 
	}
}
