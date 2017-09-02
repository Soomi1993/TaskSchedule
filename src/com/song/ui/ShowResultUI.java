package com.song.ui;

import com.song.model.Task;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by song on 2017/6/23.
 */
public class ShowResultUI {
    private static int BUTTON_HEIGHT = 40;
    private static int DELTA_X = 100;


    private static int Y_CPU1 = 10;
    private static int Y_CPU2 = 60;
    private static int Y_DELTA = 50; //每个处理单元之间Y轴间距
    private static int Y_PIM  = 110;
    private static int MAX_X  = 1000;

    protected List<List<Task>> tasks; //0:PIM tasks,1-n CPU tasks
    protected  int processorNum;

    private JFrame frame;
    private long maxTime;

    private JTextArea textArea;

    /**
     * 由时间缩小到坐标的倍数
     */
    private static float scale;

    public ShowResultUI() {
        frame = new JFrame();
        frame.setSize(MAX_X+DELTA_X+100,600);

        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    public ShowResultUI(String title,List<List<Task>> tasks,int processorNum){
        this.tasks = tasks;
        this.processorNum = processorNum;
        frame = new JFrame(title);
        frame.setSize(MAX_X+DELTA_X+100,600);

        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void show(){
        if(scale==0) {
            maxTime = findMaxTime();
            scale = 1.2f* maxTime / ((float) MAX_X - 100); //保留100的空间
        }
       addTextArea();


        addToCoord();



        frame.setVisible(true);
    }

    /**
     * 显示每个任务的详细时间
     */
    private void addTextArea(){
        textArea = new JTextArea();
        textArea.setBounds(DELTA_X,Y_CPU1+(processorNum+2)*Y_DELTA,800,800);
        textArea.setText(genResult());
        frame.add(textArea);
    }
    private String genResult(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<tasks.size();i++){
            List<Task> taskList = tasks.get(i);

            for(Task t:taskList){
                sb.append(t.id)
                        .append("--> ").
                        append(t.startTime).
                        append("  ")
                        .append(t.endTime)
                        .append(" runtime : ")
                        .append(t.endTime-t.startTime)
                        .append("\n");


            }
        }

        return sb.toString();

    }

    /**
     * 将task添加到坐标上
     */
    private void addToCoord(){

        addLabel();

        for(int i=1;i<tasks.size();i++){

            for(Task t:tasks.get(i)){
                int startX = (int) (t.startTime/scale);
                int endX = (int) (t.endTime/scale);
                addToCPU(startX,endX,t.id+"",i);
            }
        }

        for(Task t:tasks.get(0)){
            int startX = (int) (t.startTime/scale);
            int endX = (int) (t.endTime/scale);
            addToPIM(startX,endX,t.id+"",processorNum);
        }
    }



    /**
     * 将一个label添加到坐标上的指定位置
     * @param startX
     * @param endX
     * @param name
     */
    private void addToCPU(int startX,int endX,String name,int processorId){
        addButton(startX,endX,Y_CPU1+(processorId-1)*Y_DELTA,name);
    }
   // private void addToCPU2(int startX,int endX,String name){
//        addButton(startX,endX,Y_CPU2,name);
//    }
    private void addToPIM(int startX,int endX,String name ,int processorNum){
        addButton(startX,endX,Y_CPU1+(processorNum-1)*Y_DELTA,name);
    }

    /**
     * 找到所以任务中最大的时间（以此来确定放缩比例）
     * @return
     */
    private long findMaxTime(){
        long max =-1;
        for(List<Task> list:tasks){
            for(Task task :list){
                if(max<task.endTime){
                    max=task.endTime;
                }
            }
        }

        return max;
    }


    private void addButton(int startX,int endX ,int y,String name){
        JButton button = new JButton(name);
        int width = endX-startX;
        if(width<10){
            width = 20;
        }
        button.setBounds(startX+DELTA_X,y,width, BUTTON_HEIGHT);
        button.setMargin(new Insets(0,0,0,0));

        frame.add(button);
    }

    /**
     * 添加标签，显示cpu1，cpu2，pim
     */
    private void addLabel(){

        Font font = new  Font("宋体", Font.BOLD, 16);
        //cpu
        List<JLabel> labelList = new ArrayList<>();
        for(int i=1;i<processorNum;i++){
            JLabel label = new JLabel("CPU"+i,SwingConstants.CENTER);
            label.setBounds(0,Y_CPU1+(i-1)*Y_DELTA,DELTA_X,BUTTON_HEIGHT);
            label.setFont(font);
            labelList.add(label);
        }
        JLabel label = new JLabel("PIM",SwingConstants.CENTER);
        label.setBounds(0,Y_CPU1+(processorNum-1)*Y_DELTA,DELTA_X,BUTTON_HEIGHT);
        label.setFont(font);
        labelList.add(label);



        //边框
        Border black = BorderFactory.createLineBorder(Color.black);

        for(JLabel lab :labelList){
            frame.add(lab);
        }


        //为一条上button添加一个边框,包括PIM的

        for(int i=1;i<=processorNum;i++){
            JLabel lab = new JLabel();
            lab.setBorder(black);
            int delta = 2;
            lab.setBounds(DELTA_X-delta,Y_CPU1+(i-1)*Y_DELTA-delta,MAX_X+delta,BUTTON_HEIGHT+2*delta);
            frame.add(lab);
        }


        JLabel firstLabel = new JLabel();
        JLabel secondLabel = new JLabel();
        JLabel thirdLabel = new JLabel();
        firstLabel.setBorder(black);
        secondLabel.setBorder(black);
        thirdLabel.setBorder(black);
        int delta = 2;
        firstLabel.setBounds(DELTA_X-delta,Y_CPU1-delta,MAX_X+delta,BUTTON_HEIGHT+2*delta);
        secondLabel.setBounds(DELTA_X-delta,Y_CPU2-delta,MAX_X+delta,BUTTON_HEIGHT+2*delta);
        thirdLabel.setBounds(DELTA_X-delta,Y_PIM-delta,MAX_X+delta,BUTTON_HEIGHT+2*delta);

        frame.add(firstLabel);
        frame.add(secondLabel);
        frame.add(thirdLabel);

        //显示scale的lable
//        JLabel scaleLabel = new JLabel("scale: "+scale);
//        scaleLabel.setBounds(DELTA_X,Y_PIM+50,100,BUTTON_HEIGHT);
//        frame.add(scaleLabel);

    }


    public void setTitle(String title){
        frame.setName(title);
    }


}
