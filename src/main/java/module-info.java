module org.example.photoeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.photoeditor to javafx.fxml;
    exports org.example.photoeditor;
}