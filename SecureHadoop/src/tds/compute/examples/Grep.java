package tds.compute.examples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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


import tds.ICrypto;
import tds.TdsOptions;
import tds.Utils;
import tds.hom.SearchToken;
import tds.hom.Searchable;
import tds.io.Ciphertext;


/**
 * grep <word> <input> <output>
 * 
 * exact match only, with SE
 * 
 *
 */
public class Grep extends Configured implements Tool{
			
	private Searchable se; 
	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf(); 		
	
		se = new Searchable(); 		
		se.initPrivateParameters(Utils.getSymParams(conf)); 		
		
		SearchToken st = se.genToken(args[0]);				
		
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
		job.setMapOutputKeyClass(Ciphertext.class);
		job.setMapOutputValueClass(LongWritable.class); 	
		job.setOutputKeyClass(Ciphertext.class);		
		job.setOutputValueClass(LongWritable.class);
				 
		return job.waitForCompletion(true)? 0: 1;		
	}

	public static void main(String[] args) throws Exception{		
		ToolRunner.run(new Grep(), args); 
	}
}

class GrepMapper extends Mapper<NullWritable, Ciphertext, Ciphertext, LongWritable>{
	Searchable se; 
	SearchToken token; 	 
	static final String TOKEN_CIPHER="search.token.cipher";
	static final String TOKEN_WORD_KEY="search.token.wordkey"; 
	Ciphertext encryptedKeyword; 
	
	public void setup(Context context){
		Configuration conf = context.getConfiguration(); 
		
		se = new Searchable();
		se.initPrivateParameters(Utils.getSymParams(conf)); 
			
		byte[] tc=Utils.HexToBytes(conf.get(GrepMapper.TOKEN_CIPHER));
		byte[] tw = Utils.HexToBytes(conf.get(GrepMapper.TOKEN_WORD_KEY));
					
		encryptedKeyword = new Ciphertext(tc); 
		token = new SearchToken(tc,tw);	
	}
	
	//reading in search-encrypted record
	@Override
	public void map(NullWritable key, Ciphertext value, Context context) throws IOException, InterruptedException{				
			if (se.isAMatch(value, token))
				context.write(encryptedKeyword, new LongWritable(1));		
	}		
}

class GrepReducer extends Reducer<Ciphertext, LongWritable, Ciphertext, LongWritable>{
	
	@Override
	public void reduce(Ciphertext key, Iterable<LongWritable> vals, Context context) throws IOException, InterruptedException{
		long s = 0;
		for (LongWritable lw : vals)
			s+= lw.get(); 
		context.write(key, new LongWritable(s)); 
	}
}
