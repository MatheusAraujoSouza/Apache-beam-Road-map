# Descrition of the project and analysis of the methods used


## necessary knowledge


###  MapElements vs FlatMapElements

Both FlatMapElements and MapElements are transformations that can be used to transform data in a Beam pipeline.

The difference between the two is that MapElements applies a one-to-one mapping from input elements to output elements, whereas FlatMapElements applies a one-to-many mapping from input elements to output elements.

In more detail:

### MapElements:

Takes a PCollection<T> as input, where T is a certain type of element.
Applies a function that maps each input element of type T to exactly one output element of type U.
Produces a PCollection<U> as output.
For example, MapElements can be used to convert a PCollection<Integer> to a PCollection<String> by mapping each input integer to its corresponding string representation:


```java
PCollection<Integer> numbers = ...;
PCollection<String> strings = numbers.apply(MapElements.into(TypeDescriptors.strings())
  .via((Integer i) -> i.toString()));

```


### FlatMapElements:

Takes a PCollection<T> as input, where T is a certain type of element.
Applies a function that maps each input element of type T to zero or more output elements of type U.
Produces a PCollection<U> as output.
For example, FlatMapElements can be used to split a PCollection<String> into a PCollection<String> of individual words by mapping each input string to a collection of its words:


```java
PCollection<String> lines = ...;
PCollection<String> words = lines.apply(FlatMapElements.into(TypeDescriptors.strings())
  .via((String line) -> Arrays.asList(line.split("\\W+"))));
```

As shown in these examples, MapElements is useful for performing one-to-one transformations on individual elements of a collection, while FlatMapElements is useful for producing multiple output elements for each input element, such as when splitting a line of text into words.


### Count.perKey()
Count.perKey() is a transform provided by Apache Beam that counts the occurrences of each unique key in a PCollection of key-value pairs.

In the context of your code, after the words have been converted into key-value pairs where the key is a word and the value is 1, Count.perKey() is applied to the PCollection to count the number of occurrences of each word in the input text. The output of this transform is a PCollection of key-value pairs, where the key is a unique word and the value is the count of the number of times that word appears in the input text.

For example, if the input text was "the cat in the hat", the output of Count.perKey() would be a PCollection with the following key-value pairs: ("the", 2), ("cat", 1), ("in", 1), ("hat", 1).


### GroupByKey 

GroupByKey is a Beam transform that groups together all values in a PCollection that have the same key.

In Beam, a GroupByKey transform is created by calling the GroupByKey.create() method. This transform takes as input a PCollection of key-value pairs, and outputs a new PCollection where each unique key is associated with an iterable containing all the values that were originally associated with that key.

Here's an example to illustrate how GroupByKey works:

Let's say we have a PCollection of key-value pairs, where the keys are integers and the values are strings:
```java
PCollection<KV<Integer, String>> kvPairs = ...;
```

```java
PCollection<KV<Integer, Iterable<String>>> grouped = kvPairs.apply(GroupByKey.create());
```

The resulting PCollection grouped will contain one key-value pair for each unique key in the input collection. Each key-value pair in grouped will have the same key as one of the input pairs, and its value will be an Iterable containing all the values that were associated with that key in the input collection.


### Count and GroupByKey

```java
["apple banana cherry", "banana cherry", "apple"]
```
After you apply ExtractWords transform, you will have a PCollection of words:
```java
["apple", "banana", "cherry", "banana", "cherry", "apple"]
```
Now, you apply Count transform to get the number of occurrences of each word:


```java
[("apple", 2), ("banana", 2), ("cherry", 2)]
```
Notice that the output of Count transform is a PCollection of (word, count) pairs, where word is the unique word and count is the number of occurrences of that word.

However, if you want to further process the results based on the unique words (for example, if you want to sort the words by count or do some other operation based on the unique words), you need to group the (word, count) pairs by the word. This is where GroupByKey transform comes in.

After you apply GroupByKey transform to the output of Count transform, you will have a PCollection of (word, Iterable<count>) pairs:
```java
[("apple", [2]), ("banana", [2]), ("cherry", [2])]
```

###  Arrays.asList( )

The Arrays.asList() method is then used to create a new list containing a single string, which is a CSV-formatted line representing the filename and count of occurrences. This list is returned by the via() function, and will be the output of this transformation.