package Requires;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;


import javax.swing.*;

import ElevatorMechanism.Elevator;
import ElevatorMechanism.ElevatorInfo;

public class RequireButtons extends JPanel {
    //用于保存一个电梯下一次任务的关联表
    public static LinkedList<Integer> taskList = new LinkedList<Integer>();
    //储存每一个楼层的按键
    public static JButton[] pressButton = new JButton[ElevatorInfo.totalFloor * 2];
    //储存每一个楼层是否被点中
    public static boolean[] isPress = new boolean[ElevatorInfo.totalFloor * 2];

    public RequireButtons() {
        for (int i = 0; i < ElevatorInfo.totalFloor * 2; i++)
            isPress[i] = false;

        setLayout(null);
        //给按钮加上上下标记
        for (int i = 0; i < ElevatorInfo.totalFloor; i++) {
            pressButton[i] = new JButton((i + 1) + "△");
        }
        for (int i = ElevatorInfo.totalFloor; i < 2 * ElevatorInfo.totalFloor; i++) {
            pressButton[i] = new JButton(2 * ElevatorInfo.totalFloor - i + "▽");
        }

        for (int i = 0; i < ElevatorInfo.totalFloor * 2 - 1; i++) {
            if (i == ElevatorInfo.totalFloor - 1) continue;
            int j;
            if (i < ElevatorInfo.totalFloor - 1) {
                j = ElevatorInfo.totalFloor - i - 1;
            } else {
                j = i - 20;
            }


            pressButton[i].setMargin(new Insets(1, 1, 1, 1));
            pressButton[i].setFont(new Font(pressButton[i].getFont().getFontName(), pressButton[i].getFont().getStyle(), 9));

            pressButton[i].setBounds(5 + i / ElevatorInfo.totalFloor * ElevatorInfo.elevatorButtonWide,
                    j * (ElevatorInfo.floorHigh + ElevatorInfo.floorSpace) + ElevatorInfo.floorSpace
                    , ElevatorInfo.floorWide / 2, ElevatorInfo.elevatorButtonHigh);
            pressButton[i].setBackground(Color.decode("#FFD700"));
            pressButton[i].setForeground(Color.black);
            pressButton[i].addActionListener(pressButtonListener);
            add(pressButton[i]);
        }
    }

    ActionListener pressButtonListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JButton pressBtn = (JButton) e.getSource();
            for (int i = 0; i < ElevatorInfo.totalFloor * 2; i++) {
                if (pressBtn == pressButton[i]) {
                    int which = i;
                    isPress[which] = true;
                    taskList.addLast(which);
                    System.out.println("按了一下梯外按钮 " + which);
                    break;
                }
            }
            if (pressBtn.getBackground() != Color.white) {
                pressBtn.setBackground(Color.white);
            }
        }
    };

    //找到最近的电梯
    public static boolean findNearestElevator(Elevator ele, int floor, int isUp) {
        if (isUp * ele.getElevatorState() < 0) {
            for (int i = 0; i < 5; i++) {
                if (Math.abs(ele.getCurrentfloor() - floor) > Math.abs(ElevatorInfo.elevator[i].getCurrentfloor() - floor)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < 5; i++) {
                if (isUp * ElevatorInfo.elevator[i].getElevatorState() >= 0
                        && Math.abs(ele.getCurrentfloor() - floor) > Math.abs(ElevatorInfo.elevator[i].getCurrentfloor() - floor)) {
                    return false;
                }
            }
        }
        return true;
    }

    //得到是否被点中
    public static int getIsPress() {
        while (true) {
            if (taskList.isEmpty()) return -1;
            int floor = taskList.getFirst();
            if (isPress[floor]) return floor;
        }
    }
}
