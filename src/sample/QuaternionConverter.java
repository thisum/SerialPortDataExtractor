package sample;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;


/**
 * Created by thisum_kankanamge on 13/9/18.
 */
public class QuaternionConverter
{
    private static String HEADER = "@relation handgrasp_object_detection\n" +
            "\n" +
            "@attribute T_q0 real\n" +
            "@attribute T_q1 real\n" +
            "@attribute T_q2 real\n" +
            "@attribute T_q3 real\n" +
            "@attribute I_q0 real\n" +
            "@attribute I_q1 real\n" +
            "@attribute I_q2 real\n" +
            "@attribute I_q3 real\n" +
            "@attribute M_q0 real\n" +
            "@attribute M_q1 real\n" +
            "@attribute M_q2 real\n" +
            "@attribute M_q3 real\n" +
            "@attribute R_q0 real\n" +
            "@attribute R_q1 real\n" +
            "@attribute R_q2 real\n" +
            "@attribute R_q3 real\n" +
            "@attribute P_q0 real\n" +
            "@attribute P_q1 real\n" +
            "@attribute P_q2 real\n" +
            "@attribute P_q3 real\n" +
            "@attribute Class {grab_a_book, hold_a_mouse, hold_a_pen, hold_a_pencil, hold_a_bottle, hold_water_bottle, grab_water_bottle, grab_water_glass, grab_wine_glass, hold_mug_handle, hold_a_knife, hold_a_spoon, hold_a_bottle_sprayer, grab_workshop_door_lock, hold_screw_driver, hold_a_hammer, hold_a_saw, hold_a_plier, grab_emergency_door_lock}\n" +
            "\n" +
            "@data\n";

    private List<String> keepList = new ArrayList<String>();//"grab_a_book", "hold_a_mouse", "hold_a_pen", "hold_water_bottle", "grab_wine_glass", "hold_mug_handle", "hold_a_knife", "hold_a_spoon", "grab_workshop_door_lock", "grab_emergency_door_lock"
    private Path writingPath;
    private int lineCount = 0;
    private int instanceCount = 1;
    private List<StringBuilder> trainingList = new ArrayList<>();
    private StringBuilder builder;
    private boolean readingData = false;
    private MadgwickQuaternionCalculator calculator;
    private String targetFile = "";

    private float mRes = 1.50f;
    private float[][] magCal = {{1.2f, 1.2f, 1.16f}, {1.2f, 1.21f, 1.16f}, {1.21f, 1.21f , 1.17f}, {1.2f, 1.2f, 1.15f}, {1.22f, 1.21f, 1.18f}, {1.21f, 1.2f, 1.16f}};
    private float[][] magBias = {{-41.36f, 41.36f, -64.15f}, {32.47f, 474.17f, -361.29f}, {216.76f, 119.83f, -462.12f}, {288.55f, 37.76f, -215.98f}, {429.43f, 375.23f, -438.98f}, {242.51f, -557.42f, -239.12f}};
    private float[][] magScale = {{0.96f, 1.07f, 0.98f}, {0.91f, 1.08f, 1.02f}, {0.91f, 1.06f, 1.04f}, {1.02f, 1.00f, 0.98f}, {1.11f, 0.95f, 0.95f}, {1.09f, 1.07f, 0.87f}};
    private float[] q;
    private float[][] quaternions = new float[6][4];
    private MovingAvgClass []movingAvg = new MovingAvgClass[20];
    private float[] qtVals = {0.0f, 0.0f, 0.0f, 0.0f};
    private int p =0 ;
    private float avgVal = 0.0f;
    private boolean writeFile = false;

    public void readFromCSV()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String filePath = scanner.next();
        createConvertedFileDirectory(filePath);

        initializeCalculators();

        listAllFiles(filePath);

