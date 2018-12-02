package com.thisum;

import java.text.DecimalFormat;

public class AngleResult
{
    private DecimalFormat df = new DecimalFormat("#.##");

    private double yaw;
    private double pitch;
    private double roll;
    private double q1;
    private double q2;
    private double q3;
    private double q4;

    public AngleResult(double yaw, double pitch, double roll, double q1, double q2, double q3, double q4)
    {
        this.yaw = Double.valueOf(df.format(yaw));
        this.pitch = Double.valueOf(df.format(pitch));
        this.roll = Double.valueOf(df.format(roll));
        this.q1 = Double.valueOf(df.format(q1));
        this.q2 = Double.valueOf(df.format(q2));
        this.q3 = Double.valueOf(df.format(q3));
        this.q4 = Double.valueOf(df.format(q4));
    }

    public double[] getAngles()
    {
        return new double[]{yaw, pitch, roll};
    }

    public double[] getQuarternions()
    {
        return new double[]{q1, q2, q3, q4};
    }
}
