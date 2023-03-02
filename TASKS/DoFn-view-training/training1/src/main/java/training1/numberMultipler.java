package training1;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

public class numberMultipler {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Create mock input data
    TestStream<String> input = TestStream.create(StringUtf8Coder.of())
        .addElements("1", "2", "3", "4", "5")
        .advanceWatermarkToInfinity();

    PCollection<Integer> numbers = pipeline.apply(input)
        .apply(MapElements.into(TypeDescriptors.integers()).via(Integer::valueOf));

    PCollection<Integer> multipliedNumbers = numbers.apply(ParDo.of(new DoFn<Integer, Integer>() {
      /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	@ProcessElement
      public void processElement(ProcessContext c) {
        Integer input = c.element();
        Integer output = input * 30;
        c.output(output);
      }
    }));
    
    
       
    PCollection<Float> divideNumber = multipliedNumbers.apply(ParDo.of(new DoFn<Integer,Float >(){
    	@ProcessElement
    	public void processElement(ProcessContext c) {
            Integer input = c.element();
    		 float output = input.floatValue() / 5.0f;
    	        c.output(output);
    	}
    }));
    
    //To print the output value 
    
    divideNumber.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
    .apply("PrintNumbers",ParDo.of(new DoFn<String,Void>(){
    	
    	@ProcessElement
    	public void processElement(ProcessContext c) {
        	System.out.println(c.element());

    	}
    }));



    pipeline.run();
  }
}