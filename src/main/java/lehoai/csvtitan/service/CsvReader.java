package lehoai.csvtitan.service;

import lehoai.csvtitan.service.core.Schema;
import lehoai.csvtitan.service.core.SchemaDetector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Reading CSV files with configurable options and automatic data type detection.
 * Emphasizes performance by buffering lines and efficient schema detection.
 */
public class CsvReader {

    /**
     * Configuration class for customizing CSV reading behavior.
     * Includes settings for encoding, delimiter, quotation marks, and buffer size.
     */
    public static class CsvReaderConfig {

        /**
         * The character encoding of the CSV file. Default is "UTF-8".
         */
        public String encode;

        /**
         * The delimiter used to separate values in the CSV file. Default is a comma (",").
         */
        public String delimiter;

        /**
         * The character used for quoting string values in the CSV file. Default is null (no quoting).
         */
        public Boolean stringQuotation;

        /**
         * The number of lines to buffer when reading the CSV file. Default is 100.
         */
        public int bufferedLines;

        /**
         * Constructs a default configuration for CSV reading.
         */
        public CsvReaderConfig() {
            this.stringQuotation = null;
            this.delimiter = ",";
            this.encode = "UTF-8";
            this.bufferedLines = 100;
        }
    }

    public final CsvReaderConfig config;
    private final BufferedReader br;
    private Schema[] schemas;
    private String[] firstDataLine;
    private final String fileName;

    /**
     * Constructs a {@code CsvReader} with the given file path and the default configuration.
     *
     * @param filePath the path to the CSV file
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public CsvReader(String filePath) throws IOException {
        this(filePath, new CsvReaderConfig());
    }

    /**
     * Constructs a {@code CsvReader} with the given file path and configuration.
     *
     * @param filePath the path to the CSV file
     * @param config   the configuration for reading the CSV file
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public CsvReader(String filePath, CsvReaderConfig config) throws IOException {
        this.config = config;
        br = new BufferedReader(new FileReader(filePath, Charset.forName(this.config.encode)));
        Path path = Paths.get(filePath);
        this.fileName = path.getFileName().toString();
        readMeta();
    }

    /**
     * Reads a batch of lines from the CSV file based on the buffer size in the configuration.
     *
     * @return a list of string arrays, where each array represents a row of CSV values,
     * or {@code null} if an error occurs during reading
     */
    public List<String[]> readLines() {
        try {
            List<String[]> result = new ArrayList<>(this.config.bufferedLines);
            if (firstDataLine != null) {
                result.add(firstDataLine);
                firstDataLine = null;
            }
            String line;
            while (result.size() < this.config.bufferedLines && (line = br.readLine()) != null) {
                result.add(line.split(this.config.delimiter));
            }
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Closes the CSV reader and releases associated resources.
     */
    public void close() {
        try {
            br.close();
        } catch (IOException _) {
            // Ignored to ensure cleanup without exceptions being propagated
        }
    }

    /**
     * Gets the schema of the CSV file, including column names and data types.
     *
     * @return an array of {@link Schema} objects representing the column metadata
     */
    public Schema[] getSchemas() {
        return schemas;
    }

    /**
     * Csv file name
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Reads the metadata from the CSV file, including column headers and the first line of data,
     * to infer the schema (column names and data types).
     *
     * @throws IOException if an I/O error occurs during reading
     */
    private void readMeta() throws IOException {
        String[] headers = br.readLine().split(this.config.delimiter);
        schemas = new Schema[headers.length];
        firstDataLine = br.readLine().split(this.config.delimiter);

        SchemaDetector sd = new SchemaDetector();

        for (int i = 0; i < schemas.length; i++) {
            schemas[i] = new Schema();
            schemas[i].name = headers[i];
            schemas[i].type = sd.detectType(firstDataLine[i]);
        }
    }
}