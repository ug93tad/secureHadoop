package examples.pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import crypto.Elgamal;
import crypto.Paillier;
import crypto.Switchable;

import examples.pagerank.PagerankNaive.MapStage1;
import examples.pagerank.PagerankNaive.MapStage2;
import examples.pagerank.PagerankNaive.PrCounters;
import examples.pagerank.PagerankNaive.RedStage1;
import examples.pagerank.PagerankNaive.RedStage2;

import tds1.encryptor.PagerankEncryptor;
import types.LargeNumber;
import types.PaillierCiphertext;
import types.pagerank.PRWritable;
import types.pagerank.PrefWritable;

/**
 * Modified implementation of HiBench's NaivePagerank implementation, including
 * 2 stages:
 * 
 * Inputs: edge vector, encrypted Pr vector, encrypted Preference vector. makesymmetric = false
 * 
 * Stage 1:
 * 
 * + Map Input: edge vector, encrypted Pr vector 
 * + Map Output: node index, ObjectWritable:Text|PrWritable 
 * + Reduce Input: node index, {Text: neighbor}, PrWritable 
 * + Reduce Output: X = {(nb: neighbor, normalized pr)}
 * 
 * Stage 2: 
 * + Map Input: X + encrypted Pr vector + encrypted Pref vector 
 * + Map Output: X + encrypted Pr + encrypted Pref 
 * + Reduce Input: node index, {{normalized pr from neigbor: PaillierCiphertext}, PrWritable, PrefWritable} 
 * + Reduce Output: new encrypted Pr vector
 */
public class Pagerank extends Configured implements Tool {

	protected Path edge_path = null;
	protected Path encrypted_pr_path = null;
	protected Path encrypted_pref_path = null; 
	protected Path tempmv_path = null;
	protected Path output_path = null;
	protected int number_nodes = 0;
	protected int niteration = 32;
	protected int nreducers = 1;

	static class MapStage1 extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {

