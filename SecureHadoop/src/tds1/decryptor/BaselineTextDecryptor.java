package tds1.decryptor;

import java.io.IOException;

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

import crypto.Det;
import crypto.Rand;

/**
 * Decrypt the data encrypted with WordcountBaseline, which is SequenceFileInput<BytesWritable,BytesWritable>
 * and output format is TextOutputFormat
 *
 */
public class BaselineTextDecryptor extends Configured implements Tool{
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = this.getConf();  
		Job job = new Job(conf); 
		
		job.setJobName("basline  decryptor"); 
		job.setJarByClass(BaselineTextDecryptor.class); 
		FileInputFormat.addInputPath(job, new Path(arg0[0])); 
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class); 
		job.setMapperClass(BaselineTextDecryptorMapper.class);
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(LongWritable.class); 
				
		
		job.setNumReduceTasks(0); 
		
		return job.waitForCompletion(true)?0:1; 		
	}

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselineTextDecryptor(), args); 
	}
}

class BaselineTextDecryptorMapper extends Mapper<BytesWritable, BytesWritable, Text, LongWritable>{
	private Rand crypto; 
			
	
	@Override
	public void setup(Context context){
		Configuration conf = context.getConfiguration();			
		
		crypto = new Rand(); 			
		crypto.init(conf.get("key")); 		
	}
	
	/* decrypt the key, keep the value
	 */
	@Override
	public void map(BytesWritable key, BytesWritable val, Context context)
			throws IOException, InterruptedException {
		String[] s = new String(crypto.decrypt_word_rnd(val.copyBytes(), key.copyBytes())).split("\\s+"); 		
		context.write(new Text(new String(s[0])), new LongWritable(new Long(s[1]).longValue()));
	}
}