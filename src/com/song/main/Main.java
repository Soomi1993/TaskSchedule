package com.song.main;


import com.song.factory.HEFTSchedule;
import com.song.factory.MySchedule;
import com.song.factory.Schedule;

import java.io.IOException;

/**
 * Created by song on 2017/4/22.
 */
public class Main {
    public static void main(String[] args) throws IOException {
//      //  String filePath = "D:/task_info/leastMulti_10000_new_test.txt";
//        String filePath = "D:/task_info/sim2.0/5task_49000.txt";
//        String filePath2 = "D:/task_info/sim2.0/5task_50000.txt";
//        String filePath3 = "D:/task_info/sim2.0/5task_49000_Copy.txt";
//        String filePath4 = "D:/task_info/sim2.0/5task_50000_Copy.txt";
//        String filePath5 = "D:/task_info/sim2.0/5task_1G_1333_assoc2.txt";
//        String filePath6 = "D:/task_info/sim2.0/5task_1G_1333_assoc2_latency20.txt";
        String filePathSpmv = "D:/task_info/sim2.0/spmv/spmv_40.txt";

//        String filePathPageRank = "D:/task_info/sim2.0/pageRank/pageRank_12.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/pageRank/pageRank_19k_20.txt";
        String filePathPageRank = "D:/task_info/sim2.0/pageRank/pageRank_19k_30.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/fft/fft_15task.txt";


//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussianElimilation_5x5.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian_5x5.txt";
//        String gaussian = "D:/task_info/sim2.0/gen/wordcount_63k_40.txt";
        String pagerank_thread = "D:/task_info/sim2.0/gen/pagerank_thread_7.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian10x10.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian15x15.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian40x40.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian30x30.txt";
//        String filePathPageRank = "D:/task_info/sim2.0/gaussian/gaussian25x25.txt";
//        String gaussian = "D:/task_info/sim2.0/gaussian/gaussian30x30.txt";
//        String kmeans = "D:/task_info/sim2.0/kmeans/kmeans.txt";
//        String wordcount = "D:/task_info/sim2.0/pbfs/pbfs.txt";

//        String pbfs = "D:/task_info/sim2.0/gen/bfs_15M_19.txt";
//        String pagerank = "D:/task_info/sim2.0/gen/pagerank_19k_40.txt";
//        String histogram = "D:/task_info/sim2.0/gen/histogram_45k_20.txt";
//        String test = "D:/task_info/sim2.0/gen/test_histogram.txt";
//
//        String radix = "D:/task_info/sim2.0/gen/radix_500k_40.txt";
//        String bfs = "D:/task_info/sim2.0/gen/bfs_43k_40.txt";
//        String gaussian = "D:/task_info/sim2.0/gen/gaussian_29k_40.txt";
//
//
//
//        String spmv = "D:/task_info/sim2.0/gen/spmv_628k_40.txt";
//        String wordcount = "D:/task_info/sim2.0/gen/wordcount_630k_40.txt";


//        String kmeans = "D:/task_info/sim2.0/0901after/kmeans_4.8M_40.txt";
        String kmeans = "D:/task_info/sim2.0/0901after/kmeans_480k_40.txt";
        String histogram = "D:/task_info/sim2.0/0901after/histogram_4M_20.txt";
        String bfs = "D:/task_info/sim2.0/0901after/bfs_5.9M_40.txt";
        String radix = "D:/task_info/sim2.0/0901after/radix_5M_40.txt";
        String pagerank = "D:/task_info/sim2.0/0901after/pagerank_5.8M_10.txt";
        String gaussian = "D:/task_info/sim2.0/0901after/gaussian_3M_40.txt";
        String spmv = "D:/task_info/sim2.0/0901after/spmv_11.5M_40.txt";

        GenTaskInfo genTaskInfo = new GenTaskInfo();
        //genTaskInfo.add(filePathPageRank);
//        genTaskInfo.add(filePathPageRank);

        genTaskInfo.add(spmv);
        genTaskInfo.add(kmeans);
        genTaskInfo.add(histogram);
        genTaskInfo.add(bfs);
        genTaskInfo.add(radix);
        genTaskInfo.add(pagerank);
        genTaskInfo.add(gaussian);

//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);
//        genTaskInfo.add(pagerank);

//        genTaskInfo.add(kmeans);
//        genTaskInfo.add(kmeans);
//        genTaskInfo.add(test);
//        genTaskInfo.add(test);
//        genTaskInfo.add(test);
//        genTaskInfo.add(test);
//        genTaskInfo.add(test);
//        genTaskInfo.add(test);





        genTaskInfo.genTaskQueue();
        genTaskInfo.printInfo();
//
        Schedule mySchedule = new MySchedule(genTaskInfo.getTaskQueue());
        mySchedule.start();

        mySchedule.showResult();
        mySchedule.print();



        Schedule heft = new HEFTSchedule(genTaskInfo.getTaskQueue());
        heft.start();
        heft.print();

        heft.showResult();

        System.out.println("============================result=============================");
        System.out.println("=====================MY=================");
        mySchedule.sim();
        System.out.println("=====================HEFT=================");
        heft.sim();


        long myCompletedTime = mySchedule.getMaxCompletedTime();
        long heftCompletedTime = heft.getMaxCompletedTime();
        System.out.println("My completed time : "+myCompletedTime);
        System.out.println("HEFT completed time : "+heftCompletedTime);

        float rate = (heftCompletedTime-myCompletedTime)/((float)myCompletedTime);
        System.out.println("速度提升百分比： "+rate);

    }
}
