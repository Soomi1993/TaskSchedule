package com.song.factory;

import com.song.model.Task;
import com.song.ui.ShowResultUI;

import java.util.List;
import java.util.Queue;

/**
 * Created by song on 2017/5/27.
 */
public class Simulate extends Schedule{
    public Simulate(List<Queue<Task>> taskQueues) {
        super(taskQueues);
    }


    protected String title;

    private List<List<Task>> scheduledTask;

    public Simulate(List<Queue<Task>> taskQueues,List<List<Task>> task ,String title){
        super(taskQueues);
        this.scheduledTask = task;
        this.title = title;
    }

    private int getExeUnit(Task task){
        for(int i=0;i<scheduledTask.size();i++){
            if(scheduledTask.get(i).contains(task)){
                return i;
            }
        }
        throw new RuntimeException("查找任务的处理单元出错");
    }


    @Override
    public void start() {
        Task curTask;
        while (isQueuesNotEmpty()){
            curTask = getAndPollMinESTTask();
            curTask.startTime = 0;
            curTask.endTime = 0;

            int exeUnit = getExeUnit(curTask);
            if (exeUnit == PIM) {
                long estPIM = calMaxPrepTaskEndTime(curTask,PIM); //计算EST
                if(time[PIM] <estPIM){
                    time[PIM] = estPIM;
                }
                time[PIM] = sync(curTask,time[PIM]);

                //更新任务时间
                curTask.startTime = time[PIM];
                time[PIM] +=curTask.pimTime;

                curTask.endTime = time[PIM];
                curTask.isAllocated  =true;
                curTask.processorId = PIM;
                tasks.get(PIM).add(curTask);

            }else { //在CPU上
                System.out.println("  CPU "+exeUnit);

                long est = calMaxPrepTaskEndTime(curTask,PIM); //计算EST
                if(time[exeUnit] <est){
                    time[exeUnit] = est;
                }
                time[exeUnit] = sync(curTask,time[exeUnit]);
                int delay[] = calDelay(curTask,exeUnit);


                curTask.startTime = time[exeUnit];

                time[exeUnit] = time[exeUnit]+curTask.cpuTime +delay[exeUnit]; //更新当前分配的处理单元的时间
                curTask.endTime = time[exeUnit];
                tasks.get(exeUnit).add(curTask);
                //更新其它处理单元的时间（干扰延迟产生的）,当前处理单元延迟时间以及在前面加过
//                for(int i=1;i<processorNum;i++){
//                    if(i==exeUnit){
//                        continue;
//                    }
//                    if(delay[i]>0){
//                        List<Task> list = tasks.get(exeUnit);
//                        list.get(list.size()-1).endTime += delay[i];
//                    }
//                }
            }

            System.out.println(curTask.toString()+" start: "+curTask.startTime+" end："+curTask.endTime);
        }
        showResult();

        printInfo("Simulation");

        calcCompletedTime();
    }
    private long sync(Task curtask,long exeTime){

        List<Task> prepList = getPrepNodes(curtask);
        long maxEndTime = 0;
        for(Task item:prepList){
            if(maxEndTime<item.endTime){
                maxEndTime = item.endTime;
            }
        }
        if(maxEndTime<exeTime){
            return exeTime;
        }else {
         //   System.out.println("==========同步=========");
            return maxEndTime;
        }

    }

    private long completedTime;

    /**
     * 计算所有任务的完成时间
     */
    private void calcCompletedTime(){
        completedTime = 0;
        for(int i=0;i<processorNum;i++){
            if(completedTime<time[i]){
                completedTime = time[i];
            }
        }
    }

    public long getCompletedTime() {
        return completedTime;
    }

    @Override
    public void showResult(){
        ShowResultUI showResultUI = new ShowResultUI(title,tasks,processorNum);
        showResultUI.show();
    }
}
