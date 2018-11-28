package com.thisum.calib;

import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

/**
 * Created by thisum_kankanamge on 7/9/18.
 */
public class GraphPanel
{
    private XYChart.Series xySeries;
    private XYChart.Series xzSeries;
    private XYChart.Series yzSeries;


    public GridPane getGraphPanel()
    {
        xySeries = new XYChart.Series();
        xzSeries = new XYChart.Series();
        yzSeries = new XYChart.Series();

        GridPane gridPane =  new GridPane();

        NumberAxis xAxis1 = new NumberAxis(-600, 600, 100);
        xAxis1.setLabel("X");

        NumberAxis yAxis1 = new NumberAxis(-600, 600, 100);
        yAxis1.setLabel("Y");

        NumberAxis xAxis2 = new NumberAxis(-600, 600, 100);
        xAxis1.setLabel("X");

        NumberAxis zAxis2 = new NumberAxis(-600, 600, 100);
        zAxis2.setLabel("Z");

        NumberAxis xAxis3 = new NumberAxis(-600, 600, 100);
        xAxis1.setLabel("X");

        NumberAxis zAxis3 = new NumberAxis(-600, 600, 100);
        zAxis2.setLabel("Z");

        ScatterChart<String, Number> xyScatterChart = new ScatterChart(xAxis1, yAxis1);
        ScatterChart<String, Number> xzScatterChart = new ScatterChart(xAxis2, zAxis2);
        ScatterChart<String, Number> yzScatterChart = new ScatterChart(xAxis3, zAxis3);

        xyScatterChart.getData().addAll(xySeries);
        xzScatterChart.getData().addAll(xzSeries);
        yzScatterChart.getData().addAll(yzSeries);

        xyScatterChart.setPrefSize(400, 400);
        xzScatterChart.setPrefSize(400, 400);
        yzScatterChart.setPrefSize(400, 400);

        gridPane.add(xyScatterChart, 0, 0);
        gridPane.add(xzScatterChart, 1, 0);
        gridPane.add(yzScatterChart, 0, 1);

        return gridPane;
    }

    public void setData(final int x, final int y, final int z)
    {
        Platform.runLater(()->{

            xySeries.getData().add(new XYChart.Data(x, y));
            xzSeries.getData().add(new XYChart.Data(x, z));
            yzSeries.getData().add(new XYChart.Data(y, z));

        });
    }


}
