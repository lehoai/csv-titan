package lehoai.csvtitan.service.sort;

import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.CsvWriter;
import lehoai.csvtitan.service.core.CsvConfig;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implements an external merge sort algorithm for sorting large CSV files.
 * Splits input data into smaller chunks, sorts them, and merges them efficiently.
 */
public class MergeSort {

    private static final int CHUNK_SIZE = 300000; // Number of rows per chunk.
    private static final int THREADS_SIZE = 4; // Number of threads to process chunks concurrently.
    private CsvReader csvReader;

    /**
     * Sorts a CSV file based on the specified column and outputs the result to another file.
     *
     * @param fileInput    Path to the input CSV file.
     * @param config       CSV configuration settings.
     * @param outputFile   Path to the output CSV file.
     * @param sortColIndex Index of the column to sort by.
     * @param isAsc        True for ascending order, false for descending order.
     * @throws IOException If an I/O error occurs.
     */
    public void sort(String fileInput, CsvConfig config, String outputFile, int sortColIndex, boolean isAsc) throws IOException {
        config.bufferedLines = CHUNK_SIZE;
        this.csvReader = new CsvReader(fileInput, config);
        this.csvReader.readMeta();
        List<Path> sortedChunks = splitAndSortChunks(sortColIndex, isAsc);
        mergeSortedChunks(sortedChunks, outputFile, sortColIndex, isAsc);
        deleteTempFiles(sortedChunks);
    }

    /**
     * Sorts a CSV file based on the specified column and outputs the result to another file.
     *
     * @param fileInput    Path to the input CSV file.
     * @param config       CSV configuration settings.
     * @param outputFile   Path to the output CSV file.
     * @param sortColIndex Index of the column to sort by.
     * @param isAsc        True for ascending order, false for descending order.
     * @param chunkSize    Chunk size use to split data source file
     * @throws IOException If an I/O error occurs.
     */
    public void sort(String fileInput, CsvConfig config, String outputFile, int sortColIndex, boolean isAsc, int chunkSize) throws IOException {
        config.bufferedLines = chunkSize;
        this.csvReader = new CsvReader(fileInput, config);
        this.csvReader.readMeta();
        List<Path> sortedChunks = splitAndSortChunks(sortColIndex, isAsc);
        mergeSortedChunks(sortedChunks, outputFile, sortColIndex, isAsc);
        deleteTempFiles(sortedChunks);
    }

    /**
     * Splits the input CSV data into sorted chunks.
     *
     * @param sortColIndex Index of the column to sort by.
     * @param isAsc        True for ascending order, false for descending order.
     * @return A list of paths to the sorted chunk files.
     */
    private List<Path> splitAndSortChunks(int sortColIndex, boolean isAsc) {
        List<Path> chunkFiles = new ArrayList<>();
        boolean isEof = false;

        while (!isEof) {
            List<Thread> threads = new ArrayList<>();
            List<List<CSVRecord>> chunkDataList = new ArrayList<>();

            for (int i = 0; i < THREADS_SIZE; i++) {
                List<CSVRecord> chunkData = csvReader.readLines();
                if (chunkData.isEmpty()) {
                    isEof = true;
                    break;
                }
                chunkDataList.add(chunkData);
            }

            for (List<CSVRecord> chunkData : chunkDataList) {
                threads.add(new Thread(() -> {
                    try {
                        chunkFiles.add(writeSortedChunk(chunkData, sortColIndex, isAsc));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            threads.forEach(Thread::start);
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return chunkFiles;
    }

    /**
     * Writes a sorted chunk of data to a temporary file.
     *
     * @param chunk        List of CSV records to sort.
     * @param sortColIndex Index of the column to sort by.
     * @param isAsc        True for ascending order, false for descending order.
     * @return Path to the temporary file containing the sorted chunk.
     * @throws IOException If an I/O error occurs.
     */
    private Path writeSortedChunk(List<CSVRecord> chunk, int sortColIndex, boolean isAsc) throws IOException {
        Comparator<String> comparator = isAsc ? Comparator.naturalOrder() : Comparator.reverseOrder();
        chunk.sort(Comparator.comparing(row -> row.get(sortColIndex), comparator));

        Path tempFile = Files.createTempFile("chunk_", ".csv");
        try (CsvWriter csvWriter = new CsvWriter(tempFile.toString(), csvReader.getConfig(), csvReader.getRawHeader())) {
            csvWriter.write(chunk);
        }

        return tempFile;
    }

    /**
     * Merges sorted chunks into a single output file.
     *
     * @param sortedChunks List of paths to sorted chunk files.
     * @param outputFile   Path to the output file.
     * @param sortColIndex Index of the column to sort by.
     * @param isAsc        True for ascending order, false for descending order.
     * @throws IOException If an I/O error occurs.
     */
    private void mergeSortedChunks(List<Path> sortedChunks, String outputFile, int sortColIndex, boolean isAsc) throws IOException {
        try (CsvWriter csvWriter = new CsvWriter(outputFile, csvReader.getConfig(), csvReader.getRawHeader())) {
            Comparator<String> comparator = isAsc ? Comparator.naturalOrder() : Comparator.reverseOrder();

            List<CsvReader> csvReaders = new ArrayList<>();
            List<CSVRecord> tmpRecords = new ArrayList<>();

            for (Path chunk : sortedChunks) {
                CsvReader reader = new CsvReader(chunk.toString(), this.csvReader.getConfig());
                csvReaders.add(reader);
                tmpRecords.add(reader.readLine());
            }

            while (!csvReaders.isEmpty()) {
                int minIndex = 0;
                for (int i = 1; i < tmpRecords.size(); i++) {
                    if (comparator.compare(tmpRecords.get(i).get(sortColIndex), tmpRecords.get(minIndex).get(sortColIndex)) < 0) {
                        minIndex = i;
                    }
                }

                csvWriter.write(tmpRecords.get(minIndex));
                if (csvReaders.get(minIndex).hasNext()) {
                    tmpRecords.set(minIndex, csvReaders.get(minIndex).readLine());
                } else {
                    csvReaders.get(minIndex).close();
                    csvReaders.remove(minIndex);
                    tmpRecords.remove(minIndex);
                }
            }
        }
    }

    /**
     * Deletes temporary files used during sorting.
     *
     * @param files List of paths to temporary files.
     */
    private void deleteTempFiles(List<Path> files) {
        for (Path file : files) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                System.err.println("Failed to delete temp file: " + file);
            }
        }
    }
}