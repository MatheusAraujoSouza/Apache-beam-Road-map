# TRANSFORMATIONS

Transforms are the operations in your pipeline, and provide a generic processing framework. You provide processing logic in the form of a function object (colloquially referred to as “user code”), and your user code is applied to each element of an input <strong style="color:red">PCollection</strong> (or more than one <strong style="color:red">PCollection</strong>). Depending on the pipeline runner and back-end that you choose, many different workers across a cluster may execute instances of your user code in parallel. The user code running on each worker generates the output elements that are ultimately added to the final output PCollection that the transform produces.


### Applying Transformations 

 Applying transforms
To invoke a transform, you must apply it to the input PCollection. Each transform in the Beam SDKs has a generic apply method . Invoking multiple Beam transforms is similar to method chaining, but with one slight difference: You apply the transform to the input PCollection, passing the transform itself as an argument, and the operation returns the output PCollection. This takes the general form:

```java 
[Output PCollection] = [Input PCollection].apply([Transform])
```

Because Beam uses a generic apply method for PCollection, you can both chain transforms sequentially and also apply transforms that contain other transforms nested within (called composite transforms in the Beam SDKs).


```java 
[Final Output PCollection] = [Initial Input PCollection].apply([First Transform])
.apply([Second Transform])
.apply([Third Transform])
```
However, note that a transform does not consume or otherwise alter the input collection — remember that a PCollection is immutable by definition. This means that you can apply multiple transforms to the same input PCollection to create a branching pipeline, like so:

```java 
[PCollection of database table rows] = [Database Table Reader].apply([Read Transform])
[PCollection of 'A' names] = [PCollection of database table rows].apply([Transform A])
[PCollection of 'B' names] = [PCollection of database table rows].apply([Transform B])
```

# Core Beam transforms

Beam provides the following core transforms, each of which represents a different processing paradigm:


## ParDo
In Apache Beam, ParDo is a transform that applies a user-defined function to each element of a PCollection. It allows you to perform custom processing on the elements of a PCollection in a distributed and parallel manner.

ParDo is implemented as a DoFn function that defines the processing to be performed on each element of the PCollection. The DoFn function can take one or more @ProcessElement methods, which are called for each element of the PCollection, and can output zero or more elements using the ProcessContext.output() method.

Here is an example of using ParDo to transform a PCollection of integers into a PCollection of squares:


```java 
PCollection<Integer> input = p.apply(Create.of(1, 2, 3, 4, 5));
PCollection<Integer> output = input.apply(ParDo.of(new DoFn<Integer, Integer>() {
  @ProcessElement
  public void processElement(ProcessContext c) {
    int x = c.element();
    c.output(x * x);
  }
}));
```

In this example, the ParDo transform applies the processElement function to each element of the input PCollection. The function squares the input element and outputs the result using the ProcessContext.output() method. The output PCollection contains the squares of the elements in the input PCollection.


# How dofn work
 In Apache Beam, DoFn is a class that defines the processing to be performed on each element of a PCollection by the ParDo transform. A DoFn is a user-defined function that can take one or more @ProcessElement methods, which are called for each element of the PCollection, and can output zero or more elements using the ProcessContext.output() method.


 
```java 
class SquareFn extends DoFn<Integer, Integer> {
  @ProcessElement
  public void processElement(ProcessContext c) {
    int x = c.element();
    c.output(x * x);
  }
}
```

In this example, the SquareFn class extends the DoFn class and defines a @ProcessElement method that takes an input element from the PCollection, squares it, and outputs the result using the ProcessContext.output() method.

You can then use this DoFn class in a ParDo transform:

```java 
PCollection<Integer> input = ...;
PCollection<Integer> output = input.apply(ParDo.of(new SquareFn()));
```


DoFn<Integer, Integer> is a class definition that tells Apache Beam that the DoFn is going to work on a PCollection of integers as input and output the same. The DoFn class definition consists of two generic types: <InputT, OutputT>. The first generic type InputT is the type of the input PCollection elements and the second generic type OutputT is the type of the output PCollection elements.


It's also possible to define a DoFn that accepts multiple types of inputs and produces multiple types of outputs by using Tuple classes. For example, DoFn<Tuple2<Integer,String>, Tuple3<Integer,Integer,String>> this DoFn accepts a Tuple2 where the first element is an integer, and the second element is a string, and output is a Tuple3 with the first element being the square of the first element of the input Tuple2, the second element is the input first element, and the third element is the input second element of the Tuple2.


