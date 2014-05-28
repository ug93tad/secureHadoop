package tds1.decryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import crypto.Det;
import crypto.Paillier;
import crypto.Rand;

/**
 * Baseline encryption 
 * 
 * The output format is SequenceFile:
 * <key = IV> <value = the rest> 
 *
 */
public class BaselineAggregateDecryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("baseline aggregate decryption");
		job.setJarByClass(BaselineAggregateDecryptor.class); 
		job.setMapperClass(BaselineAggregateMapper.class);
		job.setNumReduceTasks(0);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
				
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class BaselineAggregateMapper extends Mapper<BytesWritable, BytesWritable, NullWritable, Text>{
		Rand rand;		 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 		
			rand = new Rand();
			rand.init(conf.get("key")); 			 					
		}
		
		@Override
		public void map(BytesWritable key, BytesWritable value, Context context) throws IOException, InterruptedException{						
			byte[] iv = key.copyBytes(); 
			
			byte[] pt = rand.decrypt_word_cbc(value.copyBytes(),  iv);   			
			context.write(NullWritable.get(), new Text(new String(pt))); 
		}
	}
		
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselineAggregateDecryptor(), args); 
	}
}
