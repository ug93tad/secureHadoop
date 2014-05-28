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

/**
 * Decrypt the data encrypted with DetEncryptor. 
 * The assumed input is SequenceFileInput<BytesWritable,LongWritable>
 * and output format is TextOutputFormat
 *
 */
public class DetDecryptor extends Configured implements Tool{
	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = this.getConf();  
		Job job = new Job(conf); 
		
		job.setJobName("deterministic decryptor"); 
		job.setJarByClass(DetDecryptor.class); 
		FileInputFormat.addInputPath(job, new Path(arg0[0])); 
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class); 
		job.setMapperClass(DetDecryptorMapper.class);
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(LongWritable.class); 
				
		job.setNumReduceTasks(0); 
		
		return job.waitForCompletion(true)?0:1; 		
	}

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new DetDecryptor(), args); 
	}
}

class DetDecryptorMapper extends Mapper<BytesWritable, LongWritable, Text, LongWritable>{
	private Det crypto; 
		
	static Logger log = Logger.getLogger(DetDecryptorMapper.class); 	
	
	@Override
	public void setup(Context context){
		Configuration conf = context.getConfiguration();			
		
		crypto = new Det(); 			
		crypto.det_init(conf.get("key"), conf.get("iv")); 		
	}
	
	/* decrypt the key, keep the value
	 */
	@Override
	public void map(BytesWritable key, LongWritable val, Context context)
			throws IOException, InterruptedException {
		byte[] ct = crypto.decrypt_word(key.copyBytes());
		context.write(new Text(new String(ct)), val);
	}
}