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
    Pipeline pipeline = Pipeline.create(); // Creates a new pipeline.

    // Reads text files from a directory specified by a filepattern.
    PCollection<KV<String, Long>> wordCounts = pipeline
        .apply(TextIO.read().from("C:\\Source\\Git\\myTraining\\Apache-beam-Road-map\\TASKS\\TEXT-FILE-PARSER\\resources\\iron\\*"))

    // Splits each line of text into individual words and emits them as a PCollection of strings.
        .apply("ExtractWords", FlatMapElements.into(TypeDescriptors.strings())
            .via((String line) -> Arrays.asList(line.split("\\W+"))))

    // Maps each word to a key-value pair where the key is the word and the value is 1L.
        .apply("ToKV", MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
            .via((String word) -> KV.of(word, 1L)))

    // Counts the occurrences of each word and produces a PCollection of key-value pairs where the key is a unique word and the value is the number of times it appears.
        .apply("CountWords", Count.perKey());

    // Formats the output of the word count as a string and prints it to the console.
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

    // Groups the word count results by filename and formats them as a CSV string.
    PCollection<KV<String, Iterable<Long>>> groupedCounts = wordCounts.apply(GroupByKey.create());
    groupedCounts.apply("FormatAsCsv", FlatMapElements.into(TypeDescriptors.strings())
            .via((SerializableFunction<KV<String, Iterable<Long>>, Iterable<String>>) kv -> {
              String filename = kv.getKey();
              Long count = kv.getValue().iterator().next();
              return Arrays.asList(filename + "," + count);
            }))

    // Writes the CSV-formatted word count results to a file in the 'gold' folder.
            .apply(TextIO.write().to("C:\\Source\\Git\\myTraining\\Apache-beam-Road-map\\TASKS\\TEXT-FILE-PARSER\\resources\\gold\\").withSuffix(".csv"));

    pipeline.run(); // Runs the pipeline.
  }
}