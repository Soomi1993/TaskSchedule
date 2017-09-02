package com.song.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by song on 2017/7/4.
 */
public class GenGaussianTaskGraph {

    public static void main(String[] args) {

        // matrix: n x m
        int n = 30;
        int m = n;
        String filePath = "D:/task_info/sim2.0/gaussian/gaussian"+n+"x"+n+".txt";


        /**
         * 这个3个数组大小为n-1
         */
        int baseCpuTime = (int) (5668/5f*n);
        int basePimTime = (int) (5211/5f*n);
        int baseMemCount = (int) (21/5f*n);

        int [] cpuTime = new int[n];
        int [] pimTime =  new int[n];
        int [] memCount =  new int[n];
        for(int i=0;i<n;i++){
            cpuTime[i] = (int) (baseCpuTime/((float)n)*(n-i));
            pimTime[i] = (int) (basePimTime/((float)n)*(n-i));
            memCount[i] = (int) (baseMemCount/((float)n)*(n-i));
        }

        Task[] tasks = new Task[n-1];
        for(int i=0;i<n-1;i++){
            Task task = new Task();
            task.cpuTime = cpuTime[i];
            task.pimTime = pimTime[i];
            task.memCount = memCount[i];
            tasks[i] = task;
        }


        writeToFile(n,filePath,tasks);


    }

    static int process = 2;
    static int memory_delay = 200;

    /**
     * 方阵 n x n
     */
    private static void writeToFile(int n,String fileName,Task[] tasks) {
        int nodeSize = (n + 2) * (n - 1) / 2;
        int edgeSize = n * (n - 1) - 1;

        File outFile = new File(fileName);

//        FileOutputStream fout = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(outFile);

            //1、打印首部信息
            fw.write(String.format("%s\n",nodeSize));
            fw.write(String.format("%s\n",edgeSize));
            fw.write(String.format("%s\n",process));
            fw.write(String.format("%s\n",memory_delay));
            //2、打印任务图结构，任务编号从1开始
            int num = 1;
            for(int i=n;i>1;i--){
                for(int j = 1;j<i;j++ ){
                    writeGraph(fw,num,num+j);
                }
                num++;
                int step = i-1;
                if(step<2){//结束
                    break;
                }
                for(int j=1;j<i;j++){
                    int start = num;
                    int end = num+ step;
                    writeGraph(fw,start,end);
                    num ++;
                }

            }

            //3、打印每个节点信息

            int nodeNum = 1;
            int arrIndex = 0;
            for(int i=n;i>=2;i--){

                for(int j=0;j<i;j++){
                    writeGraphNodeInfo(fw,nodeNum,tasks[arrIndex].cpuTime,
                            tasks[arrIndex].pimTime,tasks[arrIndex].memCount);
                    nodeNum++;
                }
                arrIndex++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }






        if(fw!=null){
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 输出任务图xinx
     * @param fw
     * @param start
     * @param end
     * @throws IOException
     */
    private static void writeGraph(FileWriter fw,int start,int end) throws IOException {
        int edgeValue = 1;
        fw.write(String.format("%d\t%d\t%d\n",start,end,edgeValue));
    }
    private static void writeGraphNodeInfo(FileWriter fw,int node,int cpuTime,int pimTime, int memCount) throws IOException {
        fw.write(String.format("%d\t%d\t%d\t%d\n",node,cpuTime,pimTime,memCount));
    }
    static class Task {
        int cpuTime;
        int pimTime;
        int memCount;
    }
}
