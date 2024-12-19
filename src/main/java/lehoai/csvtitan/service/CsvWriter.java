package lehoai.csvtitan.service;

import lehoai.csvtitan.service.core.CsvConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Utility class for writing data to a CSV file.
 * Provides methods for writing individual records or batches of records.
 * Implements AutoCloseable for ensuring resources are properly released.
 */
public class CsvWriter implements AutoCloseable {

    private final CSVPrinter printer;
    private int flushCount = 0; // Tracks the number of records written before flushing.

    /**
     * Constructs a CsvWriter instance.
     *
     * @param output  Path to the output CSV file.
     * @param config  Configuration settings for the CSV file.
     * @param headers List of column headers for the CSV file.
     * @throws IOException If an I/O error occurs during initialization.
     */
    public CsvWriter(String output, CsvConfig config, List<String> headers) throws IOException {
        this.printer = new CSVPrinter(
                new FileWriter(output, Charset.forName(config.encode)),
                CSVFormat.Builder.create()
                        .setHeader(headers.toArray(new String[0]))
                        .setSkipHeaderRecord(false)
                        .setDelimiter(config.delimiter)
                        .setQuote('"')
                        .build()
        );
    }

    /**
     * Writes a list of CSV records to the file.
     *
     * @param data List of CSV records to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void write(List<CSVRecord> data) throws IOException {
        printer.printRecords(data);
        printer.flush();
    }

    /**
     * Writes a single CSV record to the file.
     * Automatically flushes after every 200 records.
     *
     * @param data CSV record to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    public void write(CSVRecord data) throws IOException {
        printer.printRecord(data);
        flushCount++;
        if (flushCount >= 200) {
            printer.flush();
            flushCount = 0;
        }
    }

    /**
     * Closes the writer and releases associated resources.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        printer.flush();
        printer.close();
    }
}