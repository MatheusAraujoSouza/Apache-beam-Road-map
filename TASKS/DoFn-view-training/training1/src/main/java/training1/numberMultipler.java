package training1;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TimestampedValue;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class numberMultipler {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Create mock input data with timestamps
    TestStream<String> input = TestStream.create(StringUtf8Coder.of())
        .addElements(TimestampedValue.of("1", Instant.now()))
        .advanceWatermarkToInfinity();

    PCollection<Integer> numbers = pipeline.apply(input)
        .apply(MapElements.into(TypeDescriptors.integers()).via(Integer::valueOf));

    // Apply FixedWindows of 1 minute to the input data
    PCollection<Integer> windowedNumbers = numbers.apply(Window.into(FixedWindows.of(Duration.standardMinutes(1))));

    PCollection<Double> multipliedAndSummedNumbers = windowedNumbers.apply(ParDo.of(new DoFn<Integer, Double>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        Integer input = c.element();
        Double output = input * 30.0 + 12.0;
        c.output(output);
      }
    })).apply(Sum.doublesGlobally());

    PCollection<Float> dividedNumbers = multipliedAndSummedNumbers.apply(MapElements.into(TypeDescriptors.floats()).via(new SerializableFunction<Double, Float>() {
      @Override
      public Float apply(Double input) {
        return input.floatValue() / 5.0f;
      }
    }));

    // To print the output value
    dividedNumbers.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
        .apply("PrintNumbers",ParDo.of(new DoFn<String,Void>(){
          @ProcessElement
          public void processElement(ProcessContext c) {
            System.out.println(c.element());
          }
        }));

    pipeline.run();
  }
}