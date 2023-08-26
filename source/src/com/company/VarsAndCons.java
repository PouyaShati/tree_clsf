package com.company;

import java.util.ArrayList;

/**
 * Created by Pouya-Laptop on 1/29/2021.
 */
public class VarsAndCons
{

    public static int getVarA(int j, int t)
    {
        int result;

        result = t*Main.f + j + 1;


        if(Main.var_cnt < result)
            Main.var_cnt = result;

        return result;
    }

    public static int getVarS(int i, int t)
    {
        int result;


        result = Main.tree.branchCount * Main.f + t*Main.n + i + 1;

        if(Main.var_cnt < result)
            Main.var_cnt = result;

        return result;
    }

    public static int getVarZ(int i, int t)
    {
        int result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + (t-Main.tree.branchCount) * Main.n + i + 1;

        if(Main.var_cnt < result)
            Main.var_cnt = result;

        return result;
    }



    public static int getVarG(int t, int g)
    {
        int result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + (t-Main.tree.branchCount) * Main.label_cnt + g + 1;

        if(Main.var_cnt < result)
            Main.var_cnt = result;

        return result;
    }

    public static int getVarP(int i)
    {
        int result = Main.tree.branchCount * Main.f + Main.tree.branchCount*Main.n + Main.tree.leafCount * Main.n + Main.tree.leafCount * Main.label_cnt + i + 1;

        if(Main.var_cnt < result)
            Main.var_cnt = result;

        return result;
    }


    public static void addConA()
    {
        addConA(Main.tree);
    }

    public static void addConA(Node node)
    {
        if(node.left != null)
        {
            int[] temp = new int[Main.f];
            for (int j = 0; j < temp.length; j++)
                temp[j] = getVarA(j, node.id);
            Main.hardClauses.add(temp);

            for (int j1 = 0; j1 < temp.length; j1++)
                for (int j2 = j1 + 1; j2 < temp.length; j2++)
                    Main.hardClauses.add(new int[]{-getVarA(j1, node.id), -getVarA(j2, node.id)});

            addConA(node.left);
            addConA(node.right);
        }
    }


    public static void addConSplit()
    {
        ArrayList<Integer> seenNodes = new ArrayList<>();
        addConSplit(Main.tree, seenNodes);
    }

