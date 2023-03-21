# Additional outputs 

ParDo is a data processing transform in Apache Beam that is used to apply a user-defined function (also called a "DoFn") to each element of a PCollection. The ParDo transform always produces a main output PCollection, which contains the result of applying the user-defined function to each input element.

However, in addition to the main output PCollection, you can also use ParDo to produce any number of additional output PCollections. These additional output PCollections are created using the "SideOutput" feature of ParDo, which allows you to tag output elements with a specific side-output tag.

When you choose to have multiple outputs in your ParDo transform, your ParDo function returns all of the output PCollections (including the main output) bundled together. These output PCollections are bundled together into a special object called a "TupleTaggedOutput" object, which contains all of the output PCollections as key-value pairs, where the key is the side-output tag and the value is the output PCollection.

For example, let's say you have a ParDo function that processes input elements and produces two types of output: "valid" elements and "invalid" elements. You could use the SideOutput feature of ParDo to tag each output element with either the "valid" or "invalid" side-output tag, and then return a TupleTaggedOutput object containing both the "valid" and "invalid" output PCollections. You could then use these output PCollections in downstream processing steps, such as filtering out invalid elements or processing valid elements differently than invalid elements.


here's an example of how to use multiple output tags in Apache Beam using Java:

Suppose we have a PCollection of integers, and we want to process each integer and output it to different PCollections based on whether it is even or odd. We can achieve this using the ParDo transform with multiple output tags.

First, we define the multiple output tags:
```java
public static final TupleTag<Integer> EVEN_TAG = new TupleTag<Integer>(){};
public static final TupleTag<Integer> ODD_TAG = new TupleTag<Integer>(){};
```
We define two TupleTag objects, EVEN_TAG and ODD_TAG, to represent the even and odd output PCollections, respectively.

Next, we define our ParDo function:

```java
public static class EvenOddDoFn extends DoFn<Integer, Integer> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        Integer input = c.element();
        if (input % 2 == 0) {
            c.output(EVEN_TAG, input);
        } else {
            c.output(ODD_TAG, input);
        }
    }
}
```

Our ParDo function takes an input element of type Integer and outputs it to either the EVEN_TAG or ODD_TAG output PCollection based on whether it is even or odd. We use the ProcessContext object to output the element to the appropriate output PCollection using the output method.

Finally, we apply the ParDo transform with the multiple output tags:

```java
PCollectionTuple outputs = input.apply(ParDo.of(new EvenOddDoFn())
    .withOutputTags(EVEN_TAG, TupleTagList.of(ODD_TAG)));
```

Here, we apply the ParDo transform with the EvenOddDoFn function and specify the multiple output tags using the withOutputTags method. The EVEN_TAG output PCollection is the main output PCollection, and we specify the ODD_TAG output PCollection as a side output using the TupleTagList.of method.

The apply method returns a PCollectionTuple object containing both the EVEN_TAG and ODD_TAG output PCollections. We can access these PCollections using the get method of the PCollectionTuple object:

```java
PCollection<Integer> evenOutput = outputs.get(EVEN_TAG);
PCollection<Integer> oddOutput = outputs.get(ODD_TAG);
```

Now we can use the evenOutput and oddOutput PCollections in downstream processing steps, such as writing them to different output files or performing different computations on them.
