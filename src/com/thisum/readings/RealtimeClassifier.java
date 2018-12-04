package com.thisum.readings;

import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.Arrays;

public class RealtimeClassifier
{
    private RandomForest anglesRF;
    private RandomForest quaterRF;

    private SMO anglesSMO;
    private SMO quaterSMO;

    private Instances quatDataInstances;
    private Instances anglDataInstances;
    private double predication = 0.0;
    private String predictedClass = "";
    private int lastPrediction = 0;
    private int tempPrediction = 0;

    public void loadClassifier()
    {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        BufferedReader quatInput = null;
        BufferedReader angleInput = null;

        try
        {
            fis = new FileInputStream("/Users/thisum/Documents/Personel_Docs/NUS_MSc/Research/Programing/Application/SerialPortDataExtractor/angle_classifier.model");
            ois = new ObjectInputStream(fis);
            anglesRF = ( RandomForest ) ois.readObject();

            fis = new FileInputStream("/Users/thisum/Documents/Personel_Docs/NUS_MSc/Research/Programing/Application/SerialPortDataExtractor/quat_classifier.model");
            ois = new ObjectInputStream(fis);
            quaterRF = ( RandomForest ) ois.readObject();

            angleInput = new BufferedReader(new FileReader("/Users/thisum/Documents/Personel_Docs/NUS_MSc/Research/Programing/Application/SerialPortDataExtractor/angle_test.arff"));
            anglDataInstances = new Instances(angleInput);
            anglDataInstances.setClassIndex(anglDataInstances.numAttributes() - 1);

            quatInput = new BufferedReader(new FileReader("/Users/thisum/Documents/Personel_Docs/NUS_MSc/Research/Programing/Application/SerialPortDataExtractor/quat_test.arff"));
            quatDataInstances = new Instances(quatInput);
            quatDataInstances.setClassIndex(quatDataInstances.numAttributes() - 1);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fis.close();
                ois.close();
                angleInput.close();
                quatInput.close();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    public void classifyAngles(double[][] doubleValues)
    {
        double[] flatArray = Arrays.stream(doubleValues).flatMapToDouble(Arrays::stream).toArray();
        try
        {
            DenseInstance denseInstance = new DenseInstance(1.0, flatArray);
            anglDataInstances.add(denseInstance);

            Instance current = anglDataInstances.instance(0);
            predication = anglesRF.classifyInstance(current);
            tempPrediction = ( int ) predication;
//            if(tempPrediction != lastPrediction)
            {
                predictedClass = predication == -1 ? "Not Detected" : anglDataInstances.classAttribute().value(tempPrediction);

                System.out.println("prediction: " + predictedClass + "   score: " + predication + "\n");
                lastPrediction = tempPrediction;
            }

            anglDataInstances.remove(0);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }


    public void classifyQuat(double[][] doubleValues)
    {
        double[] flatArray = Arrays.stream(doubleValues).flatMapToDouble(Arrays::stream).toArray();

        try
        {
            DenseInstance denseInstance = new DenseInstance(1.0, flatArray);
            quatDataInstances.add(denseInstance);

            Instance current = quatDataInstances.instance(0);
            predication = quaterRF.classifyInstance(current);
            tempPrediction = ( int ) predication;
//            if(tempPrediction != lastPrediction)
            {
                predictedClass = predication == -1 ? "Not Detected" : quatDataInstances.classAttribute().value(tempPrediction);

                System.out.println("prediction: " + predictedClass + "   score: " + predication + "\n");
                lastPrediction = tempPrediction;
            }

            quatDataInstances.remove(0);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public double classifyInstance(Instance instance) throws Exception
    {
//        double[] dist = rf.distributionForInstance(instance);
////        double[] dist = smo.distributionForInstance(instance);
//        if(dist == null) {
//            throw new Exception("Null distribution predicted");
//        } else {
//            switch(instance.classAttribute().type()) {
//                case 0:
//                case 3:
//                    return dist[0];
//                case 1:
//                    double max = 0.0D;
//                    int maxIndex = 0;
//
//                    for(int i = 0; i < dist.length; ++i) {
//                        if(dist[i] > max) {
//                            maxIndex = i;
//                            max = dist[i];
//                        }
//                    }
//
//                    if(max > 0.0D) {
//                        return (double)maxIndex;
//                    }
//
//                case 2:
//                default:
//                    return Utils.missingValue();
//            }
//        }
        return 0.0d;
    }

}
