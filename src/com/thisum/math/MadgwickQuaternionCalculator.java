package com.thisum.math;

import com.thisum.AngleResult;

/**
 * Created by thisum_kankanamge on 13/9/18.
 */
public class MadgwickQuaternionCalculator
{
    private float[] q = {1.0f, 0.0f, 0.0f, 0.0f};
    private double GyroMeasError = Math.toRadians(40.0f);
    private double beta = Math.sqrt(3.0f / 4.0f) * GyroMeasError;
    private double declination = 19.5;
    private double yaw, pitch, roll = 0.0;
    private int lastUpdate = 1;
    private long deltaT = 0;

    private float q1, q2, q3, q4;
    private double norm;
    private float hx, hy, _2bx, _2bz;;

    private float q1q1 = 0.0f;
    private float q1q2 = 0.0f;
    private float q1q3 = 0.0f;
    private float q1q4 = 0.0f;
    private float q2q2 = 0.0f;
    private float q2q3 = 0.0f;
    private float q2q4 = 0.0f;
    private float q3q3 = 0.0f;
    private float q3q4 = 0.0f;
    private float q4q4 = 0.0f;
    private float deltat = 0.03f;

    private float _2q1      = 0.0f;
    private float _2q2      = 0.0f;
    private float _2q3      = 0.0f;
    private float _2q4      = 0.0f;
    private float _2q1q3    = 0.0f;
    private float _2q3q4    = 0.0f;
    private float _2q1mx;
    private float _2q1my;
    private float _2q1mz;
    private float _2q2mx;
    private float _4bx;
    private float _4bz;
    private double s1, s2, s3, s4;
    private double qDot1, qDot2, qDot3, qDot4;
    private double a12, a22, a31, a32, a33;


    public void reset()
    {
        float[] q = {1.0f, 0.0f, 0.0f, 0.0f};
    }

    public AngleResult calculateQuaternions(double ax, double ay, double az, double gx, double gy, double gz, double mx, double my, double mz)
    {
        q1 = q[0];
        q2 = q[1];
        q3 = q[2];
        q4 = q[3];

        _2q1 = 2 * q1;
        _2q2 = 2 * q2;
        _2q3 = 2 * q3;
        _2q4 = 2 * q4;
        _2q1q3 = 2 * q1 * q3;
        _2q3q4 = 2 * q3 * q4;

        q1q1 = q1 * q1;
        q1q2 = q1 * q2;
        q1q3 = q1 * q3;
        q1q4 = q1 * q4;
        q2q2 = q2 * q2;
        q2q3 = q2 * q3;
        q2q4 = q2 * q4;
        q3q3 = q3 * q3;
        q3q4 = q3 * q4;
        q4q4 = q4 * q4;

        norm = Math.sqrt(ax * ax + ay * ay + az * az);

        if (norm == 0.0f) return null;

        norm = 1.0f / norm;        // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Normalise magnetometer measurement
        norm = Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0.0f) return null; // handle NaN
        norm = 1.0f/norm;
        mx *= norm;
        my *= norm;
        mz *= norm;

        // Reference direction of Earth's magnetic field
        _2q1mx = (float)(2.0f * q1 * mx);
        _2q1my = (float)(2.0f * q1 * my);
        _2q1mz = (float)(2.0f * q1 * mz);
        _2q2mx = (float)(2.0f * q2 * mx);
        hx = (float)(mx * q1q1 - _2q1my * q4 + _2q1mz * q3 + mx * q2q2 + _2q2 * my * q3 + _2q2 * mz * q4 - mx * q3q3 - mx * q4q4);
        hy = (float)(_2q1mx * q4 + my * q1q1 - _2q1mz * q2 + _2q2mx * q3 - my * q2q2 + my * q3q3 + _2q3 * mz * q4 - my * q4q4);
        _2bx = (float)Math.sqrt(hx * hx + hy * hy);
        _2bz = (float)(-_2q1mx * q3 + _2q1my * q2 + mz * q1q1 + _2q2mx * q4 - mz * q2q2 + _2q3 * my * q4 - mz * q3q3 + mz * q4q4);
        _4bx = 2 * _2bx;
        _4bz = 2 * _2bz;

