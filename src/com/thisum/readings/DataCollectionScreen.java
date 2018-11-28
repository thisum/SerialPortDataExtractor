package com.thisum.readings;

import com.thisum.DataListener;
import com.thisum.SerialClass;
import com.thisum.math.MadgwickQuaternionCalculator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataCollectionScreen extends GridPane implements EventHandler<ActionEvent>, DataListener
{
    private static final String HEADER_ATTRIBUTES = "@relation handgrasp_object_detection\n\n" +
//            "@attribute T_ax real\n" +
//            "@attribute T_ay real\n" +
//            "@attribute T_az real\n" +
//            "@attribute T_gx real\n" +
//            "@attribute T_gy real\n" +
//            "@attribute T_gz real\n" +
//            "@attribute T_mx real\n" +
//            "@attribute T_my real\n" +
//            "@attribute T_mz real\n" +
//            "@attribute I_ax real\n" +
//            "@attribute I_ay real\n" +
//            "@attribute I_az real\n" +
//            "@attribute I_gx real\n" +
//            "@attribute I_gy real\n" +
//            "@attribute I_gz real\n" +
//            "@attribute I_mx real\n" +
//            "@attribute I_my real\n" +
//            "@attribute I_mz real\n" +
//            "@attribute M_ax real\n" +
//            "@attribute M_ay real\n" +
//            "@attribute M_az real\n" +
//            "@attribute M_gx real\n" +
//            "@attribute M_gy real\n" +
//            "@attribute M_gz real\n" +
//            "@attribute M_mx real\n" +
//            "@attribute M_my real\n" +
//            "@attribute M_mz real\n" +
//            "@attribute R_ax real\n" +
//            "@attribute R_ay real\n" +
//            "@attribute R_az real\n" +
//            "@attribute R_gx real\n" +
//            "@attribute R_gy real\n" +
//            "@attribute R_gz real\n" +
//            "@attribute R_mx real\n" +
//            "@attribute R_my real\n" +
//            "@attribute R_mz real\n" +
//            "@attribute P_ax real\n" +
//            "@attribute P_ay real\n" +
//            "@attribute P_az real\n" +
//            "@attribute P_gx real\n" +
//            "@attribute P_gy real\n" +
//            "@attribute P_gz real\n" +
//            "@attribute P_mx real\n" +
//            "@attribute P_my real\n" +
//            "@attribute P_mz real\n" +
//            "@attribute B_ax real\n" +
//            "@attribute B_ay real\n" +
//            "@attribute B_az real\n" +
//            "@attribute B_gx real\n" +
//            "@attribute B_gy real\n" +
//            "@attribute B_gz real\n" +
//            "@attribute B_mx real\n" +
//            "@attribute B_my real\n" +
//            "@attribute B_mz real\n";
            "@attribute Q0 real\n" +
            "@attribute Q1 real\n" +
            "@attribute Q2 real\n" +
            "@attribute Q3 real\n" ;

    private static String ATTRIBUTES_CLASS = "@attribute Class {";

    private GridPane gridTopPane;
    private GridPane gridBottomPane;

    private Button startBtn;
    private Button stopBtn;
    private Button writeToFileBtn;
    private Button closeBtn;
    private Button clearBtn;
    private Button undoBtn;
    private Button graphButton;

    private CheckBox fixSampleSizeChk;

    private Label userNameLbl;
    private TextField userNameTxt;
    private Label objectLbl;
    private Label sampleCntLbl;
    private Label sampleCntAmtLbl;
    private TextField objectTxt;
    private TextField sampleSizeTxt;

    private TextArea logsTxt;
    private ObjTable objTable;

    private boolean write;
    private String objectName;

    private List<String> objectList = new ArrayList<>();
    private SerialClass serialClass1;
    private int totalSampleCount = 0;
    private int fixedSampleSize = 0;
    private int trackFixedSample = 0;
    private int experimentCount = 1;
    private boolean addToList = false;
    private GraphPopPanel graphPopPanel;

    private int totalPrintedLines = 0;
    private List<StringBuffer> bufferList = new ArrayList<>();
    private double anglesAry[][] = new double[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private MadgwickQuaternionCalculator calculators[] = new MadgwickQuaternionCalculator[6];

    public DataCollectionScreen(SerialClass serialClass1)
    {
        this.serialClass1 = serialClass1;
        for(int i=0; i<6; i++)
        {
            calculators[i] = new MadgwickQuaternionCalculator();
        }
        setup();
    }

    private void setup()
    {
        objTable = new ObjTable();

        startBtn = new Button("Start");
        stopBtn = new Button("Stop");
        writeToFileBtn = new Button("Write To File");
        closeBtn = new Button("Close");
        undoBtn = new Button("Undo");
        graphButton = new Button("Show Graph");

        userNameLbl = new Label("User: ");
        userNameTxt = new TextField();

        objectLbl = new Label("Object: ");
        objectTxt = new TextField();

        sampleCntLbl = new Label("Sample Count: ");
        sampleCntAmtLbl = new Label("0");

        clearBtn = new Button("Clear");
        fixSampleSizeChk = new CheckBox("Fix Sample Size: ");
        fixSampleSizeChk.setSelected(true);
        sampleSizeTxt = new TextField();
        sampleSizeTxt.setEditable(true);
        sampleSizeTxt.setText("" + 50);

        logsTxt = new TextArea();

        startBtn.setOnAction(this);
        stopBtn.setOnAction(this);
        writeToFileBtn.setOnAction(this);
        closeBtn.setOnAction(this);
        clearBtn.setOnAction(this);
        fixSampleSizeChk.setOnAction(this);
        undoBtn.setOnAction(this);
        graphButton.setOnAction(this);

        gridTopPane = new GridPane();
        gridBottomPane = new GridPane();

        gridTopPane.add(userNameLbl, 0, 0, 1, 1);
        gridTopPane.add(userNameTxt, 1, 0, 4, 1);
        gridTopPane.add(fixSampleSizeChk, 5, 0, 1, 1);
        gridTopPane.add(sampleSizeTxt, 6, 0, 1, 1);

        gridTopPane.add(objectLbl, 0, 1, 1, 1);
        gridTopPane.add(objectTxt, 1, 1, 1, 1);
        gridTopPane.add(startBtn, 2, 1, 1, 1);
        gridTopPane.add(stopBtn, 3, 1, 1, 1);
        gridTopPane.add(sampleCntLbl, 4, 1, 1, 1);
        gridTopPane.add(sampleCntAmtLbl, 5, 1, 1, 1);
        gridTopPane.add(undoBtn, 6, 1, 1, 1);

        gridTopPane.setHgap(10);
        gridTopPane.setVgap(10);
        gridBottomPane.setHgap(10);
        gridBottomPane.setVgap(10);

        gridBottomPane.add(writeToFileBtn, 0, 0);
        gridBottomPane.add(clearBtn, 1, 0);
        gridBottomPane.add(closeBtn, 2, 0);
        gridBottomPane.add(graphButton, 3, 0);

        this.add(gridTopPane, 0, 0, 2,1);
        this.add(logsTxt, 0, 1);
        this.add(objTable.getTable(), 1, 1);
        this.add(gridBottomPane, 0, 3, 2, 1);

        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(10));

        gridTopPane.setMinWidth(1180);
        logsTxt.setMinWidth(960);
        logsTxt.setMinHeight(500);
        gridBottomPane.setMinWidth(1180);

        serialClass1.setDataListener(this);

        write = false;
        objectList.clear();
        String object = objTable.updateStatus(experimentCount);
        objectTxt.setText(object);
    }

    @Override
    public void handle(ActionEvent event)
    {
        if( event.getSource() == startBtn )
        {
            if(addToList)
            {
                bufferList.add(new StringBuffer(logsTxt.getText()));
                logsTxt.clear();
                addToList = false;
            }

            trackFixedSample = 0;
            objTable.updateCount(experimentCount, true);
            fixedSampleSize = fixSampleSizeChk.isSelected() ? Integer.parseInt(sampleSizeTxt.getText()) : 0;
            String obj = objectTxt.getText().trim();
            objectName = "," + obj;
            if( !objectList.contains(obj) )
            {
                objectList.add(objectTxt.getText().trim());
            }
            write = true;
        }
        else if( event.getSource() == stopBtn )
        {
            experimentCount++;
            String object = objTable.updateStatus(experimentCount);
            objectTxt.setText(object);
            write = false;
        }
        else if( event.getSource() == writeToFileBtn )
        {
            writeToFile();
        }
        else if( event.getSource() == closeBtn )
        {
            serialClass1.close();
            System.exit(0);
        }
        else if( event.getSource() == clearBtn )
        {
            userNameTxt.clear();
            objectTxt.clear();
            objectList.clear();
            ATTRIBUTES_CLASS = "@attribute Class {";
            sampleCntAmtLbl.setText("");
            totalSampleCount = 0;
            logsTxt.clear();
            fixSampleSizeChk.setSelected(false);
            sampleSizeTxt.setEditable(false);
            sampleSizeTxt.setText("0");
            fixedSampleSize = 0;
            sampleCntAmtLbl.setText("0");
            experimentCount = 1;
            String object = objTable.clearTable();
            objectTxt.setText(object);
            bufferList.clear();
        }
        else if( event.getSource() == fixSampleSizeChk )
        {
            sampleSizeTxt.setEditable(fixSampleSizeChk.isSelected());
            fixedSampleSize = 0;
            sampleSizeTxt.setText("" + 0);
        }
        else if(event.getSource() == undoBtn)
        {
            experimentCount--;
            objTable.updateCount(experimentCount, false);
            String object = objTable.updateStatus(experimentCount);
            objectTxt.setText(object);
        }
        else if(event.getSource() == graphButton)
        {
            graphPopPanel = new GraphPopPanel();
            graphPopPanel.create();
        }
    }

    private void writeToFile()
    {
        ATTRIBUTES_CLASS += objectList.toString().replace("[", "").replace("]", "") + "}";

        try( Writer fileWriter = new FileWriter(userNameTxt.getText() + ".arff") )
        {
            fileWriter.write(HEADER_ATTRIBUTES);
            fileWriter.write(ATTRIBUTES_CLASS);
            fileWriter.write("\n\n@data\n");

            bufferList.forEach(buffer -> {
                try
                {
                    fileWriter.write(buffer.toString());
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });

            fileWriter.write(logsTxt.getText());
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataAvailable(int deviceId, String s)
    {
        String line = setAngles(deviceId, s);
        if(( line!= null) && write)
        {
            line = Arrays.deepToString(anglesAry).replace("[[", "").replace("]]", "") ;//+ "#" + line;

            if( fixedSampleSize == 0 )
            {
                updateDataOnUI(line);
            }
            else if( (fixedSampleSize > 0 && trackFixedSample < fixedSampleSize) )
            {
                trackFixedSample++;
                updateDataOnUI(line);
            }
            else if(fixedSampleSize > 0 && trackFixedSample == fixedSampleSize)
            {
                experimentCount++;
                String object = objTable.updateStatus(experimentCount);
                objectTxt.setText(object);
                write = false;
            }
        }

//        if(graphPopPanel != null)
//        {
//            graphPopPanel.addData(s);
//        }

    }

    private String setAngles(int deviceId, String line)
    {
        boolean success = true;
        int device = 0;
        double[] data = null;
        double[] angles = {0.0, 0.0, 0.0};
        StringBuffer pringLine = new StringBuffer();

        for( String reading : line.split("#", 6))
        {
            data = Arrays.stream(reading.split(",")).mapToDouble(Double::parseDouble).toArray();
            device = (int)data[0];
            pringLine.append(reading);
            pringLine.append(",");

            switch( device )
            {
                case 1:
                    angles = calculators[0].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[0] = angles;
                    else success = false;
                    break;
                case 2:
                    angles = calculators[1].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[1] = angles;
                    else success = false;
                    break;
                case 3:
                    angles = calculators[2].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[2] = angles;
                    else success = false;
                    break;
                case 4:
                    angles = calculators[3].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[3] = angles;
                    else success = false;
                    break;
                case 5:
                    angles = calculators[4].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[4] = angles;
                    else success = false;
                    break;
                case 6:
                    angles = calculators[5].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null) anglesAry[5] = angles;
                    else success = false;
                    break;
                default:
                    success = false;
                    break;
            }
        }

        return (success) ? pringLine.toString() : null;
    }

    private void updateDataOnUI(final String s)
    {
        totalSampleCount++;
        Platform.runLater(() ->
                          {
                              String line = s.replaceFirst(",", "");
                              logsTxt.appendText(line + objectName + "\n");
                              sampleCntAmtLbl.setText("" + totalSampleCount);
                              totalPrintedLines++;
                              if(totalPrintedLines%2000 == 0)
                              {
                                  addToList = true;
                              }
                          });
    }

    public void stop()
    {
        totalSampleCount = 0;
        serialClass1.close();
    }
}