In order to perform some logic on each element of a PCollection inside a DoFn, you will typically use the ProcessContext object. The ProcessContext object provides access to the current element of the PCollection that is being processed, as well as methods for outputting zero or more elements, and accessing any side inputs that were passed to the DoFn.


The @ProcessElement annotation is used to mark a method inside the DoFn class as the processing logic for each element of the PCollection. This method will be called once for each element in the PCollection, and the ProcessContext object is passed as a parameter to the method.

Here's an example of a simple DoFn that applies some logic on each element of a PCollection using the ProcessContext:


```java 
class MyDoFn extends DoFn<Integer, String> {
  @ProcessElement
  public void processElement(ProcessContext c) {
    int x = c.element();
    if (x > 10) {
      c.output(x + " is greater than 10");
    } else {
      c.output(x + " is less than or equal to 10");
    }
  }
}
```


chained transformation examples: 

```java 
Pipeline pipeline = Pipeline.create(options);
PCollection<String> lines = pipeline.apply(TextIO.read().from("gs://dataflow-samples/shakespeare/kinglear.txt"));

// Split each line into words.
PCollection<String> words = lines.apply(ParDo.of(new DoFn<String, String>() {
  @ProcessElement
  public void processElement(ProcessContext c) {
    for (String word : c.element().split("[^a-zA-Z']+")) {
      c.output(word);
    }
  }
}));

// Count the number of occurrences of each word.
PCollection<KV<String, Long>> wordCounts = words.apply(Count.perElement());

// Format the results for printing.
PCollection<String> results = wordCounts.apply(MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
  @Override
  public String apply(KV<String, Long> input) {
    return input.getKey() + ": " + input.getValue();
  }
}));

// Write the results to a file.
results.apply(TextIO.write().to("wordcounts"));

pipeline.run().waitUntilFinish();
```

### PColectionsView 
In Apache Beam, a "view" is a way to materialize the contents of a PCollection (a collection of elements in the pipeline) so that it can be used as an input to another part of the pipeline. PCollectionView is the abstract base class that defines a view in Apache Beam. By using a PCollectionView, you can access the elements of the PCollection in a way that allows you to perform additional processing, such as aggregation or filtering.

To use a PCollectionView in a pipeline, you can do the following:

Create the PCollection that you want to use as a view.
Apply the appropriate transformation to create a PCollectionView from the PCollection. You can use View.asMap(), View.asList(), View.asIterable(), or View.asSingleton() to create different types of views.
Pass the PCollectionView as a side input to another part of the pipeline, by annotating the DoFn with @ProcessElement with the @SideInput annotation and passing it to the ParDo transformation with .withSideInputs(view).
In your DoFn function, you can access the elements of the view using the ProcessContext.sideInput() method, which returns an object of the same type as the PCollectionView. For example, if you created a PCollectionView using View.asList(), you can access the elements of the view in the DoFn by calling context.sideInput(view) and casting the result to a List.


```java 
PCollection<Integer> mainInput = ...;
PCollection<String> sideInput = ...;

// Create a view from the side input collection
PCollectionView<Iterable<String>> sideInputView = sideInput.apply(View.asIterable());

// Pass the view as a side input to the main transformation
mainInput.apply(ParDo.of(new MyDoFn(sideInputView)));
```




```java 
PCollection<String> words = pipeline.apply(...);
PCollection<KV<String, Long>> wordCounts = words
    .apply(Count.perElement());

final PCollectionView<Map<String, Long>> wordCountsView = wordCounts
    .apply(View.asMap());

PCollection<String> filteredWords = words
    .apply("FilterWords", ParDo.of(new DoFn<String, String>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        Map<String, Long> wordCountsMap = c.sideInput(wordCountsView);
        String word = c.element();
        Long count = wordCountsMap.get(word);
        if (count != null && count > 10) {
          c.output(word);
        }
      }
    }).withSideInputs(wordCountsView));
```


## via
 The .via method allows you to specify a sequence of transforms to apply to a given input. Essentially, the .via method allows you to chain multiple transforms together into a single pipeline, where each transform operates on the output of the previous transform.

The .via method takes two arguments: the first argument is the name of the transform you want to apply, and the second argument is the input to that transform. The input to the transform can be either a PCollection or a PCollectionList, depending on the type of transform you want to apply.


without via method: 
```java 
Pipeline p = Pipeline.create();
PCollection<String> lines = p.apply(TextIO.read().from("gs://my-bucket/input.txt"));
PCollection<String> words = lines.apply(ParDo.of(new ExtractWordsFn()));
PCollection<KV<String, Long>> wordCounts = words.apply(Count.perElement());
wordCounts.apply(TextIO.write().to("gs://my-bucket/output"));
p.run();
```

