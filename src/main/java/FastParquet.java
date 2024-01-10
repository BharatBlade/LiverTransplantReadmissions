import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

/**
 * A utility class for reading from and writing to Parquet files using Apache Avro and Parquet libraries.
 */
public class FastParquet {
    // Parquet reader for reading GenericRecord from Parquet file
    public ParquetReader<GenericRecord> reader;
    // Default CSV delimiter
    public char delimiter = ',';
    // Utility classes for reading and writing files
    public FastReader scanner;
    public FastWriter writer;

    /**
     * Constructor that sets the Hadoop home directory.
     */
    public FastParquet() {
        try {
            System.setProperty("hadoop.home.dir", new java.io.File(".").getCanonicalPath() + "\\hadoop-3.0.0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the ParquetReader to read GenericRecord from the specified Parquet file.
     *
     * @param filePath The path to the Parquet file.
     */
    @SuppressWarnings("deprecation")
    public void parquetReader(String filePath) {
        try {
            reader = new AvroParquetReader<GenericRecord>(new Path((new File(filePath)).toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the next line from the Parquet file.
     *
     * @return The next line as a string, or null if an IOException occurs.
     */
    public String nextLine() {
        try {
            GenericRecord nextRecord = reader.read();
            return nextRecord.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Closes the ParquetReader.
     */
    public void parquetCloseReader() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an Avro schema based on the provided fields.
     *
     * @param fields The fields for which the schema is created.
     * @return The Avro schema as a string.
     */
    public String createSchema(String[] fields) {
        // Build the Avro schema string
        String schema = "{"
                + "\"namespace\": \"org.myorg.myname\",\"type\": \"record\","
                + "\"name\": \"patient\",\""
                + "fields\": [ ";
        for (int i = 0; i < fields.length - 1; i++) {
            schema += "{\"name\": \"" + fields[i].replace(" ", "") + "\", \"type\": [\"string\", \"null\"]}, ";
        }
        schema += "{\"name\": \"" + fields[fields.length - 1].replace(" ", "") + "\", \"type\": [\"string\", \"null\"]} ]}";
        // Clean up the schema string
        schema = schema.replace(".", "").replace("(", "").replace(")", "").replace("_", "");
        return schema;
    }

    /**
     * Creates a GenericRecord based on the provided CSV line, fields, and Avro schema.
     *
     * @param line      The CSV line to convert to a GenericRecord.
     * @param fields    The fields used for mapping CSV values to GenericRecord.
     * @param avroSchema The Avro schema for the GenericRecord.
     * @return The GenericRecord created from the CSV line.
     */
    public GenericData.Record createRecord(String line, String[] fields, Schema avroSchema) {
        String line2 = line;
        String[] arr = scanner.csvLineToArray(line2, delimiter);
        GenericData.Record record = new GenericData.Record(avroSchema);
        for (int i = 0; i < fields.length; i++) {
            record.put(fields[i], arr[i]);
        }
        return record;
    }

    /**
     * Writes CSV data to a Parquet file using the provided parameters.
     *
     * @param inputFile     The path to the input CSV file.
     * @param outputFile    The path to the output Parquet file.
     * @param codec         The compression codec to use.
     * @param pageSize      The page size for the Parquet file.
     * @param rowGroupSize  The row group size for the Parquet file.
     */
    public void parquetWriteCSVToParquet(String inputFile, String outputFile, CompressionCodecName codec, int pageSize, long rowGroupSize) {
        // Initialize a FastReader for reading CSV lines
        FastReader cr = new FastReader(inputFile);
        // Read the first line to get the CSV fields
        String line = cr.nextLine();
        // Prepend "Index" to the first line
        line = "Index" + line;

        // Convert the CSV fields to an array
        String[] fields = scanner.csvLineToArray(line, delimiter);
        // Create an Avro schema based on the fields
        Schema avroSchema = (new Schema.Parser().setValidate(true)).parse(createSchema(fields));
        System.out.println(line);
        System.out.println(Arrays.toString(fields));
        double count = 0;
        long time = System.currentTimeMillis();
        try {
            // Initialize a ParquetWriter using AvroParquetWriter
            try (@SuppressWarnings("deprecation")
            ParquetWriter<Object> writer = AvroParquetWriter.builder(new Path(outputFile))
                    .withSchema(avroSchema)
                    .withCompressionCodec(codec)
                    .withConf(new Configuration())
                    .withPageSize(pageSize)
                    .withRowGroupSize(rowGroupSize)
                    .build()) {
                // Read the next CSV line
                line = cr.nextLine();
                // Process each CSV line until the end of the file
                while (line != null) {
                    // Create a GenericRecord from the CSV line
                    org.apache.avro.generic.GenericData.Record record = createRecord(line, fields, avroSchema);
                    try {
                        // Write the record to the Parquet file
                        writer.write(record);
                        count++;
                        // Print progress every 10,000 records
                        if (count % 10000 == 0) {
                            System.out.println(count + "\t" + (System.currentTimeMillis() - time));
                        }
                    } catch (Exception e) {
                        System.out.println("Error in the following record (line " + count + ") detected: " + record);
                        e.printStackTrace();
                    }
                    // Read the next CSV line
                    line = cr.nextLine();
                }
                // Close the ParquetWriter
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        // Close the FastReader
        cr.close();
    }
}
