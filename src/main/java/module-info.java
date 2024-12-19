module csvtitan {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.apache.commons.csv;
    exports lehoai.csvtitan;
    exports lehoai.csvtitan.ui;
    exports lehoai.csvtitan.service;
    exports lehoai.csvtitan.service.core;
    exports lehoai.csvtitan.service.sort;
    opens lehoai.csvtitan to javafx.fxml;
    opens lehoai.csvtitan.ui to javafx.fxml;
    opens lehoai.csvtitan.service to javafx.fxml;
    opens lehoai.csvtitan.service.core to javafx.fxml;
    opens lehoai.csvtitan.service.sort to javafx.fxml;
}