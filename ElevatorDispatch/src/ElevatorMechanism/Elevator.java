package ElevatorMechanism;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import Requires.RequireButtons;

class InnerPanel extends JPanel {
    Elevator elevator;
    JButton[] buttonFloor;
    JButton openButton = new JButton("开");
    JButton closeButton = new JButton("关");

    InnerPanel(Elevator ele) {
        setLayout(null);
        elevator = ele;
        buttonFloor = new JButton[ElevatorInfo.totalFloor];

        for (int i = 0, j = 19; i < ElevatorInfo.totalFloor; i++, j--) {
            buttonFloor[j] = new JButton("" + (ElevatorInfo.totalFloor - i));
            buttonFloor[j].setMargin(new Insets(1, 1, 1, 1));
            buttonFloor[j].setFont(new Font(buttonFloor[j].getFont().getFontName(), buttonFloor[j].getFont().getStyle(), 9));
            buttonFloor[j].setBounds(0, 2 + i * ElevatorInfo.elevatorButtonHigh,
                    ElevatorInfo.elevatorButtonWide, ElevatorInfo.elevatorButtonHigh);
            buttonFloor[j].setBackground(Color.white);
            buttonFloor[j].addActionListener(buttonFloorListener);
            buttonFloor[j].setCursor(new Cursor(Cursor.HAND_CURSOR));
            add(buttonFloor[j]);
        }


        openButton.setBounds(0, ElevatorInfo.totalFloor * ElevatorInfo.elevatorButtonHigh,
                ElevatorInfo.elevatorButtonWide / 2, ElevatorInfo.elevatorButtonHigh / 2);
        openButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openButton.setMargin(new Insets(1, 0, 1, 0));
        openButton.setFont(new Font(openButton.getFont().getFontName(), openButton.getFont().getStyle(), 9));
        openButton.addActionListener(openFloorListener);

        openButton.setBackground(Color.white);

        closeButton.setBounds(ElevatorInfo.elevatorButtonWide / 2, ElevatorInfo.totalFloor * ElevatorInfo.elevatorButtonHigh,
                ElevatorInfo.elevatorButtonWide / 2, ElevatorInfo.elevatorButtonHigh / 2);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setMargin(new Insets(1, 0, 1, 0));
        closeButton.setFont(new Font(closeButton.getFont().getFontName(), closeButton.getFont().getStyle(), 9));
        closeButton.addActionListener(closeFloorListener);
        closeButton.setBackground(Color.white);

        add(openButton);
        add(closeButton);
    }

    ActionListener buttonFloorListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int floor = Integer.parseInt(((JButton) e.getSource()).getText());

            if (elevator.getCurrentfloor() == floor && elevator.isOpen) {
                elevator.reopen();
                return;
            }
            if (elevator.getElevatorState() == 0 && elevator.getCurrentfloor() == floor) {
                elevator.open();
                return;
            }

            //点击楼层以后的颜色
            buttonFloor[floor - 1].setBackground(Color.decode("#CFD8DC"));
            buttonFloor[floor - 1].setBorder(BorderFactory.createLineBorder(Color.decode("#B2EBF2"), 2));
            buttonFloor[floor - 1].setOpaque(true);
            if (floor == ElevatorInfo.totalFloor) {
                elevator.setArrival(2 * ElevatorInfo.totalFloor - floor);
                return;
            }
            if (floor == 1) {
                elevator.setArrival(0);
                return;
            }

            if (elevator.getCurrentfloor() < floor) {
                elevator.setArrival(floor - 1);
            } else if (elevator.getCurrentfloor() > floor) {
                elevator.setArrival(2 * ElevatorInfo.totalFloor - floor);
            } else if (elevator.getCurrentfloor() == floor) {
                if (elevator.getElevatorState() == 1 || elevator.getElevatorState() == 2)
                    elevator.setArrival(2 * ElevatorInfo.totalFloor - floor);
                else if (elevator.getElevatorState() == -1 || elevator.getElevatorState() == -2)
                    elevator.setArrival(floor - 1);
            }
        }
    };

    ActionListener closeFloorListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (!elevator.isOpen) return;
            elevator.close();
        }
    };

    ActionListener openFloorListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (elevator.isOpen) elevator.reopen();
            else if (elevator.getElevatorState() == 0) elevator.setArrival(elevator.getCurrentfloor() - 1);
        }
    };
}

class OuterPanel extends JPanel {
    JLabel[] floor;
    Elevator elevator;