        System.out.println("finished writing files");
    }

    private void initializeCalculators()
    {
        calculator = new MadgwickQuaternionCalculator();
        for(int k =0; k<20; k++)
        {
            movingAvg[k] = new MovingAvgClass(5);
        }
    }

    private void createConvertedFileDirectory(String filePath)
    {
        Path path = Paths.get(filePath).getParent();
        writingPath = Paths.get(path.toString(), "Converted_Files");
        //if directory exists?
        if( !Files.exists(writingPath) )
        {
            try
            {
                Files.createDirectories(writingPath);
            }
            catch( IOException e )
            {
                //fail to create directory
                e.printStackTrace();
            }
        }
    }

    private void listAllFiles(String path)
    {
        System.out.println("Listing all the files in the path\n");
        try( Stream<Path> paths = Files.walk(Paths.get(path)) )
        {
            paths.forEach(filePath ->
                          {
                              try
                              {
                                  if( Files.isRegularFile(filePath) && !Files.isHidden(filePath) )
                                  {
                                      readContent(filePath);
                                  }
                              }
                              catch( Exception e )
                              {
                                  e.printStackTrace();
                              }
                          });
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            writeFinalFile();
        }
    }


    private void readContent(Path filePath)
    {
        targetFile = filePath.getFileName().toString().split("\\.")[0];
        System.out.println("processing file: " + targetFile + "\n");
        builder = new StringBuilder();
        trainingList.add(builder);
        final String[] label = {""};

        try
        {
            List<String> fileLines = Files.readAllLines(filePath);
            fileLines.forEach(line ->
                              {
                                  try
                                  {
                                      if(readingData)
                                      {
                                          lineCount++;
                                          String convertedString = "";
                                          String[] elements = line.split(",", 55);
                                          if(!label[0].isEmpty() && !label[0].equals(elements[54]))
                                          {
                                              resetMovingAvgCals();
                                              calculator.reset();
                                              writeFile = false;
                                          }
                                          label[0] = elements[54];
                                          elements[54] = "0";

                                          double[] val = Arrays.stream(elements)
                                                  .mapToDouble(Double::parseDouble)
                                                  .toArray();
                                          double mx, my, mz;
                                          for(int i=0; i<6; i++)
                                          {
                                              int pos = i*9;
                                              mx = val[pos+6]*mRes*magCal[i][0] - magBias[i][0];
                                              my = val[pos+7]*mRes*magCal[i][1] - magBias[i][1];
                                              mz = val[pos+8]*mRes*magCal[i][2] - magBias[i][2];
                                              mx *= magScale[i][0];
                                              my *= magScale[i][1];
                                              mz *= magScale[i][2];
                                              q = calculator.calculateQuaternions(val[pos], val[pos+1], val[pos+2], val[pos+3], val[pos+4], val[pos+5], mx, my, mz);
                                              quaternions[i] = new float[]{q[0], q[1], q[2], q[3]};
                                          }

                                          //TODO to wirte the quaternion values directly to a file
                                          //for(int i=0; i<5; i++)
                                          //{
                                          //    convertedString += "," + quatDiff(quaternions[i], quaternions[5]);
                                          //}
                                          //convertedString = convertedString.replaceFirst(",", "");
                                          //builder.append(convertedString).append(",").append(label[0]).append("\n");
                                          //
                                          //if(lineCount>5000)
                                          //{
                                          //    builder = new StringBuilder();
                                          //    trainingList.add(builder);
                                          //    lineCount = 0;
                                          //}

                                          //TODO Write moving averages to a file
                                          for(int i=0; i<5; i++)
                                          {
                                              qtVals = quatDiff(quaternions[i], quaternions[5]);
                                              for(p = 0; p<qtVals.length; p++)
                                              {
                                                  avgVal = movingAvg[i*4 + p].calculateAvg(qtVals[p]);
                                                  if(avgVal != -1)
                                                  {
                                                      convertedString += "," + avgVal;
                                                      writeFile = true;
                                                  }
                                              }

                                          }
                                          if(writeFile)
                                          {
                                              convertedString = convertedString.replaceFirst(",", "");
                                              builder.append(convertedString).append(",").append(label[0]).append("\n");

                                              if(lineCount>5000)
                                              {
                                                  builder = new StringBuilder();
                                                  trainingList.add(builder);
                                                  lineCount = 0;
                                              }
                                          }
                                      }
                                      else
                                      {
                                          readingData = line.equals("@data");
                                      }
                                  }
                                  catch( Exception e )
                                  {
                                      e.printStackTrace();
                                      System.out.println("line count: " + lineCount);
                                  }

                              });
            readingData = false;
            writeFinalFile();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    private float[] quatDiff(float[] A, float[] back)
    {
        float B[] = { back[0], -back[1], -back[2], -back[3]};

        float q00 = (A[0] * B[0] - A[1] * B[1] - A[2] * B[2] - A[3] * B[3])*100;
        float q11 = (A[0] * B[1] + A[1] * B[0] + A[2] * B[3] - A[3] * B[2])*100;
        float q22 = (A[0] * B[2] - A[1] * B[3] + A[2] * B[0] + A[3] * B[1])*100;
        float q33 = (A[0] * B[3] + A[1] * B[2] - A[2] * B[1] + A[3] * B[0])*100;

        return new float[]{q00, q11, q22, q33};
//        return q00 + "," + q11 + "," + q22 + "," + q33;
    }

    private void resetMovingAvgCals()
    {
        for(int k =0; k<20; k++)
        {
            movingAvg[k].reset();
        }
    }

    private void writeFinalFile()
    {
        System.out.println("writing final file...\n");
        try( Writer trainWriter = new FileWriter(writingPath + "/" + targetFile + "_exclude_qat.arff"))
        {
            trainWriter.write(HEADER);
            trainingList.forEach(stringBuilder ->
                                 {
                                     try
                                     {
                                         trainWriter.write(stringBuilder.toString());
                                     }
                                     catch( IOException e )
                                     {
                                         e.printStackTrace();
                                     }
                                 });
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

}