with via mthod: 

```java 
Pipeline p = Pipeline.create();
PCollection<String> lines = p.apply(TextIO.read().from("gs://my-bucket/input.txt"));
PCollection<String> words = lines.apply(ParDo.of(new ExtractWordsFn()));
PCollection<KV<String, Long>> wordCounts = words.apply(Count.perElement());
PCollection<String> formattedWords = wordCounts.apply(MapElements.via(new FormatWordsFn()));
formattedWords.apply(TextIO.write().to("gs://my-bucket/output"));
p.run();
```


The difference between .via and .apply in Apache Beam is that .via allows you to apply a transformation to a PCollection multiple times, whereas .apply applies the transformation to a PCollection once.

.via is used when you want to apply a sequence of transforms to a PCollection in a way that is more flexible than a single .apply. With .via, you can apply multiple transformations in a pipeline, and you can apply them in any order. This can be useful when you have multiple transforms that need to be performed on a single PCollection, or when you want to reuse a set of transforms in multiple parts of your pipeline.

In contrast, .apply applies a single transformation to a PCollection. It is the basic building block for creating a pipeline in Apache Beam, and it is used to specify the transforms that should be applied to a PCollection to produce the desired output.

In summary, .via provides more flexibility and control over the application of multiple transforms to a PCollection, while .apply is used to apply a single transform to a PCollection.

## MapElements

"MapElements" is a transformation in Apache Beam that allows you to apply a function to each element of a PCollection and produce a new PCollection with the results. This can be useful in a variety of data processing scenarios, such as transforming data into a different format, converting data types, or performing data cleaning and normalization.

One of the big advantages of using MapElements is that it allows you to perform these transformations in a parallel and scalable manner, taking advantage of the distributed nature of Apache Beam. This can greatly improve the performance of your data processing pipelines, especially for large data sets. Additionally, using MapElements can make your code more readable and maintainable, as you can encapsulate complex data transformations into a single, reusable function.

## parDo vs mapElements

both ParDo and MapElements allow you to perform custom transformations on the elements of a PCollection, but there are some differences between them that can influence when you should use one or the other.

In general, ParDo is more powerful and flexible, but also more complex to use. With ParDo, you can perform more complex transformations, like splitting a single element into multiple outputs, grouping elements into different PCollections based on some criteria, and more.

MapElements is a simpler operation that can be used for straightforward transformations, like converting elements from one type to another, adding or removing fields, etc. If you need to perform a simple transformation, then MapElements is usually the easier choice.

So, the general rule of thumb is to use ParDo when you need to perform a complex transformation and MapElements when you need to perform a simple one.

Example: 

```java 
PCollection<String> words = ...;
PCollection<Integer> wordLengths = words.apply(
    MapElements.into(TypeDescriptors.integers())
        .via(word -> word.length()));
```

In this example, the input PCollection of words is transformed into a PCollection of their length by using the MapElements transform. The MapElements transform takes as input a SimpleFunction that specifies the transformation to apply to each element of the input PCollection. In this case, the transformation is the length() method of the String class. The MapElements transform also requires a type descriptor for the output elements, which is specified using TypeDescriptors.integers().

The MapElements transform can be a more efficient alternative to ParDo when the transformation applied to each element is simple and can be represented using a SimpleFunction. On the other hand, if the transformation applied to each element is more complex or requires access to side inputs, then ParDo is the more appropriate transform to use.

## DoFn lifeCycle


 The Apache Beam DoFn lifecycle refers to the sequence of events that occur when executing a DoFn. The lifecycle is defined by the following steps:

1- Initialization: When a DoFn is created, it is initialized with any required resources or configurations. This is usually done in the DoFn's constructor.

2- Preparation: Before processing the first element, the DoFn's startBundle method is called. This method is used to set up any state or resources that are required for processing.

3- Processing: For each input element, the DoFn's processElement method is called. This method performs the actual processing of the element, such as transforming the data, filtering out unwanted data, or aggregating results.

4- Completion: After all elements have been processed, the DoFn's finishBundle method is called. This method is used to perform any final processing or cleanup that is required.

5- Teardown: Finally, the DoFn's resources are released, and the DoFn is destroyed.

6- These steps provide a structure for writing and executing processing functions in Apache Beam, making it easier to create and manage data processing pipelines.




You have a DoFn that needs to keep track of some state across multiple elements in a PCollection. The startBundle method is a convenient place to initialize the state, and the finishBundle method is a convenient place to persist the state for later use.


