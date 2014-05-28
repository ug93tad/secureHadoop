package examples;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Wordcount extends Configured implements Tool{

	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Word count, Anh"); 
		job.setJarByClass(Wordcount.class); 
		job.setMapperClass(WordCountMapper.class); 
		job.setReducerClass(WordCountReducer.class);
		job.setCombinerClass(WordCountReducer.class); 
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class); 
		
		FileInputFormat.addInputPath(job, new Path(arg0[0]));
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		return job.waitForCompletion(true) ? 0:1; 
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new Wordcount(), args); 
	}
}

class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable>{
	@Override
	public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException{
		StringTokenizer ss = new StringTokenizer(val.toString()); 
		while (ss.hasMoreTokens()){
			context.write(new Text(ss.nextToken()), new LongWritable(1));
		}		 
	}
}

class WordCountReducer extends Reducer<Text,LongWritable, Text, LongWritable>{
	@Override
	public void reduce(Text key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s =0;
		for (LongWritable val : vals)
			s+=val.get(); 
		context.write(key, new LongWritable(s)); 
	}
}
