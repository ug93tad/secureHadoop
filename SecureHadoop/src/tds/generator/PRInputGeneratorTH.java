package tds.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Generate the initial pagerank vector = (1/N)
 * 
 */
public class PRInputGeneratorTH extends Configured implements Tool{
	public static void gen_initial_vector(int number_nodes, Path vector_path,
			Configuration conf) throws IOException {
		final FileSystem fs = FileSystem.get(conf);
		SequenceFile.Writer writer = null;
		writer = SequenceFile.createWriter(fs, conf, new Path(vector_path.toString()+"/pagerank"), IntWritable.class,
				ObjectWritable.class);
		
		
		int i, j = 0;		
		int milestone = number_nodes / 10;
		double initial_rank = 1.0 / (double) number_nodes;
		for (i=0; i<number_nodes; i++){
			//writer.append(new IntWritable(i), new ObjectWritable(new Text("v"+number_nodes*number_nodes)));
			Text text = new Text("v"+initial_rank); 
			writer.append(new IntWritable(i), new ObjectWritable(text));
			if (++j >milestone){				
				System.out.print("."); 
				j=0; 
			}
		}
		writer.close(); 
		System.out.println(); 
				
	}

	/**
	 * Generate the preference vector used for computing personalized PageRank. 
	 * The default value for this vector is 1/N
	 * 
	 * The format is: <node index> p<value>
	 * where 'p' is to distinguish itself to initial pagerank value 'v'
	 */
	public static void gen_pref_vector(int number_nodes, Path vector_path,
			Configuration conf) throws IOException {
		final FileSystem fs = FileSystem.get(conf);
		SequenceFile.Writer writer = null;
		writer = SequenceFile.createWriter(fs, conf, new Path(vector_path.toString()+"/preference"), IntWritable.class,
				ObjectWritable.class);
		
		
		int i, j = 0;		
		int milestone = number_nodes / 10;
		double initial_rank = 1.0 / (double) number_nodes;
		
		for (i=0; i<number_nodes; i++){
			Text text = new Text("p"+initial_rank); 
			writer.append(new IntWritable(i), new ObjectWritable(text));
			if (++j >milestone){
				System.out.print("."); 
				j=0; 
			}
		}
		writer.close(); 
		System.out.println(); 
			
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration(); 
		Job job = new Job(conf); 
		job.setJarByClass(PRInputGeneratorTH.class); 
		job.setMapperClass(EdgeMapper.class); 
		job.setNumReduceTasks(0); 
		
		//job.setInputFormatClass(KeyValueTextInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 		
		FileInputFormat.setInputPaths(job, new Path(args[1])); 
		FileOutputFormat.setOutputPath(job, new Path(args[2])); 
		job.setOutputKeyClass(IntWritable.class); 
		job.setOutputValueClass(ObjectWritable.class); 
		
		return job.waitForCompletion(true) ? 0 : 1;
	}
	
	
	/**
	 * converting NLInputFormat of edge file to the SequenceFileInput format	
	 */
	static class EdgeMapper extends Mapper<LongWritable, Text, IntWritable, ObjectWritable>{
		
		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{			
			String[] line = value.toString().split("\t");
			//IntWritable nb = new IntWritable(new Integer(line[2]).intValue()); 
			Text nb = new Text(line[2]); 			
			context.write(new IntWritable(new Integer(line[1]).intValue()),
					new ObjectWritable(nb)); 
		}
	}
	
	/**
	 * args: n <original edge file> <new edge file> 
	 * @throws Exception 
	 */
	public static void main(String args[]) throws Exception {
		//convert the edge file
		ToolRunner.run(new PRInputGeneratorTH(), args); 
		
		Configuration conf = new Configuration(); 
		int n = new Integer(args[0]).intValue(); 
		Path path = new Path("/pr_vector");
		PRInputGeneratorTH.gen_initial_vector(n, path, conf);
		Path path1 = new Path("/pref_vector"); 
		PRInputGeneratorTH.gen_pref_vector(n, path1, conf);
	}
	
}
