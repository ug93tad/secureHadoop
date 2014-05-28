package tds1.encryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.terasort.TeraInputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import crypto.Rand;

/**
 * Encrypting the input file using AES in probablistic/randomized mode
 * Output file is of the form:
 * <Random IV, AES-CFB ciphertext>
 * 
 * InputFormat is TextInputFormat
 * OutputFormat is SequenceFileOutputFormat
 */
public class BaselineSortEncryptor extends Configured implements Tool{

	public static int KEY_LENGTH=10; 
	public static int VALUE_LENGTH=90;
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Baseline sort encryptor"); 
		job.setJarByClass(BaselineSortEncryptor.class); 
		job.setMapperClass(BaselineSortMapper.class); 
		
		job.setInputFormatClass(TeraInputFormat.class); 									
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(BytesWritable.class); 
		
		job.setNumReduceTasks(0); 
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
				
		return job.waitForCompletion(true)?0:1;
	}

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselineSortEncryptor(), args);
	}


	/**
	 * The IV is generated randomly. Using TextInputFormat, we encrypt only the
	 * value
	 */
	static class BaselineSortMapper extends
			Mapper<Text, Text, BytesWritable, BytesWritable> {
		private Rand crypto;
		static final int AES_BLOCK_SIZE = 16;

		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			crypto = new Rand();
			crypto.init(conf.get("key"));

		}

		@Override
		public void map(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			byte[] randomIV = crypto.randomBytes(AES_BLOCK_SIZE);

			byte[] pt = new byte[KEY_LENGTH + VALUE_LENGTH];
			System.arraycopy(key.copyBytes(), 0, pt, 0, KEY_LENGTH);
			System.arraycopy(value.copyBytes(), 0, pt, KEY_LENGTH, VALUE_LENGTH);
			// byte[] ct = crypto.encrypt_word_rnd(value.toString(), randomIV);
			byte[] ct = crypto.encrypt_word_cbc(pt, randomIV);
			System.out.println("ct size = "+ct.length); 
			context.write(new BytesWritable(randomIV), new BytesWritable(ct));
		}
	}
}
