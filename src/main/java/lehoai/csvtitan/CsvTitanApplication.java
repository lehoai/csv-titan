package lehoai.csvtitan;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lehoai.csvtitan.ui.MainController;

import java.io.IOException;

/**
 * The main entry point for the CsvTitan application.
 * This class extends the {@link javafx.application.Application} class and sets up the primary stage
 * with the main view and its controller.
 */
public class CsvTitanApplication extends Application {

    /**
     * Starts the JavaFX application by initializing the main stage.
     *
     * @param stage the primary stage for the application
     * @throws IOException if the main FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CsvTitanApplication.class.getResource("screen/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        // Set the primary stage for the main controller
        MainController controller = fxmlLoader.getController();
        controller.setPrimaryStage(stage);

        stage.setTitle("CsvTitan");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method for launching the application.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}