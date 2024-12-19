package lehoai.csvtitan.ui;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lehoai.csvtitan.service.core.CsvConfig;
import lehoai.csvtitan.service.core.Schema;
import lehoai.csvtitan.service.sort.MergeSort;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the CSV Sort screen in a JavaFX application.
 * Handles user interactions for sorting a CSV file using the MergeSort algorithm.
 */
public class CsvSortController {

    /**
     * Listener interface for handling successful sort events.
     */
    public interface SortSuccessListener {
        void onSortSuccess(String output);
    }

    private SortSuccessListener sortSuccessListener;

    @FXML
    private ComboBox<String> sortType;

    @FXML
    private Button closeButton;

    @FXML
    private ComboBox<String> sortColumn;

    @FXML
    private TextField fileOutput;

    @FXML
    private Button fileOutputBtn;

    @FXML
    private Button sortButton;

    private String filePath;
    private CsvConfig config;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Initializes the controller and sets up event handlers for the UI elements.
     */
    @FXML
    public void initialize() {
        // Populate sort type dropdown with options.
        sortType.setItems(FXCollections.observableArrayList(Arrays.asList("ASC", "DESC")));
        sortType.getSelectionModel().select(0);

        // Set up file chooser for output file selection.
        fileOutputBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showSaveDialog(fileOutputBtn.getScene().getWindow());
            if (selectedFile != null) {
                fileOutput.setText(selectedFile.getAbsolutePath());
            }
        });

        // Set up the close button to close the application window.
        closeButton.setOnAction(event -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        // Set up the sort button to initiate the sorting process.
        sortButton.setOnAction(event -> {
            sortButton.setDisable(true);
            sortButton.setText("Processing...");

            // Create and configure the background task for sorting.
            BackgroundTask task = new BackgroundTask(
                    sortColumn.getSelectionModel().getSelectedIndex(),
                    sortType.getSelectionModel().getSelectedIndex(),
                    filePath,
                    fileOutput.getText(),
                    config
            );

            // Handle task success.
            task.setOnSucceeded(event1 -> {
                sortButton.setDisable(false);
                sortButton.setText("Sort");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Sort Successful");
                alert.showAndWait();

                if (sortSuccessListener != null) {
                    sortSuccessListener.onSortSuccess(fileOutput.getText());
                }
            });

            // Handle task failure.
            task.setOnFailed(event1 -> {
                sortButton.setDisable(false);
                sortButton.setText("Sort");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Sort Failed");
                alert.showAndWait();
            });

            // Submit the task to the executor service.
            executorService.submit(task);
        });
    }

    /**
     * Sets the schema list to populate the sort column dropdown.
     *
     * @param schemaList List of schemas representing columns in the CSV.
     */
    public void setSchemaList(List<Schema> schemaList) {
        sortColumn.setItems(FXCollections.observableArrayList(schemaList.stream().map(s -> s.name).toList()));
        sortColumn.getSelectionModel().select(0);
    }

    /**
     * Sets the CSV configuration.
     *
     * @param config Configuration for the CSV file.
     */
    public void setConfig(CsvConfig config) {
        this.config = config;
    }

    /**
     * Sets the file path of the input CSV file.
     *
     * @param filePath Path to the input file.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Sets the listener for handling successful sort events.
     *
     * @param sortSuccessListener Listener to notify when sorting is successful.
     */
    public void setSortSuccessListener(SortSuccessListener sortSuccessListener) {
        this.sortSuccessListener = sortSuccessListener;
    }

    /**
     * Background task for performing the CSV sort operation.
     * Executes the sorting in a separate thread to prevent UI blocking.
     */
    static class BackgroundTask extends Task<Void> {

        private final String inputFile;
        private final String fileOutput;
        private final int sortColIndex;
        private final int sortType;
        private final CsvConfig config;

        /**
         * Constructs a BackgroundTask for sorting.
         *
         * @param sortColIndex Index of the column to sort by.
         * @param sortType     Sorting order (0 for ascending, 1 for descending).
         * @param inputFile    Path to the input file.
         * @param fileOutput   Path to the output file.
         * @param config       Configuration for the CSV file.
         */
        public BackgroundTask(int sortColIndex, int sortType, String inputFile, String fileOutput, CsvConfig config) {
            this.sortColIndex = sortColIndex;
            this.sortType = sortType;
            this.inputFile = inputFile;
            this.fileOutput = fileOutput;
            this.config = config;
        }

        @Override
        protected Void call() throws Exception {
            MergeSort mergeSort = new MergeSort();
            mergeSort.sort(inputFile, config, fileOutput, sortColIndex, sortType == 0);
            return null;
        }
    }
}