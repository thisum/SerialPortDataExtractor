package com.thisum;

import com.thisum.calib.CalibratorScreen;
import com.thisum.readings.DataCollectionScreen;
import com.thisum.readings.GraphPopPanel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application implements EventHandler<ActionEvent>
{
    private GridPane basePane;
    private SerialClass serialClass;
    private Button caribrateButton;
    private Button readingButton;
    private GridPane gridPane;

    public static void main(String[] args)
    {
        launch(args);
//        CSVProcessor csv_processor = new CSVProcessor();
//        csv_processor.readFile();

//        CSVWalkinProcessor processor = new CSVWalkinProcessor();
//        processor.readFile();

//        QuaternionConverter converter = new QuaternionConverter();
//        converter.readFromCSV();

    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        gridPane = new GridPane();
        caribrateButton = new Button("Calibration");
        readingButton = new Button("Data Reading");

        caribrateButton.setOnAction(this);
        readingButton.setOnAction(this);

        serialClass = new SerialClass(1);
        basePane = new DataCollectionScreen(serialClass);

        HBox hbox = new HBox();
        hbox.setSpacing(10);
//        hbox.getChildren().addAll(readingButton, caribrateButton);

        gridPane.add(basePane, 0, 0 );
        gridPane.add(hbox, 0, 1 );

        Scene scene = new Scene(gridPane, 1260, 800);
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
        perspectiveCamera.setNearClip(0.001);
        perspectiveCamera.setFarClip(10000);
        scene.setCamera(perspectiveCamera);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();
    }

    @Override
    public void handle(ActionEvent event)
    {
        if(event.getSource() == caribrateButton)
        {
            gridPane.getChildren().remove(basePane);
            basePane = new CalibratorScreen(serialClass);
            gridPane.add(basePane, 0, 0 );
        }
        else if(event.getSource() == readingButton)
        {
            gridPane.getChildren().remove(basePane);
            basePane = new DataCollectionScreen(serialClass);
            gridPane.add(basePane, 0, 0 );
        }
    }
}
