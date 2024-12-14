package lehoai.csvtitan.service.core;

/**
 * Represents the metadata of a column in a CSV file.
 * This class defines the column's name and data type.
 */
public class Schema {

    /**
     * The name of the column.
     */
    public String name;

    /**
     * The data type of the column, as defined by the {@link Type} enum.
     */
    public Type type;
}