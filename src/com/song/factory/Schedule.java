package com.song.factory;

import com.song.main.TaskInfo;
import com.song.model.Task;
import com.song.parameters.PublicParams;
import com.song.ui.ShowResultUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by song on 2017/5/27.
 */
public abstract class Schedule {

    public static final int PIM = 0;


    protected List<Queue<Task>> taskQueues; //面向多个应用程序，包含多个任务队列
    protected List<Queue<Task>> taskQueuesCopy;

    protected long curTime;    //当前时间
    protected int processorNum = PublicParams.CPU_COUNT + 1;
    protected long[] time = new long[PublicParams.CPU_COUNT + 1]; //0:pim时间，1——n为cpu n的时间

    protected List<List<Task>> tasks; //0:PIM tasks,1-n CPU tasks
    protected static final int MEM_DELAY = 200;


    protected  Simulate simulate;
    public long getMaxCompletedTime(){
        return simulate.getCompletedTime();
    }

    public Schedule(List<Queue<Task>> taskQueues) {
        this.taskQueues = new ArrayList<>();
        this.taskQueuesCopy = new ArrayList<>();
        for (int i = 0; i < taskQueues.size(); i++) {
            Queue<Task> queue = new LinkedList<>();
            Queue<Task> queueCopy = new LinkedList<>();
            for (Task item : taskQueues.get(i)) {


                queue.add(item);
                queueCopy.add(item);
            }
            this.taskQueues.add(queue);
            taskQueuesCopy.add(queueCopy);
        }

        //初始化保存每个执行单元任务的list
        tasks = new ArrayList<>();
        for (int i = 0; i < PublicParams.CPU_COUNT + 1; i++) {
            tasks.add(new ArrayList<>());
        }
    }


    public void showResult() {
    }



    /**
     * 获取所有任务队列的第一个任务中具有最小EST的任务
     *
     * @return
     */
    protected Task getAndPollMinESTTask() {
        long minEST = Long.MAX_VALUE;
        Task minESTTask = null;
        int queueId = -1;
        for (int i = 0; i < taskQueues.size(); i++) {
            Queue<Task> queue = taskQueues.get(i);
            if (queue.isEmpty()) {
                continue;
            } else {
                Task task = queue.peek();
                long est = calMaxPrepTaskEndTime(task, -1); //计算EST,-1表示包含通信时间
                if (est < minEST) {
                    minEST = est;
                    minESTTask = task;
                    queueId = i;
                }

            }
        }
        taskQueues.get(queueId).poll(); //从任务队列中移除该任务
        return minESTTask;
    }

