package lehoai.csvtitan.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lehoai.csvtitan.CsvTitanApplication;
import lehoai.csvtitan.service.CsvReader;
import lehoai.csvtitan.service.core.CsvConfig;
import lehoai.csvtitan.service.core.Encoding;
import lehoai.csvtitan.service.core.Schema;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;

/**
 * Controller for managing the CSV viewer tab in a tab panel.
 * Provides functionalities for configuring, reloading, and displaying CSV data and schema.
 */
public class CsvTabController implements CsvSortController.SortSuccessListener {

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

    @FXML
    public Button btnSort;

    /**
     * TableView to display the data from the CSV file.
     */
    @FXML
    private TableView<CSVRecord> tblData;

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

    private MainController mainController;

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
            this.csvReader.readMeta();
            this.loadData();
        } catch (IOException _) {
            // Handle initialization errors gracefully (ignored for now)
        }

        btnSort.setOnAction(_ -> {
            FXMLLoader loader = new FXMLLoader(CsvTitanApplication.class.getResource("screen/sort-view.fxml"));
            try {
                Pane root = loader.load();
                CsvSortController controller = loader.getController();
                controller.setSchemaList(tblSchema.getItems());
                controller.setConfig(csvReader.getConfig());
                controller.setFilePath(filePath);
                controller.setSortSuccessListener(this);

                Scene scene = new Scene(root);
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(btnSort.getScene().getWindow());
                dialog.setTitle("Sort CSV");
                dialog.setScene(scene);
                dialog.show();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error occurred");
                alert.setContentText("Can't open file\n" + e.getMessage());
                alert.showAndWait();
            }
        });

        tabView.setOnClosed(_ -> this.csvReader.close());
        btnReload.setOnMouseClicked(_ -> {
            this.csvReader.close();
            try {
                this.csvReader = new CsvReader(filePath, this.getConfig());
                this.csvReader.readMeta();
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
        List<CSVRecord> rawData = this.csvReader.readLines();
        int i = 0;
        for (Schema schema : schemas) {
            final int colIndex = i;
            TableColumn<CSVRecord, String> column = new TableColumn<>(schema.name);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
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
     * @return a {@link CsvConfig} object with user-defined settings
     */
    private CsvConfig getConfig() {
        CsvConfig config = new CsvConfig();
        config.delimiter = "".equals(delimiterField.getText()) ? "," : delimiterField.getText();
        config.encode = encodingComboBox.getSelectionModel().getSelectedItem();
        if (!"".equals(bufferLinesField.getText())) {
            config.bufferedLines = Integer.parseInt(bufferLinesField.getText());
        }
        return config;
    }

    @Override
    public void onSortSuccess(String output) {
        mainController.openCsvFile(output);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}