    //设置了电梯的参数和外观
    OuterPanel(Elevator ele) {
        elevator = ele;
        setLayout(null);
        floor = new JLabel[ElevatorInfo.totalFloor];

        ele.elevator.setBounds(0,
                (ElevatorInfo.totalFloor - 1) * (ElevatorInfo.floorHigh + ElevatorInfo.floorSpace) + ElevatorInfo.floorSpace,
                ElevatorInfo.floorWide, ElevatorInfo.floorHigh);

        ele.elevator.setBackground(Color.decode("#00B8D4"));
        ele.elevator.setBorder(BorderFactory.createLineBorder(Color.decode("#F8F8FF"), 2));
        ele.elevator.setText("     -- " + String.valueOf(elevator.getCurrentfloor() + 1));
        ele.elevator.setOpaque(true);

        //增加了这个电梯

        add(ele.elevator);

        for (int i = 0, j = 19; i < ElevatorInfo.totalFloor; i++, j--) {
            floor[j] = new JLabel("" + (ElevatorInfo.totalFloor - i));
            floor[j].setOpaque(true);
            floor[j].setBackground(Color.black);
            floor[j].setForeground(Color.cyan);
            floor[j].setHorizontalAlignment(JLabel.CENTER);
            floor[j].setBorder(BorderFactory.createLineBorder(Color.decode("#ECEFF1"), 1));
            floor[j].setBounds(0,
                    i * (ElevatorInfo.floorHigh + ElevatorInfo.floorSpace) + ElevatorInfo.floorSpace,
                    ElevatorInfo.floorWide, ElevatorInfo.floorHigh);
            add(floor[j]);
        }

    }
}


public class Elevator extends Thread {
    JLabel elevator = new JLabel();
    private InnerPanel inside = new InnerPanel(this);
    private OuterPanel outside = new OuterPanel(this);
    private int state;


    private boolean restart;
    private int currentfloor;
    private boolean[] arrival;
    private int willArrive;
    boolean isOpen;



    public Elevator() {

        isOpen = false;
        willArrive = 1;
        state = 0;
        currentfloor = 1;
        arrival = new boolean[ElevatorInfo.totalFloor * 2];

        for (int i = 0; i < ElevatorInfo.totalFloor * 2; i++) {
            arrival[i] = false;
        }
    }

    //得到电梯状态
    public int getElevatorState() {
        return state;
    }


    public void run() {
        while (true) {
            searchTargetFloor(this);
            state = setDirection();

            if (state == 1 || state == 2) {
                slideUp();
            } else if (state == -1 || state == -2) {
                slideDown();
            } else if (state == 100) {
                System.out.println("100");
                arrival[currentfloor - 1] = RequireButtons.isPress[currentfloor - 1] = false;
                RequireButtons.pressButton[currentfloor - 1].setBackground(Color.decode("#FFD700"));
                open();
            } else if (state == -100) {
                System.out.println("-100");
                arrival[2 * ElevatorInfo.totalFloor - currentfloor] = RequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor] = false;
                RequireButtons.pressButton[2 * ElevatorInfo.totalFloor - currentfloor].setBackground(Color.decode("#FFD700"));
                open();
            }
        }
    }

    private void slideUp() {
        //走了一层楼梯
        for (int i = 0; i < ElevatorInfo.floorHigh + ElevatorInfo.floorSpace; i++) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
            }
            elevator.setLocation(elevator.getLocation().x,
                    elevator.getLocation().y - 1);
        }
        currentfloor++;
        elevator.setText("     ▲ " + String.valueOf(getCurrentfloor()));

        //电梯向上接到了要向下走的顾客
        if (willArrive == currentfloor && state == 2 && !arrival[currentfloor - 1] && !RequireButtons.isPress[currentfloor - 1]) {
            System.out.println("目标");
            arrival[2 * ElevatorInfo.totalFloor - currentfloor] = false;
            inside.buttonFloor[currentfloor - 1].setBackground(Color.white);

            RequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor] = false;
            RequireButtons.pressButton[2 * ElevatorInfo.totalFloor - currentfloor].setBackground(Color.decode("#FFD700"));
            elevator.setText("     -- " + String.valueOf(getCurrentfloor()));
            open();

        } else if (arrival[currentfloor - 1] || RequireButtons.isPress[currentfloor - 1]) {
            System.out.println("顺路停一下");
            inside.buttonFloor[currentfloor - 1].setBackground(Color.white);
            arrival[currentfloor - 1] = false;

            RequireButtons.isPress[currentfloor - 1] = false;
            RequireButtons.pressButton[currentfloor - 1].setBackground(Color.decode("#FFD700"));
            elevator.setText("     -- " + String.valueOf(getCurrentfloor()));
            open();
        }
    }

    private void slideDown() {
        for (int i = 0; i < ElevatorInfo.floorHigh + ElevatorInfo.floorSpace; i++) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
            }
            elevator.setLocation(elevator.getLocation().x,
                    elevator.getLocation().y + 1);
        }
        currentfloor--;
        elevator.setText("     ▼ " + String.valueOf(getCurrentfloor()));

        if (willArrive == currentfloor && state == -2 && !arrival[2 * ElevatorInfo.totalFloor - currentfloor] && !RequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor]) {
            arrival[currentfloor - 1] = false;
            inside.buttonFloor[currentfloor - 1].setBackground(Color.white);

            RequireButtons.isPress[currentfloor - 1] = false;
            RequireButtons.pressButton[currentfloor - 1].setBackground(Color.decode("#FFD700"));
            elevator.setText("     -- " + String.valueOf(getCurrentfloor()));
            open();
        } else if (arrival[2 * ElevatorInfo.totalFloor - currentfloor] || RequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor]) {
            arrival[2 * ElevatorInfo.totalFloor - currentfloor] = false;
            inside.buttonFloor[currentfloor - 1].setBackground(Color.white);
            RequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor] = false;
            RequireButtons.pressButton[2 * ElevatorInfo.totalFloor - currentfloor].setBackground(Color.decode("#FFD700"));
            elevator.setText("     -- " + String.valueOf(getCurrentfloor()));
            open();
        }

    }


    //设置电梯的运行方向
