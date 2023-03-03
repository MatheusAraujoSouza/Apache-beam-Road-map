package training1;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;

public class numberMultipler {

  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Create mock input data
    PCollection<Integer> numbers = pipeline.apply(Create.of(1, 2, 3, 4, 5));

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
    
       
    PCollection<Double> divideNumber = multipliedNumbers.apply(ParDo.of(new DoFn<Integer,Double >(){
    	@ProcessElement
    	public void processElement(ProcessContext c) {
            Integer input = c.element();
    		 double output = input.doubleValue() / 5.0;
    	        c.output(output);
    	}
    }));
    
    PCollection<Double> SumNumberPerElement = multipliedNumbers.apply(ParDo.of(new DoFn<Integer,Double >(){
    	@ProcessElement
    	public void processElement(ProcessContext c) {
            Integer input = c.element();
    		 double output = input.doubleValue() + 12.0;
    	        c.output(output);
    	}
    }));
    
    
    PCollection<Double> sum = divideNumber.apply(Sum.doublesGlobally());
    
    PCollection<Double> convertedDivideNumber = divideNumber.apply(MapElements.into(TypeDescriptors.doubles()).via(Double::valueOf));
    
    PCollectionList<Double> collections = PCollectionList.of(sum).and(convertedDivideNumber);
    PCollection<Double> combined = collections.apply(Flatten.<Double>pCollections());

    

    //To print the output value 
    
    combined.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
    .apply("PrintNumbers",ParDo.of(new DoFn<String,Void>(){
    	
    	@ProcessElement
    	public void processElement(ProcessContext c) {
        	System.out.println(c.element());

    	}
    }));



    pipeline.run();
  }
}