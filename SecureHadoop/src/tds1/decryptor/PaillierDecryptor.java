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

import crypto.Paillier;
import crypto.Rand;

/**
 * To test the output
 * 
 * Input is SequenceFile:
 * <key = encrypted source IP> <value = encrypted revenue>
 *
 */
public class PaillierDecryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		Job job = new Job(conf);
		job.setJobName("paillier decryptor");
		job.setJarByClass(PaillierDecryptor.class);
		
		job.setMapperClass(PaillierDecryptorMapper.class);
		job.setNumReduceTasks(0);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class  PaillierDecryptorMapper extends Mapper<BytesWritable, BytesWritable, Text, LongWritable>{
		
		Paillier paillier;
		Rand rand;
		byte[] fixedIV; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 
			paillier = new Paillier();
			paillier.init_public_key(conf.get("pub.key")); 
			paillier.init_private_key(conf.get("priv.key"));
			rand = new Rand(); 
			rand.init(conf.get("key"));
			
			fixedIV = new byte[16];
			for (int i=0; i<16; i++)
				fixedIV[i] = 0; 
		}
		
		@Override
		public void map(BytesWritable key, BytesWritable value, Context context)
				throws IOException, InterruptedException {
			String source = new String(rand.decrypt_word_cbc(key.copyBytes(),
					fixedIV));
			long sum = paillier.decrypt(value.copyBytes());
			context.write(new Text(source), new LongWritable(sum));
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new PaillierDecryptor(), args);
	}
}
