package tds1.decryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.terasort.TeraOutputFormat;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds1.encryptor.BaselineSortEncryptor;
import tds1.io.TeraEncryptedInputFormat;

import crypto.Det;
import crypto.Rand;

/**
 * Encrypting the input file using AES in probablistic/randomized mode
 * Output file is of the form:
 * <Random IV, AES-CFB ciphertext>
 * 
 * InputFormat is TextInputFormat
 * OutputFormat is SequenceFileOutputFormat
 */
public class BaselineSortDecryptor extends Configured implements Tool{

	public static int KEY_LENGTH=10; 
	public static int VALUE_LENGTH=90;
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Baseline sort encryptor"); 
		job.setJarByClass(BaselineSortDecryptor.class); 
		job.setMapperClass(BaselineSortDecryptor.BaselineSortMapper.class); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class); 
	
		
		job.setNumReduceTasks(0); 
		
		job.setOutputFormatClass(TeraOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
				
		return job.waitForCompletion(true)?0:1;
	}

	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselineSortDecryptor(), args);
	}


	/**
	 * Decrypt
	 */
	static class BaselineSortMapper extends
			Mapper<BytesWritable, BytesWritable, Text, Text> {
		private Rand crypto;
		static final int AES_BLOCK_SIZE = 16;

		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			crypto = new Rand();
			crypto.init(conf.get("key"));

		}

		@Override
		public void map(BytesWritable key, BytesWritable value, Context context)
				throws IOException, InterruptedException {
			byte[] pt = crypto.decrypt_word_cbc(value.copyBytes(),
					key.copyBytes());
			byte[] k = new byte[BaselineSortEncryptor.KEY_LENGTH];
			byte[] v = new byte[BaselineSortEncryptor.VALUE_LENGTH];
			System.arraycopy(pt, 0, k, 0, BaselineSortEncryptor.KEY_LENGTH);
			System.arraycopy(pt, BaselineSortEncryptor.KEY_LENGTH, v, 0,
					BaselineSortEncryptor.VALUE_LENGTH);
			context.write(new Text(k), new Text(v));
		}
	}
}