```java 
public class MyDoFn extends DoFn<InputT, OutputT> {
  private Map<String, Integer> wordCounts = new HashMap<>();

  @StartBundle
  public void startBundle(Context c) {
    // Initialize the state
    wordCounts = new HashMap<>();
  }

  @ProcessElement
  public void processElement(ProcessContext c) {
    String word = c.element().getWord();
    int count = wordCounts.getOrDefault(word, 0) + 1;
    wordCounts.put(word, count);
    c.output(word, count);
  }

  @FinishBundle
  public void finishBundle(Context c) {
    // Persist the state
    ...
  }
}
```


where we have a PCollection of integers and we want to extract the squares of the numbers in the collection. Here's one way to do it using ParDo without explicitly using the lifecycle methods:



```java 
PCollection<Integer> numbers = ...;
PCollection<Integer> squares = numbers.apply(
    ParDo.of(new DoFn<Integer, Integer>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            int number = c.element();
            int square = number * number;
            c.output(square);
        }
    }));
```
In this example, we use a ParDo transform to process each element in the numbers PCollection and compute its square. The computation is done in the processElement method.

Now let's see how this example can be improved by using the lifecycle methods:

```java 
PCollection<Integer> numbers = ...;
PCollection<Integer> squares = numbers.apply(
    ParDo.of(new DoFn<Integer, Integer>() {
        @StartBundle
        public void startBundle(Context c) {
            // Code to run before processing the first element in a bundle.
            // For example, initializing state.
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            int number = c.element();
            int square = number * number;
            c.output(square);
        }

        @FinishBundle
        public void finishBundle(Context c) {
            // Code to run after processing the last element in a bundle.
            // For example, cleaning up state.
        }
    }));

```


In this example, we added three lifecycle methods: startBundle, processElement, and finishBundle. The startBundle method is called before processing the first element in a bundle, and the finishBundle method is called after processing the last element in a bundle. In this example, we're not actually doing anything with the lifecycle methods, but in real-world use cases, these methods can be used to initialize and clean up state, such as counters, accumulators, or stateful variables that are used within the processElement method.

By using the lifecycle methods, we have more control over the processing of elements in a PCollection. This can be useful for scenarios where we need to track state or perform additional processing before and after processing each element.


## GroupByKey

"GroupByKey is a Beam transform for processing collections of key/value pairs. It’s a parallel reduction operation, analogous to the Shuffle phase of a Map/Shuffle/Reduce-style algorithm. The input to GroupByKey is a collection of key/value pairs that represents a multimap, where the collection contains multiple pairs that have the same key, but different values. Given such a collection, you use GroupByKey to collect all of the values associated with each unique key." -- Beam documentation 

GroupByKey is a Beam transform that groups the elements of a PCollection by their keys. It is similar to the group by clause in SQL. When you apply GroupByKey to a PCollection, it creates a new PCollection where each element is a key-value pair, where the key is the unique key of a group and the value is an iterable of all the values in that group.

Here's an example of how to use GroupByKey in Java:


```java 
PCollection<KV<String, Integer>> input = ...;

PCollection<KV<String, Iterable<Integer>>> grouped = input.apply(GroupByKey.create());

// Do something with the grouped PCollection

```


In this example, input is a PCollection of key-value pairs, where the keys are strings and the values are integers. When we apply GroupByKey to input, it groups the elements by key and creates a new PCollection of key-value pairs, where the keys are the unique keys from input and the values are iterables of all the values with that key.

Once you have a PCollection that has been grouped using GroupByKey, you can use it in subsequent transformations, such as ParDo. For example:


```java 
PCollection<KV<String, Iterable<Integer>>> grouped = ...;

PCollection<KV<String, Integer>> sum = grouped.apply(ParDo.of(new DoFn<KV<String, Iterable<Integer>>, KV<String, Integer>>() {
    @ProcessElement
    public void processElement(ProcessContext c) {
        KV<String, Iterable<Integer>> kv = c.element();
        String key = kv.getKey();
        Iterable<Integer> values = kv.getValue();
        int sum = 0;
        for (Integer value : values) {
            sum += value;
        }
        c.output(KV.of(key, sum));
    }
}));

```

In this example, we have a PCollection that has been grouped using GroupByKey. We then apply a ParDo transform to the grouped PCollection. The ParDo takes an element from the input PCollection, which is a key-value pair where the key is a string and the value is an iterable of integers. The ParDo then calculates the sum of the integers and outputs a new key-value pair, where the key is the same string and the value is the sum of the integers.


## CoGroupByKey


