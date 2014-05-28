package examples.pagerank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
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
import crypto.Rand;
import crypto.Switchable;

import examples.pagerank.PagerankNaive.MapStage1;
import examples.pagerank.PagerankNaive.MapStage2;
import examples.pagerank.PagerankNaive.PrCounters;
import examples.pagerank.PagerankNaive.RedStage1;
import examples.pagerank.PagerankNaive.RedStage2;

import tds1.encryptor.PagerankEncryptor;
import types.LargeNumber;
import types.PaillierCiphertext;
import types.pagerank.PRCiphertext;
import types.pagerank.PRCiphertextNormalized;
import types.pagerank.PRWritable;
import types.pagerank.PrefCiphertext;
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
 * + Map Output: node index, ObjectWritable:Text|PRCiphertext 
 * + Reduce Input: node index, {Text: neighbor}, PRCiphertextNormalized 
 * + Reduce Output: X = {(nb: neighbor, normalized pr)}
 * 
 * Stage 2: 
 * + Map Input: X + encrypted Pr vector + encrypted Pref vector 
 * + Map Output: X + encrypted Pr + encrypted Pref 
 * + Reduce Input: node index, {{normalized pr from neigbor: PRCiphertextNormalized}, PRCiphertext, PrefCiphertext} 
 * + Reduce Output: new encrypted Pr vector
 */
public class BaselinePagerank extends Configured implements Tool {

	protected static enum PrCounters { CONVERGE_CHECK }
	
	protected Path edge_path = null;
	protected Path encrypted_pr_path = null;
	protected Path encrypted_pref_path = null; 
	protected Path tempmv_path = null;
	protected Path output_path = null;
	protected int number_nodes = 0;
	protected int niteration = 32;
	protected int nreducers = 1;
	protected double converge_threshold = 0.0001; 
	static class MapStage1 extends Mapper<IntWritable, ObjectWritable, IntWritable, Text> {

