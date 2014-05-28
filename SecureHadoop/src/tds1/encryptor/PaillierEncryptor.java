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
 * Encrypt data generated from HiBench's autogen. 
 * 
 * The output format is SequenceFile:
 * <key = IV> <value = 16 bytes source IP + 256 bytes adRevenue + the rest> 
 *
 */
public class PaillierEncryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Paillier paillier = new Paillier();
		paillier.keygen(conf.get("pub.key"), conf.get("priv.key")); 
		Job job = new Job(conf); 
		
		job.setJobName("paillier encryption"); 
		job.setJarByClass(PaillierEncryptor.class);
		job.setMapperClass(PaillierMapper.class);
		job.setNumReduceTasks(0); 
		
		job.setInputFormatClass(KeyValueTextInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(BytesWritable.class); 
				
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class PaillierMapper extends Mapper<Text, Text, BytesWritable, BytesWritable>{
		Rand rand;
		Paillier paillier; 		
		byte[] fixedIV; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 		
			rand = new Rand();
			rand.init(conf.get("key")); 
			paillier = new Paillier(); 
			paillier.init_public_key(conf.get("pub.key"));			
			fixedIV = new byte[16];
			for (int i=0; i<16; i++) 
				fixedIV[i] = 0; 		
			
		}
		
		@Override
		public void map(Text key, Text value, Context context) throws IOException, InterruptedException{
			String[] vals = value.toString().split(","); 
			String sourceIP = vals[0];
			String rev = vals[3]; 
			String rest = vals[1] + "," + vals[2] + "," + vals[4] + ","
					+ vals[5] + "," + vals[6] + "," + vals[7] + "," + vals[8];
			byte[] iv = rand.randomBytes(16); //this is the key
			
			byte[] encryptedRest = rand.encrypt_word_cbc(rest, iv);  
			byte[] encryptedSource = rand.encrypt_word_cbc(sourceIP, fixedIV);//16 bytes					
			byte[] encryptedRev = paillier.encrypt(new Integer(rev).intValue()); //256 bytes				
			
			byte[] ct = new byte[encryptedRest.length+encryptedSource.length+encryptedRev.length];						
			System.arraycopy(encryptedSource, 0, ct, 0, encryptedSource.length);
			System.arraycopy(encryptedRev, 0, ct, encryptedSource.length,
					encryptedRev.length);
			System.arraycopy(encryptedRest, 0, ct, encryptedSource.length
					+ encryptedRev.length, encryptedRest.length); 
			context.write(new BytesWritable(iv), new BytesWritable(ct)); 	
		}
	}
		
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new PaillierEncryptor(), args); 
	}
}
