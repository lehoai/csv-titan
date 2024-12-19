package lehoai.csvtitan.service.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A utility class for detecting the data type of given value in a CSV column.
 * The detection is based on the value's format and content.
 */
public class SchemaDetector {

    /**
     * Popular datetime format
     */
    public static final String[] COMMON_DATE_FORMATS = {
            "yyyy-MM-dd",          // 2023-12-14
            "dd-MM-yyyy",          // 14-12-2023
            "MM/dd/yyyy",          // 12/14/2023
            "yyyy/MM/dd",          // 2023/12/14
            "dd/MM/yyyy",          // 14/12/2023
            "EEE, dd MMM yyyy",    // Thu, 14 Dec 2023
            "MMMM dd, yyyy",       // December 14, 2023
            "yyyyMMdd",            // 20231214
            "ddMMyyyy",            // 14122023
            "dd.MM.yyyy",          // 14.12.2023
            "yyyy.MM.dd",          // 2023.12.14

            // Định dạng ngày giờ
            "yyyy-MM-dd HH:mm:ss", // 2023-12-14 13:45:30
            "dd-MM-yyyy HH:mm:ss", // 14-12-2023 13:45:30
            "MM/dd/yyyy HH:mm:ss", // 12/14/2023 13:45:30
            "yyyy/MM/dd HH:mm:ss", // 2023/12/14 13:45:30
            "dd/MM/yyyy HH:mm:ss", // 14/12/2023 13:45:30
            "yyyy-MM-dd'T'HH:mm:ss", // 2023-12-14T13:45:30 (ISO 8601)
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 2023-12-14T13:45:30.123Z (UTC ISO 8601)
            "EEE, dd MMM yyyy HH:mm:ss z",  // Thu, 14 Dec 2023 13:45:30 GMT
            "MMMM dd, yyyy HH:mm:ss",       // December 14, 2023 13:45:30
            "yyyyMMddHHmmss",              // 20231214134530
    };

    /**
     * Detects the data type of the given value.
     *
     * @param value the value to analyze
     * @return the detected {@link Type} of the value
     */
    public Type detectType(String value) {
        if (isInteger(value)) {
            return Type.INT;
        }
        if (isDouble(value)) {
            return Type.DOUBLE;
        }
        if (isBoolean(value)) {
            return Type.BOOLEAN;
        }
        if (isDate(value)) {
            return Type.DATE;
        }
        return Type.STRING;
    }

    /**
     * Checks if the given value is an integer.
     *
     * @param value the value to check
     * @return true if the value is an integer, false otherwise
     */
    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given value is a double-precision floating-point number.
     *
     * @param value the value to check
     * @return true if the value is a double, false otherwise
     */
    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given value is a boolean.
     * The value is considered a boolean if it equals "true" or "false" (case-insensitive).
     *
     * @param value the value to check
     * @return true if the value is a boolean, false otherwise
     */
    private boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    /**
     * Checks if the given value is a valid date.
     * The validation is strict and does not allow ambiguous date formats.
     *
     * @param value the value to check
     * @return true if the value is a valid date, false otherwise
     */
    private boolean isDate(String value) {
        for (String format : COMMON_DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format); // Specify a pattern if necessary
                sdf.setLenient(false);
                sdf.parse(value);
                return true;
            } catch (ParseException _) {
            }
        }
        return false;
    }
}