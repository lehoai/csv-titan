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
import lehoai.csvtitan.service.CsvUnion;
import lehoai.csvtitan.service.core.Encoding;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the CSV union screen, which allows users to select two CSV files,
 * configure encoding options, and merge them into a single output file.
 * The union operation is performed in a background thread to ensure the UI remains responsive.
 */
public class CsvUnionController {

    /**
     * Interface for handling events when the CSV union operation completes successfully.
     */
    public interface UnionSuccessListener {
        /**
         * Called when the CSV union operation completes successfully.
         *
         * @param output the absolute path to the output file
         */
        void onUnionSuccess(String output);
    }

    // UI Components
    @FXML
    public Button closeButton; // Button to close the union screen
    @FXML
    public TextField fileInput1; // TextField to display the path of the first input file
    @FXML
    public Button fileInputBtn1; // Button to select the first input file
    @FXML
    public TextField fileInput2; // TextField to display the path of the second input file
    @FXML
    public Button fileInputBtn2; // Button to select the second input file
    @FXML
    public TextField fileOutput; // TextField to display the path of the output file
    @FXML
    public Button fileOutputBtn; // Button to specify the output file path
    @FXML
    public Button unionButton; // Button to trigger the union operation
    @FXML
    public ComboBox<String> encodingComboBox; // Dropdown menu to select file encoding

    // Background thread executor for running union tasks
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Listener for handling success events
    private UnionSuccessListener unionSuccessListener;

    /**
     * Initializes the controller.
     * Sets up event handlers for buttons and initializes the encoding dropdown menu.
     */
    @FXML
    public void initialize() {
        // Populate the encoding dropdown with available encoding options
        encodingComboBox.setItems(FXCollections.observableArrayList(Encoding.getEncodings()));
        encodingComboBox.getSelectionModel().select(0); // Default to the first encoding (e.g., UTF-8)

        // Set up the close button to close the window when clicked
        closeButton.setOnAction(_ -> {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        });

        // Set up the file input buttons to open file selection dialogs
        fileInputBtn1.setOnAction(_ -> {
            File selectedFile = this.selectFile();
            if (selectedFile != null) {
                fileInput1.setText(selectedFile.getAbsolutePath());
            }
        });
        fileInputBtn2.setOnAction(_ -> {
            File selectedFile = this.selectFile();
            if (selectedFile != null) {
                fileInput2.setText(selectedFile.getAbsolutePath());
            }
        });

        // Set up the file output button to open a save file dialog
        fileOutputBtn.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File outFile = fileChooser.showSaveDialog(fileOutputBtn.getScene().getWindow());
            if (outFile != null) {
                fileOutput.setText(outFile.getAbsolutePath());
            }
        });

        // Set up the union button to start the merge operation
        unionButton.setOnAction(_ -> {
            // Disable the button and show a "Processing..." label
            unionButton.setDisable(true);
            unionButton.setText("Processing...");

            // Create and configure the background task for the union operation
            BackgroundTask task = new BackgroundTask(
                    fileInput1.getText(),
                    fileInput2.getText(),
                    fileOutput.getText(),
                    encodingComboBox.getSelectionModel().getSelectedItem());

            // Handle success of the background task
            task.setOnSucceeded(_ -> {
                unionButton.setDisable(false);
                unionButton.setText("Union");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Union Successful");
                alert.showAndWait();

                // Notify the listener about the successful union operation
                if (unionSuccessListener != null) {
                    unionSuccessListener.onUnionSuccess(fileOutput.getText());
                }
            });

            // Handle failure of the background task
            task.setOnFailed(_ -> {
                unionButton.setDisable(false);
                unionButton.setText("Union");
            });

            // Submit the task to the executor service
            executorService.submit(task);
        });
    }

    /**
     * Sets the listener for handling union success events.
     *
     * @param unionSuccessListener the listener to be notified when the union operation succeeds
     */
    public void setUnionSuccessListener(UnionSuccessListener unionSuccessListener) {
        this.unionSuccessListener = unionSuccessListener;
    }

    /**
     * Opens a file chooser dialog for selecting a file.
     *
     * @return the selected {@link File}, or {@code null} if no file is selected
     */
    private File selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(fileInputBtn1.getScene().getWindow());
    }

    /**
     * Background task for performing the CSV union operation.
     * Executes the merge operation in a separate thread to avoid blocking the UI.
     */
    static class BackgroundTask extends Task<Void> {

        private final String file1;
        private final String file2;
        private final String fileOutput;
        private final String encoding;

        /**
         * Constructs a new {@code BackgroundTask} for merging two CSV files.
         *
         * @param file1      the absolute path of the first input file
         * @param file2      the absolute path of the second input file
         * @param fileOutput the absolute path of the output file
         * @param encoding   the character encoding to use for reading and writing
         */
        public BackgroundTask(String file1, String file2, String fileOutput, String encoding) {
            this.file1 = file1;
            this.file2 = file2;
            this.fileOutput = fileOutput;
            this.encoding = encoding;
        }

        /**
         * Executes the CSV union operation.
         *
         * @return {@code null} upon successful completion
         * @throws Exception if an error occurs during the union operation
         */
        @Override
        protected Void call() throws Exception {
            CsvUnion csvUnion = new CsvUnion(encoding);
            csvUnion.union(file1, file2, fileOutput);
            return null;
        }
    }
}