"
  CoGroupByKey performs a relational join of two or more key/value PCollections that have the same key type. Design Your Pipeline shows an example pipeline that uses a join.

  Consider using CoGroupByKey if you have multiple data sets that provide information about related things. For example, let’s say you have two different files with user data: one file has names and email addresses; the other file has names and phone numbers. You can join those two data sets, using the user name as a common key and the other data as the associated values. After the join, you have one data set that contains all of the information (email addresses and phone numbers) associated with each name.
" -- documentation Guide apache-beam


The CoGroupByKey transform in Apache Beam is used to combine two or more PCollections of key-value pairs based on their keys. It is similar to the GroupByKey transform, but it works on multiple collections simultaneously.

CoGroupByKey takes multiple input PCollections and groups the elements by their keys. The resulting output PCollection is a collection of key-value pairs, where the key is the original key and the value is a CoGbkResult object. The CoGbkResult object contains an iterable of values for each input PCollection.


```java
PCollection<KV<String, Integer>> pc1 = ...;
PCollection<KV<String, String>> pc2 = ...;

PCollection<KV<String, CoGbkResult>> result = KeyedPCollectionTuple
    .of(pc1, someTag)
    .and(pc2, someOtherTag)
    .apply(CoGroupByKey.create());

PCollection<KV<String, Tuple2<Iterable<Integer>, Iterable<String>>>> finalResult = result
    .apply(ParDo.of(new DoFn<KV<String, CoGbkResult>, KV<String, Tuple2<Iterable<Integer>, Iterable<String>>>>() {
      @ProcessElement
      public void processElement(ProcessContext c) {
        KV<String, CoGbkResult> e = c.element();
        Iterable<Integer> pc1Values = e.getValue().getAll(someTag);
        Iterable<String> pc2Values = e.getValue().getAll(someOtherTag);
        c.output(KV.of(e.getKey(), Tuple2.of(pc1Values, pc2Values)));
      }
    }));
```

In this example, we have two input PCollections, pc1 and pc2, that we want to co-group based on their common key. We use KeyedPCollectionTuple to tag each input PCollection with a unique tag. Then, we pass the tagged PCollections to CoGroupByKey to co-group them based on their common key. Finally, we use ParDo to process the resulting CoGbkResult object, extracting the values from each input PCollection using the respective tags. We then output a KV pair with the original key and a tuple containing the iterable of values from each input PCollection.

Another example:

```java
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.transforms.windowing.WindowFn;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class BundleDataExample {

  public static void main(String[] args) {
    List<Integer> input = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      input.add(i);
    }

    Pipeline pipeline = Pipeline.create();
    PCollection<Integer> inputCollection = pipeline.apply(Create.of(input));

    int bundleSize = 3;
    PCollection<KV<Integer, List<Integer>>> bundledData = inputCollection
        .apply(Window.into(FixedWindows.of(Duration.standardMinutes(1))))
        .apply(ParDo.of(new BundleDataFn(bundleSize)))
        .apply(GroupByKey.create())
        .apply(ParDo.of(new MergeBundlesFn()));

    bundledData.apply(ParDo.of(new OutputBundleFn()));
    
    pipeline.run();
  }

  static class BundleDataFn extends DoFn<Integer, KV<Integer, Integer>> {
    private int bundleSize;
    private int bundleId = 0;

    public BundleDataFn(int bundleSize) {
      this.bundleSize = bundleSize;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
      int value = c.element();
      KV<Integer, Integer> bundleEntry = KV.of(bundleId, value);
      c.output(bundleEntry);

      if ((bundleId + 1) % bundleSize == 0) {
        bundleId++;
      }
    }
  }

  static class MergeBundlesFn extends DoFn<KV<Integer, Iterable<Integer>>, KV<Integer, List<Integer>>> {
    @ProcessElement
    public void processElement(ProcessContext c) {
      int bundleId = c.element().getKey();
      List<Integer> bundleValues = new ArrayList<>();
      for (Integer value : c.element().getValue()) {
        bundleValues.add(value);
      }
      KV<Integer, List<Integer>> bundle = KV.of(bundleId, bundleValues);
      c.output(bundle);
    }
  }

  static class OutputBundleFn extends DoFn<KV<Integer, List<Integer>>, Void> {
    @ProcessElement
    public void processElement(ProcessContext c) {
      KV<Integer, List<Integer>> bundle = c.element();
      System.out.println("Bundle " + bundle.getKey() + ": " + bundle.getValue());
    }
  }
}
```

