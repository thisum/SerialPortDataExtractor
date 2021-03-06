package com.thisum.math;

/**
 * Created by thisum_kankanamge on 12/9/18.
 */
public class MahonyQuaternionCalculator
{
    private float Kp = 30.0f;
    private float Ki = 0.02f;

    private float[] q = {0.0f, 0.0f, 0.0f, 0.0f};
    private float[] eInt = {0.0f, 0.0f, 0.0f};
    private float norm;
    private float hx, hy, bx, bz;
    private float vx, vy, vz, wx, wy, wz;
    private float ex, ey, ez;
    private float pa, pb, pc;
    private float q1, q2, q3, q4;

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

    public float[] calculateQuaternions(float ax, float ay, float az, float gx, float gy, float gz, float mx, float my, float mz)
    {
        q1 = q[0];
        q2 = q[1];
        q3 = q[2];
        q4 = q[3];

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
        mx *= norm;
        my *= norm;
        mz *= norm;

        // Reference direction of Earth's magnetic field
        hx = 2.0f * mx * (0.5f - q3q3 - q4q4) + 2.0f * my * (q2q3 - q1q4) + 2.0f * mz * (q2q4 + q1q3);
        hy = 2.0f * mx * (q2q3 + q1q4) + 2.0f * my * (0.5f - q2q2 - q4q4) + 2.0f * mz * (q3q4 - q1q2);
        bx = (float ) Math.sqrt((hx * hx) + (hy * hy));
        bz = 2.0f * mx * (q2q4 - q1q3) + 2.0f * my * (q3q4 + q1q2) + 2.0f * mz * (0.5f - q2q2 - q3q3);

        // Estimated direction of gravity and magnetic field
        vx = 2.0f * (q2q4 - q1q3);
        vy = 2.0f * (q1q2 + q3q4);
        vz = q1q1 - q2q2 - q3q3 + q4q4;
        wx = 2.0f * bx * (0.5f - q3q3 - q4q4) + 2.0f * bz * (q2q4 - q1q3);
        wy = 2.0f * bx * (q2q3 - q1q4) + 2.0f * bz * (q1q2 + q3q4);
        wz = 2.0f * bx * (q1q3 + q2q4) + 2.0f * bz * (0.5f - q2q2 - q3q3);

        // Error is cross product between estimated direction and measured direction of gravity
        ex = (ay * vz - az * vy) + (my * wz - mz * wy);
        ey = (az * vx - ax * vz) + (mz * wx - mx * wz);
        ez = (ax * vy - ay * vx) + (mx * wy - my * wx);
        if (Ki > 0.0f)
        {
            eInt[0] += ex;      // accumulate integral error
            eInt[1] += ey;
            eInt[2] += ez;
        }
        else
        {
            eInt[0] = 0.0f;     // prevent integral wind up
            eInt[1] = 0.0f;
            eInt[2] = 0.0f;
        }

        // Apply feedback terms
        gx = (float)(0.07 * gx + Kp * ex + Ki * eInt[0]);
        gy = (float)(0.07 * gy + Kp * ey + Ki * eInt[1]);
        gz = (float)(0.07 * gz + Kp * ez + Ki * eInt[2]);

        // Integrate rate of change of quaternion
        pa = q2;
        pb = q3;
        pc = q4;
        q1 = q1 + (-q2 * gx - q3 * gy - q4 * gz) * (0.5f * deltat);
        q2 = pa + (q1 * gx + pb * gz - pc * gy) * (0.5f * deltat);
        q3 = pb + (q1 * gy - pa * gz + pc * gx) * (0.5f * deltat);
        q4 = pc + (q1 * gz + pa * gy - pb * gx) * (0.5f * deltat);

        // Normalise quaternion
        norm = (float)Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);
        norm = 1.0f / norm;
        q[0] = q1 * norm;
        q[1] = q2 * norm;
        q[2] = q3 * norm;
        q[3] = q4 * norm;

        return q;
    }

}
