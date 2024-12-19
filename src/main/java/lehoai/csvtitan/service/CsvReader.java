package lehoai.csvtitan.service;

import lehoai.csvtitan.service.core.CsvConfig;
import lehoai.csvtitan.service.core.Schema;
import lehoai.csvtitan.service.core.SchemaDetector;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reading CSV files with configurable options and automatic data type detection.
 * Emphasizes performance by buffering lines and efficient schema detection.
 */
public class CsvReader {

    private static final char STRING_QUOTE = '"';

    private final CSVParser csvParser;
    private final CsvConfig config;
    private final String fileName;
    private CSVRecord firstDataLine;
    private Schema[] schemas;

    /**
     * Constructs a {@code CsvReader} with the given file path and configuration.
     *
     * @param config the configuration for reading the CSV file
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public CsvReader(String filePath, CsvConfig config) throws IOException {
        this.config = config;
        csvParser = new CSVParser(new FileReader(filePath, Charset.forName(config.encode)),
                CSVFormat.Builder.create()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setDelimiter(config.delimiter)
                        .setQuote(STRING_QUOTE)
                        .build()
        );
        Path path = Paths.get(filePath);
        fileName = path.getFileName().toString();
    }

    /**
     * Reads a batch of lines from the CSV file based on the buffer size in the configuration.
     *
     * @return a list of string arrays, where each array represents a row of CSV values,
     * or {@code null} if an error occurs during reading
     */
    public List<CSVRecord> readLines() {
        List<CSVRecord> result = new ArrayList<>(this.config.bufferedLines);
        if (firstDataLine != null) {
            result.add(firstDataLine);
            firstDataLine = null;
        }
        while (result.size() < this.config.bufferedLines && csvParser.iterator().hasNext()) {
            result.add(csvParser.iterator().next());
        }
        return result;
    }

    public CSVRecord readLine() {
        if (firstDataLine != null) {
            CSVRecord record = firstDataLine;
            firstDataLine = null;
            return record;
        }
        return csvParser.iterator().next();
    }

    public boolean hasNext() {
        return csvParser.iterator().hasNext();
    }

    /**
     * Closes the CSV reader and releases associated resources.
     */
    public void close() {
        try {
            csvParser.close();
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
     * Return raw header as String
     *
     * @return raw header
     */
    public List<String> getRawHeader() {
        return Arrays.stream(schemas).map(s -> s.name).toList();
    }

    public CsvConfig getConfig() {
        return config;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Reads the metadata from the CSV file, including column headers and the first line of data,
     * to infer the schema (column names and data types).
     */
    public void readMeta() {
        List<String> headers = csvParser.getHeaderNames();
        schemas = new Schema[headers.size()];
        firstDataLine = csvParser.iterator().next();

        SchemaDetector sd = new SchemaDetector();

        for (int i = 0; i < schemas.length; i++) {
            schemas[i] = new Schema();
            schemas[i].name = headers.get(i);
            schemas[i].type = sd.detectType(firstDataLine.get(i));
        }
    }
}