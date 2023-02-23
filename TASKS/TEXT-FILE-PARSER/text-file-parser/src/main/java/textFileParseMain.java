import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;

public class textFileParseMain {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    PCollection<KV<String, Long>> wordCounts = pipeline
        .apply(TextIO.read().from("C:\\Source\\Git\\myTraining\\Apache-beam-Road-map\\TASKS\\TEXT-FILE-PARSER\\resources\\iron\\*"))
        .apply("ExtractWords", FlatMapElements.into(TypeDescriptors.strings())
            .via((String line) -> Arrays.asList(line.split("\\W+"))))
        .apply("ToKV", MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
            .via((String word) -> KV.of(word, 1L)))
        .apply("CountWords", Count.perKey());
    
    
    wordCounts.apply(MapElements.into(TypeDescriptors.strings())
            .via((KV<String, Long> wordCount) -> {
              String word = wordCount.getKey();
              Long count = wordCount.getValue();
              return word + ": " + count;
            }))
            .apply("PrintResults", ParDo.of(new DoFn<String, Void>() {
              @ProcessElement
              public void processElement(ProcessContext c) {
                System.out.println(c.element());
              }
            }));
    
    
    PCollection<KV<String, Iterable<Long>>> groupedCounts = wordCounts.apply(GroupByKey.create());

    groupedCounts.apply("FormatAsCsv", FlatMapElements.into(TypeDescriptors.strings())
            .via((SerializableFunction<KV<String, Iterable<Long>>, Iterable<String>>) kv -> {
              String filename = kv.getKey();
              Long count = kv.getValue().iterator().next();
              return Arrays.asList(filename + "," + count);
            }))
            .apply(TextIO.write().to("C:\\Source\\Git\\myTraining\\Apache-beam-Road-map\\TASKS\\TEXT-FILE-PARSER\\resources\\gold\\").withSuffix(".csv"));
    pipeline.run();
  }
}