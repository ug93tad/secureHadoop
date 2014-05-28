package tds1.encryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
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
public class BaselineAggregateEncryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("baseline aggregate encryption"); 
		job.setJarByClass(BaselineAggregateEncryptor.class); 
		job.setMapperClass(BaselineAggregateMapper.class);
		job.setNumReduceTasks(0);
		
		job.setInputFormatClass(KeyValueTextInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(BytesWritable.class);
				
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class BaselineAggregateMapper extends Mapper<Text, Text, BytesWritable, BytesWritable>{
		Rand rand;		 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 		
			rand = new Rand();
			rand.init(conf.get("key")); 			 					
		}
		
		@Override
		public void map(Text key, Text value, Context context) throws IOException, InterruptedException{						
			byte[] iv = rand.randomBytes(16); //this is the key
			
			byte[] encryptedRest = rand.encrypt_word_cbc(value.toString(), iv);  			
			context.write(new BytesWritable(iv), new BytesWritable(encryptedRest)); 
		}
	}
		
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselineAggregateEncryptor(), args); 
	}
}
