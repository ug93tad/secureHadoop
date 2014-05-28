package examples;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class MaxTemperature {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = Job.getInstance(new Configuration()); 
		job.setJobName("max temperature test"); 
		job.setJarByClass(MaxTemperature.class); 
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		
		job.setMapperClass(MaxTemperatureMapper.class);
		job.setReducerClass(MaxTemperatureReducer.class); 
		
		job.setInputFormatClass(TextInputFormat.class); 
		job.setMapOutputValueClass(LongWritable.class); 
		job.setOutputValueClass(LongWritable.class); 
		
		job.waitForCompletion(true); 
	}
}

class MaxTemperatureMapper extends
		Mapper<LongWritable, Text, LongWritable, LongWritable> {
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String[] s = value.toString().split(" ");
		context.write(new LongWritable(new Long(s[0]).longValue()),
				new LongWritable(new Long(s[1]).longValue()));
	}
}

class MaxTemperatureReducer extends
		Reducer<LongWritable, LongWritable, LongWritable, LongWritable> {
	@Override
	public void reduce(LongWritable key, Iterable<LongWritable> values,
			Context context) throws IOException, InterruptedException {
		long max = Integer.MIN_VALUE;
		for (LongWritable val : values) {
			if (val.get() > max)
				max = val.get();
		}
		context.write(key, new LongWritable(max));
	}
}