module csvtitan {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    exports lehoai.csvtitan;
    exports lehoai.csvtitan.ui;
    exports lehoai.csvtitan.service;
    exports lehoai.csvtitan.service.core;
    opens lehoai.csvtitan to javafx.fxml;
    opens lehoai.csvtitan.ui to javafx.fxml;
    opens lehoai.csvtitan.service to javafx.fxml;
    opens lehoai.csvtitan.service.core to javafx.fxml;
}