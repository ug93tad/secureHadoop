package examples.pagerank;

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
 * Original edges are generated using HiBench's pagerank
 * 
 */
public class Generator extends Configured implements Tool{
	public static void gen_initial_vector(int number_nodes, Path vector_path,
			Configuration conf) throws IOException {
		final FileSystem fs = FileSystem.get(conf);
		SequenceFile.Writer writer = null;
		writer = SequenceFile.createWriter(fs, conf, new Path(vector_path.toString()+"/pagerank"), IntWritable.class,
				ObjectWritable.class);
		
		
		int i, j = 0;		
		int milestone = number_nodes / 10;
		for (i=0; i<number_nodes; i++){
			//writer.append(new IntWritable(i), new ObjectWritable(new Text("v"+number_nodes*number_nodes)));
			writer.append(new IntWritable(i), new ObjectWritable(new Text("v"+number_nodes*number_nodes)));
			if (++j >milestone){
				System.out.print("."); 
				j=0; 
			}
		}
		writer.close(); 
		System.out.println(); 
		
		/*String file_name = "pagerank_init_vector.temp";
		FileWriter file = new FileWriter(file_name);
		BufferedWriter out = new BufferedWriter(file);

		System.out.print("Creating initial pagerank vectors...");
		double initial_rank = 1.0 / (double) number_nodes;

		int fixedVal=1; 
		
		for (i = 0; i < number_nodes; i++) {
			//out.write(i + "\tv" + initial_rank + "\n");
			out.write(i+"\tv"+fixedVal+"\n");
			if (++j > milestone) {
				System.out.print(".");
				j = 0;
			}
		}
		out.close();
		System.out.println("");

		// copy it to curbm_path, and delete temporary local file.
		final FileSystem fs = FileSystem.get(conf);
		fs.copyFromLocalFile(true, new Path("./" + file_name), new Path(
				vector_path.toString() + "/" + file_name));*/
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
		for (i=0; i<number_nodes; i++){
			writer.append(new IntWritable(i), new ObjectWritable(new Text("p"+number_nodes*number_nodes)));
			if (++j >milestone){
				System.out.print("."); 
				j=0; 
			}
		}
		writer.close(); 
		System.out.println(); 
		
		/*int i, j = 0;
		int milestone = number_nodes / 10;
		String file_name = "pagerank_pref_vector";
		FileWriter file = new FileWriter(file_name);
		BufferedWriter out = new BufferedWriter(file);

		System.out.print("Creating initial preference vectors...");
		double initial_rank = 1.0 / (double) number_nodes;
		
		int fixedVal = 1; 
		for (i = 0; i < number_nodes; i++) {
			//out.write(i + "\tp" + initial_rank + "\n");
			out.write(i + "\tp" + fixedVal + "\n");
			if (++j > milestone) {
				System.out.print(".");
				j = 0;
			}
		}
		out.close();
		// copy it to curbm_path, and delete temporary local file.
		final FileSystem fs = FileSystem.get(conf);
		fs.copyFromLocalFile(true, new Path("./" + file_name), new Path(
				vector_path.toString() + "/" + file_name));*/
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration(); 
		Job job = new Job(conf); 
		job.setJarByClass(Generator.class); 
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
		ToolRunner.run(new Generator(), args); 
		
		Configuration conf = new Configuration(); 
		int n = new Integer(args[0]).intValue(); 
		Path path = new Path("/pr_vector");
		Generator.gen_initial_vector(n, path, conf);
		Path path1 = new Path("/pref_vector"); 
		Generator.gen_pref_vector(n, path1, conf);
	}
	
}
