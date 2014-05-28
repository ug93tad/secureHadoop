package tds.compute.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
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
import tds.functions.IAddFunction;
import tds.hom.Paillier;
import tds.io.Ciphertext;
import tds.io.EncryptedVector;
import tds.io.EncryptedVectorWritable;


/**
 * Aggregate implementation over Paillier-encrypted data. 
 * 
 * Input is the sequence file generated by PaillierEncryptor
 *
 */
public class Aggregate extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		Job job = new Job(conf); 
		job.setJobName("aggregate paillier"); 
		job.setJarByClass(Aggregate.class);
		
		job.setMapperClass(AggregatePaillierMapper.class);
		job.setCombinerClass(AggregatePaillierReducer.class); 
		job.setReducerClass(AggregatePaillierReducer.class); 
		job.setOutputKeyClass(Ciphertext.class); 
		job.setOutputValueClass(Ciphertext.class);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		
		return job.waitForCompletion(true) ? 0 : 1;
	}

	//output (<encrypted sourceIP>,<Paillier encrypted revenue>)
	static class AggregatePaillierMapper extends Mapper<NullWritable, EncryptedVectorWritable, Ciphertext, Ciphertext>{						
		/* key = IV -> ignored
		 */				
		
		@Override
		public void map(NullWritable key, EncryptedVectorWritable value, Context context)
				throws IOException, InterruptedException {			
			Ciphertext newKey = new Ciphertext(value.get().getQuick(0)); 
			Ciphertext newVal = new Ciphertext(value.get().getQuick(1)); 												
			context.write(newKey, newVal);
		}
				
	}
	
	static class AggregatePaillierReducer extends Reducer<Ciphertext, Ciphertext, Ciphertext, Ciphertext>{
		ICrypto crypto;
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 			
			crypto = new Paillier(); 
			crypto.initPublicParameters(Utils.getPaillierPub(conf)); 			
		}
		
		@Override
		public void reduce(Ciphertext key, Iterable<Ciphertext> values,
				Context context) throws IOException, InterruptedException{
			IAddFunction paillier = (Paillier)crypto; 
			
			Iterator<Ciphertext> it = values.iterator();
			Ciphertext encryptedSum = it.next();
			while (it.hasNext()) {
				try {
					encryptedSum = paillier
							.add(encryptedSum, it.next());
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
			context.write(key, encryptedSum);
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new Aggregate(), args); 
	}
}