In this example, we create a list of 10 integers and define a bundle size of 3. We apply a Window transform with a FixedWindows windowing function to split the input into windows of 1 minute duration, and then apply a ParDo transform that uses a BundleDataFn to assign each element to a bundle. The BundleDataFn creates a key-value pair with the bundle ID as the key and the element as the value. When the number of elements assigned to a bundle reaches the bundle size, the BundleDataFn increments the bundle ID.

The resulting key-value pairs are then grouped by key using a GroupByKey transform, and the values in each group


## Combine
The Combine transformation in Apache Beam is used to combine elements in a PCollection using a specified combining function. The combining function takes two input values and produces a single output value. The output value can be of a different type than the input values.

Here is an example of how to use the Combine transformation to compute the sum of a PCollection of integers:

```java
PCollection<Integer> numbers = ... // a PCollection of integers
PCollection<Integer> sum = numbers.apply(Combine.globally(new SumIntegersFn()));
```
In this example, we use the SumIntegersFn class to define the combining function:
```java
public class SumIntegersFn extends CombineFn<Integer, Integer, Integer> {
  public Integer createAccumulator() { return 0; }
  public Integer addInput(Integer sum, Integer input) { return sum + input; }
  public Integer mergeAccumulators(Iterable<Integer> accumulators) {
    int merged = 0;
    for (Integer accumulator : accumulators) {
      merged += accumulator;
    }
    return merged;
  }
  public Integer extractOutput(Integer sum) { return sum; }
}
```
In this example, the createAccumulator method initializes the accumulator to 0, the addInput method takes the current sum and adds the next element to it, and the mergeAccumulators method merges two accumulators into one. Finally, the extractOutput method returns the final sum.
The header CombineFn<Integer, Integer, Integer> represents the input, output and accumulator types for a CombineFn in Apache Beam.

The first type parameter Integer represents the type of elements being combined. In this case, the elements are of type Integer.
The second type parameter Integer represents the type of the accumulator. The accumulator is a mutable container that is used to accumulate the intermediate results during the combine operation. In this case, the accumulator is also of type Integer.
The third type parameter Integer represents the type of the output that will be produced after all the elements have been combined. In this case, the output is also of type Integer.

So, when the Combine transformation is applied to the numbers PCollection, it combines all the integers in the PCollection using the SumIntegersFn function to produce a single output value.


the CombineFn interface is a standard interface in the Apache Beam library. It defines the methods that need to be implemented for a function that can combine elements of a certain type.


The SumIntsFn class has three methods that need to be implemented as part of the CombineFn interface:
<br>
<br>
1- createAccumulator() - creates a new mutable accumulator for a new key.<br>
2- addInput(accumulator, input) - adds an input value to an accumulator.<br>
3-mergeAccumulators(accumulators) - merges accumulators from multiple threads or machines.<br><br>
These methods are used by the Beam framework to combine elements in a distributed and parallelized manner. When the combine() method is called, Beam will create a new instance of SumIntsFn, call createAccumulator() to create a new mutable accumulator for the key, and then call addInput() for each input element to add it to the accumulator. Finally, it will call mergeAccumulators() to merge the accumulators from different workers and return the final combined result.

In this example, we use the SumIntegersFn class to define the combining function:



## Flatten 

In Java, Apache Beam uses the Flatten transform to combine multiple PCollections of the same type into a single PCollection. The Flatten transform takes a PCollectionList as input and outputs a single PCollection that contains all the elements from all the input PCollections.


```java
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

Pipeline pipeline = Pipeline.create();

PCollection<String> pcollection1 = pipeline.apply(TextIO.read().from("source1.txt"));
PCollection<String> pcollection2 = pipeline.apply(TextIO.read().from("source2.txt"));

PCollectionList<String> pcollectionList = PCollectionList.of(pcollection1).and(pcollection2);
PCollection<String> mergedPCollection = pcollectionList.apply(Flatten.<String>pCollections());
```

In this example, we first create two PCollections pcollection1 and pcollection2 using TextIO.read().from(). We then create a PCollectionList using PCollectionList.of() and and() methods, and add the input PCollections to the list.

We then apply the Flatten transform to the PCollectionList using the apply() method, specifying the data type of the PCollections using <String>.

The output of the Flatten transform is a single PCollection called mergedPCollection that contains all the elements from pcollection1 and pcollection2.

It's important to note that the order of elements in the output PCollection is not guaranteed. If you need to preserve the order of elements, you should use a different transform like GroupByKey or Combine.PerKey(). Also, note that the PCollectionList can contain any number of PCollections, and they don't have to be of the same size.


## Partition

