package sample;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

    Stage window = new Stage();

    public Stage display(String title, String message){
  //window.initModality(Modality.APPLICATION_MODAL);
  //    window.initModality(Modality.WINDOW_MODAL);
        window.setTitle(title);

        Label label = new Label();
        label.setText(message);
        //label.;
        VBox layout = new VBox(20);
        layout.getChildren().add(label);

        Scene scene = new Scene(layout, 250, 100);
        window.setScene(scene);
        return window;
    }

}
