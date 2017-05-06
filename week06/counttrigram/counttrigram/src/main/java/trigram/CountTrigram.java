package trigram;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import trigram.TopN.Map;
import trigram.TopN.Reduce;

/**
@트라이그램이란?  
	트라이 그램이란 연속된 세 단어를 말한다. 
@What
	- 앞서만든 TopN까지 연결해서 실행하여 빈도수로 가장 큰 10개의 트라이그램을 보여주도록 프로그램을 작성할 것이다.
 	- 2개의 MapReduce잡을 연속하여 실행하는 방법  = JobChanning을 할 예정이다.
	- CountTrigram을 실행한 후 TopN을 실행할 예정
	
주어진 텍스트를 단어들의 리스트로 쪼갠 다음. 세번째 토큰을 기준으로 먼저 읽은 두 개의 단어를 별도로 유지하면서 
이 세 개의 단어를 기반으로 키를 만들어냅니다.
*/
public class CountTrigram {
	
	public static class Map extends Mapper<Text, Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable(1);
		private Text trigram = new Text();
		@Override
		protected void map(Text key, Text value, Mapper<Text, Text, Text, IntWritable>.Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			
			StringTokenizer tokenizer = new StringTokenizer(line, "\t\r\n\f |,!#\"$.'%&=+-_^@`~:?<>(){}[];*/");
		      if (tokenizer.countTokens() >= 3) {
		    	  //단어의 개수가 3개 이상일 경우에 작동된다.
		    	  String firstToken = tokenizer.nextToken().toLowerCase();
		    	  String secondToken = tokenizer.nextToken().toLowerCase();
		    	  
		    	  while(tokenizer.hasMoreTokens()){
		    		  String thirdToken = tokenizer.nextToken().toLowerCase();
		    		  trigram.set(firstToken + " " + secondToken + " " + thirdToken);
		    		  context.write(trigram, one);
		    		  
		    		  firstToken = secondToken;
		    		  secondToken = thirdToken;
		    	  }
		      }
		
		}
	}
	
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>{

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values){
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
	
	public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException{
		// TopN과 CountTrigram을 연결하여 실행하는 코드가 들어간다.
		// 예제 프로그램의 소스 디렉토리에 복사해서 사용합니다. 
		
		Configuration conf = new Configuration();
		Job job = new Job(conf, "Count Trigram");
		
		job.setJarByClass(CountTrigram.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);
		//컴바이너 작용 리듀서  = 컴바이너 작용
		
		// 입력 포맷은 키 벨류 텍스트 클래스를 사용하고 있습니다.
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		//CountTrigram잡을 실행합니다.
		if (!job.waitForCompletion(true)){
			return;
		}
		
		//CountTrigram의 잡 실행이 잘 되었으면 다음으로 TopN잡을 설정하기 시작합니다.
		Configuration conf2 = new Configuration();
		Job job2 = new Job(conf2,"TopN");

		job2.setJarByClass(TopN.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(LongWritable.class);
		
		// 맵 클래스와 리듀스 클래스를 설정할 때 TopN밑의 클래스로 지정하고 있습니다.
		job2.setMapperClass(TopN.Map.class);
		job2.setReducerClass(TopN.Reduce.class);
		//사실 리듀스 태스크의 기본은 1이라서 따로 설정하지 않아도 되지만... 명시적으로...
		job2.setNumReduceTasks(1);
		
		job2.setInputFormatClass(KeyValueTextInputFormat.class);
		job2.setOutputFormatClass(TextOutputFormat.class);
		
		// args[1]은 CountTrigram의 처리결과 저장 디렉토리였는데 TopN에서는 입력으로 사용되고 있다.
		//TopN의 처리 결과는 그 디렉토리 밑에 topN이란 서브디렉토리에 저장된다.
		//N은 10으로 하드코딩되어있는데 Main함수의 인자로 받도록 바꿀 수도 있겠죠.
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[1]+"//topN"));
		job2.getConfiguration().setInt("topN", 30);
		
		if(!job2.waitForCompletion(true)){
			return;
		}
	}
}