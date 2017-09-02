package com.song.main;

import com.song.model.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by song on 2017/5/27.
 */
public class GenTaskInfo {


    private List<Queue<Task>> taskQueues = new LinkedList<>(); //面向多个应用程序，包含多个任务队列
    List<TaskInfo> taskInfoList = new ArrayList<>();  //添加进来的每个应用




    public void add(String fileName) throws IOException {
        taskInfoList.add(new TaskInfo(fileName));
    }
//    public void add(String graphFileName, String timeInfoFileName) throws IOException {
//        taskInfoList.add(new TaskInfo(graphFileName,timeInfoFileName));
//    }



    public void genTaskQueue(){
        List<Task> list = new ArrayList<>();
        for(TaskInfo item:taskInfoList){
            list.addAll(Arrays.asList(item.tasks));
            int listSize = list.size();
            Queue<Task> queue = new LinkedList<>();
            for(int i=0;i<listSize;i++){
                queue.add(popMaxRankTask(list));
            }
            taskQueues.add(queue);
        }
    }
    private Task popMaxRankTask(List<Task> list){
        float maxRank = -1;
        Task maxRankTask = null;
        for(Task item: list){
            if(maxRank<item.rank){
                maxRank = item.rank;
                maxRankTask = item;
            }
        }
        list.remove(maxRankTask);
        return maxRankTask;
    }



    public List<Queue<Task>> getTaskQueue(){

        return taskQueues;
    }

    public void printInfo(){
        for(Queue<Task> taskQueue :taskQueues) {
            for (Task item : taskQueue) {
                System.out.printf("%s---->%2d rank: %.0f\n", item.parent.name, item.id, item.rank);
                // System.out.println(item.parent.name+"----> "+item.id+"  rank:"+item.rank);
            }
        }
    }
    public void printMaxEndTime(){
        for(TaskInfo i:taskInfoList){
            i.printMaxEndTime();
        }
    }
}
