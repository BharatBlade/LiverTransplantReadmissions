import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ConvertCSV2Parquet {
    public static void main(String[] args) {
        // Input and output file paths
        String in = "LIVER_DATA.csv";
        String out = "LIVER_DATA.parquet";

        // Create an instance of FastParquet
        FastParquet fp = new FastParquet();

        try {
            // Convert CSV to Parquet with GZIP compression, adjust page size and row group size as needed
            fp.parquetWriteCSVToParquet(in, out, CompressionCodecName.GZIP, 1024 * 1024, 128 * 1024 * 1024);
            System.out.println("Conversion completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
    }
}
