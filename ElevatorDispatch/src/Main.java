import javax.swing.*;

import ElevatorMechanism.Elevator;
import ElevatorMechanism.ElevatorInfo;
import Requires.RequireButtons;

import java.awt.*;

class View extends JFrame {
    public View() {
        super("Greilfang's Elevator(SCAN)");
        getContentPane().setBackground(Color.decode("#DC143C"));

        RequireButtons require = new RequireButtons();
        require.setBounds(30, ElevatorInfo.windowMargin,
                ElevatorInfo.elevatorButtonWide * 2,
                ElevatorInfo.elevatorButtonHigh * ElevatorInfo.totalFloor + 3 * 2);
        require.setBackground(Color.decode("#FFF0F5"));
        require.setBorder(BorderFactory.createLineBorder(Color.decode("#455A64"), 2));
        add(require);

        ElevatorInfo.elevator = new Elevator[5];
        for (int i = 0; i < 5; i++) {
            ElevatorInfo.elevator[i] = new Elevator();
            ElevatorInfo.elevator[i].add(this, i);
        }

        setSize(ElevatorInfo.windowWide, ElevatorInfo.windowHigh);
        this.setVisible(true);
        this.setResizable(false);
        for (int i = 0; i < 5; i++) {
            ElevatorInfo.elevator[i].start();
        }
    }
}



public class Main {

    public static void main(String[] args) {
        View frame = new View();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}