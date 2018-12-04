package com.thisum;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


public class SerialClass implements SerialPortEventListener
{
    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {"/dev/tty.usbmodem4869681"};
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 9600;
    private DataListener dataListener;
    private boolean calculateQ = false;
    private int deviceId = 0;

    public SerialClass(int deviceId)
    {
        this.deviceId = deviceId;
        initialize();
    }

    public void initialize()
    {
        // the next line is for Raspberry Pi and
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186

        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while( portEnum.hasMoreElements() )
        {
            CommPortIdentifier currPortId = ( CommPortIdentifier ) portEnum.nextElement();
            for( String portName : PORT_NAMES )
            {
                if( currPortId.getName().equals(portName) )
                {
                    portId = currPortId;
                    break;
                }
            }
        }
        if( portId == null )
        {
            System.out.println("Could not find COM port.");
            return;
        }

        try
        {
            // open serial port, and use class name for the appName.
            serialPort = ( SerialPort ) portId.open(this.getClass().getName(),
                                                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                                           SerialPort.DATABITS_8,
                                           SerialPort.STOPBITS_1,
                                           SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch( Exception e )
        {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close()
    {
        if( serialPort != null )
        {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent)
    {
        if( oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
        {
            try
            {
                String inputLine = input.readLine();
                if(!calculateQ)  System.out.println(inputLine);

                if(dataListener != null && calculateQ)
                {
                    dataListener.onDataAvailable(deviceId, inputLine);
                }

                if(!calculateQ && inputLine.contains("**##**"))
                {
                    calculateQ = true;
                    System.out.println("--------------------------------- Setup Done --------------------------------- \n");
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public void setDataListener(DataListener dataListener)
    {
        this.dataListener = dataListener;
//        mockData();
    }

    private void mockData()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while(true)
                    {
                        try
                        {
                            dataListener.onDataAvailable(deviceId, "1,-0.95,0.13,0.35,0.05,0.01,0.02,267.82,-100.7,-324.49#2,-1.0,0.21,-0.08,0.05,0.02,-0.0,327.44,-268.86,182.96#3,-0.89,0.29,-0.45,0.07,0.01,0.01,305.68,-262.13,283.24#4,-0.86,0.59,-0.02,0.07,0.01,-0.02,193.65,-422.91,145.42#5,-0.81,0.73,0.08,0.06,-0.0,-0.05,215.28,-392.28,163.59#6,0.49,0.8,0.32,-0.03,-0.03,-0.05,-294.36,-382.09,62.62");
                            Thread.sleep(50);
                        }
                        catch( NullPointerException e )
                        {

                        }
                    }
                }
                catch( InterruptedException e )
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}