//1 表示向上运行 顾客要朝上的
//-1表示朝下运行,顾客要朝下的
//2表示向上运行，顾客要朝下的
//-2表示朝下运行,顾客要朝上的
//0 表示没有运行
    private int setDirection() {
        //100和-100是静止的时候的手动开关门
        if ((state == 0) && (arrival[currentfloor - 1]))
            return 100;
        if ((state == 0) && (arrival[2 * ElevatorInfo.totalFloor - currentfloor]))
            return -100;
        if (state == 0) {
            for (int i = 0; i < ElevatorInfo.totalFloor; i++) {
                if (arrival[i]) {
                    willArrive = i + 1;
                    if (willArrive > currentfloor) return 1;
                    else return -2;
                }
                if (arrival[2 * ElevatorInfo.totalFloor - i - 1]) {
                    willArrive = i + 1;
                    if (willArrive > currentfloor) return 2;
                    else return -1;
                }
            }
        }
        if (state == 1 || state == 2)    //电梯状态向上
        {
            for (int i = ElevatorInfo.totalFloor - 1; i > currentfloor - 1; i--) {
                if (arrival[i]) {
                    willArrive = Math.max(i + 1, willArrive);
                    return 1;
                }
                if (arrival[2 * ElevatorInfo.totalFloor - i - 1]) {
                    willArrive = Math.max(i + 1, willArrive);
                    return 2;
                }
            }
        }
        if (state == -1 || state == -2) {
            for (int i = 0; i < currentfloor - 1; i++) {
                if (arrival[2 * ElevatorInfo.totalFloor - i - 1]) {
                    willArrive = Math.min(i + 1, willArrive);
                    return -1;
                }
                if (arrival[i]) {
                    willArrive = Math.min(i + 1, willArrive);
                    return -2;
                }
            }
        }
        return 0;
    }

    static private synchronized void searchTargetFloor(Elevator ele)    //寻找电梯的目标楼层
    {
        //从队列中拿出任务楼层
        int isUp = 0;
        int i = RequireButtons.getIsPress();
        if(i<5 && i>0){
            System.out.println("上"+i);
        }
        else if(i>20){
            System.out.println("下"+i);
        }
        if (i == -1) return;
        int floor = i + 1;
        if (floor > ElevatorInfo.totalFloor) {
            floor = 2 * ElevatorInfo.totalFloor - floor;
            //表示目标楼层请求要下楼
            isUp = -1;
        } else if (floor < ElevatorInfo.totalFloor) {
            //表示目标楼层请求要上楼
            isUp = 1;
        }
        //floor已经被转化成了具体的楼层
        if (RequireButtons.isPress[i] && RequireButtons.findNearestElevator(ele, floor, isUp)) {
            System.out.println("add "+i);
            ele.setArrival(i);
            RequireButtons.isPress[i] = false;
            RequireButtons.taskList.removeFirst();
        }
    }

    public void setArrival(int i) {
        arrival[i] = true;
    }

    public int getCurrentfloor() {
        return currentfloor;
    }

    public void open() {
        elevator.setBackground(Color.pink);
        elevator.setOpaque(false);
        isOpen = true;

        restart = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            if (restart) open();
        }
        close();
    }

    public void close() {
        if (isOpen && state != -100 && state != 100) this.interrupt();
        elevator.setOpaque(true);
        elevator.setBackground(Color.decode("#00B8D4"));
        isOpen = false;
    }

    public void reopen() {
        restart = true;
        this.interrupt();
    }

    public void add(JFrame frame, int i) {
        frame.setLayout(null);

        inside.setBounds((i + 1) * 190, ElevatorInfo.windowMargin,
                ElevatorInfo.elevatorButtonWide * 2, ElevatorInfo.elevatorButtonHigh * ((ElevatorInfo.totalFloor >> 1) + 2));
        inside.setSize(ElevatorInfo.elevatorButtonWide, (ElevatorInfo.totalFloor + 1) * ElevatorInfo.elevatorButtonHigh);
        inside.setBackground(Color.decode("#DC143C"));

        outside.setBounds(190 * (i + 1) + ElevatorInfo.elevatorButtonWide + 2, ElevatorInfo.windowMargin,
                ElevatorInfo.floorWide,
                (ElevatorInfo.floorHigh + ElevatorInfo.floorSpace) * ElevatorInfo.totalFloor);
        frame.add(outside);
        frame.add(inside);

    }
}

