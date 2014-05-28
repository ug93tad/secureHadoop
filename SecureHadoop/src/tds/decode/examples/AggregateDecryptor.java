package tds.decode.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Det;
import tds.common.Rand;
import tds.hom.Paillier;
import tds.io.Ciphertext;


/**
 * To test the output
 * 
 * Input is SequenceFile:
 * <key = encrypted source IP> <value = encrypted revenue>
 *
 */
public class AggregateDecryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		Job job = new Job(conf);
		job.setJobName("paillier decryptor");
		job.setJarByClass(AggregateDecryptor.class);
		
		job.setMapperClass(PaillierDecryptorMapper.class);
		job.setNumReduceTasks(0);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class  PaillierDecryptorMapper extends Mapper<Ciphertext, Ciphertext, Text, LongWritable>{
		
		ICrypto crypto;
		Det det;		 
		boolean isHom; 
		
		@Override
		public void setup(Context context){
						
			Configuration conf = context.getConfiguration(); 
			this.isHom = conf.getBoolean(TdsOptions.HOM_OPTION, false);
			
			if (this.isHom){
			crypto = new Paillier(); 
			crypto.initPublicParameters(Utils.getPaillierPub(conf)); 
			crypto.initPrivateParameters(Utils.getPaillierPriv(conf));
			}
			else{
				crypto = new Rand(); 
				crypto.initPrivateParameters(Utils.getSymParams(conf)); 
			}
			
			det = new Det();
			det.initPrivateParameters(Utils.getSymParams(conf)); 
			
		}
		
		@Override
		public void map(Ciphertext key, Ciphertext value, Context context)
				throws IOException, InterruptedException {			
			String source = det.decryptToText(key.getContent().copyBytes());
			long sum = Long.parseLong(crypto.decryptToText(value.getContent().copyBytes()));			
			context.write(new Text(source), new LongWritable(sum));
		}
	}
	
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new AggregateDecryptor(), args);
	}
}
