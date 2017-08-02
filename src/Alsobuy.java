import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

public class Alsobuy {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

//            String[]  order_arr = value.toString().split("   ");
//            System.out.println("order" + order_arr[0]);
//            System.out.println("goods" + order_arr[1]);

            String[] goods_arr = value.toString().split(",");
            if(goods_arr.length <= 1){
                return ;
            }

            Set<String> result = new HashSet<String>();

            for (String goods : goods_arr ) {
                for (String goods1 : goods_arr ) {
                    if(goods.compareTo(goods1) > 0) {
                        result.add(goods1 +"_"+ goods);
                    }else if(goods.compareTo(goods1) < 0) {
                        result.add(goods +"_"+ goods1);
                    }
                }
            }

            for (String link : result){
                word.set(link);
                context.write(word, one);
            }
        }
    }

    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            //System.out.println(key.toString()+ ":" + sum);
            if(sum < 16){
                return;
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(Alsobuy.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}