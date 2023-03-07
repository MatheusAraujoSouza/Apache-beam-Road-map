package coGroupByKey;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypeDescriptors;

import java.util.Arrays;
import java.util.List;

public class coGroupByKeyTraining {
	
	 public static void main(String[] args) {
		Pipeline pipeline = Pipeline.create();
		PCollection<KV<String, Integer>> pc1 = pipeline.apply(Create.of(1, 2, 3))
			    .apply(WithKeys.of("tag1"));
			PCollection<KV<String, Integer>> pc2 = pipeline.apply(Create.of(2, 3, 4))
			    .apply(WithKeys.of("tag2"));
			final TupleTag<Integer> tag1 = new TupleTag<>();
			final TupleTag<Integer> tag2 = new TupleTag<>();
			PCollection<KV<String, CoGbkResult>> result = KeyedPCollectionTuple
			    .of(tag1, pc1)
			    .and(tag2, pc2)
			    .apply(CoGroupByKey.create());

			PCollection<Integer> output = result.apply(ParDo.of(new DoFn<KV<String, CoGbkResult>, Integer>() {
			    @ProcessElement
			    public void processElement(ProcessContext c) {
			        KV<String, CoGbkResult> e = c.element();
			        Integer tag1Value = e.getValue().getOnly(tag1, 0);
			        Integer tag2Value = e.getValue().getOnly(tag2, 0);
			        c.output(tag1Value + tag2Value);
			    }
			}));
		
		output.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
	    .apply("PrintNumbers",ParDo.of(new DoFn<String,Void>(){
	    	
	    	@ProcessElement
	    	public void processElement(ProcessContext c) {
	        	System.out.println(c.element());

	    	}
	    }));
		
        pipeline.run();

				
	}

}
