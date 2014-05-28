package tds.encode.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
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

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Rand;
import tds.hom.Elgamal;
import tds.hom.Paillier;
import tds.io.Ciphertext;
import tds.io.EncryptedDenseVector;

/**
 * Encrypt 2 vectors: pagerank and preference
 * 
 * 1. Pagerank vector:
 * Input: <node index> <pr>
 * Output: <node index> <E(pr):
 * <Int, Paillier>
 * 
 * TH: <Int, RND>
 * 
 * 2. Preference vector:
 * Input: <node index> <pref>
 * Output: <node index> <E(pref*0.15*N)> <E(pref*0.15*N/|O|)> <E(0.85*N)> <E(0.85*N/|O|)>
 * <Int, <Paillier, Paillier, Elgamal, Elgamal>>
 * 
 * TH: <Int, RND>
 */
public class PagerankEncryptor extends Configured implements Tool{
		
	public static Ciphertext getPRConst(ICrypto mul, int n) throws IOException{		
		int val = (int)(0.85*n*n*n);
		return new Ciphertext(mul.encryptString(val+"")); //factor 					
	}
	
	
	//read in <offset> <line: index || pr>  (ONLY THIS FOR THE TH VERSION)
	//        <offset> <line: index || neighbour> 
	static class EncryptPrMapper extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		
		ICrypto rnd;
		boolean isHom; 
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration(); 			
			this.isHom = conf.getBoolean(TdsOptions.HOM_OPTION, false);
			if (!this.isHom) {
				List<String> params = new ArrayList<String>();
				params.add(conf.get(TdsOptions.KEY_OPTION));
				this.rnd = new Rand();
				this.rnd.initPrivateParameters(params); 
			}
		}
		
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
			ObjectWritable ow = value; 			
			if (!this.isHom){
				Text t = (Text)value.get(); 
				Ciphertext ct = new Ciphertext(this.rnd.encryptString(t.toString()));
				ow = new ObjectWritable(ct); 
			}			
			
			context.write(key, ow);						
		}
	}
	
	//Applicable to HOM option only
	static class EncryptPrReducer extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		ICrypto add, mul; 		 
		int n; 		
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();  
			n = conf.getInt(TdsOptions.NUM_NODES, 0); 
			
			this.mul = new Elgamal();
			this.add = new Paillier();
			this.add.initPublicParameters(Utils.getPaillierPub(conf));
			this.add.initPrivateParameters(Utils.getPaillierPriv(conf)); 
			this.mul.initPublicParameters(Utils.getElgamalPub(conf));
			this.mul.initPrivateParameters(Utils.getElgamalPriv(conf)); 					
		}
		
		
		@Override
		public void reduce(IntWritable key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException{
			int degree=0;
			int pr=0; 			
			for (ObjectWritable o : values){
				Text t = (Text)o.get(); 
				if (!t.toString().startsWith("v")){
					degree++; 
				}
				else{
					pr = new Integer(t.toString().substring(1)).intValue(); 					
				}
			}		
					
			int val = (int)(0.85*n*n*n);			
						
			ObjectWritable prObj = new ObjectWritable(new PRWritable(
					new Ciphertext(this.add.encryptString(pr + "")),
					new Ciphertext(this.add.encryptString((pr / degree) + "")),
					new Ciphertext(mul.encryptString((val / degree) + "")))); 
			context.write(key, prObj); 		
						
		}
	}
	
		
	/**
	 * reading both edges and pref vector (or only pref vector for TH version) 
	 * output <e1><e2> <p<v>>
	 */
	static class EncryptPrefMapper extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable>{
		ICrypto rand;
		boolean isHom; 
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();
			this.isHom = conf.getBoolean(TdsOptions.HOM_OPTION, false); 
			if (!this.isHom) {
				rand = new Rand();
				List<String> params = new ArrayList<String>();
				params.add(conf.get(TdsOptions.KEY_OPTION));
				rand.initPrivateParameters(params);
			}
		}
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
			ObjectWritable ow = value; 
			if (!this.isHom){
				Text t = (Text)value.get(); 
				Ciphertext ct = new Ciphertext(this.rand.encryptString(t.toString()));
				ow = new ObjectWritable(ct); 
			}
			
			context.write(key,ow);
		}
	}
	
	/**
	 * input: <i> [e1 e2 ... ] [p<v>] //For the HOM case only
	 */
	static class EncryptPrefReducer extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable>{
		
		ICrypto add;  	
		int n; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();  
			n = conf.getInt(TdsOptions.NUM_NODES, 0); 			
			add = new Paillier(); 
			add.initPublicParameters(Utils.getPaillierPub(conf));
			add.initPrivateParameters(Utils.getPaillierPriv(conf)); 			 			
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
			int val1 = (int)(0.15*n*n*n*pref); 
			int val2 = val1/outdegree;			
			
			ObjectWritable prefObj = new ObjectWritable(new PrefWritable(
					new Ciphertext(this.add.encryptString(val1 + "")),
					new Ciphertext(this.add.encryptString(val2 + "")))); 
			
			context.write(key, prefObj);
		}
	}
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();  
		ICrypto add, mul; 
		add = new Paillier();
		add.keyGen(conf.get(TdsOptions.PAILLIER_PUBLIC_KEY_FILE), conf.get(TdsOptions.PAILLIER_PRIVATE_KEY_FILE)); 
		mul = new Elgamal();
		mul.keyGen(conf.get(TdsOptions.ELGAMAL_PUBLIC_KEY_FILE), conf.get(TdsOptions.ELGAMAL_PRIVATE_KEY_FILE)); 		
		
		int n = Integer.parseInt(args[0]);  
		conf.set(TdsOptions.NUM_NODES, n+"");
		
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
		Configuration conf = this.getConf(); 
		Job job = new Job(conf);
		job.setJobName("encrypting Pagerank vector");
		job.setJarByClass(PagerankEncryptor.class);
		job.setMapperClass(EncryptPrMapper.class);		 
		
		if (conf.getBoolean(TdsOptions.HOM_OPTION, false)){
			FileInputFormat.setInputPaths(job, inputPr, inputEdges);
			job.setReducerClass(EncryptPrReducer.class);
			job.setNumReduceTasks(nReducers); 
		}
		else{
			FileInputFormat.setInputPaths(job, inputPr);
			job.setNumReduceTasks(0);
		}
		
		FileOutputFormat.setOutputPath(job, output);
		
		job.setInputFormatClass(SequenceFileInputFormat.class);		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class);
		job.waitForCompletion(true); 
	}
	
	private void encryptPref(int n, int nReducers, Path inputPref, Path inputEdges, Path output) throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 
		Job job = new Job(conf);
		job.setJobName("encrypting Preference vector");
		job.setJarByClass(PagerankEncryptor.class);
		job.setMapperClass(EncryptPrefMapper.class);		
		
		
		if (conf.getBoolean(TdsOptions.HOM_OPTION, false)){
			FileInputFormat.setInputPaths(job, inputPref, inputEdges);
			job.setReducerClass(EncryptPrefReducer.class);
			job.setNumReduceTasks(nReducers);
		}
		else{
			FileInputFormat.setInputPaths(job, inputPref);
			job.setNumReduceTasks(0);
		}
		
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
