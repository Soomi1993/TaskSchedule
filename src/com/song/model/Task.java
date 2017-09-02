package com.song.model;

import com.song.main.TaskInfo;
import com.song.parameters.PublicParams;

/**
 * Created by song on 2017/4/22.
 */
public class Task {
    public TaskInfo parent;
    public float rank;
    public int id; //从1开始
    public long cpuTime;
    public long pimTime;
    public int memCount;

    public long startTime;
    public long endTime;
    public long interferenceTime;

    public boolean isAllocated = false;
    public int processorId;
    public long averageRunTime(){
        long result = (cpuTime* PublicParams.CPU_COUNT+pimTime)/(PublicParams.CPU_COUNT+1);
        return result;
    }

    /**
     * 计算两个节点之间的通信时间
     * @param from 从0开始
     * @param to
     * @return
     */
    public int communicationTime(int from,int to){
        int dataSize = parent.relation[from][to];
        int delay = 200;
        int communication = (int) (delay + dataSize/((float)PublicParams.BANDWIDTH));
        return communication;
    }

    @Override
    public String toString() {
        return parent.name+":"+id;
    }
}
