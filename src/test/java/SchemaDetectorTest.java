import lehoai.csvtitan.service.core.SchemaDetector;
import lehoai.csvtitan.service.core.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaDetectorTest {

    @Test
    void detectInt() {
        SchemaDetector detector = new SchemaDetector();
        Type type = detector.detectType("1");
        assertEquals(Type.INT, type);
    }

    @Test
    void detectDouble() {
        SchemaDetector detector = new SchemaDetector();
        Type type = detector.detectType("1.5");
        assertEquals(Type.DOUBLE, type);
    }

    @Test
    void detectDatetime() {
        SchemaDetector detector = new SchemaDetector();
        Type type = detector.detectType("2024-12-12");
        assertEquals(Type.DATE, type);
    }

    @Test
    void detectBoolean() {
        SchemaDetector detector = new SchemaDetector();
        Type type = detector.detectType("false");
        assertEquals(Type.BOOLEAN, type);
    }

    @Test
    void detectString() {
        SchemaDetector detector = new SchemaDetector();
        Type type = detector.detectType("false1");
        assertEquals(Type.STRING, type);
    }
}
