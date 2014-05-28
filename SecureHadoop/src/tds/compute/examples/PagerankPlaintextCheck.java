package tds.compute.examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Generate a sortable file from the ranking vector, for sanity check
 *
 */
public class PagerankPlaintextCheck extends Configured implements Tool{

	static class PrCheckMapper extends Mapper<LongWritable, Text, DoubleWritable, IntWritable>{
		
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] line = value.toString().split("\t");
			int idx = Integer.parseInt(line[0]);
			double val = Double.parseDouble(line[1].substring(1));
			context.write(new DoubleWritable(val), new IntWritable(idx));
		}
		
	}
	
	static class PrCheckReducer extends Reducer<DoubleWritable, IntWritable, DoubleWritable, IntWritable>{
		
		@Override
		public void reduce(DoubleWritable key, Iterable<IntWritable> value, Context context)
				throws IOException, InterruptedException {
			for (IntWritable val: value)
				context.write(key, val); 
		}
		
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf); 
		
		Path input = new Path(args[0]); 
		Path output = new Path(args[1]); 
		job.setJarByClass(PagerankPlaintextCheck.class);
		job.setMapperClass(PrCheckMapper.class);
		job.setReducerClass(PrCheckReducer.class); 
		
		FileInputFormat.setInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, output); 
		
		job.setInputFormatClass(TextInputFormat.class); 
		job.setOutputFormatClass(TextOutputFormat.class);
		/*job.setMapOutputKeyClass(DoubleWritable.class);
		job.setMapOutputValueClass(IntWritable.class);*/ 
		job.setOutputKeyClass(DoubleWritable.class); 
		job.setOutputValueClass(IntWritable.class); 
				
		
		return job.waitForCompletion(true)?0: 1; 
	}
	
	
	/**
	 * @param args: <input file> <output file>
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{		
			ToolRunner.run(new PagerankPlaintextCheck(), args); 
	}
	
}
