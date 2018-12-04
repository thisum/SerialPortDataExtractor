package com.thisum.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class QuaternionCalculator
{
    private DecimalFormat df = new DecimalFormat("#.##");
    private double rad2deg = 57.29;
    private StringBuffer quatBuffer = new StringBuffer();
    private StringBuffer eulerBuffer = new StringBuffer();

    public static void main(String[] args)
    {
        QuaternionCalculator calculator = new QuaternionCalculator();
        calculator.manager();
    }

    private void manager()
    {
        try
        {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter file name: ");
            String filePath = scanner.next();
            Path path = Paths.get(filePath);
            readFile(path);
            writeToFile(path.getParent());
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

    }

    private void readFile(Path filePath) throws Exception
    {
        String []elements = null;
        double []qVals = null;
        List<String> lines = Files.readAllLines(filePath);

        for(String line: lines)
        {
            elements = line.split(",", 25);
            qVals = Arrays.stream(Arrays.copyOfRange(elements, 0, 24)).mapToDouble(Double::parseDouble).toArray();

            quatDiffnew(Arrays.copyOfRange(qVals, 0, 4), Arrays.copyOfRange(qVals, 4, 8));
            quatDiffnew(Arrays.copyOfRange(qVals, 0, 4), Arrays.copyOfRange(qVals, 8, 12));
            quatDiffnew(Arrays.copyOfRange(qVals, 0, 4), Arrays.copyOfRange(qVals, 12, 16));
            quatDiffnew(Arrays.copyOfRange(qVals, 0, 4), Arrays.copyOfRange(qVals, 16, 20));
            quatDiffnew(Arrays.copyOfRange(qVals, 0, 4), Arrays.copyOfRange(qVals, 20, 24));
            quatBuffer.append(elements[24]);
            quatBuffer.append("\n");

            eulerBuffer.append(elements[24]);
            eulerBuffer.append("\n");
        }

    }

    public void quatDiffnew(double[] A, double[] B)
    {
        B[1] = -B[1];
        B[2] = -B[2];
        B[3] = -B[3];

        double q00 = A[0] * B[0] - A[1] * B[1] - A[2] * B[2] - A[3] * B[3];
        double q11 = A[0] * B[1] + A[1] * B[0] + A[2] * B[3] - A[3] * B[2];
        double q22 = A[0] * B[2] - A[1] * B[3] + A[2] * B[0] + A[3] * B[1];
        double q33 = A[0] * B[3] + A[1] * B[2] - A[2] * B[1] + A[3] * B[0];

        quatBuffer.append(Double.valueOf(df.format(q00)));
        quatBuffer.append(",");
        quatBuffer.append(Double.valueOf(df.format(q11)));
        quatBuffer.append(",");
        quatBuffer.append(Double.valueOf(df.format(q22)));
        quatBuffer.append(",");
        quatBuffer.append(Double.valueOf(df.format(q33)));
        quatBuffer.append(",");


        double q_X;
        double q_Y;
        double q_Z;

        double unit = q00 * q00 + q11 * q11 + q22 * q22 + q33 * q33;// q0 * q0 + q3 * q3 + q1 * q1 + q2 * q2;
        double test = q11 * q22 + q33 * q00;//q1 * q2 + q3 * q0;
        if (test > 0.499 * unit)
        {
            // Singularity at north pole
            q_X = 2 * Math.atan2(q11, q00);//q1, q0);  // Yaw
            q_Y = 180 * 0.5;                         // Pitch
            q_Z = 0;                                // Roll
        }

        else if (test < -0.499 * unit)
        {
            // Singularity at south pole
            q_X = -2 * Math.atan2(q11, q00);//q1, q0); // Yaw
            q_Y = -180 * 0.5;                        // Pitch
            q_Z = 0;                                // Roll
        }

        else
        {
            q_X = qx(q00, q11, q22, q33);
            q_Y = qy(q00, q11, q22, q33);
            q_Z = qz(q00, q11, q22, q33);
        }

        eulerBuffer.append(Double.valueOf(df.format(q_X)));
        eulerBuffer.append(",");
        eulerBuffer.append(Double.valueOf(df.format(q_Y)));
        eulerBuffer.append(",");
        eulerBuffer.append(Double.valueOf(df.format(q_Z)));
        eulerBuffer.append(",");
    }

    public double qx(double q0, double q1, double q2, double q3)
    {
        double qx = Math.atan2(2 * (-q2 * q1 + q0 * q3), (2 * q1 * q1 + 2 * q0 * q0) - 1) * rad2deg;
        return qx;
    }

    public double qy(double q0, double q1, double q2, double q3)
    {
        double qy = Math.asin(2 * (q1 * q3 + q0 * q2)) * rad2deg;
        return qy;
    }

    public double qz(double q0, double q1, double q2, double q3)
    {
        double qz = -Math.atan2(2 * (q2 * q3 - q0 * q1), (2 * q0 * q0 + 2 * q3 * q3) - 1) * rad2deg;
        return qz;
    }

    private void writeToFile(Path path)
    {
        System.out.println("writing testing and training files...\n");
        try( Writer trainWriter = new FileWriter(path.toString() + "/quat_train.arff");
             Writer testWriter = new FileWriter(path.toString() + "/euler_train.arff")
        )
        {
            trainWriter.write(quatBuffer.toString());

            testWriter.write(eulerBuffer.toString());
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
