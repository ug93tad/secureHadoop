package tds.decode.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Rand;
import tds.compute.examples.TeraEncryptedInputFormat;
import tds.compute.examples.TeraOutputFormat;
import tds.hom.Ope;
import tds.trustedhw.math.CompareFunction;

public class TeraSortDecryptor extends Configured implements Tool{

	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Job job = new Job(conf); 
		
		job.setJarByClass(TeraSortDecryptor.class);
		job.setJobName("OPE decryptor"); 
		job.setMapperClass(OpeMapper.class); 
		job.setNumReduceTasks(0); 
		
		if (conf.getBoolean(TdsOptions.HOM_OPTION, false))
			job.setInputFormatClass(TeraEncryptedInputFormat.class);
		else
			job.setInputFormatClass(SequenceFileInputFormat.class); 
		job.setOutputFormatClass(TeraOutputFormat.class); 
		job.setOutputKeyClass(Text.class); 
		job.setOutputValueClass(Text.class); 
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
				
		return job.waitForCompletion(true)?0: 1;
	}

	static class OpeMapper extends Mapper<Text,Text,Text,Text>{
		ICrypto crypto; 
		Rand rand; 
		boolean isHom = false;
		int counter = 0; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();			
			this.crypto = new Ope();		
			this.rand = new Rand(); 
			this.crypto.initPrivateParameters(Utils.getOpeParams(conf));
			this.rand.initPrivateParameters(Utils.getSymParams(conf)); 			
		}
		
		/* input is: 10 bytes key - 90 bytes values
		 */
		@Override
		public void map(Text key, Text val, Context context) throws IOException, InterruptedException{					 
			
			byte[] keyVal = key.copyBytes(); 
			byte[] valVal = val.copyBytes(); 
			Text newKey = new Text(this.crypto.decryptRaw(keyVal));
			Text newVal = new Text(this.rand.decryptRaw(valVal)); 			
			context.write(newKey, newVal); 			
			
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new TeraSortDecryptor(), args); 
	}
}
