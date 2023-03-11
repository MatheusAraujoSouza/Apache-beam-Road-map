package partitionTraining;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Partition;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.Partition.PartitionFn;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;

public class partition {
  public static void main(String[] args) {
    Pipeline pipeline = Pipeline.create();

    // Create a PCollection of integers
    PCollection<Integer> input =
        pipeline.apply(Create.of(1, 2, 3, 4, 5, 6));

    // Partition the input PCollection into two partitions based on whether the element is even or odd
    PCollectionList<Integer> partitions =
        input.apply(
            Partition.of(
                2,
                new PartitionFn<Integer>() {
                  @Override
                  public int partitionFor(Integer element, int numPartitions) {
                    // Partition element into one of numPartitions partitions based on whether it's even or odd
                    if (element % 2 == 0) {
                      return 0;
                    } else {
                      return 1;
                    }
                  }
                }));

    // Process each partition independently
    PCollection<Integer> evenPartition = partitions.get(0);
    PCollection<Integer> oddPartition = partitions.get(1);
    
    evenPartition.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
    .apply("PrintNumbers", ParDo.of(new DoFn<String, Void>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            System.out.println("evenPartition "+c.element());
        }
    }));
    
    oddPartition.apply(MapElements.into(TypeDescriptors.strings()).via(Object::toString))
    .apply("PrintNumbers", ParDo.of(new DoFn<String, Void>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            

            System.out.println("oddPartition "+c.element());
        }
    }));


    pipeline.run();
  }
}