package examples;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Implementing the aggregate query generated by HiBench data generator
 * 
 * Select sourceIP, SUM(adRevenue) from userVist groupby sourceIP
 *
 * Data is of the form:
 * index \t sourceIP,destURL,date,adREvenue,userAgent,countryCode,languageCode,keyword,duration
 * 
 * Input type is KeyValueTextInputFormat (with default separator)
 */
public class AggregateAll extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();		
		Job job = new Job(conf); 
		
		job.setJobName("aggregate query"); 
		job.setJarByClass(AggregateAll.class); 
		job.setMapperClass(AggregateMapper.class);
		job.setCombinerClass(AggregateReducer.class); 
		job.setReducerClass(AggregateReducer.class); 
						
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class); 
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		return job.waitForCompletion(true)?0:1;
	}

	//extracts adRevenue
	static class AggregateMapper extends Mapper<Text, Text, Text, LongWritable>{
		
		public void map(Text key, Text val, Context context) throws IOException, InterruptedException{
			String[] vals = val.toString().split(","); 
			context.write(new Text(""),
					new LongWritable(new Long(vals[3]).longValue())); 			
		}
	}
	
	static class AggregateReducer extends Reducer<Text, LongWritable, Text, LongWritable>{
		
		public void reduce(Text key, Iterable<LongWritable> vals,
				Context context) throws IOException, InterruptedException {
			long s = 0;
			for (LongWritable l : vals)
				s += l.get();
			context.write(key, new LongWritable(s));
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new AggregateAll(), args);  
	}
}
