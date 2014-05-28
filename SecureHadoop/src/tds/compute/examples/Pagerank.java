package tds.compute.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;


import org.apache.hadoop.mapreduce.Counter;
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

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.encode.examples.PRWritable;
import tds.encode.examples.PagerankEncryptor;
import tds.encode.examples.PrefWritable;
import tds.hom.Elgamal;
import tds.hom.Paillier;
import tds.hom.switchable.Switchable;
import tds.io.Ciphertext;
import tds.io.EncryptedDenseVector;
import tds.io.LargeNumber;


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
				else{ //EncryptedDenseVector
					pr = (PRWritable)obj; 
				}
			}			
			for (IntWritable iw : outedges){				
				context.write(iw, new ObjectWritable(pr.getPagerankNormalized()));
			}
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
		Ciphertext prConst; 
		
		@Override
		public void setup(Context context) throws IOException {
			Configuration conf = context.getConfiguration(); 
			number_nodes = Integer.parseInt(conf.get(TdsOptions.NUM_NODES));						
			elgamal = new Elgamal(); 
			paillier = new Paillier(); 
			elgamal.initPublicParameters(Utils.getElgamalPub(conf));
			elgamal.initPrivateParameters(Utils.getElgamalPriv(conf)); 
			paillier.initPublicParameters(Utils.getPaillierPub(conf));
			paillier.initPrivateParameters(Utils.getPaillierPriv(conf)); 															
						
			switchable = new Switchable(paillier, elgamal); 				
			prConst = PagerankEncryptor.getPRConst(elgamal, number_nodes); 
		}

		/* values: {PaillierCiphertext from neighbor, PRWritable for this node, PrefWritable for this node}
		 */
		public void reduce(IntWritable key, Iterable<ObjectWritable> values,
				Context context) throws IOException, InterruptedException {
						
			Ciphertext sum = null; 			
			PRWritable pr = null;
			PrefWritable pref = null; 
			
			boolean first = true; 
			for (ObjectWritable o: values){
				Object obj = o.get(); 
				if (obj instanceof PRWritable){
					pr = (PRWritable)obj;  
				}
				else if (obj instanceof PrefWritable){
					pref = (PrefWritable) obj; 
				}
				else{ //PaillierCiphertext
					Ciphertext pct = (Ciphertext)obj; 					
					if (first){						
						sum = pct;
						first = false; 
					}
					else{						 					
						sum = ((Paillier)paillier).add(sum, pct); 
					}					
				}
			}
			
			
			//multiply with factor
			//byte[] mul = switchable.addToMul(sum); //switchable.switchToMultiplicative(sum);				
			Ciphertext mul = switchable.addToMul(sum); 
			context.getCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_ELG).increment(1); 
					//new Ciphertext(switchable.addToMul(sum.getContent().copyBytes()));
			
			Ciphertext val1 = elgamal.product(mul, this.prConst); 						
			
			Ciphertext val1ToSum = switchable.mulToAdd(val1);  
			context.getCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_PAL).increment(1); 
			
			Ciphertext newPr = paillier.add(val1ToSum, pref.getPref()); 			
			
			//normalized pr 
			Ciphertext val2 = elgamal.product(mul, pr.getPagerankConstNormalized()); 
			
			Ciphertext val2ToSum = switchable.mulToAdd(val2); 
			context.getCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_PAL).increment(1); 
			 
			Ciphertext normPr = paillier.add(val2ToSum, pref.getPrefNormalized());
			
			
			PRWritable content = new PRWritable(newPr, normPr,
					pr.getPagerankConstNormalized());  
			
			context.getCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH).increment(3); 
			
			context.write(key, new ObjectWritable(content)); 						
		}
    }

    static class DecryptAndSortMapper extends Mapper<IntWritable, ObjectWritable, LargeNumber, Text>{
    	Paillier paillier; 
    	@Override
    	public void setup(Context context){
    		Configuration conf = context.getConfiguration(); 
    		paillier = new Paillier();
    		paillier.initPublicParameters(Utils.getPaillierPub(conf));
    		paillier.initPrivateParameters(Utils.getPaillierPriv(conf));     					    		
    	}
    	
    	@Override
    	public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{
    		PRWritable pr = (PRWritable) value.get();    		
    		String pt = paillier.decryptToText(pr.getPagerank().getContent().copyBytes()); 
    		context.write(new LargeNumber(pt), new Text(key.toString()));
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
		conf.set(TdsOptions.NUM_NODES, "" + number_nodes);
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
		System.out.println("Stage 1: "); 
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue()); 
		c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_ELG); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue());
		c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_PAL); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue());
	}
	
	private void runStage2() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf();  
		conf.set(TdsOptions.NUM_NODES, "" + number_nodes);
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
		
		System.out.println("Stage 2: "); 
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue()); 
		c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_ELG); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue());
		c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.SWITCH_TO_PAL); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue());
	}
	
	private void runDecryptAndSort(Path verificationPath) throws IOException, ClassNotFoundException, InterruptedException{
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
		FileOutputFormat.setOutputPath(job, verificationPath);
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
		
		long startTime = System.currentTimeMillis(); 
		//finally, decrypt and sort
		this.runDecryptAndSort(new Path(args[7])); 
		long endTime = System.currentTimeMillis(); 
		System.out.println("Decryption time = "+(endTime-startTime)); 
		return 0;
	}

	/**
	 * args: <n> <nReducers> <nIterations> <edge path> <pr path> <pref path> <output path> <verification path>
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new Pagerank(), args);
	}
}
