package examples;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import crypto.Paillier;

/**
 * Aggregate implementation over Paillier-encrypted data. 
 * 
 * Input is the sequence file generated by PaillierEncryptor
 *
 */
public class AggregatePaillier extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		Job job = new Job(conf); 
		job.setJobName("aggregate paillier"); 
		job.setJarByClass(AggregatePaillier.class);
		
		job.setMapperClass(AggregatePaillierMapper.class);
		job.setCombinerClass(AggregatePaillierReducer.class); 
		job.setReducerClass(AggregatePaillierReducer.class); 
		job.setOutputKeyClass(BytesWritable.class); 
		job.setOutputValueClass(BytesWritable.class);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		
		return job.waitForCompletion(true) ? 0 : 1;
	}

	//output (<encrypted sourceIP>,<Paillier encrypted revenue>)
	static class AggregatePaillierMapper extends Mapper<BytesWritable, BytesWritable, BytesWritable, BytesWritable>{						
		/* key = IV -> ignored
		 */				
		
		@Override
		public void map(BytesWritable key, BytesWritable value, Context context)
				throws IOException, InterruptedException {
			// sourceIP is the first 16 bytes
			byte[] sourceIP = new byte[16];
			byte[] ct = value.copyBytes();
			System.arraycopy(ct, 0, sourceIP, 0, 16);

			byte[] encryptedRev = new byte[256];
			System.arraycopy(ct, 16, encryptedRev, 0, 256);	
									
			context.write(new BytesWritable(sourceIP), new BytesWritable(
					encryptedRev));
		}
				
	}
	
	static class AggregatePaillierReducer extends Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable>{
		Paillier paillier; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 
			paillier = new Paillier();
			paillier.init_public_key(conf.get("pub.key")); 			
		}
		
		@Override
		public void reduce(BytesWritable key, Iterable<BytesWritable> values,
				Context context) throws IOException, InterruptedException {
			Iterator<BytesWritable> it = values.iterator();
			byte[] encryptedSum = it.next().copyBytes();
			while (it.hasNext()) {
				encryptedSum = paillier
						.add(encryptedSum, it.next().copyBytes());
			}
			context.write(key, new BytesWritable(encryptedSum));
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new AggregatePaillier(), args); 
	}
}
