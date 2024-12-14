package lehoai.csvtitan.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lehoai.csvtitan.CsvTitanApplication;

import java.io.File;
import java.io.IOException;

/**
 * Controller for the main screen of the application.
 * Manages the tab pane and menu bar, and provides functionality for opening CSV files,
 * managing tabs, and triggering application-wide actions.
 */
public class MainController implements CsvUnionController.UnionSuccessListener {

    /**
     * The main {@link TabPane} that holds the tabs for each opened CSV file.
     */
    @FXML
    private TabPane mainTabPane;

    /**
     * The primary stage of the application, used for file dialogs and other UI interactions.
     */
    private Stage primaryStage;

    /**
     * Opens a file dialog to allow the user to select a CSV file, then creates a new tab
     * to display and interact with the selected file.
     */
    @FXML
    public void onOpenCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(this.primaryStage);
        if (selectedFile != null) {
            openCsvFile(selectedFile.getPath());
        }
    }

    /**
     * Initializes the controller and configures the menu bar to use the system menu bar
     * if supported.
     */
    @FXML
    public void initialize() {
    }

    /**
     * Show form union
     */
    @FXML
    public void onOpenUnion() {
        try {
            FXMLLoader loader = new FXMLLoader(CsvTitanApplication.class.getResource("screen/union-view.fxml"));
            Pane root = loader.load();
            CsvUnionController controller = loader.getController();
            controller.setUnionSuccessListener(this);
            // Configure the dialog
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this.primaryStage);
            dialog.setTitle("Union CSV");

            Scene scene = new Scene(root);
            dialog.setScene(scene);
            dialog.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText("Can't open file\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Sets the primary stage for this controller.
     * Used for displaying file dialogs and other stage-based interactions.
     *
     * @param primaryStage the primary stage of the application
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    /**
     * Open csv and display to MainTab
     *
     * @param path csv file path
     */
    private void openCsvFile(String path) {
        try {
            FXMLLoader childViewLoader = new FXMLLoader(CsvTitanApplication.class.getResource("view/csv-tab-view.fxml"));
            childViewLoader.setControllerFactory(_ -> {
                CsvTabController controller = new CsvTabController();
                controller.setFilePath(path);
                return controller;
            });
            mainTabPane.getTabs().add(childViewLoader.load());
            mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText("Can't open file\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Callback after union success
     *
     * @param output the absolute path to the output file
     */
    @Override
    public void onUnionSuccess(String output) {
        openCsvFile(output);
    }
}