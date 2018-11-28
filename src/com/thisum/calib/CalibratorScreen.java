package com.thisum.calib;

import com.thisum.DataListener;
import com.thisum.SerialClass;
import com.thisum.calib.GraphPanel;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.util.Arrays;

public class CalibratorScreen extends GridPane implements DataListener
{
    private TextArea logsTxt;
    private GraphPanel graphPanel;
    private SerialClass serialClass;
    private boolean readCalibValue;
    private static String START_POINT = "##--##--##--##";
    private static String END_POINT = "**--**--**--**";
    private int[] intValues;

    public CalibratorScreen( SerialClass serialClass )
    {
        this.serialClass = serialClass;
        logsTxt = new TextArea();
        graphPanel = new GraphPanel();

        setup();
    }

    private void setup()
    {
        this.add(graphPanel.getGraphPanel(), 0, 0);
        this.add(logsTxt, 1, 0);
        serialClass.setDataListener(this);
    }

    @Override
    public void onDataAvailable(int deviceId, String s)
    {
        try
        {
            Platform.runLater(()->{
                logsTxt.appendText(s + "\n");
            });

            if( readCalibValue )
            {
                if(s.contains(END_POINT))
                {
                    readCalibValue = false;
                }
                if(readCalibValue)
                {
                    intValues = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
                    graphPanel.setData(intValues[0], intValues[1], intValues[2]);
                }
            }
            else if(s.contains(START_POINT))
            {
                readCalibValue = true;
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
