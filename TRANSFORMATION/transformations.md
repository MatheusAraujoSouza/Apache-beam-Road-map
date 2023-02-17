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


1- The Apache Beam DoFn lifecycle refers to the sequence of events that occur when executing a DoFn. The lifecycle is defined by the following steps:

2- Initialization: When a DoFn is created, it is initialized with any required resources or configurations. This is usually done in the DoFn's constructor.

3- Preparation: Before processing the first element, the DoFn's startBundle method is called. This method is used to set up any state or resources that are required for processing.

4- Processing: For each input element, the DoFn's processElement method is called. This method performs the actual processing of the element, such as transforming the data, filtering out unwanted data, or aggregating results.

5- Completion: After all elements have been processed, the DoFn's finishBundle method is called. This method is used to perform any final processing or cleanup that is required.

6- Teardown: Finally, the DoFn's resources are released, and the DoFn is destroyed.

7- These steps provide a structure for writing and executing processing functions in Apache Beam, making it easier to create and manage data processing pipelines.




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



references: 
https://beam.apache.org/documentation/programming-guide/#applying-transforms