		@Override
		public void map(IntWritable key, ObjectWritable value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value); 						
		}
	}

	static class RedStage1 extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		int number_nodes = 0;

		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			number_nodes = Integer.parseInt(conf.get("number_nodes"));
			
			System.out.println("RedStage1: number_nodes = " + number_nodes);
		}

		@Override
		public void reduce(IntWritable key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException {
			Object obj; 
			ArrayList<IntWritable> outedges = new ArrayList<IntWritable>();
			PRWritable pr = null; 
			for (ObjectWritable o : values){
				obj = o.get();
				if (obj instanceof Text){//edge
					String idx = ((Text)obj).toString(); 
					outedges.add(new IntWritable(new Integer(idx).intValue()));
				}
				else{ //PrWritable
					pr = (PRWritable)obj; 
				}
			}			
			for (IntWritable iw : outedges)				
				context.write(iw, new ObjectWritable(pr.getPagerankNormalized())); 		
		}
	}
	
	static class MapStage2 extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable>
    {
		// Identity mapper
		public void map (IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException
		{
			context.write(key, value); 
		}
	}

    static class RedStage2 extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable>
    {
		int number_nodes = 0;	
		Paillier paillier; 
		Elgamal elgamal; 
		Switchable switchable; 
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration(); 
			number_nodes = Integer.parseInt(conf.get("number_nodes"));			

			System.out.println("RedStage2: number_nodes = " + number_nodes);
			
			elgamal = new Elgamal(); 
			elgamal.init_public_key(conf.get(PagerankEncryptor.ELGAMAL_PUB_FILE));			
			elgamal.init_private_key(conf.get(PagerankEncryptor.ELGAMAL_PRIV_FILE));	
			
			paillier = new Paillier(); 
			paillier.init_public_key(conf.get(PagerankEncryptor.PAILLIER_PUB_FILE));
			paillier.init_private_key(conf.get(PagerankEncryptor.PAILLIER_PRIV_FILE));
			
			switchable = new Switchable(); 
			switchable.init(conf.get(PagerankEncryptor.PAILLIER_PUB_FILE),
					conf.get(PagerankEncryptor.PAILLIER_PRIV_FILE),
					conf.get(PagerankEncryptor.ELGAMAL_PUB_FILE),
					conf.get(PagerankEncryptor.ELGAMAL_PRIV_FILE));
			//TODO: initialize paillier and elgamal and switchable
		}

		/* values: {PaillierCiphertext from neighbor, PRWritable for this node, PrefWritable for this node}
		 */
		public void reduce(IntWritable key, Iterable<ObjectWritable> values,
				Context context) throws IOException, InterruptedException {
			
			PRWritable pr = null;
			PrefWritable pref = null;
			byte[] sum = null; 
			byte[] temp = null; 
			boolean first = true; 
			int count = 0; 
			for (ObjectWritable o: values){
				Object obj = o.get();
				count++; 
				
				if (obj instanceof PRWritable){
					pr = (PRWritable) obj; 
				}
				else if (obj instanceof PrefWritable){
					pref = (PrefWritable) obj; 
				}
				else{ //PaillierCiphertext
					PaillierCiphertext pct = (PaillierCiphertext)obj; 
					if (first){						
						sum = pct.getContent().copyBytes();
						first = false;  						 
					}
					else{
						temp = pct.getContent().copyBytes(); 						
						sum = paillier.add(sum, temp); 
					}
				}
			}
			
			
			//multiply with factor
			byte[] mul = switchable.addToMul(sum); //switchable.switchToMultiplicative(sum);			
			
			//pr 
			byte[] val1 = elgamal.multiply(mul, pr.getPagerankConst().getContent().copyBytes());
			
			byte[] val1ToSum = switchable.mulToAdd(val1);//switchable.switchToAdditive(val1); 
			byte[] newPr = paillier.add(val1ToSum, pref.getPref().getContent().copyBytes()); 
			
			
			//normalized pr 
			byte[] val2 = elgamal.multiply(mul, pr.getPagerankConstNormalized().getContent().copyBytes()); 
			
			byte[] val2ToSum = switchable.mulToAdd(val2); //switchable.switchToAdditive(val2); 
			byte[] normPr = paillier.add(val2ToSum, pref.getPrefNormalized().getContent().copyBytes());
			
			
			PRWritable content = new PRWritable(newPr, normPr, pr
					.getPagerankConst().getContent().copyBytes(), pr
					.getPagerankConstNormalized().getContent().copyBytes());  
			
			context.write(key, new ObjectWritable(content)); 						
		}
    }

    static class DecryptAndSortMapper extends Mapper<IntWritable, ObjectWritable, LargeNumber, Text>{
    	Paillier paillier; 
    	@Override
    	public void setup(Context context){
    		paillier = new Paillier();
    		paillier.init_public_key(context.getConfiguration().get(PagerankEncryptor.PAILLIER_PUB_FILE));
    		paillier.init_private_key(context.getConfiguration().get(PagerankEncryptor.PAILLIER_PRIV_FILE));
    	}
    	
    	@Override
    	public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
    		PRWritable pr = (PRWritable) value.get();
    		//byte[] bpt = paillier.decryptRaw(pr.getPagerank().getContent().copyBytes());
    		String pt = paillier.decryptToString(pr.getPagerank().getContent().copyBytes()); 
    		//context.write(new BytesWritable(bpt), new Text(key.toString()+" : "+pt));
    		context.write(new LargeNumber(pt), new Text(key.toString()));
    		//context.write(new Text(pt), key);
    	}
    }
    
    static class DecryptAndSortReducer extends Reducer<LargeNumber, Text, Text, Text>{
    	@Override
    	public void reduce(LargeNumber key, Iterable<Text> vals, Context context) throws IOException, InterruptedException{
    		for (Text val: vals)
    			context.write(key.getContent(), val); 
    	}
    }
    
	private void runStage1() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 			
		conf.set("number_nodes", "" + number_nodes);
		Job job = new Job(conf); 
		
		job.setJarByClass(Pagerank.class); 
		job.setJobName("stage1");
		
		job.setMapperClass(MapStage1.class);        
		job.setReducerClass(RedStage1.class);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class); 
		
		FileInputFormat.setInputPaths(job, edge_path, encrypted_pr_path);  
		FileOutputFormat.setOutputPath(job, tempmv_path);  
		
		
		job.setNumReduceTasks( nreducers );

		job.waitForCompletion(true); 		
	}
	
	private void runStage2() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf();  
		conf.set("number_nodes", "" + number_nodes);
		Job job = new Job(conf); 
		
		job.setJarByClass(Pagerank.class);
		job.setJobName("stage 2"); 
		job.setMapperClass(MapStage2.class);        
		job.setReducerClass(RedStage2.class);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		
		FileInputFormat.setInputPaths(job, tempmv_path, encrypted_pr_path, encrypted_pref_path);  
		FileOutputFormat.setOutputPath(job, output_path);  

		job.setNumReduceTasks( nreducers );

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class);		
		
		job.waitForCompletion(true); 
	}
	
	private void runDecryptAndSort() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		job.setJarByClass(Pagerank.class);
		job.setMapperClass(DecryptAndSortMapper.class);
		job.setReducerClass(DecryptAndSortReducer.class);
		job.setNumReduceTasks(1); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(LargeNumber.class); 
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(Text.class); 
				
		FileInputFormat.setInputPaths(job, this.encrypted_pr_path);
		FileOutputFormat.setOutputPath(job, new Path("/encrypted_pr_check"));
		job.waitForCompletion(true); 
	}
	
	@Override
	public int run(String[] args) throws Exception {		
		this.number_nodes = new Integer(args[0]).intValue(); 
		this.nreducers = new Integer(args[1]).intValue(); 
		this.niteration = Integer.parseInt(args[2]);
		edge_path = new Path(args[3]);
		this.encrypted_pr_path = new Path(args[4]); 
		this.encrypted_pref_path = new Path(args[5]); 
		this.tempmv_path = new Path("/pr_tmp"); 
		this.output_path = new Path(args[6]);
				
		
	
		int cur_iteration = 1; 
	
		
		System.out.println("[PEGASUS] Computing PageRank. Max iteration = " +niteration + "\n");
		

		final FileSystem fs = FileSystem.get(getConf());
		
		// Run pagerank until converges. 
		for (int i = cur_iteration; i <= niteration; i++) {
			this.runStage1(); 
			this.runStage2(); 

			// The counter is newly created per every iteration.			
			System.out.println("Iteration = " + i);
			

			// rotate directory
			fs.delete(this.encrypted_pr_path);
			fs.delete(this.tempmv_path);
			fs.rename(this.output_path, this.encrypted_pr_path);
		}		
		
		//finally, decrypt and sort
		this.runDecryptAndSort(); 
		return 0;
	}

	/**
	 * args: <n> <nReducers> <nIterations> <edge path> <pr path> <pref path> <output path>
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Pagerank(), args);
	}
}
