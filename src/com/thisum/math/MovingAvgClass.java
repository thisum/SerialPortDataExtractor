package com.thisum.math;

import java.util.Arrays;

/**
 * Created by thisum_kankanamge on 13/9/18.
 */
public class MovingAvgClass
{
    private int elements;
    private double []ary;
    private int count;
    private double result;
    private int i = 0;

    public MovingAvgClass(int ele)
    {
        this.elements = ele;
        this.ary = new double[ele];
    }

    public float calculateAvg(float newEle)
    {
        count++;
        if(count < elements)
        {
            ary[count-1] = newEle;
            result = -1;
        }
        else if(count == elements)
        {
            ary[count-1] = newEle;
            result = Arrays.stream(ary).average().getAsDouble();
        }
        else
        {
            for(i = 1; i<elements; i++)
            {
                ary[i-1] = ary[i];
            }
            ary[elements-1] = newEle;
            result = Arrays.stream(ary).average().getAsDouble();
        }

        return (float)result;
    }

    public void reset()
    {
        count = 0;
        for( int j = 0; j<elements; j++)
        {
            ary[j] = 0.0d;
        }
    }

}