In Apache Beam, a partition is a logical subset of a larger data set that can be processed independently by a worker. Partitions are used to parallelize processing of data, which can help to improve the overall performance and efficiency of a pipeline.

The concept of partitioning is particularly important in Apache Beam because it is designed to work with large-scale data processing pipelines that run on distributed computing systems, such as Apache Spark or Google Cloud Dataflow. By partitioning data into smaller subsets, each worker can process a smaller amount of data, which can result in faster processing times.

In Apache Beam, partitioning can be performed in a variety of ways, depending on the specific needs of the pipeline. For example, you can partition data based on a specific field value, such as partitioning a customer database by state or ZIP code. Alternatively, you can partition data randomly, or based on some other criteria that makes sense for your particular use case.

Once data has been partitioned, it can be processed in parallel by multiple workers, which can help to significantly speed up the overall processing time. Apache Beam provides a variety of built-in transforms that can be used to partition data, such as the Partition transform, which allows you to partition data based on a user-defined function.


## Requirements for writing user code for Beam transforms
When you write user code for a Beam transform, you need to keep in mind that your code will be executed in a distributed manner across multiple machines.

Specifically, your function object must be serializable, meaning that it can be converted into a form that can be sent over the network. This is necessary because your code will be executed on multiple machines, and the function object needs to be sent to each machine.

In addition, your function object must be thread-compatible, meaning that it can be executed in a multi-threaded environment. However, the Beam SDKs are not thread-safe, so you need to be careful when writing multi-threaded code to ensure that it is safe to execute in a distributed environment.

Finally, it's recommended that you make your function object idempotent, which means that if the function is executed multiple times with the same input, it will produce the same output each time. This is important because the function may be retried or run multiple times on different machines, and you want to ensure that the output is consistent regardless of how many times the function is executed.


## Thread-compatibility

In Apache Beam, the functions that are used for transformations and analysis are executed on multiple worker nodes in a distributed manner. This means that the same function instance may be accessed by multiple threads on different worker nodes simultaneously. Therefore, it is important to ensure that the function is thread-compatible, meaning that it can be safely accessed by multiple threads without causing race conditions or other issues.

When a Beam pipeline is executed, it automatically manages the distribution of work across multiple worker nodes. Each worker node may have multiple threads running concurrently to process data. When a function is executed on a worker node, it is typically executed on a single thread, unless the function itself spawns additional threads.

However, the Beam SDKs themselves are not thread-safe, meaning that they are not designed to be used by multiple threads simultaneously. Therefore, if you create your own threads in your user code, you must ensure that any interactions with the Beam SDK are properly synchronized to avoid thread-safety issues.

```java
import org.apache.beam.sdk.transforms.DoFn;

public class NotThreadSafeFn extends DoFn<Integer, Void> {

  // Shared counter variable
  private int count;

  @ProcessElement
  public void processElement(ProcessContext c) {
    // Increment the counter
    count++;
    c.output(null);
  }
}
```

In this example, the NotThreadSafeFn function has a shared counter variable count. Each time processElement is called, it increments count by one. However, since this function is not thread-safe, it can lead to race conditions if multiple threads access the count variable at the same time.

If this function is used in a Beam pipeline with parallelism greater than one, multiple threads on different worker machines may try to access the count variable concurrently, leading to unpredictable and incorrect results. To make this function thread-safe, you would need to add synchronization, such as using the synchronized keyword or other thread-safe mechanisms, to ensure that only one thread can access the count variable at a time.

Additionally, note that static members in your function object are not passed to worker instances. This means that if you rely on static variables or methods in your function, those will not be available on worker nodes and your function may behave differently than expected.
 ```java
 import org.apache.beam.sdk.transforms.SerializableFunction;
import java.util.List;

public class SumFunction implements SerializableFunction<List<Integer>, Integer> {
  private int total = 0;

  @Override
  public Integer apply(List<Integer> input) {
    for (int i : input) {
      total += i;
    }
    return total;
  }
}
 ```


 In this example, the total variable is static, meaning that it belongs to the class rather than any specific instance of the class. This can lead to problems when executing the function in a distributed environment like Beam, because each worker node will have its own copy of the class, and therefore its own copy of the total variable.

 To make the code thread-safe and improve performance, you should use a counter that is safe to use across multiple threads, such as AtomicInteger, and avoid using non-final static variables in your function. Here is an example of how the code could be rewritten:

 ```java
 import org.apache.beam.sdk.transforms.DoFn;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeFn extends DoFn<Integer, Void> {

  // Shared counter variable
  private AtomicInteger count = new AtomicInteger(0);

  @ProcessElement
  public void processElement(ProcessContext c) {
    // Increment the counter
    count.incrementAndGet();
    c.output(null);
  }
}
 ```


