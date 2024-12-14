package lehoai.csvtitan.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.core.Encoding;
import lehoai.csvtitan.service.core.Schema;

import java.io.IOException;
import java.util.List;

/**
 * Controller for managing the CSV viewer tab in a tab panel.
 * Provides functionalities for configuring, reloading, and displaying CSV data and schema.
 */
public class CsvTabController {

    /**
     * Button to reload the CSV file and update the view.
     */
    @FXML
    public Button btnReload;

    /**
     * TextField to specify the number of buffered lines to read.
     */
    @FXML
    public TextField bufferLinesField;

    /**
     * CheckBox to indicate whether string quotation is used in the CSV.
     */
    @FXML
    public CheckBox cbStringQuotation;

    /**
     * TableView to display the data from the CSV file.
     */
    @FXML
    private TableView<String[]> tblData;

    /**
     * TableView to display the schema (column metadata) of the CSV file.
     */
    @FXML
    private TableView<Schema> tblSchema;

    /**
     * Tab representing the view for this CSV file.
     */
    @FXML
    private Tab tabView;

    /**
     * TextField to specify the delimiter used in the CSV file.
     */
    @FXML
    private TextField delimiterField;

    /**
     * ComboBox to select the file encoding for the CSV file.
     */
    @FXML
    private ComboBox<String> encodingComboBox;

    /**
     * The file path of the current CSV file.
     */
    private String filePath;

    /**
     * The {@link CsvReader} instance for reading the CSV file.
     */
    private CsvReader csvReader;

    /**
     * Initializes the controller and its associated components.
     * Sets default configurations, reads the CSV file, and displays its content.
     */
    @FXML
    public void initialize() {
        encodingComboBox.setItems(FXCollections.observableArrayList(Encoding.getEncodings()));
        encodingComboBox.getSelectionModel().select(0);
        try {
            this.csvReader = new CsvReader(filePath, this.getConfig());
            tabView.setText(this.csvReader.getFileName());
            this.loadData();
        } catch (IOException _) {
            // Handle initialization errors gracefully (ignored for now)
        }

        tabView.setOnClosed(_ -> this.csvReader.close());
        btnReload.setOnMouseClicked(_ -> {
            this.csvReader.close();
            try {
                this.csvReader = new CsvReader(filePath, this.getConfig());
                this.loadData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sets the file path for the current CSV file.
     *
     * @param filePath the file path to be used
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads the data and schema from the CSV file and initializes the tables.
     */
    private void loadData() {
        this.initTableData();
        this.initTableSchema();
    }

    /**
     * Initializes the data table with content from the CSV file.
     * Configures columns dynamically based on the schema.
     */
    private void initTableData() {
        tblData.getColumns().clear();
        Schema[] schemas = this.csvReader.getSchemas();
        List<String[]> rawData = this.csvReader.readLines();
        int i = 0;
        for (Schema schema : schemas) {
            final int colIndex = i;
            TableColumn<String[], String> column = new TableColumn<>(schema.name);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[colIndex]));
            tblData.getColumns().add(column);
            tblData.setItems(FXCollections.observableArrayList(rawData));
            i++;
        }
        // TODO: Reset size to ensure CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN works
        for (TableColumn<?, ?> column : tblData.getColumns()) {
            column.setPrefWidth(100);
            column.setMaxWidth(Double.MAX_VALUE);
        }
        tblData.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tblData.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblData.layout();
    }

    /**
     * Initializes the schema table with metadata from the CSV file.
     * Displays column names and types.
     */
    private void initTableSchema() {
        tblSchema.getColumns().clear();
        Schema[] schemas = this.csvReader.getSchemas();
        TableColumn<Schema, String> column1 = new TableColumn<>("Name");
        column1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().name));
        tblSchema.getColumns().add(column1);
        TableColumn<Schema, String> column2 = new TableColumn<>("Type");
        column2.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().type.name()));
        tblSchema.getColumns().add(column2);
        tblSchema.setItems(FXCollections.observableArrayList(schemas));
    }

    /**
     * Constructs a configuration object based on the user inputs from the UI.
     *
     * @return a {@link CsvReader.CsvReaderConfig} object with user-defined settings
     */
    private CsvReader.CsvReaderConfig getConfig() {
        CsvReader.CsvReaderConfig config = new CsvReader.CsvReaderConfig();
        config.delimiter = "".equals(delimiterField.getText()) ? "," : delimiterField.getText();
        config.encode = encodingComboBox.getSelectionModel().getSelectedItem();
        if (!"".equals(bufferLinesField.getText())) {
            config.bufferedLines = Integer.parseInt(bufferLinesField.getText());
        }
        config.stringQuotation = cbStringQuotation.isSelected();
        return config;
    }
}