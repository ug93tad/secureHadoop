package examples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import crypto.Det;
import crypto.SearchToken;
import crypto.Searchable;

import tds.Utils;
import tds1.encryptor.SearchEncryptor;

/**
 * grep <word> <input> <output>
 * 
 * Using the deterministically encrypted input (generated by DetEncryptor), as opposed 
 * to Grep which uses more secure encryption scheme
 *
 * 1. Encrypt the keyword with DET -> search token
 * 2. Search by comparing token with the ciphertext
 */
public class GrepDet extends Configured implements Tool{
			
	private Det det; 
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf(); 		
		System.out.format("LD_LIBRARY_PATH = %s\n",System.getenv("LD_LIBRARY_PATH"));
		System.out.format("java.library.path = %s\n",System.getProperty("java.library.path")); 
				 
		
		//create new token and set in in the configuration
		//so that all workers can see
		det = new Det(); 
		det.det_init(conf.get("key"), conf.get("iv")); 
		conf.set(GrepDetMapper.TOKEN_CIPHER, Utils.bytesToHex(det.encrypt_word(args[0]))); 
						
		
		Job job = new Job(conf); 
		job.setJobName("grep exact match"); 
		job.setJarByClass(GrepDet.class); 
		job.setMapperClass(GrepDetMapper.class);
		job.setReducerClass(GrepDetReducer.class);
		job.setCombinerClass(GrepDetReducer.class);
		
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2])); 
		
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class); 
		job.setMapOutputKeyClass(BytesWritable.class);
		job.setMapOutputValueClass(LongWritable.class); 	
		job.setOutputKeyClass(BytesWritable.class);		
		job.setOutputValueClass(LongWritable.class);
				 
		return job.waitForCompletion(true)? 0: 1;		
	}

	public static void main(String[] args) throws Exception{		
		ToolRunner.run(new GrepDet(), args); 
	}
}

class GrepDetMapper extends Mapper<NullWritable, BytesWritable, BytesWritable, LongWritable>{	
	static boolean MODE_WORD = true; 	 
	static final String TOKEN_CIPHER="search.token.cipher";
	static final String TOKEN_WORD_KEY="search.token.wordkey"; 
	BytesWritable encryptedKeyword; 
	
	public void setup(Context context){
		Configuration conf = context.getConfiguration(); 		
		//System.out.format("TOKEN CIPHER in MAP, ** = %s\n", conf.get(GrepMapper.TOKEN_CIPHER));
		//System.out.format("TOKEN WORD_KEY in MAP, ** = %s\n", conf.get(GrepMapper.TOKEN_WORD_KEY));
		
		byte[] tc=Utils.HexToBytes(conf.get(GrepMapper.TOKEN_CIPHER));		
					
		encryptedKeyword = new BytesWritable(tc); 		
		if (conf.get("encryption_mode").equals("line"))
			MODE_WORD=false;	
	}
	
	//reading in search-encrypted record
	@Override
	public void map(NullWritable key, BytesWritable value, Context context) throws IOException, InterruptedException{
		if (MODE_WORD){		
			if (value.equals(this.encryptedKeyword))
				context.write(value, new LongWritable(1));			
		}
		else{ //split the line
			BytesWritable[] splits = split(value); 
			for (BytesWritable bw : splits)
				if (bw.equals(this.encryptedKeyword))
					context.write(encryptedKeyword, new LongWritable(1)); 
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

class GrepDetReducer extends Reducer<BytesWritable, LongWritable, BytesWritable, LongWritable>{
	
	@Override
	public void reduce(BytesWritable key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		long s = 0;
		for (LongWritable lw : vals)
			s+= lw.get(); 
		context.write(key, new LongWritable(s)); 
	}
}
