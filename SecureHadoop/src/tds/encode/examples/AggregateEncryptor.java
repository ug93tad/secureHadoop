package tds.encode.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.common.Det;
import tds.common.Rand;
import tds.hom.Paillier;
import tds.io.Ciphertext;
import tds.io.EncryptedDenseVector;
import tds.io.EncryptedVector;
import tds.io.EncryptedVectorWritable;
import tds.trustedhw.math.AddFunction;



/**
 * Encrypt data generated from HiBench's autogen. 
 * 
 * The output format is SequenceFile:
 * + HOM: <null><encrypted source, encrypted rev, encrypted remaining> 
 * + TH: <null><AES> 
 * 
 *
 */
public class AggregateEncryptor extends Configured implements Tool{

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf(); 
		Paillier paillier = new Paillier();		
		paillier.keyGen(conf.get(TdsOptions.PAILLIER_PUBLIC_KEY_FILE),
				conf.get(TdsOptions.PAILLIER_PRIVATE_KEY_FILE)); 
		Job job = new Job(conf); 
		
		job.setJobName("paillier encryption"); 
		job.setJarByClass(AggregateEncryptor.class);
		job.setMapperClass(PaillierMapper.class);
		job.setNumReduceTasks(0); 
		
		job.setInputFormatClass(KeyValueTextInputFormat.class); 
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(EncryptedVectorWritable.class); 
				
		FileInputFormat.addInputPath(job, new Path(args[0])); 
		FileOutputFormat.setOutputPath(job, new Path(args[1])); 
		return job.waitForCompletion(true) ? 0 : 1;
	}

	static class PaillierMapper extends Mapper<Text, Text, NullWritable, EncryptedVectorWritable>{
		ICrypto add, rand, det;  
	
		boolean isHom = false; 
		
		@Override
		public void setup(Context context){
			Configuration conf = context.getConfiguration();						
			if (conf.getBoolean(TdsOptions.HOM_OPTION, false)){				
				this.add = new Paillier(); 
				this.add.initPublicParameters(Utils.getPaillierPub(conf));
				this.add.initPrivateParameters(Utils.getPaillierPriv(conf));
								
				this.rand = new Rand();
				this.det = new Det(); 
				this.rand.initPrivateParameters(Utils.getSymParams(conf));
				this.det.initPrivateParameters(Utils.getSymParams(conf));
				
				this.isHom = true; 
			}
			else{
				this.add = new AddFunction();
				this.add.initPrivateParameters(Utils.getSymParams(conf)); 
			}										
		}
		
		@Override
		public void map(Text key, Text value, Context context) throws IOException, InterruptedException{			 
			ArrayList<byte[]> cc = new ArrayList<byte[]>(); 
			
			if (this.isHom){
				String[] vals = value.toString().split(","); 
				String sourceIP = vals[0];
				String rev = vals[3]; 
				String rest = vals[1] + "," + vals[2] + "," + vals[4] + ","
						+ vals[5] + "," + vals[6] + "," + vals[7] + "," + vals[8];
								
				cc.add(det.encryptString(sourceIP)); 
				cc.add(add.encryptString(rev));
				cc.add(rand.encryptString(rest));								 				 			
			}
			else{//trusted hardware
				cc.add(this.add.encryptString(value.toString())); 
			}			
			context.write(NullWritable.get(), new EncryptedVectorWritable(new EncryptedDenseVector(cc))); 						
		}
	}
		
	public static void main(String[] args) throws Exception{
		ToolRunner.run(new AggregateEncryptor(), args); 
	}
}
