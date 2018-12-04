package com.thisum.readings;

import com.thisum.AngleResult;
import com.thisum.DataListener;
import com.thisum.SerialClass;
import com.thisum.math.MadgwickQuaternionCalculator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DataCollectionScreen extends GridPane implements EventHandler<ActionEvent>, DataListener
{
    private static final String HEADER_ATTRIBUTES = "@relation handgrasp_object_detection\n\n" +
            "@attribute P_q1 real\n" +
            "@attribute P_q2 real\n" +
            "@attribute P_q3 real\n" +
            "@attribute P_q4 real\n" +
            "@attribute R_q1 real\n" +
            "@attribute R_q2 real\n" +
            "@attribute R_q3 real\n" +
            "@attribute R_q4 real\n" +
            "@attribute M_q1 real\n" +
            "@attribute M_q2 real\n" +
            "@attribute M_q3 real\n" +
            "@attribute M_q4 real\n" +
            "@attribute I_q1 real\n" +
            "@attribute I_q2 real\n" +
            "@attribute I_q3 real\n" +
            "@attribute I_q4 real\n" +
            "@attribute T_q1 real\n" +
            "@attribute T_q2 real\n" +
            "@attribute T_q3 real\n" +
            "@attribute T_q4 real\n";
    private static String ATTRIBUTES_CLASS = "@attribute Class {";
    private static final Logger LOGGER = Logger.getLogger(DataCollectionScreen.class.getName());

    private DecimalFormat df = new DecimalFormat("#.##");
    private DecimalFormat df2 = new DecimalFormat("#.###");
    private GridPane gridTopPane;
    private GridPane gridBottomPane;

    private Button startBtn;
    private Button stopBtn;
    private Button writeToFileBtn;
    private Button closeBtn;
    private Button clearBtn;
    private Button undoBtn;
    private Button graphButton;
    private Button loadClassifierButton;
    private ToggleButton quatButton;
    private ToggleButton angleButton;
    private ToggleButton noPredictButton;

    private CheckBox fixSampleSizeChk;

    private Label userNameLbl;
    private TextField userNameTxt;
    private Label objectLbl;
    private Label sampleCntLbl;
    private Label sampleCntAmtLbl;
    private Label relaxStatusLbl;
    private TextField objectTxt;
    private TextField sampleSizeTxt;
    private Label statusLbl;
    private ImageView recordIcon;
    private ImageView readyIcon;

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
    private boolean quatPredict = false;
    private boolean anglePredict = false;
    private boolean relaxDataRecord = false;

    private int totalPrintedLines = 0;
    private List<StringBuffer> bufferList = new ArrayList<>();
    private List<StringBuffer> rawBufferList = new ArrayList<>();
    private List<StringBuffer> quatBufferList = new ArrayList<>();
    private List<StringBuffer> relativeAngBufferList = new ArrayList<>();
    private int rawDataLines = 0;
    private double rawAnglesAry[][] = new double[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private double rawQuaternionsAry[][] = new double[][]{{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};
    private double relativeAnglesAry[][] = new double[][]{{0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
    private double relativeQuaternionsAry[][] = new double[][]{{0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}, {0,0,0,0}};
    private MadgwickQuaternionCalculator calculators[] = new MadgwickQuaternionCalculator[6];
    private StringBuffer quartBuffer = null;
    private StringBuffer rawBuffer = null;
    private StringBuffer relativeAngBuffer = null;
    private RealtimeClassifier realtimeClassifier = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
        realtimeClassifier = new RealtimeClassifier();

        startBtn = new Button("Start");
        stopBtn = new Button("Stop");
        writeToFileBtn = new Button("Write To File");
        closeBtn = new Button("Close");
        undoBtn = new Button("Undo");
        graphButton = new Button("Show Graph");
        loadClassifierButton = new Button("Load Classifier");
        quatButton = new ToggleButton("Predict Quat");
        angleButton = new ToggleButton("Predict Angl");
        noPredictButton = new ToggleButton("No Predict");
        ToggleGroup toggleGroup = new ToggleGroup();
        quatButton.setToggleGroup(toggleGroup);
        angleButton.setToggleGroup(toggleGroup);
        noPredictButton.setToggleGroup(toggleGroup);

        userNameLbl = new Label("User: ");
        userNameTxt = new TextField();

        objectLbl = new Label("Object: ");
        objectTxt = new TextField();

        sampleCntLbl = new Label("Sample Count: ");
        sampleCntAmtLbl = new Label("0");

        relaxStatusLbl = new Label("");

        clearBtn = new Button("Clear");
        fixSampleSizeChk = new CheckBox("Fix Sample Size: ");
        fixSampleSizeChk.setSelected(true);
        sampleSizeTxt = new TextField();
        sampleSizeTxt.setEditable(true);
        sampleSizeTxt.setText("" + 500);

        logsTxt = new TextArea();

        startBtn.setOnAction(this);
        stopBtn.setOnAction(this);
        writeToFileBtn.setOnAction(this);
        closeBtn.setOnAction(this);
        clearBtn.setOnAction(this);
        fixSampleSizeChk.setOnAction(this);
        undoBtn.setOnAction(this);
        loadClassifierButton.setOnAction(this);
//        graphButton.setOnAction(this);

        try
        {
            Image readyPic = new Image(getClass().getResourceAsStream("ready.png"));
            Image recordPic = new Image(getClass().getResourceAsStream("record.png"));
            recordIcon = new ImageView(recordPic);
            readyIcon = new ImageView(readyPic);
            statusLbl = new Label();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        gridTopPane = new GridPane();
        gridBottomPane = new GridPane();

        gridTopPane.add(userNameLbl, 0, 0, 1, 1);
        gridTopPane.add(userNameTxt, 1, 0, 4, 1);
        gridTopPane.add(fixSampleSizeChk, 5, 0, 1, 1);
        gridTopPane.add(sampleSizeTxt, 6, 0, 1, 1);
        gridTopPane.add(statusLbl, 7, 0, 1, 2);
        gridTopPane.add(relaxStatusLbl, 8, 0, 1, 1);


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
        gridBottomPane.add(loadClassifierButton, 4, 0);
        gridBottomPane.add(angleButton, 5, 0);
        gridBottomPane.add(quatButton, 6, 0);
        gridBottomPane.add(noPredictButton, 7, 0);

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
        relativeAngBuffer = new StringBuffer();
//        rawBufferList.add(rawBuffer);
//        quatBufferList.add(quartBuffer);
//        relativeAngBufferList.add(relativeAngBuffer);


        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
        {
            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle)
            {
                ToggleButton button = (ToggleButton)toggleGroup.getSelectedToggle();
                if(button == noPredictButton)
                {
                    quatPredict = false;
                    anglePredict = false;
                }
                else if(button == angleButton)
                {
                    anglePredict = true;
                    quatPredict = false;
                }
                else if(button == quatButton)
                {
                    anglePredict = false;
                    quatPredict = true;
                }
            }
        });
    }

    @Override
    public void handle(ActionEvent event)
    {
        if( event.getSource() == startBtn )
        {
            if(write)
            {
                return;
            }

            statusLbl.setGraphic(null);
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

            rawBufferList.add(rawBuffer);
            quatBufferList.add(quartBuffer);
            relativeAngBufferList.add(relativeAngBuffer);

            rawBuffer = new StringBuffer();
            quartBuffer = new StringBuffer();
            relativeAngBuffer = new StringBuffer();

            write = true;
        }
        else if( event.getSource() == stopBtn )
        {
            stopRecording();
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

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setContentText("You are about to clear all the data. Please confirm");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK)
            {
                return;
            }

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
            quatBufferList.clear();
            relativeAngBufferList.clear();
            rawBuffer = new StringBuffer();
            quartBuffer = new StringBuffer();
            relativeAngBuffer = new StringBuffer();
            rawBufferList.add(rawBuffer);
            quatBufferList.add(quartBuffer);
            relativeAngBufferList.add(relativeAngBuffer);
            relaxDataRecord = false;
            relaxStatusLbl.setText("");
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
            rawBuffer.setLength(0);
            quartBuffer.setLength(0);
            relativeAngBuffer.setLength(0);
            totalSampleCount -= trackFixedSample;
            updateSampleCount();
        }
        else if(event.getSource() == graphButton)
        {
            graphPopPanel = new GraphPopPanel();
            graphPopPanel.create();
        }
        else if(event.getSource() == loadClassifierButton)
        {
            realtimeClassifier.loadClassifier();
        }
    }

    private void stopRecording()
    {
        experimentCount++;
        String object = objTable.updateStatus(experimentCount);
        boolean oneSweepDone = objTable.hasOneSweepDone();
        showDialog(oneSweepDone);

        objectTxt.setText(object);
        write = false;

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    trackFixedSample = 0;
                    Thread.sleep(1000);
                    changeIcon(readyIcon, "relax");
                    Thread.sleep(1000);
                    changeIcon(readyIcon, "3");
                    Thread.sleep(1000);
                    changeIcon(readyIcon, "2");
                    Thread.sleep(1000);
                    changeIcon(readyIcon, "1");
                    Thread.sleep(1000);
                    changeIcon(recordIcon, "recording");
                    objectName = ",none";
                    write = true;
                    Thread.sleep(2000);
                    write = false;
                    changeIcon(null, "done");
                    Thread.sleep(1000);
                    changeIcon(null, "");

                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void changeIcon(final ImageView readyIcon,final String status)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                relaxStatusLbl.setText(status);
                statusLbl.setGraphic(readyIcon);
            }
        });
    }

    private void showDialog(final boolean oneSweepDone)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                if(oneSweepDone)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setContentText("One round is done, please save the data");
                    alert.showAndWait();
                }
            }
        });
    }

    @Override
    public void onDataAvailable(int deviceId, String s)
    {
        String rawAngles = "";
        String rawQuarts = "";
        String realtiveAngles = "";
        String relativeQuarts = "";
        String line = setAngles(deviceId, s);
        if(( line!= null) && write)
        {
            calculateRelativePositions();
            rawAngles = Arrays.deepToString(rawAnglesAry).replace("[", "").replace("]", "");
            realtiveAngles = Arrays.deepToString(relativeAnglesAry).replace("[", "").replace("]", "") ;
            rawQuarts = Arrays.deepToString(rawQuaternionsAry).replace("[", "").replace("]", "") ;
            relativeQuarts = Arrays.deepToString(relativeQuaternionsAry).replace("[", "").replace("]", "") ;

            if( fixedSampleSize == 0 )
            {
                updateDataOnUI(relativeQuarts);
                writeToBuffer(line, rawAngles, rawQuarts, realtiveAngles);
            }
            else if( (fixedSampleSize > 0 && trackFixedSample < fixedSampleSize) )
            {
                trackFixedSample++;
                updateDataOnUI(relativeQuarts);
                writeToBuffer(line, rawAngles, rawQuarts, realtiveAngles);
            }
            else if(fixedSampleSize > 0 && trackFixedSample == fixedSampleSize)
            {
                stopRecording();
            }

            rawDataLines++;
//            if(rawDataLines %4000 == 0)
//            {
//                rawBuffer = new StringBuffer();
//                rawBufferList.add(rawBuffer);
//
//                quartBuffer = new StringBuffer();
//                quatBufferList.add(quartBuffer);
//
//                relativeAngBuffer = new StringBuffer();
//                relativeAngBufferList.add(relativeAngBuffer);
//            }

            if(anglePredict)
            {
                realtimeClassifier.classifyAngles(relativeAnglesAry);
            }
            else if(quatPredict)
            {
                realtimeClassifier.classifyQuat(relativeQuaternionsAry);
            }

            LOGGER.info(rawAngles);
        }
    }

    private void writeToBuffer(String line, String rawAngles, String rawQuarts, String realtiveAngles)
    {
        rawBuffer.append(line);
        rawBuffer.append(rawAngles);
        rawBuffer.append(objectName);
        rawBuffer.append("\n");

        quartBuffer.append(rawQuarts);
        quartBuffer.append(objectName);
        quartBuffer.append("\n");

        relativeAngBuffer.append(realtiveAngles);
        relativeAngBuffer.append(objectName);
        relativeAngBuffer.append("\n");
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
                        rawQuaternionsAry[0] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 2:
                    angles = calculators[1].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[1] = angles.getAngles();
                        rawQuaternionsAry[1] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 3:
                    angles = calculators[2].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[2] = angles.getAngles();
                        rawQuaternionsAry[2] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 4:
                    angles = calculators[3].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[3] = angles.getAngles();
                        rawQuaternionsAry[3] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 5:
                    angles = calculators[4].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[4] = angles.getAngles();
                        rawQuaternionsAry[4] = angles.getQuarternions();
                    }
                    else success = false;
                    break;
                case 6:
                    angles = calculators[5].calculateQuaternions(data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                    if(angles != null)
                    {
                        rawAnglesAry[5] = angles.getAngles();
                        rawQuaternionsAry[5] = angles.getQuarternions();
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

        double[] B = rawQuaternionsAry[0];
        B[1] = -B[1];
        B[2] = -B[2];
        B[3] = -B[3];

        double[] A = null;
        for(int i=1; i<6; i++)
        {
            A = rawQuaternionsAry[i];
            relativeQuaternionsAry[i-1][0] = Double.valueOf(df2.format(A[0] * B[0] - A[1] * B[1] - A[2] * B[2] - A[3] * B[3]));
            relativeQuaternionsAry[i-1][1] = Double.valueOf(df2.format(A[0] * B[1] + A[1] * B[0] + A[2] * B[3] - A[3] * B[2]));
            relativeQuaternionsAry[i-1][2] = Double.valueOf(df2.format(A[0] * B[2] - A[1] * B[3] + A[2] * B[0] + A[3] * B[1]));
            relativeQuaternionsAry[i-1][3] = Double.valueOf(df2.format(A[0] * B[3] + A[1] * B[2] - A[2] * B[1] + A[3] * B[0]));
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

    private void updateSampleCount()
    {
        Platform.runLater(() ->
                          {
                              sampleCntAmtLbl.setText("" + totalSampleCount);
                          });
    }


    private void writeToFile()
    {
        String fileName = userNameTxt.getText().trim();
        if(fileName.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("Please specify the file name");
            alert.showAndWait();

            return;
        }

        File file = new File(userNameTxt.getText() + ".arff");
        if(file.exists())
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setContentText("File Exists. User another name");
            alert.showAndWait();

            return;
        }

        rawBufferList.add(rawBuffer);
        quatBufferList.add(quartBuffer);
        relativeAngBufferList.add(relativeAngBuffer);

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
            System.out.println("******** data file written ********");
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
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });
            System.out.println("******** raw data written ********");
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }


        try( Writer fileWriter = new FileWriter(userNameTxt.getText() + "_quart.arff") )
        {
            quatBufferList.forEach(buffer -> {
                try
                {
                    fileWriter.write(buffer.toString());
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });
            System.out.println("******** quart data written ********");
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }


        try( Writer fileWriter = new FileWriter(userNameTxt.getText() + "_relative_angle.arff") )
        {
            relativeAngBufferList.forEach(buffer -> {
                try
                {
                    fileWriter.write(buffer.toString());
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });
            System.out.println("******** relative angle data written ********");
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
