package view;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;

public class interable {
	
	public static void main(String args[]) {
		Pipeline pipeline = Pipeline.create();
		PCollection<Integer> mainInput = pipeline.apply(Create.of(1,2,3,4,5));
		PCollection<Integer> secondInput = pipeline.apply(Create.of(7,7,7,7,7));
		
		
		// Create a view from the side input
		PCollectionView<Iterable<Integer>> sideInputView = secondInput.apply(View.asIterable());
		
		
		
	    // Perform some processing using both the main input and side input values
		PCollection<Integer> valueReturned = mainInput.apply(ParDo.of(new DoFn<Integer, Integer>() {
			  @ProcessElement
			  public void processElement(ProcessContext c) {
			    Integer mainInputValue = c.element();

			    // Get the values from the side input view
			    Iterable<Integer> sideInputValues = c.sideInput(sideInputView);

			    // Perform some processing using both the main input and side input values
			    Integer result = 0;
			    for (Integer sideInputValue : sideInputValues) {
			      result += sideInputValue;
			    }
			    result += mainInputValue;

			    // Output the result
			    c.output(result);
			  }
			}).withSideInputs(sideInputView));
		
		
		valueReturned.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
	    .apply("PrintNumbers",ParDo.of(new DoFn<String,Void>(){
	    	
	    	@ProcessElement
	    	public void processElement(ProcessContext c) {
	        	System.out.println(c.element());

	    	}
	    }));
		
		
	    pipeline.run();
	}

}
