package tds1.encryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import types.pagerank.EdgeCiphertext;
import types.pagerank.PRCiphertext;
import types.pagerank.PRWritable;
import types.pagerank.PrefCiphertext;
import types.pagerank.PrefWritable;
import crypto.Elgamal;
import crypto.Ope;
import crypto.Paillier;
import crypto.Rand;

import org.apache.hadoop.io.ObjectWritable;

/**
 * Encrypt 3 vectors: pagerank, preference and graph 
 * 
 * 1. Pagerank vector:
 * Input: <node index> <pr>
 * Output: <node index> <E(pr):
 * <Int, RND>
 * 
 * 2. Preference vector:
 * Input: <node index> <pref>
 * Output: <node index> <E(pref)>
 * <Int, RND>
 * 
 * 3. Edge vector:
 * Input: <x> <y>
 * Output: <random index> <E(x y)>
 * <Int, RND>
 */
public class BaselinePagerankEncryptorEG extends Configured implements Tool{

	public static String NUM_NODES = "num.nodes"; 
		
	//read in <offset> <line: index || pr> 
	//        <offset> <line: index || neighbour> 
	static class EncryptPrMapper extends Mapper<IntWritable, ObjectWritable, BytesWritable, ObjectWritable> {
		Rand rand; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 
			rand = new Rand(); 
			rand.init(conf.get("key")); 
		}
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
			Text val = (Text)value.get();	
			String pt = key.toString()+"\t"+val.toString().substring(1); 
			byte[] iv = rand.randomBytes(16); 
			byte[] ct = rand.encrypt_word_rnd(pt, iv); 			
			context.write(new BytesWritable(iv), new ObjectWritable(new PRCiphertext(ct)));						
		}
	}			
		
	/**
	 * reading both edges and pref vector, 
	 * output <e1><e2> <p<v>>
	 */
	static class EncryptPrefMapper extends Mapper<IntWritable, ObjectWritable, BytesWritable, ObjectWritable>{
		
		Rand rand; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 
			rand = new Rand(); 
			rand.init(conf.get("key")); 
		}
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context)
				throws IOException, InterruptedException {
			Text val = (Text) value.get();
			String pt = key.get() + "\t" + val.toString().substring(1);
			byte[] iv = rand.randomBytes(16);
			byte[] ct = rand.encrypt_word_rnd(pt, iv);
			context.write(new BytesWritable(iv), new ObjectWritable(
					new PrefCiphertext(ct)));
		}
	}
	
	static class EncryptEdgesMapper extends Mapper<IntWritable, ObjectWritable, BytesWritable, ObjectWritable>{
		
		Rand rand;
		int c=0; 
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 
			rand = new Rand(); 
			rand.init(conf.get("key"));
			c=0; 
		}
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
			int x = key.get();
			Text val = (Text) value.get();
			int y = Integer.parseInt(val.toString());
			String pt = x + "\t" + y;

			byte[] iv = rand.randomBytes(16);
			byte[] ct = rand.encrypt_word_rnd(pt, iv);
			context.write(new BytesWritable(iv), new ObjectWritable(
					new EdgeCiphertext(ct)));	
		}
	}
			
	@Override
	public int run(String[] args) throws Exception {		 				
		Path prInput = new Path(args[0]); 
		Path prefInput = new Path(args[1]); 
		Path edgesInput = new Path(args[2]); 
		Path prOutput = new Path(args[3]); 
		Path prefOutput = new Path(args[4]); 
		Path edgeOutput = new Path(args[5]); 
		
		//3. Encrypt pr vector
		//4. Encrypt preference vector
		this.encryptPr(prInput, prOutput);
		this.encryptPref(prefInput, prefOutput);
		this.encryptEdge(edgesInput, edgeOutput); 
		return 0;
	}
 
	private void encryptPr(Path inputPr, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(this.getConf());
		job.setJobName("encrypting Pagerank vector");
		job.setJarByClass(BaselinePagerankEncryptorEG.class);
		job.setMapperClass(EncryptPrMapper.class);	
		job.setNumReduceTasks(0); 
		
		FileInputFormat.setInputPaths(job, inputPr);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		job.waitForCompletion(true); 
	}
	
	private void encryptPref(Path inputPref, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(this.getConf());
		job.setJobName("encrypting Preference vector");
		job.setJarByClass(BaselinePagerankEncryptorEG.class);
		job.setMapperClass(EncryptPrefMapper.class);
		job.setNumReduceTasks(0);
		
		FileInputFormat.setInputPaths(job, inputPref);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		
		job.waitForCompletion(true); 
	}
	
	private void encryptEdge(Path edgeInput, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(this.getConf());
		job.setJobName("encrypting Edge vector");
		job.setJarByClass(BaselinePagerankEncryptorEG.class);
		job.setMapperClass(EncryptEdgesMapper.class);		
		job.setNumReduceTasks(0);
		
		FileInputFormat.setInputPaths(job, edgeInput);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		
		job.waitForCompletion(true); 
	}
	
	/**
	 * PageRankEncryptor <pr vector input> <pref vector input> <edge input> <pr vector out> <pref vector out> 
	 */
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new BaselinePagerankEncryptorEG(), args); 
	}
}
