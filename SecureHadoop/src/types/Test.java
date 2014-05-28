package types;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class Test {
	public static void main(String[] args) throws IOException, Exception, IllegalAccessException {
		/*Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		String HOME = "/test";
		Path out = new Path(HOME + "/" + args[0]);
		System.out.println("out path = " + out.getName());
		SequenceFile.Writer writer = null;
		writer = SequenceFile.createWriter(fs, conf, out, IntWritable.class,
				ObjectWritable.class);
		byte[] one = { 1 };
		byte[] two = { 1, 2 };
		ObjectWritable o1 = new ObjectWritable(new PageRankCiphertext(one));
		ObjectWritable o2 = new ObjectWritable(new PreferenceCiphertext(two));

		// ObjectWritable o1 = new ObjectWritable(new IntWritable(1));
		// ObjectWritable o2 = new ObjectWritable(new Text("2"));
		writer.append(new IntWritable(1), o1);
		writer.append(new IntWritable(2), o2);
		writer.close();

		SequenceFile.Reader reader = new SequenceFile.Reader(fs, out, conf);
		IntWritable key = (IntWritable) reader.getKeyClass().newInstance();
		ObjectWritable val = (ObjectWritable) reader.getValueClass()
				.newInstance();
		while (reader.next(key, val)) {
			int k = key.get();
			System.out.println("name = " + val.get().getClass().getName());
			if (val.get() instanceof PageRankCiphertext)
				System.out.println("PageRankciphertext:: " + k);
			else
				System.out.println("PreferenceCiphertext:: " + k);
		}
		reader.close();*/
	}
}
