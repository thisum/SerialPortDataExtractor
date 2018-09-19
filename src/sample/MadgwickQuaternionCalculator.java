package sample;

/**
 * Created by thisum_kankanamge on 13/9/18.
 */
public class MadgwickQuaternionCalculator
{
    private float Kp = 30.0f;
    private float Ki = 0.02f;


    private float[] q = {1.0f, 0.0f, 0.0f, 0.0f};
    private float q1, q2, q3, q4;
    private float norm;
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
    private float s1, s2, s3, s4;
    private float qDot1, qDot2, qDot3, qDot4;
    private double GyroMeasError = Math.PI * (4.0f / 180.0f);
    private double beta = Math.sqrt(3.0f / 4.0f) * GyroMeasError;

    public void reset()
    {
        float[] q = {1.0f, 0.0f, 0.0f, 0.0f};
    }

    public float[] calculateQuaternions(double ax, double ay, double az, double gx, double gy, double gz, double mx, double my, double mz)
    {
        q1 = q[0];
        q2 = q[1];
        q3 = q[2];
        q4 = q[3];

        _2q1      = 2.0f * q1;
        _2q2      = 2.0f * q2;
        _2q3      = 2.0f * q3;
        _2q4      = 2.0f * q4;
        _2q1q3    = 2.0f * q1 * q3;
        _2q3q4    = 2.0f * q3 * q4;

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

        norm = (float ) Math.sqrt(ax * ax + ay * ay + az * az);

        if (norm == 0.0f) return q;

        norm = 1.0f / norm;        // use reciprocal for division
        ax *= norm;
        ay *= norm;
        az *= norm;

        // Normalise magnetometer measurement
        norm = (float ) Math.sqrt(mx * mx + my * my + mz * mz);
        if (norm == 0.0f) return q; // handle NaN
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
        _2bx = (float ) Math.sqrt(hx * hx + hy * hy);
        _2bz = (float)(-_2q1mx * q3 + _2q1my * q2 + mz * q1q1 + _2q2mx * q4 - mz * q2q2 + _2q3 * my * q4 - mz * q3q3 + mz * q4q4);
        _4bx = 2.0f * _2bx;
        _4bz = 2.0f * _2bz;

        // Gradient decent algorithm corrective step
        s1 = (float)(-_2q3 * (2.0f * q2q4 - _2q1q3 - ax) + _2q2 * (2.0f * q1q2 + _2q3q4 - ay) - _2bz * q3 * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (-_2bx * q4 + _2bz * q2) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q3 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz));
        s2 = (float)(_2q4 * (2.0f * q2q4 - _2q1q3 - ax) + _2q1 * (2.0f * q1q2 + _2q3q4 - ay) - 4.0f * q2 * (1.0f - 2.0f * q2q2 - 2.0f * q3q3 - az) + _2bz * q4 * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q3 + _2bz * q1) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + (_2bx * q4 - _4bz * q2) * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz));
        s3 = (float)(-_2q1 * (2.0f * q2q4 - _2q1q3 - ax) + _2q4 * (2.0f * q1q2 + _2q3q4 - ay) - 4.0f * q3 * (1.0f - 2.0f * q2q2 - 2.0f * q3q3 - az) + (-_4bx * q3 - _2bz * q1) * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (_2bx * q2 + _2bz * q4) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + (_2bx * q1 - _4bz * q3) * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz));
        s4 = (float)(_2q2 * (2.0f * q2q4 - _2q1q3 - ax) + _2q3 * (2.0f * q1q2 + _2q3q4 - ay) + (-_4bx * q4 + _2bz * q2) * (_2bx * (0.5f - q3q3 - q4q4) + _2bz * (q2q4 - q1q3) - mx) + (-_2bx * q1 + _2bz * q3) * (_2bx * (q2q3 - q1q4) + _2bz * (q1q2 + q3q4) - my) + _2bx * q2 * (_2bx * (q1q3 + q2q4) + _2bz * (0.5f - q2q2 - q3q3) - mz));
        norm = (float ) Math.sqrt(s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);    // normalise step magnitude
        norm = 1.0f/norm;
        s1 *= norm;
        s2 *= norm;
        s3 *= norm;
        s4 *= norm;

        // Compute rate of change of quaternion
        qDot1 = (float)(0.5f * (-q2 * gx - q3 * gy - q4 * gz) - beta * s1);
        qDot2 = (float)(0.5f * (q1 * gx + q3 * gz - q4 * gy) - beta * s2 );
        qDot3 = (float)(0.5f * (q1 * gy - q2 * gz + q4 * gx) - beta * s3 );
        qDot4 = (float)(0.5f * (q1 * gz + q2 * gy - q3 * gx) - beta * s4 );

        // Integrate to yield quaternion
        q1 += qDot1 * deltat;
        q2 += qDot2 * deltat;
        q3 += qDot3 * deltat;
        q4 += qDot4 * deltat;
        norm = (float ) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);    // normalise quaternion
        norm = 1.0f/norm;
        q[0] = q1 * norm;
        q[1] = q2 * norm;
        q[2] = q3 * norm;
        q[3] = q4 * norm;


        return q;
    }

}
