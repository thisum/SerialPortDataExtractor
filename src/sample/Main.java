package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application implements EventHandler<ActionEvent>
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

    private GridPane gridPane;
    private GridPane gridTopPane;
    private GridPane gridBottomPane;

    private Button startBtn;
    private Button stopBtn;
    private Button writeToFileBtn;
    private Button closeBtn;
    private Button clearBtn;
    private Button undoBtn;

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
    private SerialClass serialClass;
    private int totalSampleCount = 0;
    private int fixedSampleSize = 0;
    private int trackFixedSample = 0;
    private int experimentCount = 1;
    private boolean addToList = false;

    private int totalPrintedLines = 0;
    private List<StringBuffer> bufferList = new ArrayList<>();

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
        primaryStage.setTitle("Data Extractor");

        objTable = new ObjTable();

        startBtn = new Button("Start");
        stopBtn = new Button("Stop");
        writeToFileBtn = new Button("Write To File");
        closeBtn = new Button("Close");
        undoBtn = new Button("Undo");

        userNameLbl = new Label("User: ");
        userNameTxt = new TextField();

        objectLbl = new Label("Object: ");
        objectTxt = new TextField();

        sampleCntLbl = new Label("Sample Count: ");
        sampleCntAmtLbl = new Label("0");

        clearBtn = new Button("Clear");
        gridPane = new GridPane();

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

        gridPane.add(gridTopPane, 0, 0, 2,1);
        gridPane.add(logsTxt, 0, 1);
        gridPane.add(objTable.getTable(), 1, 1);
        gridPane.add(gridBottomPane, 0, 3, 2, 1);

        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        gridTopPane.setMinWidth(1180);
        logsTxt.setMinWidth(960);
        logsTxt.setMinHeight(500);
        gridBottomPane.setMinWidth(1180);

        Scene scene = new Scene(gridPane, 1260, 800);
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera();
        perspectiveCamera.setNearClip(0.001);
        perspectiveCamera.setFarClip(10000);
        scene.setCamera(perspectiveCamera);
        primaryStage.setScene(scene);
        primaryStage.show();

        write = false;
        objectList.clear();
        String object = objTable.updateStatus(experimentCount);
        objectTxt.setText(object);
        setupDataStream();
    }

    private void setupDataStream()
    {
        serialClass = new SerialClass();
        serialClass.setDataListener(s ->
                                    {
                                        if( write )
                                        {
                                            if( fixedSampleSize == 0 )
                                            {
                                                updateDataOnUI(s);
                                            }
                                            else if( (fixedSampleSize > 0 && trackFixedSample < fixedSampleSize) )
                                            {
                                                trackFixedSample++;
                                                updateDataOnUI(s);
                                            }
                                            else if(fixedSampleSize > 0 && trackFixedSample == fixedSampleSize)
                                            {
                                                experimentCount++;
                                                String object = objTable.updateStatus(experimentCount);
                                                objectTxt.setText(object);
                                                write = false;
                                            }
                                        }
                                    });
    }

    @Override
    public void stop() throws Exception
    {
        totalSampleCount = 0;
        serialClass.close();
        super.stop();
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
            serialClass.close();
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

}
