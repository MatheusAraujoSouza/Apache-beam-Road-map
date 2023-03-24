 # Schema
 A schema is a formal definition of the structure of the data being processed in a pipeline. Schemas define the names, types, and hierarchical structure of the fields in a data record. By defining a schema for data, we can ensure that the data is in a consistent format, which makes it easier to process and analyze. Schemas can also help us catch errors early in the pipeline, before they propagate and cause larger issues.

In Beam, schemas can be used to help define the inputs and outputs of transforms, and to help with serialization and deserialization of data in different formats. Beam provides a schema library for commonly used formats, such as Avro, CSV, and JSON, and also allows you to define custom schemas for specific data formats.

One common use case for schemas in Beam is when working with BigQuery. BigQuery is a fully-managed, serverless data warehouse provided by Google Cloud Platform. BigQuery stores data in tables, which have a defined schema. To persist data from a Beam pipeline to BigQuery, we need to define a schema that matches the structure of the data being written. This can be done using the BigQueryIO class in the Beam SDK.

Here's an example of how to use a schema to write data to BigQuery using the Beam SDK

```java
// Define the schema for the data
TableSchema schema = new TableSchema();
schema.setFields(Arrays.asList(
    new TableFieldSchema().setName("name").setType("STRING"),
    new TableFieldSchema().setName("age").setType("INTEGER"),
    new TableFieldSchema().setName("email").setType("STRING")
));

// Write the data to BigQuery
PCollection<MyRecord> input = ...; // assume we have a PCollection of MyRecord objects
input.apply(BigQueryIO.writeTableRows()
        .to("my-project:my_dataset.my_table")
        .withSchema(schema)
        .withFormatFunction(MyRecordToTableRowFn.class)
        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));
```

In this example, we first define the schema for the data as a TableSchema object, which specifies the names and types of the fields in the data record. We then use the BigQueryIO.writeTableRows() method to write the data to a BigQuery table. We specify the table using the "to" method, and the schema using the "withSchema" method. We also provide a function that converts the data from our custom record format to BigQuery TableRow objects using the "withFormatFunction" method. Finally, we specify the disposition for creating and writing to the table using the "withCreateDisposition" and "withWriteDisposition" methods.

```java 
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;

public class WriteToBigQuery {
    
    public interface Options extends PipelineOptions {
        @Description("Path of the file to read from")
        @Default.String("gs://my-bucket/my-file.txt")
        String getInputFile();
        void setInputFile(String value);
        
        @Description("BigQuery table ID to write to, in the form 'project:dataset.table'")
        String getOutputTable();
        void setOutputTable(String value);
    }
    
    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        Pipeline pipeline = Pipeline.create(options);
        
        // Read data from input file
        PCollection<String> lines = pipeline.readTextFile(options.getInputFile());
        
        // Parse data into TableRow objects using a schema
        PCollection<TableRow> rows = lines.apply(
            MapElements.into(TypeDescriptor.of(TableRow.class)).via(line -> {
                String[] fields = line.split(",");
                TableRow row = new TableRow();
                row.set("name", fields[0]);
                row.set("age", Integer.parseInt(fields[1]));
                row.set("city", fields[2]);
                return row;
            })
        );
        
        // Write TableRow objects to BigQuery using the specified output table and schema
        rows.apply(
            BigQueryIO.writeTableRows()
                .to(options.getOutputTable())
                .withSchema(getSchema())
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
        );
        
        // Run the pipeline
        pipeline.run();
    }
    
    private static TableSchema getSchema() {
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("name").setType("STRING"));
        fields.add(new TableFieldSchema().setName("age").setType("INTEGER"));
        fields.add(new TableFieldSchema().setName("city").setType("STRING"));
        return new TableSchema().setFields(fields);
    }
}
```

This program reads input data from a file, parses it into TableRow objects using a schema, and writes the rows to a BigQuery table specified by the user. In this example, the schema is defined in the getSchema() method as a list of TableFieldSchema objects, which describe the name and data type of each field in the table. This schema is used when writing the data to BigQuery, to ensure that the data is correctly formatted.

Note that to run this program, you will need to provide the inputFile and outputTable options on the command line. For example, to run the program with an input file located at gs://my-bucket/my-file.txt and an output table named my-project:my-dataset.my-table, you would use the following command:

```
java -cp <path/to/your/beam/jar> WriteToBigQuery --inputFile=gs://my-bucket/my-file.txt --output
```

