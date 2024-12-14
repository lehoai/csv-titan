
import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.CsvUnion;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CsvUnionTest {

    @Test
    void detectInt() throws IOException {
        Path outCsv = Files.createTempFile("union-out", ".csv");
        CsvUnion csvUnion = new CsvUnion("UTF-8");
        csvUnion.union(
                Objects.requireNonNull(CsvUnionTest.class.getResource("normal.csv")).getFile(),
                Objects.requireNonNull(CsvUnionTest.class.getResource("normal.csv")).getFile(),
                outCsv.toString());

        CsvReader reader = new CsvReader(outCsv.toString());
        List<String[]> data = reader.readLines();
        assertEquals(10, data.size());

        File f = new File(outCsv.toString());
        f.deleteOnExit();
    }
}
