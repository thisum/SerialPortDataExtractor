package com.thisum.readings;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Arrays;

public class GraphPopPanel
{
    private XYChart.Series[] dataSeries;
    private int numLines = 0;
    private GridPane gridPane;
    private int count = 0;
    private double[] data;
    private boolean graphSet;

    public void create()
    {
        gridPane = new GridPane();
        HBox hBox = new HBox();
        TextField textField = new TextField();
        Button button = new Button("Add");
        button.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                numLines = Integer.parseInt(textField.getText());
                graphSet = true;
                setupGraph();
            }
        });
        hBox.getChildren().addAll(new Label("Points: "), textField, button);
        gridPane.add(hBox, 0, 1);
        gridPane.setPadding(new Insets(10));
        Stage stage = new Stage();
        stage.setScene(new Scene(gridPane));
        stage.setWidth(800);
        stage.setHeight(500);
        stage.show();
    }

    private void setupGraph()
    {
        dataSeries = new XYChart.Series[numLines];
        for(int i=0; i<dataSeries.length; i++)
        {
            dataSeries[i] = new XYChart.Series();
        }
        NumberAxis xAxis = new NumberAxis(0, 200, 10);
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis, yAxis);

        lineChart.getData().addAll(dataSeries);
        lineChart.setPrefWidth(750);
        gridPane.add(lineChart, 0, 0);
    }

    public void addData(String s)
    {
        if(graphSet)
        {
            Platform.runLater(()->
                              {
                                  if(dataSeries[0].getData().size() == 200)
                                  {
                                      dataSeries[0].getData().remove(0);
                                  }
                                  data = Arrays.stream(s.split(",")).mapToDouble(Double::parseDouble).toArray();
                                  for(int i = 0; i<dataSeries.length; i++ )
                                  {
                                      dataSeries[i].getData().add(new XYChart.Data(count, data[i]));
                                      count++;
                                  }
                              });
        }
    }
}
