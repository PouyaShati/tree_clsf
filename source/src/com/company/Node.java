package com.company;

import java.util.ArrayList;

/**
 * Created by Pouya-Laptop on 1/29/2021.
 */
public class Node
{
    Node left;
    Node right;

    int leafCount;
    int branchCount;

    int nodeCount;

    int id;

    int feature;
    boolean categorical;
    double threshold;
    ArrayList<Double> values;



    int label;


    public Node(Node left, Node right, int id)
    {
        this.id = id;

        this.left = left;
        this.right = right;


        leafCount = left.leafCount + right.leafCount;
        branchCount = left.branchCount + right.branchCount + 1;

        nodeCount = leafCount + branchCount;
    }

    public Node(int id)
    {
        this.id = id;

        leafCount = 1;
        branchCount = 0;

        nodeCount = leafCount + branchCount;
    }

    public static Node balancedTree(int height, int id)
    {
        if(height == 0)
            return new Node(id);

        return new Node(Node.balancedTree(height-1, id*2+1), Node.balancedTree(height-1, id*2+2), id);
    }

    public int predict(double[] point)
    {
        if(left == null)
            return label;

        if(categorical)
        {
            if(values.indexOf(point[feature])>=0)
                return left.predict(point);
            else
                return right.predict(point);
        }else
        {
            if(point[feature] < threshold)
                return left.predict(point);
            else
                return right.predict(point);
        }
    }

    public void print()
    {
        if(left != null)
        {
            System.out.println("" + id + ": " + feature);
            if(categorical)
            {
                System.out.print("{");
                for(int i=0; i<values.size(); i++)
                {
                    System.out.print(values.get(i));
                    if(i < values.size()-1)
                        System.out.print(", ");
                    else
                        System.out.println("}");
                }
            }else
                System.out.println(threshold);

            //left.print();
            //right.print();
        }else
            System.out.println("" + id + "-> " + label);
    }
}

