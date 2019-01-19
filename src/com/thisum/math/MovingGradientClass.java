package com.thisum.math;

public class MovingGradientClass
{
    private int elements;
    private double []ary;
    private int count=0;
    private double result;
    private int i = 0;

    public MovingGradientClass(int ele)
    {
        this.elements = ele;
        this.ary = new double[ele];
    }

    public double calculateGrad(float newEle)
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
            result = (ary[count-1] - ary[0])/this.elements;
        }
        else
        {
            i = (count%elements);

            if(i==0)
            {
                ary[elements-1] = newEle;
                result = (ary[elements-1] - ary[0])/this.elements;
            }
            else
            {
                ary[i-1] = newEle;
                result = (ary[i-1] - ary[i])/this.elements;
            }
        }

        return result;
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
