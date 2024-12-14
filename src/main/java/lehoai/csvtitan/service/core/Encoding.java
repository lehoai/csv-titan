package lehoai.csvtitan.service.core;

import java.util.Arrays;
import java.util.List;

/**
 * A utility class that provides a list of commonly used character encodings.
 * This class serves as a centralized location for encoding constants used across the application.
 */
public class Encoding {

    /**
     * Retrieves a list of commonly used character encodings.
     *
     * @return a {@link List} of encoding names as {@link String}
     */
    public static List<String> getEncodings() {
        return Arrays.asList(
                "UTF-8",
                "UTF-16",
                "SHIFT-JIS",
                "EUC-JP",
                "ISO-8859-1",
                "ISO-8859-3",
                "ISO-8859-15",
                "Windows 1252"
        );
    }
}