		Rand rand; 
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();		
			rand = new Rand();
			rand.init(conf.get("key")); 
		}
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context)
				throws IOException, InterruptedException {
			Object obj = value.get(); 			
			Text neighbor = null; 
			if (obj instanceof Text){
				neighbor = (Text)obj;
				context.write(key, neighbor); 				
			}
			else if (obj instanceof PRCiphertext){
				//decrypt and compute the normalized
				byte[] ct = ((PRCiphertext)obj).getContent().copyBytes(); 
				byte[] iv = new byte[16];
				byte[] cipher = new byte[ct.length-16]; 
				System.arraycopy(ct, 0, iv, 0, 16);
				System.arraycopy(ct, 16, cipher, 0, ct.length-16); 							
				String prVal = new String(rand.decrypt_word_rnd(cipher, iv));
				context.write(key, new Text("s"+prVal));				 
			}					
		}
	}

	static class RedStage1 extends Reducer<IntWritable, Text, IntWritable, ObjectWritable> {
		int number_nodes = 0;
		Rand rand; 
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();
			number_nodes = Integer.parseInt(conf.get("number_nodes"));
			rand = new Rand();
			rand.init(conf.get("key")); 
			
			
			System.out.println("RedStage1: number_nodes = " + number_nodes);
		}

		@Override
		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Object obj; 			
			ArrayList<IntWritable> outedges = new ArrayList<IntWritable>();
			double prValue=0; 
			for (Text text:values){
				if (text.toString().startsWith("s")){
					prValue = Double.parseDouble(text.toString().substring(1)); 
				}
				else{
					outedges.add(new IntWritable(Integer.parseInt(text.toString())));
				}
			}
			double normVal = prValue/outedges.size(); 
			
			byte[] newIv = rand.randomBytes(16);
			byte[] newCt = rand.encrypt_word_cbc(normVal+"", newIv); 		
			byte[] toStore = new byte[newIv.length+newCt.length]; 
			System.arraycopy(newIv, 0, toStore, 0, 16);
			System.arraycopy(newCt, 0, toStore, 16, newCt.length); 
			
			for (IntWritable iw : outedges)		{		
				context.write(iw, new ObjectWritable(new PRCiphertextNormalized(toStore)));				
			}
		}
	}
	
	static class MapStage2 extends Mapper<IntWritable, ObjectWritable, IntWritable, Text>
    {
		Rand rand; 		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();			
			rand = new Rand();
			rand.init(conf.get("key")); 			
		}
				
		@Override
		public void map (IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException
		{
			Object obj = value.get(); 
			PRCiphertextNormalized prNorm; 
			
			if (obj instanceof PRCiphertextNormalized){
				prNorm = (PRCiphertextNormalized)obj; 
				byte[] ct = prNorm.getContent().copyBytes(); 
				byte[] iv = new byte[16];
				byte[] cipher = new byte[ct.length-16]; 
				System.arraycopy(ct, 0, iv, 0, 16);
				System.arraycopy(ct, 16, cipher, 0, ct.length-16); 							
				String prVal = new String(rand.decrypt_word_rnd(cipher, iv));
				context.write(key, new Text("c"+prVal));								
			}
			else if (obj instanceof PrefCiphertext){				
				byte[] ct = ((PrefCiphertext)obj).getContent().copyBytes(); 
				byte[] iv = new byte[16];
				byte[] cipher = new byte[ct.length-16]; 
				System.arraycopy(ct, 0, iv, 0, 16);
				System.arraycopy(ct, 16, cipher, 0, ct.length-16); 							
				String prVal = new String(rand.decrypt_word_rnd(cipher, iv));
				context.write(key, new Text("p"+prVal));
			}	
			else if (obj instanceof PRCiphertext){
				byte[] ct = ((PRCiphertext)obj).getContent().copyBytes(); 
				byte[] iv = new byte[16];
				byte[] cipher = new byte[ct.length-16]; 
				System.arraycopy(ct, 0, iv, 0, 16);
				System.arraycopy(ct, 16, cipher, 0, ct.length-16); 							
				String prVal = new String(rand.decrypt_word_rnd(cipher, iv));
				context.write(key, new Text("s"+prVal));
			}
		}
	}

    static class RedStage2 extends Reducer<IntWritable, Text, IntWritable, ObjectWritable>
    {
		int number_nodes = 0;	
		Rand rand; 
			
		double converge_threshold = 0;
		int change_reported = 0;
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration(); 
			number_nodes = Integer.parseInt(conf.get("number_nodes"));			
			converge_threshold = Double.parseDouble(conf.get("converge_threshold"));
			System.out.println("RedStage2: number_nodes = " + number_nodes);
			
			rand = new Rand();
			rand.init(conf.get("key")); 						
		}

		/* values: {PRCiphertextNormalized from neighbor, PRCiphertext for this node, PrefCiphertext for this node}
		 */
		public void reduce(IntWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			
			int i;
			double next_rank = 0;
			double previous_rank = 0;
			double mixing_c = 0.85; 
			double random_coeff=0; 
			
			PRCiphertextNormalized prNorm;
			PRCiphertext pr; 
			PrefCiphertext pref; 
			byte[] iv = new byte[16]; 
			byte[] ct; 
			
			for (Text text:values){
				String pt = text.toString(); 
				if (pt.startsWith("c"))
					next_rank+=Double.parseDouble(pt.substring(1)); 
				else if (pt.startsWith("s"))
					previous_rank = Double.parseDouble(pt.substring(1)); 
				else if (pt.startsWith("p"))
					random_coeff = (1-mixing_c)*Double.parseDouble(pt.substring(1)); 
			}
												
			next_rank = next_rank * mixing_c + random_coeff;

			//encrypt this next rank
			iv = rand.randomBytes(16);
			ct = rand.encrypt_word_rnd(next_rank+"", iv); 
			byte[] toStore = new byte[iv.length+ct.length]; 
			System.arraycopy(iv, 0, toStore, 0, 16);
			System.arraycopy(ct, 0, toStore, 16, ct.length); 
			
			context.write(key, new ObjectWritable(new PRCiphertext(toStore))); 
			
			//output.collect( key, new Text("v" + next_rank ) );


			if( change_reported == 0 ) {
				double diff = Math.abs(previous_rank-next_rank);

				if( diff > converge_threshold ) {
					context.getCounter(PrCounters.CONVERGE_CHECK).increment(1); 					
					change_reported = 1;
				}
			}												
		}
    }

    static class DecryptAndSortMapper extends Mapper<IntWritable, ObjectWritable, DoubleWritable, Text>{
    	Rand rand; 
    	
    	@Override
    	public void setup(Context context){
    		rand = new Rand();
    		rand.init(context.getConfiguration().get("key"));     		
    	}
    	
    	@Override
    	public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{    		
    		PRCiphertext pr = (PRCiphertext) value.get();
    		byte[] ct = pr.getContent().copyBytes(); 
    		byte[] iv = new byte[16];
    		byte[] cipher = new byte[ct.length-16]; 
    		System.arraycopy(ct, 0, iv, 0, 16);
    		System.arraycopy(ct, 16, cipher, 0, ct.length-16); 
    		double rank = Double.parseDouble(new String(rand.decrypt_word_rnd(cipher, iv))); 
    		context.write(new DoubleWritable(rank), new Text(key.toString()));     		    		
    	}
    }
    
    static class DecryptAndSortReducer extends Reducer<DoubleWritable, Text, Text, Text>{
    	@Override
    	public void reduce(DoubleWritable key, Iterable<Text> vals, Context context) throws IOException, InterruptedException{
    		for (Text val: vals)
    			context.write(new Text(key.get()+""), val); 
    	}
    }
    
	private void runStage1() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 			
		conf.set("number_nodes", "" + number_nodes);
		Job job = new Job(conf); 
		
		job.setJarByClass(BaselinePagerank.class); 
		job.setJobName("stage1");
		
		job.setMapperClass(MapStage1.class);        
		job.setReducerClass(RedStage1.class);
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class); 
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class); 
		
		FileInputFormat.setInputPaths(job, edge_path, encrypted_pr_path);  
		FileOutputFormat.setOutputPath(job, tempmv_path);  
		
		
		job.setNumReduceTasks( nreducers );

		job.waitForCompletion(true); 		
	}
	
	private long runStage2() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf();  
		conf.set("number_nodes", "" + number_nodes);
		conf.set("converge_threshold", "" + converge_threshold);
		Job job = new Job(conf); 
		
		job.setJarByClass(BaselinePagerank.class);
		job.setJobName("stage 2"); 
		job.setMapperClass(MapStage2.class);        
		job.setReducerClass(RedStage2.class);

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		
		FileInputFormat.setInputPaths(job, tempmv_path, encrypted_pr_path, encrypted_pref_path);  
		FileOutputFormat.setOutputPath(job, output_path);  

		job.setNumReduceTasks( nreducers );		
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ObjectWritable.class);		
		
		job.waitForCompletion(true); 
		
	Counter c = job.getCounters().findCounter(PrCounters.CONVERGE_CHECK);
	
	return c.getValue(); 
						
	}
	
	private void runDecryptAndSort() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		job.setJarByClass(BaselinePagerank.class);
		job.setMapperClass(DecryptAndSortMapper.class);
		job.setReducerClass(DecryptAndSortReducer.class);
		job.setNumReduceTasks(1); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(DoubleWritable.class); 
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
				
		converge_threshold = ((double)1.0/(double) number_nodes)/10;
	
		int cur_iteration = 1; 
	
		
		System.out.println("[PEGASUS] Computing PageRank. Max iteration = " +niteration + "\n");
		

		final FileSystem fs = FileSystem.get(getConf());
		
		// Run pagerank until converges. 
		for (int i = cur_iteration; i <= niteration; i++) {
			this.runStage1(); 
			long changed = this.runStage2(); 

			// The counter is newly created per every iteration.			
			System.out.println("Iteration = " + i);
			
			if( changed == 0 ) {
							System.out.println("PageRank vector converged. Now preparing to finish...");
							fs.delete(this.encrypted_pr_path);
							fs.delete(this.tempmv_path);
							fs.rename(this.output_path, this.encrypted_pr_path);
							break;
			}

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
		ToolRunner.run(new BaselinePagerank(), args);
	}
}
