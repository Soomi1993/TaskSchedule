package com.song.factory;

import com.song.model.Task;
import com.song.ui.ShowResultUI;

import java.util.List;
import java.util.Queue;

/**
 * Created by song on 2017/5/27.
 */
public class HEFTSchedule extends Schedule {


    public HEFTSchedule(List<Queue<Task>> taskQueues) {
        super(taskQueues);
    }

    /**
     * 确定所有前驱节点是否都已经执行完毕
     *
     * @param task
     * @return
     */
    private boolean isPrepTaskComplete(Task task) {
        List<Task> prepList = getPrepNodes(task);
        for (Task item : prepList) {
            if (item.endTime > curTime) {
                return false;
            }
        }
        return true;
    }




    @Override
    public void start() {
        Task curTask;
        while (isQueuesNotEmpty()){
            curTask = getAndPollMinESTTask();


            //1.估计分配到每个处理单元的EFT
            long []eft = new long[processorNum];  //计算EFT

            //分配到PIM
            long estPIM = calMaxPrepTaskEndTime(curTask,PIM); //计算EST
            estPIM = max(estPIM,time[PIM]);
            eft[PIM] = estPIM +curTask.pimTime;

            //分配到CPU
            for(int i=1;i<processorNum;i++){
//                int delay[] = calDelay(curTask,i);
                long est = calMaxPrepTaskEndTime(curTask,i); //计算EST
                est = max(est,time[i]);
                eft[i] = est+curTask.cpuTime ;
            }



            System.out.print(curTask.toString());
            System.out.print("\t");
            System.out.print("调度过程：");
            for(int i=1;i<processorNum;i++){
                System.out.print("EFTcpu "+i+": "+eft[i]);
            }
            System.out.print("EFTpim: "+eft[PIM]);
            System.out.print("\t");

            int minEFTId = findMinEFT(eft);

            if(minEFTId == PIM){
                //1、分配到PIM
                System.out.println("  PIM");
                long est = calMaxPrepTaskEndTime(curTask,PIM); //计算EST

                if(time[PIM] <est){
                    time[PIM] = est;
                }
                //更新任务时间
                curTask.startTime = time[PIM];
                time[PIM] +=curTask.pimTime;
                curTask.endTime = time[PIM];
                curTask.isAllocated  =true;
                curTask.processorId = PIM;
                tasks.get(PIM).add(curTask);
            }else {
                //2、分配到CPU

                System.out.println("  CPU "+minEFTId);

                long est = calMaxPrepTaskEndTime(curTask,minEFTId); //计算EST
                if(time[minEFTId] <est){
                    time[minEFTId] = est;
                }

                curTask.startTime = time[minEFTId];

                time[minEFTId] = time[minEFTId]+curTask.cpuTime; //更新当前分配的处理单元的时间
                curTask.endTime = time[minEFTId];
                tasks.get(minEFTId).add(curTask);

                curTask.isAllocated  =true;
                curTask.processorId = minEFTId;
            }
        }
    }

    @Override
    public void showResult() {
        ShowResultUI showResultUI = new ShowResultUI("HEFTSchedule ",tasks,processorNum);


        showResultUI.show();
    }

    public void sim(){
        simulate = new Simulate(taskQueuesCopy,tasks,"HEFT");
        simulate.start();
        simulate.print();
    }
    @Override

    public long getMaxCompletedTime(){
        return simulate.getCompletedTime();
    }

}