    protected boolean isQueuesNotEmpty() {
        for (int i = 0; i < taskQueues.size(); i++) {
            if (!taskQueues.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从所有处理单元的EFT中，寻找最小的EFT的处理单元id
     *
     * @param eft
     * @return
     */
    protected int findMinEFT(long[] eft) {
        long min = Long.MAX_VALUE;
        int minId = -1;
        for (int i = 0; i < eft.length; i++) {
            if (min > eft[i]) {
                min = eft[i];
                minId = i;
            }
        }
        return minId;
    }


    /**
     * 获取该节点的所有前驱节点
     *
     * @param task
     * @return
     */
    protected List<Task> getPrepNodes(Task task) {
        List<Task> prepList = new ArrayList<>();
        TaskInfo taskInfo = task.parent;
        for (int i = 0; i < taskInfo.taskSize; i++) {
            if (taskInfo.relation[i][task.id - 1] != TaskInfo.INVALID_NODE) {
                prepList.add(taskInfo.tasks[i]);
            }
        }
        return prepList;
    }

    /**
     * 获取该节点的所有后继节点
     *
     * @param task
     * @return
     */
    protected List<Task> getSuccNodes(Task task) {
        List<Task> succList = new ArrayList<>();
        TaskInfo taskInfo = task.parent;
        for (int i = 0; i < taskInfo.taskSize; i++) {
            if (taskInfo.relation[task.id - 1][i] != TaskInfo.INVALID_NODE) {
                succList.add(taskInfo.tasks[i]);
            }
        }
        return succList;
    }

    /**
     * 计算在这个时间段内总共的访存次数
     *
     * @param startTime
     * @param endTime
     * @param cpuId
     * @return
     */
    protected Double calcMemCount(long startTime, long endTime, int cpuId) {
        List<Task> taskList = tasks.get(cpuId);

        float total = 0;
        long overlapTime = 0;

        if (!taskList.isEmpty()) {
            long time = taskList.get(taskList.size() - 1).endTime;
            if (time > startTime) {
                if (time < endTime) {
                    overlapTime = time - startTime;
                } else {
                    overlapTime = endTime - startTime;
                }

            }

        }


        for (int i = 0; i < taskList.size(); i++) {
            float memcount = 0;
            Task task = taskList.get(i);
            if (task.startTime <= startTime) { // 第一种情况
                if (task.endTime > startTime && task.endTime <= endTime) {
                    memcount = (task.endTime - startTime) / ((float) (task.endTime - task.startTime)) *task.memCount;
                } else if (task.endTime > startTime && task.endTime > endTime) {
                    memcount = (endTime - startTime) / ((float) (task.endTime - task.startTime)) *task.memCount;
                }
            } else if (task.startTime > startTime && task.startTime < endTime) { //第二种情况
                if (task.endTime < endTime) {
                    memcount = task.memCount;
                } else {
                    memcount = (endTime - task.startTime) / ((float) (task.endTime - task.startTime)) *task.memCount;
                }
            }
            total += memcount;

        }
        Double aDouble = new Double();
        aDouble.memCount = total;
        aDouble.time = overlapTime;

//        System.out.println("memCount ----------> "+total);
        return aDouble;

    }

    public static class Double {
        float memCount;
        long time;
    }

    public static class InterferenceInfo {
        long time; //重叠时间
        float total; //总访存次数
        float[] count = new float[PublicParams.CPU_COUNT + 1]; // 下标从1开始
    }

    /**
     * 计算重叠的时间，以及重叠的访存次数
     *
     * @param startTime
     * @param taskTime
     * @param cpuId     这个任务放入这个CPU的id
     * @return
     */
    protected InterferenceInfo getInterferenceTasks(long startTime, long taskTime, int cpuId) {
        long endTime = startTime + taskTime;
        long time = 0;
        float memCount = 0;

        InterferenceInfo info = new InterferenceInfo();

        for (int i = 1; i < processorNum; i++) {
            if (i == cpuId) {
                info.count[i] = 0;
                continue; //跳过当前分配到的目标处理单元
            }

            List<Task> taskList = tasks.get(i);
            if (taskList.isEmpty()) {
                info.count[i] = 0;
            } else {
                Double vals = calcMemCount(startTime, endTime, i);

                info.count[i] = vals.memCount;
                info.total += vals.memCount;
                if (vals.time > info.time) {
                    info.time = vals.time;
                }

            }
        }
        return info;
        //old


//
//        List<Task> list = new ArrayList<>();
//        for (int j = 1; j < tasks.size(); j++) {
//            if (j == cpuId) {
//                continue; //跳过当前分配到的目标处理单元
//            }
//
//            List<Task> taskList = tasks.get(j);
//            for (int i = 0; i < taskList.size(); i++) {
//                Task task = taskList.get(i);
//                if (task.startTime >= startTime) {
//                    list.add(task);
//                } else if (task.startTime < startTime && task.endTime < startTime) {
//                    list.add(task);
//                }
//            }
//        }
//
//        InterferenceInfo info = new InterferenceInfo();
//        if (list.isEmpty()) {
//            return info;
//        }
//
//
//        long time;
//        float memCount = 0;
//
//
//        for (int j = 1; j < tasks.size(); j++) {
//            if (j == cpuId) {
//                continue; //跳过当前分配到的目标处理单元
//            }
//            List<Task> taskList = tasks.get(j); //ith cpu tasks queue
//
//            if (startTime + taskTime < taskList.get(taskList.size() - 1).endTime) {
//                time = taskTime;
//                Task startTask = getStartTask(startTime, taskList);
//                Task endTask = getStartTask(startTime + taskTime, taskList);
//                if (startTask == endTask) {
//                    if (startTask != null)
//                        memCount = (taskTime / ((float) (startTask.cpuTime)) * startTask.memCount);
//                } else {
//                    int startIndex = taskList.indexOf(startTask);
//                    int endIndex = taskList.indexOf(endTask);
//                    memCount = (int) ((startTask.endTime - startTime) / ((float) startTask.cpuTime) * startTask.memCount);
//                    memCount = (int) (memCount + (endTask.endTime - startTime - taskTime) / ((float) endTask.cpuTime) * endTask.memCount);
//                    while (endIndex - startIndex > 1) {
//                        memCount = memCount + taskList.get(++startIndex).memCount;
//                    }
//                }
//            } else {
//                time = taskList.get(taskList.size() - 1).endTime - startTime;
//                Task startTask = getStartTask(startTime, taskList);
//                if (taskList.indexOf(startTask) == taskList.size() - 1) {
//                    //只有一个重叠任务
//                    memCount = (time / ((float) startTask.cpuTime) * startTask.memCount);
//                } else {
//
//                    if(startTask!=null){
//                        memCount = ((startTask.endTime - startTime) / ((float) startTask.cpuTime) * startTask.memCount);
//                    }
//
//
//
//                    int i = taskList.indexOf(startTask);
//
//                    i++;
//                    while (i < taskList.size()) {
//
//                        memCount += taskList.get(i).memCount;
//                        i++;//next
//                    }
//
//
//                }
//
//            }
//
//            info.count[j] = memCount;
//            info.time = info.time>time?info.time:time;
//            info.total += memCount;
//        }

    }

    /**
     * 计算EST，包括通信时间
     *
     * @param task
     * @return
     */
    protected long calMaxPrepTaskEndTime(Task task, int toProcessorId) {
        List<Task> prepList = getPrepNodes(task);
        long max = 0;
        for (Task item : prepList) {
            int communication;
            if (item.processorId == toProcessorId) {
                communication = 0;
            } else {
                communication = task.communicationTime(item.id - 1, task.id - 1);
            }
            long startTime = item.endTime + communication;
            if (startTime > max) {
                max = startTime;
            }
        }
        return max;
    }

    /**
     * 不考虑在同一个处理单元上，任务间通信=0
     *
     * @param task
     * @return
     */
    protected long calMaxPrepTaskEndTime(Task task) {
        return calMaxPrepTaskEndTime(task, -1);
    }


    /**
     * 获取在指定时间点的任务
     *
     * @param startTime
     * @param taskList
     * @return
     */
    protected Task getStartTask(long startTime, List<Task> taskList) {
        for (Task item : taskList) {
            if (item.startTime <= startTime && item.endTime >= startTime) {
                return item;
            }
        }
        for (Task item : taskList) {
            if (item.startTime > startTime) {
                return item;
            }
        }

        return null;
    }

    protected long max(long a, long b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * 计算任务curTask分配到id为cpuId的CPU上所带来的干扰延迟时间
     *
     * @param curTask
     * @param cpuId
     * @return int[] 大小为cpu数量n+1，开始下标为1
     */
    protected int[] calDelay(Task curTask, int cpuId) {
        long estOrigin = calMaxPrepTaskEndTime(curTask); //计算EST
        for (int i = 1; i < time.length; i++) {
            if (estOrigin > time[i]) {
                time[i] = estOrigin;
            }
        }

        int[] delayTime = new int[PublicParams.CPU_COUNT + 1]; //大小为cpu数量n+1，开始下标为1

        long startTime = time[cpuId];
        InterferenceInfo info = getInterferenceTasks(startTime, curTask.cpuTime, cpuId);

        info.count[cpuId] = curTask.memCount;
        for (int i = 1; i < time.length; i++) {


            //1.计算重叠时间
            long time = info.time;

            if (time <= 0) {
                delayTime[i] = 0;
                continue;
            }
            //2.计算重叠时间内访存次数
            float totalMemCount = info.total;

            //3.计算干扰时间
//            int p = PublicParams.processor;
//            int m = PublicParams.bank;
//            int tm = PublicParams.MEM_DELAY;
////            int tm = 1;
//            float ta;
//            ta = ((float) time) / totalMemCount;

            delayTime[i] = (int) (calDLight(time,totalMemCount) * info.count[i]);

        }

        return delayTime;
    }

    private float calDLight(long time,float totalMemCount){
        int p = PublicParams.processor;
        int m = PublicParams.bank;
        int tm = PublicParams.MEM_DELAY;
//            int tm = 1;
        float ta;
        ta = ((float) time) / totalMemCount;

        float result = 0.5f * p * tm * tm / (m * ta);
        return result;
    }
//    private float calHeavy(long time,float totalMemCount){
//        int p = PublicParams.processor;
//        int m = PublicParams.bank;
//        int tm = PublicParams.MEM_DELAY;
//
//        float result = p*tm/m;
//        return result;
//    }


    public abstract void start();

    public void sim() {
        /*
        在子类中实现
         */
    }


    public void print() {
        System.out.println("=======================任务分配结果=======================");
        for (int i = 1; i < processorNum; i++) {
            System.out.print("在CPU" + i + "上：");
            for (Task j : tasks.get(i)) {
                System.out.print(j.toString() + ",");
            }
            System.out.print("\t 完成时间：" + time[i]);
            System.out.println();
        }
        System.out.print("在PIM上 ：");
        for (Task i : tasks.get(PIM)) {
            System.out.print(i.toString() + ",");
        }
        System.out.print("\t 完成时间：" + time[PIM]);
        System.out.println();


    }

    protected void printInfo(String name){
        System.out.println("==============================================="+name+"====================================================");
        LinkedList<Task> list = (LinkedList<Task>) taskQueuesCopy.get(0);
        for(int i=0;i<list.size();i++){
            Task task = list.get(i);
            System.out.println(task.id+" ---> "+task.startTime+" "+task.endTime +" " +(task.endTime-task.startTime));
        }
    }
}