        s1 = (-_2q3 * (2.0 * q2q4 - _2q1q3 - ax) + _2q2 * (2.0 * q1q2 + _2q3q4 - ay) - _2bz * q3 * (_2bx * (0.5 - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (-_2bx * q4 + _2bz * q2) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q3 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5 - q2q2 - q3q3) - mz));

        s2 = (_2q4 * (2.0 * q2q4 - _2q1q3 - ax) + _2q1 * (2.0 * q1q2 + _2q3q4 - ay) - 4.0 * q2 * (1.0 - 2.0 * q2q2 - 2.0 * q3q3 - az)
                + _2bz * q4 * (_2bx * (0.5 - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q3 + _2bz * q1) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
                + (_2bx * q4 - _4bz * q2) * ( _2bx * (q1q3 + q2q4) + _2bz * ( 0.5 - q2q2 - q3q3) - mz));

        s3 = (-_2q1 * (2.0 * q2q4 - _2q1q3 - ax) + _2q4 * (2.0 * q1q2 + _2q3q4 - ay) - 4.0 * q3 * (1.0 - 2.0 * q2q2 - 2.0 * q3q3 - az)
                + (-_4bx * q3 - _2bz * q1) * (_2bx * (0.5 - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q2 + _2bz * q4) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my)
                + (_2bx * q1 - _4bz * q3) * (_2bx * (q1q3 + q2q4) + _2bz * (0.5 - q2q2 - q3q3) - mz));

        s4 = (_2q2 * (2.0 * q2q4 - _2q1q3 - ax) + _2q3 * (2.0 * q1q2 + _2q3q4 - ay) + (-_4bx * q4 + _2bz * q2) * (_2bx * (0.5 - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx)
                + (-_2bx * q1 + _2bz * q3) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q2 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5 - q2q2 - q3q3) - mz));

        norm = 1 / Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        qDot1 = 0.5 * (-q2 * gx - q3 * gy - q4 * gz) - beta * s1;
        qDot2 = 0.5 * (q1 * gx + q3 * gz - q4 * gy) - beta * s2;
        qDot3 = 0.5 * (q1 * gy - q2 * gz + q4 * gx) - beta * s3;
        qDot4 = 0.5 * (q1 * gz + q2 * gy - q3 * gx) - beta * s4;

        q1 += qDot1 * deltat;
        q2 += qDot2 * deltat;
        q3 += qDot3 * deltat;
        q4 += qDot4 * deltat;
        norm = 1 / Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);

        q1 = (float)(q1 * norm);
        q2 = (float)(q2 * norm);
        q3 = (float)(q3 * norm);
        q4 = (float)(q4 * norm);

        q[0] = q1;
        q[1] = q2;
        q[2] = q3;
        q[3] = q4;

        a12 = 2.0 * (q2 * q3 + q1 * q4);
        a22 = q1 * q1 + q2 * q2 - q3 * q3 - q4 * q4;
        a31 = 2.0 * (q1 * q2 + q3 * q4);
        a32 = 2.0 * (q2 * q4 - q1 * q3);
        a33 = q1 * q1 - q2 * q2 - q3 * q3 + q4 * q4;

        pitch = Math.toDegrees(-Math.asin(a32));
        roll = Math.toDegrees(Math.atan2(a31, a33));

        yaw = declination + Math.toDegrees(Math.atan2(a12, a22));
        if (yaw > 270) yaw -= 360.0;
        if(roll<0) roll += 360.0f;
        if(roll >270) roll -=360.0f;


        return new AngleResult(yaw, pitch, roll, q1, q2, q3, q4);
    }

}
