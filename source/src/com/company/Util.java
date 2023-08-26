package com.company;

import java.util.ArrayList;

/**
 * Created by Pouya-Laptop on 1/29/2021.
 */
public class Util
{
    public static int pow(int a, int b)
    {
        if(b == 0)
            return 1;
        else return pow(a, b-1) * a;
    }

    public static void sort(double[][] data, int feature, int[][] sorted, int start, int end)
    {
        if(start >= end)
            return;

        sort(data, feature, sorted, start, (start+end)/2);
        sort(data, feature, sorted, (start+end)/2+1, end);

        int[] temp = new int[end-start+1];

        int i1 = start;
        int i2 = (start+end)/2+1;
        for(int i=0; i<end-start+1; i++)
            if(i1 <= (start+end)/2 && i2 <= end)
            {
                if(data[sorted[i1][feature]][feature] <  data[sorted[i2][feature]][feature])
                {
                    temp[i] = sorted[i1][feature];
                    i1++;
                }else
                {
                    temp[i] = sorted[i2][feature];
                    i2++;
                }
            }else if(i1 <= (start+end)/2)
            {
                temp[i] = sorted[i1][feature];
                i1++;
            }
            else if(i2 <= end)
            {
                temp[i] = sorted[i2][feature];
                i2++;
            }

        for(int i=0; i<end-start+1; i++)
            sorted[i+start][feature] = temp[i];
    }


    public static String getAssignment(String[] input)
    {
        String result = "";

        for(int i=0; i<input.length; i++)
            if(input[i].charAt(0) == '-')
                result = result + "0";
            else
                result = result + "1";

        //System.out.println(result);

        return result;
    }


    public static void compress()
    {
        long startTime = System.currentTimeMillis();

        int[] groups = new int[Main.n];

        int gc=1;

        for(int i1=0; i1<Main.n; i1++)
        {
            if(groups[i1] > 0)
                continue;
            groups[i1] = gc;
            boolean close;
            for(int i2=i1+1; i2<Main.n; i2++)
            {
                if(Main.labels[i1] != Main.labels[i2])
                    continue;
                if(groups[i2] > 0)
                    continue;
                close = true;
                for(int i3=0; i3<Main.n; i3++)
                    if(Main.labels[i3] == Main.labels[i1])
                        continue;
                    else
                        for(int j=0; j<Main.f; j++)
                        {
                            if(Main.categorical[j])
                            {
                                if(Main.data[i3][j] == Main.data[i1][j])
                                    close = false;
                                if(Main.data[i3][j] == Main.data[i2][j])
                                    close = false;
                                break;
                            }
                            else
                            {
                                if(Main.data[i3][j] >= Main.data[i1][j] && Main.data[i3][j] <= Main.data[i2][j])
                                    close = false;
                                if(Main.data[i3][j] <= Main.data[i1][j] && Main.data[i3][j] >= Main.data[i2][j])
                                    close = false;
                                break;
                            }
                        }
                if(close)
                    groups[i2] = gc;
            }
            gc++;
        }

        int new_n = gc-1;
        double[][] new_data = new double[new_n][];
        int[] new_labels = new int[new_n];

        for(int i1=0; i1<Main.n; i1++)
        {
            new_data[groups[i1]-1] = Main.data[i1];
            new_labels[groups[i1]-1] = Main.labels[i1];
        }


        System.out.println("Size compressed from " + Main.n + " to " + new_n + " in " + (System.currentTimeMillis() - startTime));

        Main.n = new_n;
        Main.data = new_data;
        Main.labels = new_labels;
    }

}

