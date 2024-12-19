package lehoai.csvtitan;

import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.core.CsvConfig;
import lehoai.csvtitan.service.sort.MergeSort;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MergeSortTest {

    @Test
    void normalSort() throws IOException {
        Path outCsv = Files.createTempFile("sort-out", ".csv");
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort(Objects.requireNonNull(CsvUnionTest.class.getResource("sortdata.csv")).getFile(), new CsvConfig(), outCsv.toString(), 5, true, 100);

        CsvConfig config = new CsvConfig();
        config.bufferedLines = 50000;
        CsvReader reader = new CsvReader(outCsv.toString(), config);
        List<CSVRecord> data = reader.readLines();
        assertEquals(50000, data.size());
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).get(5).compareTo(data.get(i - 1).get(5)) < 0) {
                fail();
            }
        }

        File f = new File(outCsv.toString());
        f.deleteOnExit();
    }

    @Test
    @Disabled("This test is ignored because performance test")
    void performance() throws IOException {
        Path outCsv = Files.createTempFile("sort-out", ".csv");
        MergeSort mergeSort = new MergeSort();
        mergeSort.sort("/Users/lehoai/Desktop/2021_Yellow_Taxi_Trip_Data.csv", new CsvConfig(), outCsv.toString(), 5, true);

        File f = new File(outCsv.toString());
        f.deleteOnExit();
    }
}
