# Simple Example

Suppose you have a stream of sensor readings coming in every second. Each sensor reading has a timestamp indicating when the reading was taken. You want to calculate the average of the last 10 readings for each sensor and output the result.

To do this, you can apply windowing to your PCollection of sensor readings. You can define a fixed windows of 10 seconds, which means that each window will contain 10 sensor readings. Then, you can apply a GroupByKey transform to group the sensor readings by sensor ID and window, and use a Combine transform to compute the average of the readings in each window for each sensor.

Here's what the code might look like in Java:

```java
PCollection<KV<String, Double>> averages = readings
  .apply(Window.<SensorReading>into(FixedWindows.of(Duration.standardSeconds(10))))
  .apply(ParDo.of(new DoFn<SensorReading, KV<String, Double>>() {
    @ProcessElement
    public void processElement(ProcessContext c) {
      SensorReading reading = c.element();
      Instant windowStart = c.window().start();
      String sensorId = reading.getSensorId();
      Double readingValue = reading.getValue();
      KV<String, Double> sensorReadingKey = KV.of(sensorId, windowStart);
      c.output(sensorReadingKey, readingValue);
    }
  }))
  .apply(GroupByKey.create())
  .apply(ParDo.of(new DoFn<KV<String, Iterable<Double>>, KV<String, Double>>() {
    @ProcessElement
    public void processElement(ProcessContext c) {
      String sensorId = c.element().getKey();
      Iterable<Double> readings = c.element().getValue();
      Double sum = 0.0;
      int count = 0;
      for (Double reading : readings) {
        sum += reading;
        count++;
      }
      Double average = sum / count;
      c.output(KV.of(sensorId, average));
    }
  }));
```

In this example, we use a fixed windows of 10 seconds using the FixedWindows class, and then apply a ParDo transform to emit each sensor reading keyed by the sensor ID and window start time. We then group the readings by sensor ID and window using GroupByKey, and compute the average of the readings in each window for each sensor using another ParDo transform. The output is a PCollection of key-value pairs where the key is the sensor ID and the value is the average of the last 10 readings for that sensor.
