package com.thisum.readings;

import com.thisum.AngleResult;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DataCollectionScreen extends GridPane implements EventHandler<ActionEvent>, DataListener
{
    private static final String HEADER_ATTRIBUTES = "@relation handgrasp_object_detection\n\n" +
            "@attribute P_Y real\n" +
            "@attribute P_P real\n" +
            "@attribute P_R real\n" +
            "@attribute R_Y real\n" +
            "@attribute R_P real\n" +
            "@attribute R_R real\n" +
            "@attribute M_Y real\n" +
            "@attribute M_P real\n" +
            "@attribute M_R real\n" +
            "@attribute I_Y real\n" +
            "@attribute I_P real\n" +
            "@attribute I_R real\n" +
            "@attribute T_Y real\n" +
            "@attribute T_P real\n" +
            "@attribute T_R real\n";
    private static String ATTRIBUTES_CLASS = "@attribute Class {";
    private static final Logger LOGGER = Logger.getLogger(DataCollectionScreen.class.getName());

    private DecimalFormat df = new DecimalFormat("#.##");
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
    private List<StringBuffer> rawBufferList = new ArrayList<>();
    private List<StringBuffer> quartBufferList = new ArrayList<>();
    private int rawDataLines = 0;
    private double rawAnglesAry[][] = new double[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private double rawQuarternionsAry[][] = new double[][]{{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};
    private double relativeAnglesAry[][] = new double[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private MadgwickQuaternionCalculator calculators[] = new MadgwickQuaternionCalculator[6];
    private StringBuffer quartBuffer = null;
    private StringBuffer rawBuffer = null;

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
        rawBuffer = new StringBuffer();
        quartBuffer = new StringBuffer();
        rawBufferList.add(rawBuffer);
        quartBufferList.add(quartBuffer);
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
            rawDataLines = 0;
            rawBufferList.clear();
            quartBufferList.clear();
            rawBuffer = new StringBuffer();
            quartBuffer = new StringBuffer();
            rawBufferList.add(rawBuffer);
            quartBufferList.add(quartBuffer);

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
                    System.out.println("******** data file written ********");
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

        try( Writer fileWriter = new FileWriter(userNameTxt.getText() + "_raw.arff") )
        {
            rawBufferList.forEach(buffer -> {
                try
                {
                    fileWriter.write(buffer.toString());
                    System.out.println("******** raw data written ********");
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }


        try( Writer fileWriter = new FileWriter(userNameTxt.getText() + "_quart.arff") )
        {
            quartBufferList.forEach(buffer -> {
                try
                {
                    fileWriter.write(buffer.toString());
                    System.out.println("******** quart data written ********");
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataAvailable(int deviceId, String s)
    {
        String rawAngles = "";
        String rawQuarts = "";
        String realtiveAngles = "";
        String line = setAngles(deviceId, s);
        if(( line!= null) && write)
        {
            calculateRelativePositions();
            rawAngles = Arrays.deepToString(rawAnglesAry).replace("[", "").replace("]", "");
            realtiveAngles = Arrays.deepToString(relativeAnglesAry).replace("[", "").replace("]", "") ;
            rawQuarts = Arrays.deepToString(rawQuarternionsAry).replace("[", "").replace("]", "") ;

            if( fixedSampleSize == 0 )
            {
                updateDataOnUI(realtiveAngles);
                writeToBuffer(line, rawAngles, rawQuarts);
            }
            else if( (fixedSampleSize > 0 && trackFixedSample < fixedSampleSize) )
            {
                trackFixedSample++;
                updateDataOnUI(realtiveAngles);
                writeToBuffer(line, rawAngles, rawQuarts);
            }
            else if(fixedSampleSize > 0 && trackFixedSample == fixedSampleSize)
            {
                experimentCount++;
                String object = objTable.updateStatus(experimentCount);
                objectTxt.setText(object);
                write = false;
            }

            rawDataLines++;
            if(rawDataLines %4000 == 0)
            {
                rawBufferList.add(rawBuffer);
                rawBuffer = new StringBuffer();

                quartBufferList.add(quartBuffer);
                quartBuffer = new StringBuffer();
            }

            LOGGER.info(rawAngles);
        }
    }

    private void writeToBuffer(String line, String rawAngles, String rawQuarts)
    {
        rawBuffer.append(line);
        rawBuffer.append(rawAngles);
        rawBuffer.append(objectName);
        rawBuffer.append("\n");
        quartBuffer.append(rawQuarts);
        quartBuffer.append(objectName);
        quartBuffer.append("\n");
    }

    private String setAngles(int deviceId, String line)
    {
        boolean success = true;
        int device = 0;
        double[] data = null;
        AngleResult angles = null;
        StringBuffer dataLine = new StringBuffer();

        for( String reading : line.split("#", 6))
        {
            data = Arrays.stream(reading.split(",")).mapToDouble(Double::parseDouble).toArray();
            device = (int)data[0];
            dataLine.append(data[1]+","+data[2]+","+data[3]+","+data[4]+","+data[5]+","+data[6]+","+data[7]+","+data[8]+","+data[9]+"#");

            switch( device )
            {
                case 1:
                    angles = calculators[0].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[0] = angles.getAngles();
                        rawQuarternionsAry[0] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 2:
                    angles = calculators[1].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[1] = angles.getAngles();
                        rawQuarternionsAry[1] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 3:
                    angles = calculators[2].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[2] = angles.getAngles();
                        rawQuarternionsAry[2] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 4:
                    angles = calculators[3].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[3] = angles.getAngles();
                        rawQuarternionsAry[3] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 5:
                    angles = calculators[4].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[4] = angles.getAngles();
                        rawQuarternionsAry[4] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 6:
                    angles = calculators[5].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[5] = angles.getAngles();
                        rawQuarternionsAry[5] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                default:
                    success = false;
                    break;
            }
        }

        return (success) ? dataLine.toString() : null;
    }

    private void calculateRelativePositions()
    {
        double[] palm = rawAnglesAry[0];
        for(int i=1; i<6; i++)
        {
            relativeAnglesAry[i-1][0] = Double.valueOf(df.format(rawAnglesAry[i][0] - palm[0]));
            relativeAnglesAry[i-1][1] = Double.valueOf(df.format(rawAnglesAry[i][1] - palm[1]));
            relativeAnglesAry[i-1][2] = Double.valueOf(df.format(rawAnglesAry[i][2] - palm[2]));
        }
    }

    private void updateDataOnUI(final String s)
    {
        totalSampleCount++;
        Platform.runLater(() ->
                          {
                              logsTxt.appendText(s + objectName + "\n");
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
