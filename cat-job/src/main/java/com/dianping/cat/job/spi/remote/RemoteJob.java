package com.dianping.cat.job.spi.remote;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class RemoteJob extends Configured implements Tool {
    
    public static class MapClass extends Mapper<LongWritable, Text, Text, Text> {
        
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
                        
            String[] citation = value.toString().split("\t");
            context.write(new Text(citation[1]), new Text(citation[0]));
        }
    }
    
    public static class ReduceClass extends Reducer<Text, Text, Text, Text> {
        
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
                           
            String csv = "";
            for (Text val:values) {
                if (csv.length() > 0) csv += ",";
                csv += val.toString();
            }
            
            context.write(key, new Text(csv));
        }
    }
    
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        
        Job job = new Job(conf, "RemoteJob");
        job.setJarByClass(RemoteJob.class);
        
        Path in = new Path(args[0]);
        Path out = new Path(args[1]);
        FileInputFormat.setInputPaths(job, in);
        FileOutputFormat.setOutputPath(job, out);
        
        job.setMapperClass(MapClass.class);
        job.setReducerClass(ReduceClass.class);
        
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        job.setNumReduceTasks(3);
        
        System.exit(job.waitForCompletion(true)?0:1);
        
        return 0;
    }
    
    public static void main(String[] args) throws Exception { 
        int res = ToolRunner.run(new Configuration(), new RemoteJob(), args);
        
        System.exit(res);
    }
}
