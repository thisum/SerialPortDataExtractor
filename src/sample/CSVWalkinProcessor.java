package sample;

import javafx.scene.layout.HBox;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by thisum_kankanamge on 10/9/18.
 */
public class CSVWalkinProcessor
{
    private int lineCount = 0;
    private int instanceCount = 1;
    private List<StringBuilder> trainingList = new ArrayList<>();
    private List<StringBuilder> testingList = new ArrayList<>();
    private StringBuilder training;

    private StringBuilder owner;
    private boolean readingData = false;

    private Path writingPath;

    private static String HEADER = "@relation handgrasp_object_interaction_detection\n" +
            "\n" +
            "@attribute T_ax real\n" +
            "@attribute T_ay real\n" +
            "@attribute T_az real\n" +
            "@attribute T_gx real\n" +
            "@attribute T_gy real\n" +
            "@attribute T_gz real\n" +
            "@attribute T_mx real\n" +
            "@attribute T_my real\n" +
            "@attribute T_mz real\n" +
            "@attribute I_ax real\n" +
            "@attribute I_ay real\n" +
            "@attribute I_az real\n" +
            "@attribute I_gx real\n" +
            "@attribute I_gy real\n" +
            "@attribute I_gz real\n" +
            "@attribute I_mx real\n" +
            "@attribute I_my real\n" +
            "@attribute I_mz real\n" +
            "@attribute M_ax real\n" +
            "@attribute M_ay real\n" +
            "@attribute M_az real\n" +
            "@attribute M_gx real\n" +
            "@attribute M_gy real\n" +
            "@attribute M_gz real\n" +
            "@attribute M_mx real\n" +
            "@attribute M_my real\n" +
            "@attribute M_mz real\n" +
            "@attribute R_ax real\n" +
            "@attribute R_ay real\n" +
            "@attribute R_az real\n" +
            "@attribute R_gx real\n" +
            "@attribute R_gy real\n" +
            "@attribute R_gz real\n" +
            "@attribute R_mx real\n" +
            "@attribute R_my real\n" +
            "@attribute R_mz real\n" +
            "@attribute P_ax real\n" +
            "@attribute P_ay real\n" +
            "@attribute P_az real\n" +
            "@attribute P_gx real\n" +
            "@attribute P_gy real\n" +
            "@attribute P_gz real\n" +
            "@attribute P_mx real\n" +
            "@attribute P_my real\n" +
            "@attribute P_mz real\n" +
            "@attribute B_ax real\n" +
            "@attribute B_ay real\n" +
            "@attribute B_az real\n" +
            "@attribute B_gx real\n" +
            "@attribute B_gy real\n" +
            "@attribute B_gz real\n" +
            "@attribute B_mx real\n" +
            "@attribute B_my real\n" +
            "@attribute B_mz real\n" +
            "@attribute Class {flip_through_a_book, use_a_mouse, write_with_a_pen, write_with_a_pencil, close_a_bottle, open_a_bottle, carry_water_bottle, grab_water_bottle, grab_water_glass, grab_wine_glass, hold_mug_handle, grab_mug, cut_with_a_knife, use_a_spoon, use_a_bottle_sprayer, grab_workshop_door_lock, use_screw_driver, use_a_hammer, use_a_saw, use_a_plier, grab_emergency_door_lock}\n" +
            "\n" +
            "@data\n";

    public void readFile()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String filePath = scanner.next();

        createSplittedDirectory(filePath);

        listAllFiles(filePath);

        System.out.println("\nfinishing.....");
    }

    private void createSplittedDirectory(String filePath)
    {
        Path path = Paths.get(filePath).getParent();
        writingPath = Paths.get(path.toString(), "Walkin_Files");
        //if directory exists?
        if (!Files.exists(writingPath)) {
            try {
                Files.createDirectories(writingPath);
            } catch (IOException e) {
                //fail to create directory
                e.printStackTrace();
            }
        }
    }

    private void readContent(Path filePath, StringBuilder stringBuilder)
    {
        String fileName = filePath.getFileName().toString().split("\\.")[0];
        System.out.println("processing file: " + fileName + "\n");

        try
        {
            List<String> fileLines = Files.readAllLines(filePath);

            fileLines.forEach(line ->
            {
                if(readingData)
                {
                    stringBuilder.append(line).append("\n");
                }
                else
                {
                    readingData = line.equals("@data");
                }

            });
            readingData = false;
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    private void listAllFiles(String path)
    {
        System.out.println("Listing all the files in the path\n");

        for(int i = 0; i < 12; i++)
        {
            trainingList.clear();
            try( Stream<Path> paths = Files.walk(Paths.get(path)) )
            {
                final int[] file = {0};

                    int finalI = i;
                    final String[] fileOwer = new String[1];
                    paths.forEach(filePath ->
                                  {
                                      try
                                      {
                                          if( Files.isRegularFile(filePath) && !Files.isHidden(filePath))
                                          {
                                              if( file[0] != finalI )
                                              {
                                                  try
                                                  {
                                                      training = new StringBuilder();
                                                      trainingList.add(training);

                                                      readContent(filePath, training);
                                                  }
                                                  catch( Exception e )
                                                  {
                                                      e.printStackTrace();
                                                  }
                                              }
                                              else
                                              {
                                                  fileOwer[0] = filePath.getFileName().toString().split("\\.")[0];
                                                  //                                          owner = new StringBuilder();
                                                  //                                          readContent(filePath, owner);
                                              }

                                              file[0]++;
                                          }
                                      }
                                      catch( Exception e )
                                      {
                                          e.printStackTrace();
                                      }

                                  });

                    writeFinalFile(fileOwer[0]);

            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {

            }
        }
    }


    private void writeFinalFile(String s)
    {
        System.out.println("writing final testing and training files...\n");
        try( Writer trainWriter = new FileWriter(writingPath + "/" + "without_" + s + "_train.arff");
//             Writer testWriter = new FileWriter(writingPath + "/" + s + "train.arff")
        )
        {
            trainWriter.write(HEADER);
            trainingList.forEach(stringBuilder -> {
                try
                {
                    trainWriter.write(stringBuilder.toString());
                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }
            });

//            testWriter.write(HEADER);
//            trainWriter.write(owner.toString());
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
