package com.song.main;


import com.song.model.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by song on 2017/4/22.
 */
public class TaskInfo {
    public static final int NO_EDGE = -1;
    public static final int INVALID_NODE = -1;
    public static int count = 1;
    public String name = "t" + (count++);

    public int taskSize;
    public int edgeSize;
    public int[][] relation;

    int cpuCount;
    int memDelay;

    private float[] ranks;

    public Task[] tasks;    //需要用到的结果
    public Queue<Integer> taskQueue; //生成的任务队列


    //private ReadTgff tgff;

    /**
     * 从自定义文件创建TaskInfo
     *
     * @param fileName
     * @throws IOException
     */
    public TaskInfo(String fileName) throws IOException {
        init(fileName);
    }

//    /**
//     * 从TGFF中创建TaskInfo
//     *
//     * @param graphFileName
//     * @param timeInfoFileName
//     */
//    public TaskInfo(String graphFileName, String timeInfoFileName) throws FileNotFoundException {
//        ReadTgff readTgff = new ReadTgff(graphFileName, timeInfoFileName);
//        readTgff.execute();
//        this.tgff = readTgff;
//        initByTGFF();
//
//        taskQueue = taskPriority();
//
//        //test
//        testGraphPrint();
//        printEveryTaskInfo();
//    }
//
//    private void initByTGFF() {
//        this.taskSize = tgff.taskSize;
//        this.edgeSize = tgff.edgeList.size();
//        this.cpuCount = 2;
//        this.memDelay = 200;
//
//        this.relation = tgff.relation;
//        tasks = new Task[taskSize];
//
//        int index, cpuTime, pimTime, memCount;
//        for (int k = 0; k < taskSize; k++) {
//            index = k + 1;
//
//            /**
//             * 添加的节点，忽略运行时间
//             */
//            if (tgff.isGenExit && k == taskSize - 1) {
//                Task tasks = new Task();
//                tasks.parent = this;
//                tasks.id = index;
//                tasks.cpuTime = 1;
//                tasks.pimTime = 1;
//                tasks.memCount = 0;
//
//                tasks[index - 1] = tasks;
//
//                break;
//            }
//
//
//            ReadTgff.TimeInfo timeInfo = tgff.getRandomTimeInfo();
//            cpuTime = timeInfo.cpuTime;
//            pimTime = timeInfo.pimTime;
//            memCount = timeInfo.memCount;
//
//
//            Task tasks = new Task();
//            tasks.parent = this;
//            tasks.id = index;
//            tasks.cpuTime = cpuTime;
//            tasks.pimTime = pimTime;
//            tasks.memCount = memCount;
//
//            tasks[index - 1] = tasks;
//        }
//
//
//    }

    private void init(String fileName) throws IOException {

        readFile(fileName);
        taskQueue = taskPriority();
    }

    /**
     * read params from file
     *
     * @param fileName
     * @throws IOException
     */
    private void readFile(String fileName) throws IOException {
        FileInputStream fin = new FileInputStream(new File(fileName));
        Scanner scanner = new Scanner(fin);
        this.taskSize = scanner.nextInt();
        this.edgeSize = scanner.nextInt();
        this.cpuCount = scanner.nextInt();
        this.memDelay = scanner.nextInt();

        relation = new int[taskSize][taskSize];
        tasks = new Task[taskSize];

        for (int i = 0; i < taskSize; i++) {
            for (int j = 0; j < taskSize; j++) {
                relation[i][j] = NO_EDGE;
            }
        }

        int i, j, c;
        for (int k = 0; k < edgeSize; k++) {
            i = scanner.nextInt();
            j = scanner.nextInt();
            c = scanner.nextInt();
            relation[i - 1][j - 1] = c;
        }

        int index, cpuTime, pimTime, memCount;
        for (int k = 0; k < taskSize; k++) {
            index = scanner.nextInt();
            cpuTime = scanner.nextInt();
            pimTime = scanner.nextInt();
            memCount = scanner.nextInt();


            Task task = new Task();
            task.parent = this;
            task.id = index;
            task.cpuTime = cpuTime;
            task.pimTime = pimTime;
            task.memCount = memCount;

            tasks[index - 1] = task;
        }
        fin.close();


    }

