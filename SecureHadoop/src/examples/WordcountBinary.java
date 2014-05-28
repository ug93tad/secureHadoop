package examples;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * @author dinhtta
 * wordcount for data generated by DetEncryptor
 * 
 * -D encryption_mode=line/word
 */
public class WordcountBinary extends Configured implements Tool{

	@Override
	public int run(String[] arg0) throws Exception {
		Configuration conf = getConf(); 
		Job job = new Job(conf); 
		
		job.setJobName("Word count, Anh"); 
		job.setJarByClass(WordcountBinary.class); 
		job.setMapperClass(WordCountBinaryMapper.class); 
		job.setReducerClass(WordCountBinaryReducer.class);
		job.setCombinerClass(WordCountBinaryReducer.class); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setMapOutputKeyClass(BytesWritable.class);
		job.setMapOutputValueClass(LongWritable.class);
		job.setOutputKeyClass(BytesWritable.class);
		job.setOutputValueClass(LongWritable.class); 
		
		FileInputFormat.addInputPath(job, new Path(arg0[0]));
		FileOutputFormat.setOutputPath(job, new Path(arg0[1])); 
		
		return job.waitForCompletion(true) ? 0:1; 
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new WordcountBinary(), args); 
	}
}

class WordCountBinaryMapper extends Mapper<NullWritable, BytesWritable, BytesWritable, LongWritable>{
	static boolean MODE_WORD=true;
	static Logger log = Logger.getLogger(WordCountBinaryMapper.class); 
	
	@Override
	public void setup(Context context){
		Configuration conf = context.getConfiguration();
		if (conf.get("encryption_mode").equals("line"))
			MODE_WORD=false;		
	}
	
	@Override
	public void map(NullWritable key, BytesWritable val, Context context) throws IOException, InterruptedException{
		/*String[] ss = val.toString().split(" "); 
		for (String s:ss)*/
		if (MODE_WORD)
			context.write(val, new LongWritable(1));
		else{ //split the line
			BytesWritable[] splits = split(val); 
			for (BytesWritable bw : splits)
				context.write(bw, new LongWritable(1)); 
		}
	}
	
	//splitting the bytes array created by line-mode encryption
	static BytesWritable[] split(BytesWritable val){
		byte[] vals = val.copyBytes();
		int currentIndex=0;
		byte len=0;
		ArrayList<BytesWritable> list = new ArrayList<BytesWritable>();
		int counter=0; 
		while (currentIndex<vals.length){			
			len=vals[currentIndex];			
			currentIndex++;
			byte[] tmp = new byte[len]; 
			System.arraycopy(vals, currentIndex, tmp, 0, len);
			list.add(new BytesWritable(tmp)); 
			currentIndex+=len;
			counter++; 
		}
		return list.toArray(new BytesWritable[counter]); 		
	}
}

class WordCountBinaryReducer extends Reducer<BytesWritable,LongWritable, BytesWritable, LongWritable>{
	@Override
	public void reduce(BytesWritable key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		int s =0;
		for (LongWritable val : vals)
			s+=val.get(); 
		context.write(key, new LongWritable(s)); 
	}
}