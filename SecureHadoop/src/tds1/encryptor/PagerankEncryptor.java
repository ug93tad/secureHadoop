package tds1.encryptor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
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

import types.pagerank.PRWritable;
import types.pagerank.PrefWritable;
import crypto.Elgamal;
import crypto.Ope;
import crypto.Paillier;
import org.apache.hadoop.io.ObjectWritable;

/**
 * Encrypt 2 vectors: pagerank and preference
 * 
 * 1. Pagerank vector:
 * Input: <node index> <pr>
 * Output: <node index> <E(pr):
 * <Int, Paillier>
 * 
 * 2. Preference vector:
 * Input: <node index> <pref>
 * Output: <node index> <E(pref*0.15*N)> <E(pref*0.15*N/|O|)> <E(0.85*N)> <E(0.85*N/|O|)>
 * <Int, <Paillier, Paillier, Elgamal, Elgamal>>
 */
public class PagerankEncryptor extends Configured implements Tool{

	public static String PAILLIER_PUB_FILE = "paillier.pub.key"; 
	public static String PAILLIER_PRIV_FILE = "paillier.priv.key";
	public static String ELGAMAL_PUB_FILE = "elgamal.pub.key";
	public static String ELGAMAL_PRIV_FILE = "elgamal.priv.key"; 
	public static String NUM_NODES = "num.nodes"; 
	
	Paillier paillier; 
	Elgamal elgamal; 
	
	//read in <offset> <line: index || pr> 
	//        <offset> <line: index || neighbour> 
	static class EncryptPrMapper extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
						
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
			
			context.write(key, value);						
		}
	}
	
	static class EncryptPrReducer extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		Paillier paillier; 
		Elgamal elgamal; 
		int n; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();  
			n = conf.getInt(NUM_NODES, 0); 
			
			elgamal = new Elgamal(); 
			elgamal.init_public_key(conf.get(ELGAMAL_PUB_FILE));
			elgamal.init_private_key(conf.get(ELGAMAL_PRIV_FILE));
			
			paillier = new Paillier(); 
			paillier.init_public_key(conf.get(PAILLIER_PUB_FILE));
			paillier.init_private_key(conf.get(PAILLIER_PRIV_FILE));
		}
		
		@Override
		public void reduce(IntWritable key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException{
			int degree=0;
			int pr=0; 			
			for (ObjectWritable o: values){
				Text t = (Text)o.get(); 
				if (!t.toString().startsWith("v")){
					degree++; 
				}
				else{
					pr = new Integer(t.toString().substring(1)).intValue(); 					
				}
			}
			byte[] original = paillier.encrypt(pr); 
			byte[] originalNorm = paillier.encrypt(pr/degree); 
			int val = (int)(0.85*n*n*n);
			byte[] factor = elgamal.encrypt(val); 
			byte[] factorNorm = elgamal.encrypt(val/degree); 
			PRWritable prVector = new PRWritable(original, originalNorm, factor, factorNorm); 			
			context.write(key, new ObjectWritable(prVector)); 		
			
			System.out.println("node "+key.get()+" : "+paillier.decryptToString(original)+" "+paillier.decryptToString(originalNorm)+" d = "+degree);
		}
	}
	
		
	/**
	 * reading both edges and pref vector, 
	 * output <e1><e2> <p<v>>
	 */
	static class EncryptPrefMapper extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable>{
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{			
			context.write(key,value);
		}
	}
	
	/**
	 * input: <i> [e1 e2 ... ] [p<v>]
	 */
	static class EncryptPrefReducer extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable>{
		
		Paillier paillier; 	
		int n; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();  
			n = conf.getInt(NUM_NODES, 0); 			
			
			paillier = new Paillier(); 
			paillier.init_public_key(conf.get(PAILLIER_PUB_FILE));
			paillier.init_private_key(conf.get(PAILLIER_PRIV_FILE));
		}
		
		@Override
		public void reduce(IntWritable key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException{
			int outdegree = 0; 
			int pref=0; 
			for (ObjectWritable o:values){
				Text t = (Text)o.get(); 
				if (!t.toString().startsWith("p"))
					outdegree++;
				else
					pref = new Integer(t.toString().substring(1)).intValue(); 
			}
			if (pref==0)
				System.out.println("NO PREFERENCE VALUE");
			int val1 = (int)(0.15*n*n*n*pref); 
			int val2 = val1/outdegree;
			System.out.println("node pref "+key.get()+" : "+val1+" "+val2+" d = "+outdegree+" n = "+n+" pref = "+pref);
			byte[] vals1 = paillier.encrypt(val1); 
			byte[] vals2 = paillier.encrypt(val2);
			PrefWritable prefVector = new PrefWritable(vals1, vals2); 			
			context.write(key, new ObjectWritable(prefVector));
		}
	}
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();  
		System.out.println("pub file = "+conf.get(PAILLIER_PUB_FILE));
		//1. Init Paillier
		this.paillier = new Paillier(); 
		paillier.keygen(conf.get(PAILLIER_PUB_FILE), conf.get(PAILLIER_PRIV_FILE));
		
		//2. Init Elgamal
		this.elgamal = new Elgamal(); 
		elgamal.keygen(conf.get(ELGAMAL_PUB_FILE), conf.get(ELGAMAL_PRIV_FILE));
		
		int n = new Integer(args[0]).intValue(); 
		conf.set(NUM_NODES, n+"");
		
		int nReducers = new Integer(args[1]).intValue(); 
		Path prInput = new Path(args[2]); 
		Path prefInput = new Path(args[3]); 
		Path edgesInput = new Path(args[4]); 
		Path prOutput = new Path(args[5]); 
		Path prefOutput = new Path(args[6]); 
		
		//3. Encrypt pr vector
		//4. Encrypt preference vector
		this.encryptPr(n, nReducers, prInput, edgesInput, prOutput);
		this.encryptPref(n, nReducers, prefInput, edgesInput, prefOutput);
		return 0;
	}
 
	private void encryptPr(int n, int nReducers, Path inputPr, Path inputEdges, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(this.getConf());
		job.setJobName("encrypting Pagerank vector");
		job.setJarByClass(PagerankEncryptor.class);
		job.setMapperClass(EncryptPrMapper.class);
		job.setReducerClass(EncryptPrReducer.class);
		job.setNumReduceTasks(nReducers); 
		
		FileInputFormat.setInputPaths(job, inputPr, inputEdges);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		job.waitForCompletion(true); 
	}
	
	private void encryptPref(int n, int nReducers, Path inputPref, Path inputEdges, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(this.getConf());
		job.setJobName("encrypting Preference vector");
		job.setJarByClass(PagerankEncryptor.class);
		job.setMapperClass(EncryptPrefMapper.class);
		job.setReducerClass(EncryptPrefReducer.class);
		job.setNumReduceTasks(nReducers);
		
		FileInputFormat.setInputPaths(job, inputPref, inputEdges);
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		
		job.waitForCompletion(true); 
	}
	/**
	 * PageRankEncryptor <n> <nReducers> <pr vector input> <pref vector input> <edge input> <pr vector out> <pref vector out> 
	 */
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new PagerankEncryptor(), args); 
	}
}
