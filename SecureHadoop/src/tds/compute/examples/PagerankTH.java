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

import tds.TdsOptions;
import tds.common.Rand;
import tds.io.Ciphertext;


public class PagerankTH extends Configured implements Tool {

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
	static class MapStage1 extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		
		@Override
		public void map(IntWritable key, ObjectWritable value, Context context)
				throws IOException, InterruptedException {
			context.write(key, value); 								
		}
	}

	static class RedStage1 extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable> {
		int number_nodes = 0;
		Rand rand; 
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();					
			List<String> params = new ArrayList<String>();
			params.add(conf.get(TdsOptions.KEY_OPTION));
			this.rand = new Rand();
			this.rand.initPrivateParameters(params); 	
			this.number_nodes = Integer.parseInt(conf.get(TdsOptions.NUM_NODES));			
		}

		@Override
		public void reduce(IntWritable key, Iterable<ObjectWritable> values, Context context) throws IOException, InterruptedException {
			Object obj; 			
			ArrayList<IntWritable> outedges = new ArrayList<IntWritable>();
			double prValue=0;
			String pt = "";
			Ciphertext ct = null; 
			for (ObjectWritable val:values){
				Object o = val.get();
				if (val.get() instanceof Text){
					Text t = (Text)o; 
					outedges.add(new IntWritable(Integer.parseInt(t.toString())));
				}
				else{ //Ciphertext		
					ct = (Ciphertext)o; 
					prValue = Double.parseDouble(rand
							.decryptToText(ct.getContent().copyBytes())
							.toString().substring(1)); 
					context.getCounter(TdsOptions.HEEDOOP_COUNTER.RND).increment(1); 
				}								
			}
			
			double normVal = prValue/outedges.size(); 
			Ciphertext newVal = new Ciphertext(rand.encryptString("v"+normVal)); 						 
			
			for (IntWritable iw : outedges)		{		
				context.write(iw, new ObjectWritable(newVal));				
			}
		}
	}
	
	static class MapStage2 extends Mapper<IntWritable, ObjectWritable, IntWritable, ObjectWritable>
    {			
				
		@Override
		public void map (IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException
		{
			context.write(key, value); 
		}
	}

    static class RedStage2 extends Reducer<IntWritable, ObjectWritable, IntWritable, ObjectWritable>
    {
		int number_nodes = 0;	
		Rand rand; 
			
		double converge_threshold = 0;
		int change_reported = 0;
		
		@Override
		public void setup(Context context) {
			Configuration conf = context.getConfiguration();					
			List<String> params = new ArrayList<String>();
			params.add(conf.get(TdsOptions.KEY_OPTION));
			this.rand = new Rand();
			this.rand.initPrivateParameters(params); 	
			this.number_nodes = Integer.parseInt(conf.get(TdsOptions.NUM_NODES));			
		}

		/* values: {PRCiphertextNormalized from neighbor, PRCiphertext for this node, PrefCiphertext for this node}
		 */
		public void reduce(IntWritable key, Iterable<ObjectWritable> values,
				Context context) throws IOException, InterruptedException {
						
			double next_rank = 0;			
			double mixing_c = 0.85; 
			double random_coeff=0; 
				
			for (ObjectWritable obj: values){
				Ciphertext o = (Ciphertext)obj.get(); 
				String pt = rand.decryptToText(o.getContent().copyBytes()); 
				context.getCounter(TdsOptions.HEEDOOP_COUNTER.RND).increment(1); 
				if (pt.startsWith("v"))
					next_rank+=Double.parseDouble(pt.substring(1)); 
				else if (pt.startsWith("p"))
					random_coeff = (1-mixing_c)*Double.parseDouble(pt.substring(1));
			}
			next_rank = next_rank * mixing_c + random_coeff;
			Ciphertext newVal = new Ciphertext(rand.encryptString("v"+next_rank)); 
			context.write(key, new ObjectWritable(newVal)); 						
														
		}
    }

    static class DecryptAndSortMapper extends Mapper<IntWritable, ObjectWritable, DoubleWritable, Text>{
    	Rand rand; 
    	
    	@Override
    	public void setup(Context context){
    		Configuration conf = context.getConfiguration();					
			List<String> params = new ArrayList<String>();
			params.add(conf.get(TdsOptions.KEY_OPTION));
			this.rand = new Rand();
			this.rand.initPrivateParameters(params); 				
    	}
    	
    	@Override
    	public void map(IntWritable key, ObjectWritable value, Context context) throws IOException, InterruptedException{    		
    		Ciphertext pr = (Ciphertext) value.get();
    		double rank = Double.parseDouble(rand.decryptToText(pr.getContent().copyBytes()).substring(1));     		    		
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
		conf.set(TdsOptions.NUM_NODES, "" + number_nodes);
		Job job = new Job(conf); 
		
		job.setJarByClass(PagerankTH.class); 
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
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.RND); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue()); 
	}
	
	private void runStage2() throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf();  
		conf.set("number_nodes", "" + number_nodes);
		conf.set("converge_threshold", "" + converge_threshold);
		Job job = new Job(conf); 
		
		job.setJarByClass(PagerankTH.class);
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
		Counter c = job.getCounters().findCounter(TdsOptions.HEEDOOP_COUNTER.RND); 
		System.out.println("Counter "+c.getDisplayName()+" : "+c.getValue()); 
	}
	
	private void runDecryptAndSort(Path verificationPath) throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		job.setJarByClass(PagerankTH.class);
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
				
		converge_threshold = ((double)1.0/(double) number_nodes)/10;
	
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
		ToolRunner.run(new PagerankTH(), args);
	}
}
