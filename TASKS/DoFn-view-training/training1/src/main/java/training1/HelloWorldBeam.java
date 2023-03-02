package training1;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

public class HelloWorldBeam {
    public static void main(String[] args) {
        Pipeline pipeline = Pipeline.create();

        // Create mock input data
        TestStream<String> input = TestStream.create(StringUtf8Coder.of())
            .addElements("hello","world","beam")
            .advanceWatermarkToInfinity();
        
        PCollection<String> words = pipeline.apply(input);
        
        PCollection<String> uppercase = words.apply(ParDo.of(new DoFn<String,String>(){
            @ProcessElement
            public void processElement(ProcessContext c) {
                String word = c.element();
                c.output(word.toUpperCase());
            }
        }));
            
        uppercase.apply("PrintUppercase", ParDo.of(new DoFn<String, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
                System.out.println(c.element());
                c.output(null);
            }
        }));
        
        pipeline.run();
    }
}