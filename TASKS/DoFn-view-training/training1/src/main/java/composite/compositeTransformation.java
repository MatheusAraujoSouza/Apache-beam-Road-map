package composite;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class compositeTransformation {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();
    
    PCollection<String> inputStrings = pipeline.apply(Create.of(
        "Matheus", "Marcos", "Lucas", "Joao", "Gabriel", "Pedro", "Felipe", "Andre"));

    PCollection<Integer> stringLengths = inputStrings.apply(new StringLengthTransform());

    PCollection<Integer> evenNumbers = stringLengths.apply(FilterEvenNumbers.of());

    // Output the filtered even numbers
    evenNumbers.apply(ParDo.of(new DoFn<Integer, Void>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        Integer number = c.element();
        System.out.println(number);
      }
    }));

    pipeline.run().waitUntilFinish();
  }

  public static class StringLengthTransform extends PTransform<PCollection<String>, PCollection<Integer>> {

    @Override
    public PCollection<Integer> expand(PCollection<String> input) {
      // Apply a ParDo transform with a DoFn that computes the length of each input string
      PCollection<Integer> output = input.apply(ParDo.of(new DoFn<String, Integer>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
          String inputString = c.element();
          c.output(inputString.length());
        }
      }));
      
      return output;
    }
  }

  public static class FilterEvenNumbers extends PTransform<PCollection<Integer>, PCollection<Integer>> {

    public static FilterEvenNumbers of() {
      return new FilterEvenNumbers();
    }

    @Override
    public PCollection<Integer> expand(PCollection<Integer> input) {
      PCollection<Integer> output = input.apply(ParDo.of(new DoFn<Integer, Integer>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
          Integer number = c.element();
          if (number % 2 == 0) {
            c.output(number);
          }
        }
      }));

      return output;
    }
  }
}