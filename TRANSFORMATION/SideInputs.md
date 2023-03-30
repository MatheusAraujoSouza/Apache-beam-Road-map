
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