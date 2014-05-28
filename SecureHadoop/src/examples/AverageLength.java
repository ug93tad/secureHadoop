package examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import types.LongPair;

public class AverageLength {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		Job job = new Job(new Configuration()); 
		
		job.setJobName("average word length"); 
		job.setJarByClass(AverageLength.class); 
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		
		job.setMapperClass(AverageLengthMapper.class); 
		job.setReducerClass(AverageLengthReducer.class);
		job.setCombinerClass(AverageLengthReducer.class); 
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(LongPair.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(LongPair.class); 
		
		job.waitForCompletion(true); 
	}
}

class AverageLengthMapper extends Mapper<LongWritable, Text, NullWritable, LongPair>{
	public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException{
		String[] ss = val.toString().split(" ");
		int c=0; 
		for (String s: ss){
			c+=s.length(); 
		}
		context.write(NullWritable.get(), new LongPair(c, ss.length));
	}
}

class AverageLengthReducer extends Reducer<NullWritable, LongPair, NullWritable, LongPair>{
	@Override
	public void reduce(NullWritable key, Iterable<LongPair> vals, Context context) throws IOException, InterruptedException{
		long sum=0, n=0; 
		for (LongPair val : vals){
			sum+=val.getFirst().get();
			n+=val.getSecond().get(); 
		}
		context.write(NullWritable.get(), new LongPair(sum,n)); 
	}
}