    public static void addConSplit(Node node, ArrayList<Integer> seenNodes)
    {
        if(node.left != null)
        {
                for(int j=0; j<Main.f; j++)
                {
                    if(Main.mustSplit)
                    {
                        Main.hardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[0][j], node.id)});
                        if(!Main.categorical[j])
                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[Main.n-1][j], node.id)});
                    }

                    int pointer = 0;

                    while(pointer < Main.n-1)
                    {
                        if(Main.data[Main.sorted[pointer][j]][j] == Main.data[Main.sorted[pointer+1][j]][j])
                        {
                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[pointer][j], node.id), getVarS(Main.sorted[pointer + 1][j], node.id)});
                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});
                        }else
                        {
                            if(!Main.categorical[j])
                                Main.hardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});

                            if(Main.pruneAbsent)
                            {
                                Node currentNode = Main.tree;
                                for(int h=0; h<seenNodes.size(); h++)
                                {
                                    if(seenNodes.get(h) == 1)
                                    {
                                        if(pointer == 0 || Main.data[Main.sorted[pointer-1][j]][j] != Main.data[Main.sorted[pointer][j]][j])
                                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[pointer][j], currentNode.id), -getVarS(Main.sorted[pointer][j], node.id), getVarS(Main.sorted[pointer + 1][j], node.id)});

                                        if(Main.categorical[j] && (pointer == Main.n-2 || Main.data[Main.sorted[pointer+1][j]][j] != Main.data[Main.sorted[pointer+2][j]][j]))
                                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), -getVarS(Main.sorted[pointer+1][j], currentNode.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});

                                        currentNode = currentNode.right;
                                    }
                                    else
                                    {
                                        if(pointer == 0 || Main.data[Main.sorted[pointer][j]][j] != Main.data[Main.sorted[pointer-1][j]][j])
                                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer][j], currentNode.id), -getVarS(Main.sorted[pointer][j], node.id), getVarS(Main.sorted[pointer + 1][j], node.id)});

                                        if(Main.categorical[j] && (pointer == Main.n-2 || Main.data[Main.sorted[pointer+1][j]][j] != Main.data[Main.sorted[pointer+2][j]][j]))
                                            Main.hardClauses.add(new int[]{-getVarA(j, node.id), getVarS(Main.sorted[pointer+1][j], currentNode.id), getVarS(Main.sorted[pointer][j], node.id), -getVarS(Main.sorted[pointer + 1][j], node.id)});

                                        currentNode = currentNode.left;
                                    }
                                }
                            }

                        }

                        pointer++;
                    }
                }




            ArrayList<Integer> seenNodesLeft = new ArrayList<>();
            ArrayList<Integer> seenNodesRight = new ArrayList<>();

            for(int i=0; i<seenNodes.size(); i++)
            {
                seenNodesLeft.add(seenNodes.get(i));
                seenNodesRight.add(seenNodes.get(i));
            }

            seenNodesLeft.add(0);
            seenNodesRight.add(1);


            addConSplit(node.left, seenNodesLeft);
            addConSplit(node.right, seenNodesRight);
        }
    }


    public static void addConZ()
    {
        ArrayList<Integer> seenNodes = new ArrayList<>();
        addConZ(Main.tree, seenNodes);
    }


    public static void addConZ(Node node, ArrayList<Integer> seenNodes)
    {
        if(node.left != null)
        {
            ArrayList<Integer> seenNodesLeft = new ArrayList<>();
            ArrayList<Integer> seenNodesRight = new ArrayList<>();

            for(int i=0; i<seenNodes.size(); i++)
            {
                seenNodesLeft.add(seenNodes.get(i));
                seenNodesRight.add(seenNodes.get(i));
            }

            seenNodesLeft.add(0);
            seenNodesRight.add(1);

            addConZ(node.left, seenNodesLeft);
            addConZ(node.right, seenNodesRight);
        }else
        {
            for(int i=0; i<Main.n; i++)
            {
                ArrayList<Integer> temp1 = new ArrayList<>();
                temp1.add(getVarZ(i, node.id));

                Node tempNode = Main.tree;
                for(int k=0; k<seenNodes.size(); k++)
                {
                    if(seenNodes.get(k)  == 0)
                    {
                        temp1.add(-getVarS(i, tempNode.id));
                        Main.hardClauses.add(new int[]{-getVarZ(i, node.id), getVarS(i, tempNode.id)});

                        tempNode = tempNode.left;
                    }else
                    {
                        temp1.add(getVarS(i, tempNode.id));
                        Main.hardClauses.add(new int[]{-getVarZ(i, node.id), -getVarS(i, tempNode.id)});

                        tempNode = tempNode.right;
                    }

                }

                int[] temp1array = new int[temp1.size()];
                for(int c=0; c<temp1array.length; c++)
                    temp1array[c] = temp1.get(c);


                if(Main.directAbsence)
                    Main.hardClauses.add(temp1array);
                else
                {
                    int[] temp3 = new int[Main.tree.leafCount];
                    for(int l1=Main.tree.branchCount; l1<Main.tree.branchCount + Main.tree.leafCount; l1++)
                        temp3[l1 - Main.tree.branchCount] = getVarZ(i, l1);
                    Main.hardClauses.add(temp3);

                    for(int l1=Main.tree.branchCount; l1<Main.tree.branchCount + Main.tree.leafCount; l1++)
                        for(int l2=l1+1; l2<Main.tree.branchCount + Main.tree.leafCount; l2++)
                            Main.hardClauses.add(new int[]{-getVarZ(i, l1), -getVarZ(i, l2)});
                }



            }
        }
    }


    public static void addConC()
    {
        for(int i1=0; i1<Main.n; i1++)
            for(int i2=i1+1; i2<Main.n; i2++)
            {
                double distance = 0;

                for(int j=0; j<Main.f; j++)
                    distance += (Main.data[i1][j] - Main.data[i2][j]) * (Main.data[i1][j] - Main.data[i2][j]);

                distance = Math.sqrt(distance);

                distance -= Main.dis_offset;

                if(Math.abs(distance) <= Main.dis_ignore)
                    continue;

                if(distance < 0)
                {
                    int new_c = Main.var_cnt+1;
                    Main.var_cnt ++;

                    for(int t=0; t < Main.tree.leafCount; t++)
                    {
                        Main.hardClauses.add(new int[]{-new_c, getVarZ(i1, t+Main.tree.branchCount), -getVarZ(i2, t+Main.tree.branchCount)});
                        Main.hardClauses.add(new int[]{-new_c, -getVarZ(i1, t+Main.tree.branchCount), getVarZ(i2, t+Main.tree.branchCount)});
                    }

                    Main.softClauses.add(new int[]{Math.round((float) -distance), new_c});
                    Main.weights_sum += Math.round((float) -distance);
                }else
                {
                    int new_c = Main.var_cnt+1;
                    Main.var_cnt ++;

                    for(int t=0; t < Main.tree.leafCount; t++)
                        Main.hardClauses.add(new int[]{new_c, -getVarZ(i1, t+Main.tree.branchCount), -getVarZ(i2, t+Main.tree.branchCount)});

                    Main.softClauses.add(new int[]{Math.round((float) distance), -new_c});
                    Main.weights_sum += Math.round((float) distance);
                }
            }
    }

    public static void addConGP()
    {
        addConGP(Main.tree);

        if(!Main.hardClassify)
            for(int i=0; i<Main.n; i++)
            {
                Main.softClauses.add(new int[]{1, getVarP(i)});
                Main.weights_sum += 1;
            }
    }

    public static void addConGP(Node node)
    {
        if(node.left != null)
        {
            addConGP(node.left);
            addConGP(node.right);
        }else
        {
            for (int g1 = 0; g1 < Main.label_cnt; g1++)
                for (int g2 = g1+1; g2 < Main.label_cnt; g2++)
                    Main.hardClauses.add(new int[]{-getVarG(node.id, g1), -getVarG(node.id, g2)});


            for(int i=0; i<Main.n; i++)
                if(Main.hardClassify)
                    Main.hardClauses.add(new int[]{getVarG(node.id, Main.labels[i]-1), -getVarZ(i, node.id)});
                else
                    Main.hardClauses.add(new int[]{getVarG(node.id, Main.labels[i]-1), -getVarZ(i, node.id), -getVarP(i)});

        }
    }

}
