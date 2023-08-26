package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static int repetition;
    public static int fold_cnt;

    public static ArrayList<double[]>[] folds;
    public static ArrayList<Integer>[] folds_labels;

    public static double smallest_value = 0.0001;

    public static int var_cnt=0;

    public static int orgn;
    public static int n;
    public static int f;
    public static ArrayList<double[]> orgdata;
    public static double[][] data;
    public static boolean[] categorical;
    public static boolean anyCategorical;
    public static ArrayList<Integer> orglabels;
    public static int[] labels;
    public static int label_cnt;
    public static int[][] sorted;
    public static int[][] sortedInverse;
    public static Node tree;
    public static int height;


    public static boolean classify;
    public static boolean hardClassify;

    public static double dis_offset;
    public static double dis_ignore;


    public static ArrayList<ArrayList<Double>> seenValues;

    public static boolean directAbsence;

    public static boolean mustSplit;
    public static boolean addCores;
    public static boolean toCompress;
    public static boolean pruneAbsent;

    public static int coreCnt;

    public static ArrayList<int[]> hardClauses;
    public static ArrayList<int[]> softClauses;

    public static int weights_sum = 1;

    public static Random random;

    public static void main(String[] args) throws Exception
    {
        Scanner scanner = new Scanner(System.in);
        random = new Random(System.currentTimeMillis());


        int arg_p = 0;

        repetition = 1;
        fold_cnt = 1;
        String postfix = "";
        height = 1;
        boolean printTree = false;

        while(arg_p < args.length-1)
        {
            if(args[arg_p].equals("-post"))
                postfix = args[arg_p+1];
            else if(args[arg_p].equals("-d"))
                height = Integer.parseInt(args[arg_p+1]);
            else if(args[arg_p].equals("-print"))
                printTree = Boolean.parseBoolean(args[arg_p+1]);
            arg_p += 2;
        }


        //System.out.println("Enter name:");
        String name = args[args.length-1];

        //System.out.println("Classify? (or cluster)");
        //classify = scanner.nextBoolean();
        classify = true;

        //System.out.println("Repetition?");
        //repetition = scanner.nextInt();

        //System.out.println("Fold?");
        //fold_cnt = scanner.nextInt();


        //System.out.println("Compress?");
        //toCompress = scanner.nextBoolean();
        toCompress = false; //need to fix compression

        //System.out.println("Prune Absent?");
        //pruneAbsent = scanner.nextBoolean();
        pruneAbsent = false;



        /*if(classify)
        {
            System.out.println("Hard classify?");
            hardClassify = scanner.nextBoolean();
        }*/
        hardClassify = false;

        //System.out.println("Encode z's directly at leaves?");
        //directAbsence = scanner.nextBoolean();
        directAbsence = true;

        //System.out.println("Must split?");
        //mustSplit = scanner.nextBoolean();
        mustSplit = true;

        //System.out.println("Add cores?");
        //addCores = scanner.nextBoolean();
        addCores = false;

        //System.out.println("Enter Postfix:");
        //String postfix = scanner.next();

        //System.out.println("Enter height: ");
        //height = scanner.nextInt();

        if(!classify)
        {
            System.out.println("Enter distance offset:");
            dis_offset = scanner.nextDouble();

            System.out.println("Enter distance ignore:");
            dis_ignore = scanner.nextDouble();

        }


        long startTime = System.currentTimeMillis();


        tree = Node.balancedTree(height, 0);

        readInstance(name);


        double sumAccuracy = 0.0;
        double sumTrainAccuracy = 0.0;

        int solutionCost = -1;

        for(int r=0; r<repetition; r++)
        {
            //System.out.println("------------------------------------------------------\nRepetition: " + r);

            ArrayList<double[]> orgdata_next = new ArrayList<>();
            ArrayList<Integer> orglabels_next = new ArrayList<>();


            folds = new ArrayList[fold_cnt];
            folds_labels = new ArrayList[fold_cnt];

            for(int f=0; f<fold_cnt; f++)
            {
                folds[f] = new ArrayList<>();
                folds_labels[f] = new ArrayList<>();
                int fold_size = (f<fold_cnt-1?orgn/fold_cnt:orgn-(orgn/fold_cnt)*fold_cnt+orgn/fold_cnt);
                for(int m=0; m<fold_size; m++)
                {
                    int chosen = random.nextInt(orgdata.size());
                    double[] temp_data = orgdata.remove(chosen);
                    orgdata_next.add(temp_data);
                    folds[f].add(temp_data);
                    int temp_label = orglabels.remove(chosen);
                    orglabels_next.add(temp_label);
                    folds_labels[f].add(temp_label);
                }

            }


            for(int phase=0; phase<fold_cnt; phase++)
            {
                //System.out.println("------------------\nFold: " + phase);

                if(fold_cnt > 1)
                {
                    data = new double[orgn-folds[phase].size()][];
                    labels = new int[orgn-folds[phase].size()];
                }else
                {
                    data = new double[orgn][];
                    labels = new int[orgn];
                }


                int cnt = 0;

                for(int f=0; f<fold_cnt; f++)
                {
                    if(f == phase && fold_cnt > 1)
                        continue;
                    for(int i=0; i<folds[f].size(); i++)
                    {
                        data[cnt] = folds[f].get(i);
                        labels[cnt] = folds_labels[f].get(i);
                        cnt++;
                    }
                }
                n = cnt;


                if(toCompress)
                    Util.compress();



                long phaseStartTime = System.currentTimeMillis();

                sorted = new int[data.length][];
                for(int i=0; i<n; i++)
                {
                    sorted[i] = new int[data[i].length];
                    for(int j=0; j<sorted[i].length; j++)
                        sorted[i][j] = i;
                }

                for(int j=0; j<f; j++)
                    Util.sort(data, j, sorted, 0, n-1);

                sortedInverse = new int[data.length][];
                for(int i=0; i<n; i++)
                    sortedInverse[i] = new int[data[i].length];

                for(int j=0; j<f; j++)
                    for(int i=0; i<n; i++)
                        sortedInverse[sorted[i][j]][j] = i;



                hardClauses = new ArrayList<>();
                softClauses = new ArrayList<>();

                VarsAndCons.addConA();
                VarsAndCons.addConSplit();
                VarsAndCons.addConZ();
                if(classify)
                {
                    VarsAndCons.addConGP();
                    if(addCores)
                    {
                        if(hardClassify)
                            Cores.addHardClassifyCores();
                        else
                            Cores.addOptClassifyCores();
                    }
                }
                else
                    VarsAndCons.addConC();

                String clauseName = "clauses/clauses_" + name + postfix + "_r" + r + "_f" + phase;
                //String resultName = "result_" + name + postfix + "_r" + r + "_f" + phase;
                String solutionName = "solutions/solution_" + name + postfix + "_r" + r + "_f" + phase;
                String logName = "logs/log_" + name + postfix + "_r" + r + "_f" + phase;

                writeClauses(clauseName);



                ProcessBuilder processBuilder = new ProcessBuilder();
                //processBuilder.command("bash", "-c", "timeout 30m /u/fbacchus/maxhs/maxhs -printBstSoln -printSoln -no-printOptions "+ clauseName + " > " + resultName);
                //processBuilder.command("bash", "-c", "timeout 30m /u/fbacchus/maxhs/maxhs -printBstSoln -printSoln -no-printOptions "+ clauseName);
                processBuilder.command("bash", "-c", "timeout 15m ~/SAT_Project/loandra-master/loandra_static -pmreslin-cglim=30 -weight-strategy=1 -print-model "+ clauseName);


                Process process = processBuilder.start();

                //StringBuilder output = new StringBuilder();
                String output = "";

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                FileWriter fileWriter = new FileWriter(logName);
                PrintWriter printWriter = new PrintWriter(fileWriter);


                String line;
                while ((line = reader.readLine()) != null) {
                    //output.append(line + "\n");
                    //System.out.println(line.charAt(0));
                    printWriter.write(line + "\n");
                    if(line.charAt(0) == 'v')
                        //output = line.substring(2);
                        output = Util.getAssignment(line.substring(2).split(" "));
                    if(line.charAt(0) == 'o')
                        solutionCost = Integer.parseInt(line.split(" ")[1]);
                }
                printWriter.close();

                int exitVal = process.waitFor();
                if (exitVal == 0) {
                    System.out.println("Solved!");
                    //System.out.println(output);
                    //System.exit(0);
                } /*else {
                    //abnormal...
                }*/



                writeSolution(solutionName, output, true);

                //double trainingAccuracy = getAccuracy(tree);
                //if(!anyCategorical)
                //    System.out.println("Training Accuracy: " + trainingAccuracy);
                //sumTrainAccuracy += trainingAccuracy;

                if(printTree)
                {
                    System.out.println("Tree:");
                    System.out.println("Depth: " + height);
                    ArrayList<Node> queue = new ArrayList<>();
                    queue.add(tree);

                    while(queue.size() > 0)
                    {
                        Node toPrint = queue.remove(0);
                        toPrint.print();
                        if(toPrint.left != null)
                        {
                            queue.add(toPrint.left);
                            queue.add(toPrint.right);
                        }
                    }
                }

                if(fold_cnt > 1)
                {
                    data = new double[folds[phase].size()][];
                    labels = new int[folds[phase].size()];

                    cnt = 0;
                    for(int i=0; i<folds[phase].size(); i++)
                    {
                        data[cnt] = folds[phase].get(i);
                        labels[cnt] = folds_labels[phase].get(i);
                        cnt++;
                    }
                    n = cnt;

                    double testingAccuracy = getAccuracy(tree);
                    System.out.println("Testing Accuracy: " + testingAccuracy);
                    sumAccuracy += testingAccuracy;
                }


                //System.out.println("Phase time: " + (System.currentTimeMillis() - phaseStartTime));
            }

            orgdata = orgdata_next;
            orglabels = orglabels_next;
        }

        //if(!anyCategorical)
        //    System.out.println("Average Training Accuracy: " + (sumTrainAccuracy/repetition/fold_cnt));
        if(fold_cnt == 1)
            System.out.println("Best solution cost: " + solutionCost);
        if(fold_cnt > 1)
            System.out.println("Average Testing Accuracy: " + (sumAccuracy/repetition/fold_cnt));
        //System.out.println("Average Time: " + ((System.currentTimeMillis() - startTime)/repetition/fold_cnt));
        System.out.println("Total Time: " + (System.currentTimeMillis() - startTime));

    }



    public static void writeClauses(String name) throws Exception//, boolean warmStart) throws Exception
    {
        FileWriter fileWriter = new FileWriter(name);
        PrintWriter printWriter = new PrintWriter(fileWriter);


        printWriter.write("c Height: " + height + "\n");
        printWriter.write("c classify: " + classify + "\n");
        printWriter.write("c hard classify: " + hardClassify + "\n");
        printWriter.write("c direct absence: " + directAbsence + "\n");
        printWriter.write("c must split: " + mustSplit + "\n");
        printWriter.write("c add cores: " + addCores + "\n");
        printWriter.write("c core cnt: " + coreCnt + "\n");

        if(!classify)
        {
            printWriter.write("c dis_offset: " + dis_offset + "\n");
            printWriter.write("c dis_ignore: " + dis_ignore + "\n");
        }



        //printWriter.write("c clauses produced for the cardinality constraint\n");
        printWriter.write("p wcnf " + var_cnt + " " + (hardClauses.size() + softClauses.size()) + " " + weights_sum + "\n");


        int lenSum = 0;

        for(int i=0; i<softClauses.size(); i++)
        {
            String to_write = "" + softClauses.get(i)[0];
            for(int j=1; j<softClauses.get(i).length; j++)
                to_write = to_write + " " + softClauses.get(i)[j];
            to_write = to_write + " 0\n";
            printWriter.write(to_write);
            lenSum += softClauses.get(i).length;
        }

        for(int i=0; i<hardClauses.size(); i++)
        {
            String to_write = "" + weights_sum;
            for(int j=0; j<hardClauses.get(i).length; j++)
                to_write = to_write + " " + hardClauses.get(i)[j];
            to_write = to_write + " 0\n";
            printWriter.write(to_write);
            lenSum += hardClauses.get(i).length;
        }


        System.out.println("Number of variables: " + var_cnt);
        System.out.println("Number of clauses: " + (hardClauses.size() + softClauses.size()));
        System.out.println("Clause average length: " + ((double) lenSum) / (hardClauses.size() + softClauses.size()));

        printWriter.close();
    }

    public static void readInstance(String name) throws Exception
    {
        File instance = new File("instance_" + name);
        Scanner scanner = new Scanner(instance);


        orgn = scanner.nextInt();
        f = scanner.nextInt();

        boolean labelsFirst = false;
        label_cnt = scanner.nextInt();

        if(label_cnt < 0)
        {
            label_cnt = -label_cnt;
            labelsFirst = true;
        }

        anyCategorical = false;

        if(f<0)
        {
            f = -f;
            categorical = new boolean[f];
            for(int j=0; j<f; j++)
                if(scanner.nextInt() == 1)
                {
                    categorical[j] = true;
                    anyCategorical = true;
                }
                else
                    categorical[j] = false;
        }else
        {
            categorical = new boolean[f];
        }





        /*seenValues = new ArrayList<>();

        for(int i=0; i<f; i++)
            seenValues.add(new ArrayList<>());*/


        orgdata = new ArrayList<>();

        if(label_cnt > 0)
            orglabels = new ArrayList<>();


        for(int i=0; i<orgn; i++)
        {
            if(label_cnt > 0)
                if(labelsFirst)
                    orglabels.add(scanner.nextInt());

            double[] new_entry = new double[f];
            for(int j=0; j<f; j++)
            {
                new_entry[j] = scanner.nextDouble();
                //if(seenValues.get(j).indexOf(data[i][j]) == -1)
                //    seenValues.get(j).add(data[i][j]);
            }

            orgdata.add(new_entry);

            if(label_cnt > 0)
                if(!labelsFirst)
                    orglabels.add(scanner.nextInt());

        }



    }



    public static void writeSolution(String name, String assignment, boolean writeFile) throws Exception
    {
        FileWriter fileWriter;
        PrintWriter printWriter = null;

        if(writeFile)
        {
            fileWriter = new FileWriter(name);
            printWriter = new PrintWriter(fileWriter);
        }


        int[] predicted = new int[n];

        int pointer = 0;

        if(writeFile)
            printWriter.write("a:\n");

        ArrayList<Node> toValueList = new ArrayList<>();
        toValueList.add(tree);

        for(int t=0; t<tree.branchCount; t++)
        {
            Node toValue = toValueList.remove(0);

            if(toValue.left != null)
            {
                toValueList.add(toValue.left);
                toValueList.add(toValue.right);
            }

                for(int j=0; j<f; j++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));

                    if(assignment.charAt(pointer) == '1')
                    {
                        toValue.feature = j;
                        if(anyCategorical && categorical[j])
                        {
                            toValue.categorical = true;
                            toValue.values = new ArrayList<>();
                        }
                        else
                            toValue.categorical = false;
                    }

                    pointer++;
                }
            if(writeFile)
                printWriter.write("\n");
        }
        if(writeFile)
            printWriter.write("\n");

        toValueList = new ArrayList<>();
        toValueList.add(tree);

        if(writeFile)
            printWriter.write("s:\n");
        for(int t=0; t<tree.branchCount; t++)
        {
            Node toValue = toValueList.remove(0);
            //if(toValue.left != null)
            //{
                toValueList.add(toValue.left);
                toValueList.add(toValue.right);
            //}

            int split=0;

            for(int i=0; i<n; i++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));

                    if(assignment.charAt(pointer) == '1')
                        if(toValue.categorical)
                        {
                            if(toValue.values.indexOf(data[i][toValue.feature]) < 0)
                                toValue.values.add(data[i][toValue.feature]);
                        }
                        else
                            split++;

                    pointer++;
                }

            if(writeFile)
                printWriter.write("\n");

            if(!toValue.categorical)
            {
                if(split > 0 && split < n)
                    //toValue.threshold = (data[sorted[split][toValue.feature]][toValue.feature] + data[sorted[split-1][toValue.feature]][toValue.feature])/2;
                    toValue.threshold = data[sorted[split-1][toValue.feature]][toValue.feature];
                else if(split == 0)
                    //toValue.threshold = data[sorted[split][toValue.feature]][toValue.feature] - smallest_value;
                    System.out.println("There's an error in reading the assignment");
                else if(split == n)
                    //toValue.threshold = data[sorted[split-1][toValue.feature]][toValue.feature] + smallest_value;
                    System.out.println("There's an error in reading the assignment");
            }


        }
        if(writeFile)
            printWriter.write("\n");


        if(writeFile)
            printWriter.write("z:\n");
        for(int t=0; t<tree.leafCount; t++)
        {
            for(int i=0; i<n; i++)
            {
                if(writeFile)
                    printWriter.write(" " + assignment.charAt(pointer));
                if(assignment.charAt(pointer) == '1')
                    predicted[i] = t;
                pointer++;
            }
            if(writeFile)
                printWriter.write("\n");
        }
        if(writeFile)
            printWriter.write("\n");


        if(classify)
        {
            if(writeFile)
                printWriter.write("g:\n");
            for(int t=0; t<tree.leafCount; t++)
            {
                int chosen = 1;

                for(int g=0; g<label_cnt; g++)
                {
                    if(assignment.charAt(pointer) == '1')
                        chosen = g+1;
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));
                    pointer++;
                }

                Node toValue = toValueList.remove(0);
                toValue.label = chosen;

                if(writeFile)
                    printWriter.write("\n");
            }
            if(writeFile)
                printWriter.write("\n");


            if(!hardClassify)
            {
                if(writeFile)
                    printWriter.write("p:\n");
                for(int i=0; i<n; i++)
                {
                    if(writeFile)
                        printWriter.write(" " + assignment.charAt(pointer));
                    pointer++;
                }
                if(writeFile)
                    printWriter.write("\n");
            }


        }else
        {
            if(writeFile)
                printWriter.write("Predicted: \n");
            for(int i=0; i<n; i++)
            {
                if(writeFile)
                {
                    printWriter.write("" + predicted[i]);
                    printWriter.write("\n");
                }

            }
        }

        if(writeFile)
            printWriter.close();
    }


    public static double getAccuracy(Node tree) throws Exception
    {
        /*File instance = new File("instance_" + test_name);
        Scanner scanner = new Scanner(instance);

        n = scanner.nextInt();
        f = scanner.nextInt();
        label_cnt = scanner.nextInt();

        data = new double[n][];

        if(label_cnt > 0)
            labels = new int[n];

        for(int i=0; i<n; i++)
        {
            data[i] = new double[f];
            for(int j=0; j<f; j++)
                data[i][j] = scanner.nextDouble();
            if(label_cnt > 0)
                labels[i] = scanner.nextInt();
        }*/

        double correct = 0.0;
        double incorrect = 0.0;

        for(int i=0; i<n; i++)
        {
            if(tree.predict(data[i]) == labels[i])
                correct += 1.0;
            else
                incorrect += 1.0;
        }

        return correct/(correct+incorrect);

    }
}
