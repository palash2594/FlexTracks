package sample;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AtcForm {

    static String url = "jdbc:mysql://localhost:3306/";
    static String user = "root";
    static String password = "";

    static String getAorD;
    static String sourcedest;
    static  String time1;

    Label successful = new Label("One row inserted successfully");
    VBox layout1 = new VBox(20);


    public Stage display(String title, String message){

        //window.initModality(Modality.APPLICATION_MODAL);
        Stage window1 = new Stage();
        window1.initModality(Modality.WINDOW_MODAL);
        window1.setTitle(title);
        window1.setMinWidth(300);

        //VBox layout = new VBox(20);
        //layout.getChildren().addAll(label, insertbutton);
        //layout.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label label = new Label();
        label.setText(message);
        VBox layout = new VBox(20);
        layout.getChildren().add(label);
        GridPane.setConstraints(layout, 0,0,2,1);

        //Flight Label - constrains use (child, column, row)
        Label flight = new Label("Flight No:");
        GridPane.setConstraints(flight, 0, 1);

        //Flight Input
        TextField getflight = new TextField(" ");
        GridPane.setConstraints(getflight, 1, 1);

        //Airline Label
        Label airline = new Label("Airline:");
        GridPane.setConstraints(airline, 0, 2);

        //Airline Input
        //TextField getairline = new TextField(" ");
        ComboBox<String> comboBox = new ComboBox<>();
        new AutoCompleteComboBoxListener<>(comboBox);
        //new AutoShowComboBoxHelper(comboBox, item -> buildTextToCompare(item));
        comboBox.getItems().addAll("Air Arabia","Air China","Air France","Air India","Air Mauritius","Air Seychelles", "All Nippon Airways","Bangkok Airways",
                "British Airways","Cathay Pacific","Druk Air","EgyptAir","El Al","Emirate Ethiopian Airlines","Etihad Airways","Flydubai","GoAir","Gulf Air",
                "IndiGo","IndiGo","Iran Air","Iraqi Airways","Jet Airways","Kenya Airways","Korean Air","Kuwait Airways","Lufthansa","Malaysia Airlines",
                "Malindo Air","Nepal Airlines","Oman Air","Pakistan International Airlines","Qatar Airways","Seasonal: Medina","Singapore Airlines",
                "Spice Jet","SriLankan Airlines","Swiss International Air Lines","Thai Airways","Turkish Airlines","United Airlines","Vistara");
        //comboBox.setValue("Air India");           // set a default value
        comboBox.setEditable(true);
        grid.add(comboBox, 1, 2);
        //String getairline = choiceBox.getValue();
        //GridPane.setConstraints(getairline, 1, 2);


        //Arrival or Departure Label
        Label AorD = new Label("Arrival or Departure:");
        GridPane.setConstraints(AorD, 0, 3);

        //Arrival or Departure Input
        //TextField getAorD = new TextField(" ");
        ToggleGroup group = new ToggleGroup();
        RadioButton radioButton1 = new RadioButton("Arrival");
        radioButton1.setToggleGroup(group);
        radioButton1.setUserData("Arrival");
        //radioButton1.setSelected(true);

        RadioButton radioButton2 = new RadioButton("Departure");
        radioButton2.setToggleGroup(group);
        radioButton2.setUserData("Departure");

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() != null) {
                    getAorD = group.getSelectedToggle().getUserData().toString();
                }
            }
        });

        HBox radio = new HBox();
        radio.getChildren().addAll(radioButton1, radioButton2);
        radio.setSpacing(20.0);
        grid.add(radio, 1,3 );
        //GridPane.setConstraints(getAorD, 1, 3);

        //SourceDest Label
        Label sourcedest = new Label("Source / Destination:");
        GridPane.setConstraints(sourcedest, 0, 4);

        //SourceDest Input
        TextField getsourcedest = new TextField(" ");
        GridPane.setConstraints(getsourcedest, 1, 4);

        //AircraftName Label
        Label aircraftname = new Label("Aircraft Name:");
        GridPane.setConstraints(aircraftname, 0, 5);

        //AircraftName Input
        ChoiceBox<String> choiceBox1 = new ChoiceBox<>();
        //new AutoShowComboBoxHelper(comboBox, item -> buildTextToCompare(item));
        choiceBox1.getItems().addAll("airbus380","airbus340-600","airbus320-100","airbus340-500","airbus340-200","airbus340-300","airbus320-200",
                "airbus330-200","airbus330-300","atr72-600","airbus321","airbus319","boeing747-400","boeing777-200","boeing777-200ER","boeing777-200LR","boeing777-300",
                "boeing777-300ER","boeing737-300","boeing737-400","boeing737-500","boeing737-600","boeing737-700","boeing737-800","boeing737-900",
                "boeing737-900ER","boeing737-100","boeing737-200","boeing737advanced","boeing737-700ER","boeing788");
        choiceBox1.setValue("airbus380");           // set a default value
        grid.add(choiceBox1, 1, 5);
        //TextField getaircraftname = new TextField(" ");
        //GridPane.setConstraints(getaircraftname, 1, 5);

        //TerminalName Label
        Label terminalname = new Label("Terminal Name:");
        GridPane.setConstraints(terminalname, 0, 6);

        //TerminalName Input
        ChoiceBox<String> choiceBox2 = new ChoiceBox<>();
        choiceBox2.getItems().addAll("1A","1B","1C","2");
        choiceBox2.setValue("2");           // set a default value
        grid.add(choiceBox2, 1, 6);
        //TextField getterminalname = new TextField(" ");
        //GridPane.setConstraints(getterminalname, 1, 6);


        //Time Label
        Label time = new Label("Time:");
        GridPane.setConstraints(time, 0, 7);

        //Time Input
        TimeSpinner spinner = new TimeSpinner();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        spinner.valueProperty().addListener((obs, oldTime, newTime) -> time1 = formatter.format(newTime));
        grid.add(spinner,1,7);

        successful.setVisible(false);
        //Insert
        Button insertbutton = new Button("Insert");
        insertbutton.setOnAction(e -> {
            boolean flag = true;
            String getairline1 = getChoice(comboBox);
            String getaircraftname1 = getChoice1(choiceBox1);
            String getterminalname1 = getChoice2(choiceBox2);

            String abc = "";
            try{

                Class.forName("com.mysql.jdbc.Driver").newInstance();
                Connection con = DriverManager.getConnection(url, user, password);

                String y = getsourcedest.getText().replaceAll("\\s","");
                Statement stt = con.createStatement();
                stt.execute("USE flextracks");

                ResultSet res = stt.executeQuery("SELECT * FROM `airportsinfo` WHERE iataCode = '"+y+"'");
                while(res.next()){
                    abc = res.getString("iataCode");
                }

            }catch(Exception e1){

            }

            if(abc.equals(""))
            {
                AlertBox k = new AlertBox();
                Stage window3 = k.display("AlertBox", "Invalid Source/Destination");
                successful.setVisible(false);
                window3.showAndWait();
                flag = false;
            }


            if(getflight.getText().equals(" ") || getflight.getText().equals("") || getsourcedest.getText().equals(" ") || getsourcedest.getText().equals("") ||
                    getairline1.equals("") || getairline1.equals(" ") || getAorD == null || time1 == null) {
                AlertBox g = new AlertBox();
                Stage window3 = g.display("AlertBox", "Please enter the required information");
                successful.setVisible(false);
                window3.showAndWait();
                flag = false;
            }

            if(flag == true)
            {
                successful.setVisible(true);
            }

/*
            System.out.println(getairline1);
            System.out.println(getAorD);
            System.out.println(getterminalname1);
            System.out.println(getaircraftname1);
            System.out.println(time1);
*/
            try{

                Class.forName("com.mysql.jdbc.Driver").newInstance();
                Connection con = DriverManager.getConnection(url, user, password);
                Statement stt = con.createStatement();
                stt.execute("USE flextracks");

                //stt.execute("INSERT INTO `testy`(`flightNo`) VALUES ('"+getflight.getText()+"')");
                stt.execute("INSERT INTO `flightinfoatc`(`flightNo`, `airline`,`arrivalDeparture`, `sourceDestination`,"
                        + " `aircraftName`, `terminalName`,`time`) " +
                        "VALUES ('"+getflight.getText()+"','"+getairline1+"','"+getAorD+"','"+getsourcedest.getText()+"','"+getaircraftname1+"','"+getterminalname1+"','"+time1+"')");


            }catch(Exception e1){

            }

            //comboBox.SelectedIndex = -1;
            getflight.clear();
            getsourcedest.clear();
        });


        GridPane.setConstraints(insertbutton, 1, 9);

        layout1.getChildren().add(successful);
        GridPane.setConstraints(layout1, 1,10,2,1);

        //Add everything to grid
        grid.getChildren().addAll(layout, flight, getflight, airline, AorD, sourcedest, getsourcedest,
                aircraftname, terminalname, time, insertbutton, layout1);

        Scene scene11 = new Scene(grid, 700, 500);
        scene11.getStylesheets().add(getClass().getResource("Style1.css").toExternalForm());
        window1.setScene(scene11);
        return window1;

    }

    public static String getChoice(ComboBox<String> comboBox){
        String getairline = comboBox.getEditor().getText();
        return  getairline;
    }

    public static String getChoice1(ChoiceBox<String> choiceBox1){
        String getaircraftname = choiceBox1.getValue();
        return  getaircraftname;
    }

    public static String getChoice2(ChoiceBox<String> choiceBox2){
        String getterminalname = choiceBox2.getValue();
        return  getterminalname;
    }


}