    /**
     * 对任务排序，生成任务队列
     */
    private Queue<Integer> taskPriority() {
        //1.找到出口节点,只有一个出口节点
        int exitNode = INVALID_NODE;
        for (int i = 0; i < taskSize; i++) {
            int j = 0;
            for (; j < taskSize; j++) {
                if (relation[i][j] != NO_EDGE) {
                    break;
                }
            }
            if (j == taskSize) {
                exitNode = i;
                break;
            }
        }
        System.out.println("出口节点为：" + (exitNode + 1));

        ranks = new float[taskSize];

        //2.生成任务队列

        for (int i = 0; i < ranks.length; i++) {
            ranks[i] = INVALID_NODE;
        }
        // Queue<TaskItem> taskQueue = new LinkedList<>();

        TaskItem taskItem = new TaskItem();
        taskItem.id = exitNode;
        taskItem.rank = tasks[exitNode].averageRunTime();
        // taskQueue.add(taskItem);

        ranks[taskItem.id] = taskItem.rank;//记录rank值
        tasks[taskItem.id].rank = taskItem.rank;

        int curTask;
        Queue<Integer> rankQueue = new LinkedList<>();

        //1.寻找出口节点的前驱节点
        curTask = exitNode;
        // prepList.clear();
        for (int i = 0; i < taskSize; i++) {
            if (relation[i][curTask] != INVALID_NODE) {
                rankQueue.add(i);
            }
        }
        //2.计算前驱节点中可以计算的rank,并将计算rank的节点的前驱节点放入到队列中
        while (!rankQueue.isEmpty()) {
            int task = rankQueue.poll();
            //1.判断能不能计算rank
            //找出所有的后继节点
            List<Integer> succList = new ArrayList<>();
            for (int i = 0; i < taskSize; i++) {
                if (relation[task][i] != INVALID_NODE) {
                    succList.add(i);
                }
            }
            //判断所有的后继节点的rank值是否已经计算出来
            boolean isReady = true;
            for (int succTask : succList) {
                if (ranks[succTask] == INVALID_NODE) {
                    isReady = false;
                    break;
                }
            }
            if (isReady) {
                //可以计算rank
                float maxRank = maxRank(ranks, succList);
                ranks[task] = maxRank + tasks[task].averageRunTime();
                tasks[task].rank = ranks[task];
                //寻找前驱节点,将前驱节点添加到队列中
                for (int i = 0; i < taskSize; i++) {
                    if (relation[i][task] != INVALID_NODE) {
                        rankQueue.add(i);
                    }
                }

            } else {
                //该节点不可计算，重新添加到队列中
                rankQueue.add(task);
            }

        }
        System.out.print("任务的rank值依次为：");
        for (float i : ranks) {
            System.out.printf("%.1f\t", i);
        }
        System.out.println();

        Queue<Integer> taskQueue = genTaskQueue(ranks);
        System.out.print("排序后的任务依次为：");
        for (int i : taskQueue) {
            System.out.print(1 + i + ",  ");
        }
        System.out.println();
        System.out.print("排序后的任务依次为：");
        for (int i : taskQueue) {
            System.out.print(i + ",  ");
        }
        System.out.println();
        return taskQueue;

    }

    private float maxRank(float[] ranks, List<Integer> list) {
        float max = -10000;
        for (int i : list) {
            if (max < ranks[i]) {
                max = ranks[i];
            }
        }
        return max;
    }

    /**
     * 根据rank值生成任务队列
     *
     * @param ranks
     * @return
     */
    private Queue<Integer> genTaskQueue(float[] ranks) {
        Queue<Integer> queue = new LinkedList<>();
//        List<Float> list = new LinkedList<>();
//        for (float i : ranks) {
//            list.add(i);
//        }
//        float oldMax = Float.MAX_VALUE;
//        for (int i = 0; i < ranks.length; i++) {
//            int maxIndex;
//
//            maxIndex = maxIndex(ranks, oldMax);
//            oldMax = ranks[maxIndex];
//            queue.add(maxIndex);
//        }

        List<Integer> calculatedNode = new ArrayList<>();

        for(int i=0;i<ranks.length;i++){
            int maxIndex;

            maxIndex = maxIndex(ranks,calculatedNode);

            calculatedNode.add(maxIndex);
            queue.add(maxIndex);
        }
        return queue;
    }
    private int maxIndex(float[] ranks,List<Integer> calculatedNode){
        int maxIndex = -1;
        for(int i = 0;i<ranks.length;i++){
            //除去已经放到队列中的节点
            if(!calculatedNode.contains(i)){
                if(maxIndex == -1){
                    maxIndex = i;
                }else {
                    if(ranks[maxIndex]<ranks[i]){
                        maxIndex = i;
                    }
                }
            }
        }
        return maxIndex;
    }

//    private int maxIndex(float[] ranks, float max) {
//        int maxIndex = -1;
//        for (int i = 0; i < ranks.length; i++) {
//            if (maxIndex == -1 && ranks[i] <= max) {
//                maxIndex = i;
//            } else {
//                if (ranks[i] < max && ranks[maxIndex] < ranks[i]) {
//                    maxIndex = i;
//                }
//            }
//        }
//        return maxIndex;
//    }

    class TaskItem {
        int id;
        float rank;
    }

    public void printMaxEndTime() {
        long maxEndTime = 0;
        for (Task task : tasks) {
            if (maxEndTime > task.endTime) {
                maxEndTime = task.endTime;
            }
        }
        System.out.println(name + " maxEndTime is : " + maxEndTime);
    }

    /**
     * 打印每个节点的所有前驱节点,测试读取文件生成的任务图是否正确
     */
    public void testGraphPrint() {
        List<Task> succList = new ArrayList<>();
        for (int j = 1; j <= taskSize; j++) {
            succList.clear();
            int id = j;
            for (int i = 0; i < this.taskSize; i++) {
                if (this.relation[id - 1][i] != TaskInfo.INVALID_NODE) {
                    succList.add(tasks[i]);
                }
            }
            System.out.print("节点" + (id - 1) + "的后继节点为：");
            for (Task t : succList) {
                System.out.print((t.id - 1) + ", ");
            }
            System.out.println();
        }

    }

    /**
     * 打印每个任务的各项参数
     */
    public void printEveryTaskInfo() {
        for (Task t : tasks) {
            System.out.println((t.id - 1) + " : cpu " + t.cpuTime + " , pim " + t.pimTime + " memCount " + t.memCount);
        }
    }
}
