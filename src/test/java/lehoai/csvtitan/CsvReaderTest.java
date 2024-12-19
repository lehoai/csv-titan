package lehoai.csvtitan;

import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.core.CsvConfig;
import lehoai.csvtitan.service.core.Schema;
import lehoai.csvtitan.service.core.Type;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CsvReaderTest {

    @Test
    void readNormalFile() throws IOException {
        String csvFile = Objects.requireNonNull(CsvReaderTest.class.getResource("normal.csv")).getFile();
        CsvReader csvReader = new CsvReader(csvFile, new CsvConfig());
        csvReader.readMeta();
        Schema[] schemas = csvReader.getSchemas();

        assertEquals(8, schemas.length);
        assertEquals("First name", schemas[4].name);
        assertEquals(Type.INT, schemas[1].type);

        List<CSVRecord> data = csvReader.readLines();
        assertEquals(5, data.size());
        assertEquals("14ju73", data.get(3).get(2));
    }

    @Test
    void readSemicolonFile() throws IOException {
        String csvFile = Objects.requireNonNull(CsvReaderTest.class.getResource("semicolon.csv")).getFile();

        CsvConfig csvConfig = new CsvConfig();
        csvConfig.delimiter = ";";
        CsvReader csvReader = new CsvReader(csvFile, csvConfig);
        csvReader.readMeta();
        Schema[] schemas = csvReader.getSchemas();

        assertEquals(7, schemas.length);
        assertEquals("Last name", schemas[4].name);
        assertEquals(Type.INT, schemas[0].type);

        List<CSVRecord> data = csvReader.readLines();
        assertEquals(5, data.size());
        assertEquals("mj9346", data.get(3).get(2));
    }
}