In this example, we use AtomicInteger instead of int for the counter variable. AtomicInteger is a thread-safe class that provides atomic operations, so it can be safely used across multiple threads. We also removed the static modifier from the counter variable, as static variables are not recommended for use in Beam functions.

By making these changes, the code should be thread-safe and have better performance when used in a multi-threaded environment.

This can cause unexpected behavior, because the sum calculated on each worker will be added to its own copy of total, rather than being accumulated into a single total across all workers.

To avoid this problem, it's best to avoid using static variables in your Beam functions. Instead, you should pass any necessary state as arguments to your function, or use instance variables that are serialized along with the function object.

In summary, it is important to ensure that any functions used in Beam transformations and analysis are thread-compatible to avoid issues with multiple threads accessing the same function instance simultaneously. If you need to create your own threads in your user code, you must ensure proper synchronization to avoid thread-safety issues. Finally, note that static members in your function object are not passed to worker instances, so you should avoid relying on static variables or methods in your functions.


## side inputs

In Apache Beam, a ParDo transform can have additional inputs called "side inputs" that can be accessed by the DoFn while processing the elements of the main input PCollection. These side inputs are created by using a different method than the usual apply method of the transform.

The side input provides additional data that can be read by the DoFn during processing of each element of the main input PCollection. The values of side inputs can be determined at runtime and may depend on the main input data or another branch of the pipeline.

```java
PCollection<Integer> mainInput = ...;
PCollection<String> sideInput = ...;

PCollection<Integer> output = mainInput.apply(ParDo.of(new DoFn<Integer, Integer>() {
    @ProcessElement
    public void processElement(ProcessContext c) {
        // Access the side input values
        Iterable<String> sideInputValues = c.sideInput(sideInput);
        
        // Process the main input element with the side input values
        Integer inputValue = c.element();
        // ...
        
        // Emit the output
        c.output(outputValue);
    }
}).withSideInputs(sideInput));
```

In this example, the ParDo transform has a main input PCollection of integers and a side input PCollection of strings. The DoFn accesses the side input values by calling the c.sideInput(sideInput) method inside the @ProcessElement method. The side input values are returned as an Iterable of values. The DoFn then processes each element of the main input PCollection with the side input values and outputs the results to the output PCollection.

more examples of side inputs

```java
PCollection<Integer> mainInput = ...;
PCollection<Integer> filterValues = ...;

PCollection<Integer> output = mainInput.apply(ParDo.withSideInputs(filterValues)
    .of(new DoFn<Integer, Integer>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            Iterable<Integer> filterInput = c.sideInput(filterValues);
            Integer inputValue = c.element();
            
            if (Iterables.contains(filterInput, inputValue)) {
                c.output(inputValue);
            }
        }
    }));
```

## Performing a join with a side input:


```java
PCollection<Integer> mainInput = ...;
PCollection<KV<Integer, String>> joinValues = ...;

PCollection<KV<Integer, String>> output = mainInput.apply(ParDo.withSideInputs(joinValues)
    .of(new DoFn<Integer, KV<Integer, String>>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            Iterable<KV<Integer, String>> joinInput = c.sideInput(joinValues);
            Integer inputValue = c.element();
            
            for (KV<Integer, String> joinValue : joinInput) {
                if (joinValue.getKey().equals(inputValue)) {
                    c.output(KV.of(inputValue, joinValue.getValue()));
                }
            }
        }
    }));

```
In this example, we are performing a join between mainInput and joinValues using the common key Integer. We use a side input joinValues to access the join data and output a KV<Integer, String> where the key is the input element and the value is the corresponding value from joinValues.


## Dynamic scaling with a side input:

```java
PCollection<Integer> mainInput = ...;
PCollection<Integer> scaleFactor = ...;

PCollection<Integer> output = mainInput.apply(ParDo.withSideInputs(scaleFactor)
    .of(new DoFn<Integer, Integer>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
            Iterable<Integer> scaleInput = c.sideInput(scaleFactor);
            Integer inputValue = c.element();
            
            for (Integer factor : scaleInput) {
                c.output(inputValue * factor);
            }
        }
    }));
```

In this example, we are dynamically scaling the input values in mainInput based on a side input scaleFactor. We output the scaled values for each factor in scaleFactor. This can be useful if you want to scale your data dynamically based on some external input (e.g., the number of workers in your pipeline).

references: 
https://beam.apache.org/documentation/programming-guide/#applying-transforms