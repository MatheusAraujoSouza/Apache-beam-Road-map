package training1;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

public class restOfDivision {

	  public static void main(String[] args) {
		    Pipeline pipeline = Pipeline.create();

		    // Create mock input data
		    TestStream<String> input = TestStream.create(StringUtf8Coder.of())
		        .addElements("1", "2", "3", "4", "5")
		        .advanceWatermarkToInfinity();

		    PCollection<Integer> numbers = pipeline.apply(input)
		        .apply(MapElements.into(TypeDescriptors.integers()).via(Integer::valueOf));
		    
		    PCollection<Integer> division = numbers.apply("applying the remainder of division",ParDo.of(new DoFn<Integer, Integer>(){
		    	@ProcessElement
		    	public void processElement(ProcessContext c) {
		    		int input = c.element();
		    		if(input % 2 == 0) {
		    			c.output(input);
		    		}
		    	}
		    })); 
		    
		    
		    division.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
		    .apply("Print Division", ParDo.of(new DoFn<String,Void>(){
		    	@ProcessElement
		    	public void processElement(ProcessContext c) {
		        	System.out.println(c.element());
		    	}
		    }));
		  
		    pipeline.run();
		  
	  }
}
