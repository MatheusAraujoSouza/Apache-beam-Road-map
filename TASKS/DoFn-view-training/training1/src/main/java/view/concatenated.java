package view;

import java.util.List;

import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;


public class concatenated {
	
	public static void main(String args[]) {
		Pipeline pipeline = Pipeline.create();
		PCollection<String> firstPCollection = pipeline.apply(Create.of("Matheus","Marcos","Lucas","Joao"));
		PCollection<String> secondPCollection = pipeline.apply(Create.of("Atos","Romanos","Corinthios","Galatas"));
		PCollectionView<List<String>> sideInput = secondPCollection.apply(View.asList());
		
		PCollection<String> outputPCollection = firstPCollection.apply(ParDo.of(new DoFn<String,String>(){
			@ProcessElement
			public void processElement(ProcessContext c) {
				String element= c.element();
				List<String> sideInputValues = c.sideInput(sideInput);
				String concatenated  = element;
				for(String sideInputValue : sideInputValues) {
					concatenated += "-" + sideInputValue;
				}
				c.output(concatenated);
			}
		}).withSideInputs(sideInput));
		
		outputPCollection.apply(ParDo.of(new DoFn<String,Void>(){
			@ProcessElement
			public void processElement(ProcessContext c) {
				System.out.println(c.element());
				c.output(null);
			}
		}));
		
		
		pipeline.run();
		
	}

}
