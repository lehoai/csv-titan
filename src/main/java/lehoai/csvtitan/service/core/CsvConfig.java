package lehoai.csvtitan.service.core;

/**
 * Configuration class for customizing CSV reading behavior.
 * Includes settings for encoding, delimiter, quotation marks, and buffer size.
 */
public class CsvConfig {
    /**
     * The character encoding of the CSV file. Default is "UTF-8".
     */
    public String encode;

    /**
     * The delimiter used to separate values in the CSV file. Default is a comma (",").
     */
    public String delimiter;

    /**
     * The number of lines to buffer when reading the CSV file. Default is 100.
     */
    public int bufferedLines;

    /**
     * Constructs a default configuration for CSV reading.
     */
    public CsvConfig() {
        this.delimiter = ",";
        this.encode = "UTF-8";
        this.bufferedLines = 100;
    }
}
