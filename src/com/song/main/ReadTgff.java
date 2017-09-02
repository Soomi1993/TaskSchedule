//package com.song.main;
//
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
///**
// * Created by song on 2017/6/15.
// */
//public class ReadTgff {
//    public static final String NODE = "node";
//    public static final String EDGE = "edge";
//    public static final String END = "end";
//
//    public static final int NO_EDGE = -1;
//    public static final int INVALID_NODE = -1;
//
//    List<String> nodeList = new ArrayList<>();
//    List<String> edgeList = new ArrayList<>();
//    List<TimeInfo> timeInfoList = new ArrayList<>();
//
//    int [][] relation;
//    int taskSize;
//
//    String graphFileName;
//    String timeInfoFileName;
//
//    //出口节点数量
//    int exitNodeNum = 1;
//    int exitNode;
//    boolean isGenExit = false;
//
//
//    public ReadTgff(String graphFileName, String timeInfoFileName) {
//        this.graphFileName = graphFileName;
//        this.timeInfoFileName = timeInfoFileName;
//    }
//
//    public void execute() throws FileNotFoundException {
//        readGraphFile(graphFileName);
//        readTimeInfoFile(timeInfoFileName);
//        genRelation();
//        pre();
//
//        System.out.println("处理后的出口节点为："+exitNode+" \t"+findExitNodes().get(0));
//
//    }
//
//    Random random = new Random(23);
//
//    /**
//     * 随机取一个时间TimeInfo
//     */
//    public TimeInfo getRandomTimeInfo(){
//        int rand = random.nextInt();
//        rand = Math.abs(rand);
//        System.out.print("随机数为："+rand);
//        if(timeInfoList.size()==1){
//            return timeInfoList.get(0);
//        }else {
//            rand = rand%timeInfoList.size();
//            System.out.println("\t 选择为："+rand);
//            return timeInfoList.get(rand);
//        }
//    }
//
//    /**
//     * 读取TGFF文件
//     * @param fileName
//     * @throws FileNotFoundException
//     */
//
//    public void readGraphFile(String fileName) throws FileNotFoundException {
//
//
//        try (FileReader fileReader = new FileReader(fileName);
//             BufferedReader reader = new BufferedReader(fileReader)){
//
//            boolean flag = true;
//            while (flag) {
//                String line = reader.readLine().trim();
//                if(line.startsWith(NODE)){
//                    nodeList.add(line);
//                }else if(line.startsWith(EDGE)){
//                    edgeList.add(line);
//                }else if(line.startsWith(END)){
//                    flag=false;
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("node size = "+nodeList.size());
//        System.out.println("edge size = "+edgeList.size());
//
//
//
//    }
//
//    /**
//     * 生成任务图（数组表示）
//     */
//    private void genRelation(){
//        taskSize = nodeList.size();
//        relation = new int[taskSize][taskSize];
//
//        //初始化
//        for (int i = 0; i < taskSize; i++) {
//            for (int j = 0; j < taskSize; j++) {
//                relation[i][j] = NO_EDGE;
//            }
//        }
//
//
//
//        for(String edge:edgeList){
//            String[] strings = edge.split("\"");
//
//            String startStr = strings[1];
//            String endStr = strings[3];
//
//            System.out.println(startStr+"\t"+endStr);
//            String split = "_";
//
//            int start,end;
//            start = Integer.parseInt(startStr.split(split)[1]);
//            end = Integer.parseInt(endStr.split(split)[1]);
//            System.out.println(start+"\t\t"+end);
//
//
//            relation[start][end] = 1;
//
//        }
//
//    }
//    public void readTimeInfoFile(String fileName){
//        try (FileReader fileReader = new FileReader(fileName);
//             BufferedReader reader = new BufferedReader(fileReader)){
//
//            while (true) {
//                String line = reader.readLine().trim();
//                if(line.startsWith(END)){
//                    break;
//                }
//                String[] strings = line.split("\\s");
//
//                int cpuTime = Integer.parseInt(strings[1]);
//                int pimTime = Integer.parseInt(strings[2]);
//                int memCount = Integer.parseInt(strings[3]);
//
//                TimeInfo timeInfo = new TimeInfo();
//                timeInfo.cpuTime = cpuTime;
//                timeInfo.pimTime = pimTime;
//                timeInfo.memCount = memCount;
//
//                timeInfoList.add(timeInfo);
//
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("读取Time info 文件：完毕");
//        System.out.println("配置数量为："+timeInfoList.size());
//    }
//
//    private List<Integer> findExitNodes(){
//        List<Integer> exitNodeList = new ArrayList<>();
//
//        int exitNode = INVALID_NODE;
//        for (int i = 0; i < taskSize; i++) {
//            int j = 0;
//            for (; j < taskSize; j++) {
//                if (relation[i][j] != NO_EDGE) {
//                    break;
//                }
//            }
//            if (j == taskSize) {
//                exitNode = i;
//                exitNodeList.add(exitNode);
//            }
//        }
//        System.out.print("出口节点依次为：");
//        for(int i:exitNodeList){
//            System.out.print(i+"\t");
//        }
//        return exitNodeList;
//    }
//
//
//    /**
//     * 对任务图进行处理，使其只有一个出口节点
//     */
//    private void pre(){
//        //1.找到出口节点
//        List<Integer> exitNodeList = findExitNodes();
//
//        if(exitNodeList.size()>1){
//            //将出口节点归一
//            isGenExit = true;
//
//            int [][] newRelation = new int[taskSize+1][taskSize+1];
//            //初始化
//            for (int i = 0; i < taskSize+1; i++) {
//                for (int j = 0; j < taskSize+1; j++) {
//                    newRelation[i][j] = NO_EDGE;
//                }
//            }
//
//            exitNode = taskSize;
//
//            //复制
//            for(int i=0;i<taskSize;i++){
//                for(int j=0;j<taskSize;j++){
//                    newRelation[i][j] = relation[i][j];
//                }
//            }
//            //添加
//            for(int i: exitNodeList){
//                newRelation[i][exitNode] = 1;
//            }
//
//            taskSize++;//添加节点后，节点数量加一
//            this.relation = newRelation;
//
//        }else {
//            exitNode = exitNodeList.get(0);
//        }
//
//
//    }
//
//
//    public static class TimeInfo{
//        int cpuTime;
//        int memCount;
//        int pimTime;
//    }
//
////    public static void main(String[] args) throws FileNotFoundException {
////        String graph = "D:\\tgff_file\\tgff1.vcg";
////        String timeInfo = "D:\\tgff_file\\timeInfo.txt";
////
////        ReadTgff readTgff = new ReadTgff(graph,timeInfo);
////        readTgff.execute();
////
////    }
//}
