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


import crypto.SearchToken;
import crypto.Searchable;

import tds.Utils;
import tds1.encryptor.SearchEncryptor;

/**
 * grep <word> <input> <output>
 * 
 * exact match only 
 *
 */
public class Grep extends Configured implements Tool{
			
	private Searchable se; 
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf(); 		
		System.out.format("LD_LIBRARY_PATH = %s\n",System.getenv("LD_LIBRARY_PATH"));
		System.out.format("java.library.path = %s\n",System.getProperty("java.library.path")); 
				 
		
		//create new token and set in in the configuration
		//so that all workers can see
		se = new Searchable(); 
		se.init(conf.get("key")); 
		SearchToken st = se.getToken(args[0]);		
		conf.set(GrepMapper.TOKEN_CIPHER, Utils.bytesToHex(st.ciphertext));  
		conf.set(GrepMapper.TOKEN_WORD_KEY, Utils.bytesToHex(st.wordKey));
		
		Job job = new Job(conf); 
		job.setJobName("grep exact match"); 
		job.setJarByClass(Grep.class); 
		job.setMapperClass(GrepMapper.class);
		job.setReducerClass(GrepReducer.class);
		job.setCombinerClass(GrepReducer.class);
		
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
		ToolRunner.run(new Grep(), args); 
	}
}

class GrepMapper extends Mapper<NullWritable, BytesWritable, BytesWritable, LongWritable>{
	Searchable se; 
	SearchToken token; 
	static boolean MODE_WORD = true; 	 
	static final String TOKEN_CIPHER="search.token.cipher";
	static final String TOKEN_WORD_KEY="search.token.wordkey"; 
	BytesWritable encryptedKeyword; 
	
	public void setup(Context context){
		Configuration conf = context.getConfiguration(); 
		se = new Searchable();
		se.init(conf.get("key"));
		//System.out.format("TOKEN CIPHER in MAP, ** = %s\n", conf.get(GrepMapper.TOKEN_CIPHER));
		//System.out.format("TOKEN WORD_KEY in MAP, ** = %s\n", conf.get(GrepMapper.TOKEN_WORD_KEY));
		
		byte[] tc=Utils.HexToBytes(conf.get(GrepMapper.TOKEN_CIPHER));
		byte[] tw = Utils.HexToBytes(conf.get(GrepMapper.TOKEN_WORD_KEY));
					
		encryptedKeyword = new BytesWritable(tc); 
		token = new SearchToken(tc,tw);		
		if (conf.get("encryption_mode").equals("line"))
			MODE_WORD=false;	
	}
	
	//reading in search-encrypted record
	@Override
	public void map(NullWritable key, BytesWritable value, Context context) throws IOException, InterruptedException{
		if (MODE_WORD){			
			if (se.match(value.copyBytes(), token))
				context.write(value, new LongWritable(1));
		}
		else{ //split the line
			BytesWritable[] splits = split(value); 
			for (BytesWritable bw : splits)
				if (se.match(bw.copyBytes(), token))
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

class GrepReducer extends Reducer<BytesWritable, LongWritable, BytesWritable, LongWritable>{
	
	@Override
	public void reduce(BytesWritable key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		long s = 0;
		for (LongWritable lw : vals)
			s+= lw.get(); 
		context.write(key, new LongWritable(s)); 
	}
}
