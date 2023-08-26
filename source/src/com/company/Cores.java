package com.company;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Pouya-Laptop on 2/7/2021.
 */
public class Cores
{
    public static void addOptClassifyCores()
    {
        Scanner scanner = new Scanner(System.in);

        int n = Main.n;
        int p = Main.f;
        int length = Main.tree.leafCount+1;
        //int length = 2;



        double data[][] = Main.data;
        int label[] = Main.labels;


        boolean[][] smaller = new boolean[n][];

        for(int i1=0; i1<n; i1++)
        {
            smaller[i1] = new boolean[n];
            for(int i2=0; i2<n; i2++)
            {
                boolean is_smaller = true;
                for(int j=0; j<p; j++)
                    if(data[i1][j] > data[i2][j])
                        is_smaller = false;
                smaller[i1][i2] = is_smaller && (label[i1] != label[i2]);
            }
        }

        int[][] paths = new int[n][];

        for(int i=0; i<n; i++)
        {
            paths[i] = new int[length];
            paths[i][0] = 1;
        }

        for(int l=1; l<length; l++)
        {
            for(int i1=0; i1<n; i1++)
            {
                paths[i1][l] = 0;
                for(int i2=0; i2<n; i2++)
                    if(smaller[i1][i2])
                        paths[i1][l] += paths[i2][l-1];
            }
        }

        int sum = 0;

        for(int i=0; i<n; i++)
            sum += paths[i][length-1];

        System.out.println("Number of all paths is: " + sum);

        System.out.println("Core max count:");
        Main.coreCnt = scanner.nextInt();

        System.out.println("Consider weight?");
        boolean conWeight = scanner.nextBoolean();



        ArrayList<int[]> chosenPaths = new ArrayList<>();

        Random random = new Random(System.currentTimeMillis());

        OUTER: for(int cc=0; cc<Main.coreCnt; cc++)
        {
            ArrayList<Integer> added = new ArrayList<>();

            int[] temp = new int[length];

            ArrayList<Integer> choices = new ArrayList<>();
            for(int i=0; i<n; i++)
            {
                if(conWeight)
                    for(int j=0; j<paths[i][length-1]; j++)
                        choices.add(i);
                else if(paths[i][length-1] > 0)
                    choices.add(i);
            }
            if(choices.size() == 0)
                break;

            temp[0] = choices.get(random.nextInt(choices.size()));
            added.add(temp[0]);

            for(int len=length-2; len >= 0; len--)
            {
                choices = new ArrayList<>();
                for(int i=0; i<n; i++)
                    if(added.indexOf(i) < 0 && smaller[temp[length - 2 - len]][i]) // is the first part redundant?
                    {
                        if(conWeight)
                            for(int j=0; j<paths[i][len]; j++)
                                choices.add(i);
                        else if(paths[i][len] > 0)
                            choices.add(i);
                    }
                if(choices.size() == 0)
                    continue OUTER;
                temp[length-1-len] = choices.get(random.nextInt(choices.size()));
            }

            chosenPaths.add(temp);
        }

        System.out.println("Number of added cores: " + chosenPaths.size());

        for(int i=0; i<chosenPaths.size(); i++)
        {
            int[] temp = new int[length];
            for(int k=0; k<length; k++)
                temp[k] = -VarsAndCons.getVarP(chosenPaths.get(i)[k]);

            Main.hardClauses.add(temp);
        }
    }



    public static void addHardClassifyCores()
    {
        Scanner scanner = new Scanner(System.in);

        int n = Main.n;
        int p = Main.f;

        double data[][] = Main.data;
        int label[] = Main.labels;
        int length = Main.tree.leafCount+1;




        boolean[][] smaller = new boolean[n][];

        for(int i1=0; i1<n; i1++)
        {
            smaller[i1] = new boolean[n];
            for(int i2=0; i2<n; i2++)
            {
                boolean is_smaller = true;
                for(int j=0; j<p; j++)
                    if(data[i1][j] > data[i2][j])
                        is_smaller = false;
                smaller[i1][i2] = is_smaller && (label[i1] != label[i2]);
            }
        }

        int[][] ascendPaths = new int[n][];

        for(int i=0; i<n; i++)
        {
            ascendPaths[i] = new int[length];
            ascendPaths[i][0] = 1;
        }

        for(int l=1; l<length; l++)
        {
            for(int i1=0; i1<n; i1++)
            {
                ascendPaths[i1][l] = 0;
                for(int i2=0; i2<n; i2++)
                    if(smaller[i1][i2])
                        ascendPaths[i1][l] += ascendPaths[i2][l-1];
            }
        }


        int[][] descendPaths = new int[n][];

        for(int i=0; i<n; i++)
        {
            descendPaths[i] = new int[length];
            descendPaths[i][0] = 1;
        }

        for(int l=1; l<length; l++)
        {
            for(int i1=0; i1<n; i1++)
            {
                descendPaths[i1][l] = 0;
                for(int i2=0; i2<n; i2++)
                    if(smaller[i2][i1])
                        descendPaths[i1][l] += descendPaths[i2][l-1];
            }
        }


        int sum=0;
        OUTER: for(int i=0; i<n; i++)
        {
            int l;
            for (l = length - 1; l >= 0; l--)
            {
                if (ascendPaths[i][l] > 0)
                    break;
            }

            for (int k = 1; k <= l; k++)
            {
                Main.hardClauses.add(new int[]{-VarsAndCons.getVarZ(i, Main.tree.branchCount + Main.tree.leafCount - k)});
                sum++;
            }
        }
        System.out.println("cores of method 1: " + sum);


        sum=0;
        OUTER: for(int i=0; i<n; i++)
        {
            int l;
            for (l = length - 1; l >= 0; l--)
            {
                if (descendPaths[i][l] > 0)
                    break;
            }

            for (int k = 1; k <= l; k++)
            {
                Main.hardClauses.add(new int[]{-VarsAndCons.getVarZ(i, Main.tree.branchCount - 1 + k)});
                sum++;
            }
        }
        System.out.println("cores of method 2: " + sum);

    }
}
