# OS-ElevatorDispatch
First assignment of OS course, implementing the  elevator dispatching algorithm.

## 一、系统概述
### 1.1需求分析
运用操作系统进程管理的知识，结合操作系统调度算法，模拟电梯调度的功能。本电梯模拟系统有5个电梯，共20层楼。每个电梯在每一层有一个操作面板，往上和往下按钮以及显示当前楼层。每次按下按钮，五部电梯同时响应，将执行调度算法，选择最优电梯进行调度，调度过程可变，总是选择最优电梯进行调度，尽可能模拟真实电梯情况。考虑到电梯运行当中可能会出现操作不及时(模拟拥挤等情况)而造成的调度延时，在此基础上采用SCAN服务的调度。保证载客效率的最大化,同时在上下行过程中对电梯内乘客提供当前楼层和上下行状态的动态提示.

### 1.2系统功能
#### 1.2.1基本功能
共5个电梯，20层楼，每个电梯在每层楼有两个按钮，表示往上和往下请求，显示当前楼层的面板。门上有标签,可以随着电梯运行动态更新电梯状态显示给电梯内乘客。电梯内部有一个控制面板，用户进入后才能选择楼层，界面中在电梯旁边。另设开门关门键，提供手动电梯开关门操作，在界面中放置在电梯最下方。在不执行开门关门时，电梯会在设定时间间隔内自动执行。
#### 1.2.2 进阶功能
这个电梯的调度调度算法不采用先来先得的服务方法，而是参考了操作系统中的磁盘扫描（SCAN）算法，提高了载客效率。

调度以目的地方向优先，优先调度运行方向和乘客目标楼层方向相同的电梯，在此基础上比较电梯相对于楼层的距离。一部电梯只有在一个方向上处理完往该方向（如向上）的所有乘客请求并静止后，才会变换方向处理所有向另一个方向的请求，如果在这部电梯运行方向上有与之相反方向的请求，则尽可能调动其他与乘客方向相同或静止的电梯。

对于电梯内乘客的内请求，始终优先于外请求，一旦发起不需要 加入任务列表，在电梯运行到每一层直接检测，一旦发现有请求就直接开门。同时还实现了顺路到达的功能，其中向上过程中只响应向上请求，向下过程只响应向下请求。

具体设计会在后面的代码设计中展示。

### 1.3开发工具
  > JAVA SWING 进行开发

  > Intellij Idea作为开发工具

  > 运行在Windows系统上。





## 二、代码设计
### 2.1线程分配

```java
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
```

线程的启动和分配在View类的构造函数中，当View被构造时，就完成了场景的布置和线程的创建和启动，我为5个电梯每个都分配了一个线程，5个线程并行执行各自的同一套逻辑。



### 2.2电梯运行逻辑
#### 2.2.1整体逻辑

```java
//1 表示向上运行 顾客要朝上的
//-1表示朝下运行,顾客要朝下的
//2表示向上运行，顾客要朝下的
//-2表示朝下运行,顾客要朝上的
//0 表示没有运行
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
              arrival[2 * ElevatorInfo.totalFloor - currentfloor] = falseRequireButtons.isPress[2 * ElevatorInfo.totalFloor - currentfloor] = false;
              RequireButtons.pressButton[2 * ElevatorInfo.totalFloor - currentfloor].setBackground(Color.decode("#FFD700"));
              open();
          }
      }
  }
```

电梯的运行逻辑还是比较清晰，每一个电梯不停地执行 寻找目标楼层---->根据目标设置运动状态---->根据状态执行对应行为。



#### 2.2.2 寻找目标楼层

```java
   static private synchronized void searchTargetFloor(Elevator ele)    //寻找电梯的目标楼层
    {
        //从队列中拿出任务楼层
        if(!ele.arrival[ele.getRecentFloor()]){
            ele.setRecentDirection(0);
        }
        int isUp = 0;
        int i = RequireButtons.getIsPress();
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
            if(isUp*ele.getRecentDirection()>=0) {
                System.out.println("add " + i);
                ele.setArrival(i);
                RequireButtons.isPress[i] = false;
                RequireButtons.taskList.removeFirst();
                ele.setRecentDirection(isUp);
                ele.setRecentFloor(i);
            }
        }
    }
```

elevator类里保存有上一次加入任务列表的楼层的数字和方向，如果发现没有被完成就阻止相反方向任务加入，确保运行的高效率。

i表示从任务列表中获得最先要执行的外请求,如果floor大于总楼数,说明外请求要求下降,isUp设置为-1;反之设置为1;

将其传入findNearestElevator当中,只有其返回值是True才能确定当前执行线程的电梯是离目标楼层最近的电梯,然后将楼层从待分配任务中取出,放入该电梯自己的任务列表中。



#### 2.2.3 确定距离目标楼层最近的电梯

```java
    public static boolean findNearestElevator(Elevator ele, int floor, int isUp) {
        if (isUp * ele.getElevatorState() < 0) {
            for (int i = 0; i < 5; i++) {
                if (Math.abs(ele.getCurrentfloor() - floor) > Math.abs(ElevatorInfo.elevator[i].getCurrentfloor() - floor)
                        ||isUp * ElevatorInfo.elevator[i].getElevatorState() >= 0) {
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
```

 判断一个电梯是否是所有电梯中离目标楼层最近的，分两种情况讨论:
+ **运行方向和外请求方向一致,isUp和State乘积为正**

  此时需要其他电梯isUp和State乘积也为正,且到目标楼层距离小于该电梯到目标楼层距离即可证明该电梯不是距离目标最近的。

+ **运行方向和外请求方向相反,isUp和State乘积为负**

  此时，只需要一辆电梯isUp和State乘积为正或者到目标距离小于该电梯到目标楼层距离，即可证明该电梯不是距离目标最近的。

+ **若两条件都不符合，则该电梯就是最近的。**



#### 2.2.4 设置电梯运行方向

```java
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
```

100和-100是当电梯到的已经加入该电梯任务列表的楼层时的状态;

0表示电梯当前静止;

当电梯静止时,根据新点击的楼层位置确定接下来运行方向;

当电梯向上移动时,取当前任务和最新点击楼层高度中高的一层,设置为新的当前任务.

当电梯向下运动时,取当前任务和最新点击楼层高度中低的一层,设置为新的当前任务.

当电梯到达目标楼层,会把对应的arrival的目标楼层置为False并开门,如果没有其他arrival为True,则以上3种情况全部符合,返回0,电梯设为静止状态,不再运动.





#### 2.2.5 电梯运动

```java
// 上升   
private void slideUp() {
        //一次调用走一层楼梯
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

//下降
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
```

上升和下降是并列的两个函数,每一次调用使得电梯移动一个楼层.移动之后检查状态,对于上升来说,分为到达目标楼层外请求向下和外请求向上两种情况考虑;

下降函数亦然;

到达指定楼层后,上升下降函数都执行一部分相同操作,恢复楼层颜色,清空isPress的该楼层和更新电梯任务列表;



#### 2.3 函数和类

##### Elevator类
运行调度的主体

| 函数名            | 作用                                  |
| ----------------- | ------------------------------------- |
| Elevator          | 构造函数,构造电梯                     |
| run               | 多线程需要重载的函数,运行逻辑的主函数 |
| slideUp,slideDown | 控制电梯上升下降                      |
| setDirection      | 确定电梯接下来运动方向                |
| setArrival        | 添加某一楼层进入该电梯任务列表        |
| searchTargetFloor | 寻找新的外请求楼层                    |
| getCurrentFloor   | 返回电梯当前了楼层                    |
| Open,Close        | 开关门                                |
| add               | 增加电梯的外设和内设                  |



| 成员变量         | 作用                                  |
| ----------------- | ------------------------------------- |
| Elevator          | 电梯                     |
| InnerPanal   | 内设 |
| OuterPanal | 控制电梯上升下降                      |
| restart     | 确定是否有手动开门请求         |
| currentFloor    | 返回电梯当前楼层  |
| searchTargetFloor | 电梯任务列表                |
| willArrive | 返回最新发起外请求的楼层 |
| Open,Close        | 开关门                                |
| add               | 增加电梯的外设和内设                  |



##### InnerPanel类

处理电梯内乘客请求的类

| 函数名                               | 作用                        |
| ------------------------------------ | --------------------------- |
| InnerPanel                           | 构造函数,构造了开关按钮     |
| buttonFloorListener                  | 监听函数,监听内请求         |
| closeFloorListener,openFloorListener | 监听函数,监听手动开关门请求 |

| 成员变量         | 作用                                  |
| ----------------- | ------------------------------------- |
| elevator          | 电梯                     |
| buttonFloor | 保存每层内请求的监听器 |
| openButton,closeButton | 保存监听手动开关门请求的监听器   |

##### OuterPanel类
设置电梯外观和参数的类

| 函数名                               | 作用                        |
| ------------------------------------ | --------------------------- |
| OutererPanel                           | 构造函数,构造了电梯外观参数     |


| 成员变量         | 作用                                  |
| ----------------- | ------------------------------------- |
| elevator          | 电梯                     |
| floor | 储存每一层外观,不涉及逻辑 |



##### RequireButtons类

处理外请求的类

| 函数名              | 作用                               |
| ------------------- | ---------------------------------- |
| RequireButtons      | 构造函数,构造了外部按钮            |
| pressButtonListener | 监听函数,监听外请求                |
| getIsPress          | 获得分配任务表中第一个要分配的任务 |
| findNearestElevator | 寻找离某一楼层最近的电梯           |


| 成员变量    | 作用                      |
| ----------- | ------------------------- |
| isPress     | 储存某一楼层是否被点中    |
| taskList    | 待分配目标楼层            |
| pressButton | 储存每一层外观,不涉及逻辑 |



## 三、运行效果
#### 上升时顺路带走要上楼乘客

![](.\img\上升.png)

#### 下降时顺路带走要下楼乘客

![](.\img\下降.png)

#### 手动开关门

 (开按钮周围有蓝色边框说明当前被点中,电梯位于第4层)

![](.\img\手动开关门.png)

#### 动态更新运行状态
![](.\img\箭头向上.png)


![](.\img\箭头向下.png)


![](.\img\当前静止.png)

#### 多台电梯同时运行

![](.\img\同时运行.png)





## 四、分析

对于操作系统有了更深的理解，用线程模拟进程，每一部电梯是⼀个进程，而所有的请求是资源，我们 做这个电梯的⽬目的是模拟进程调度的问题，也就是模拟所有的请求资源如何分配给电梯，电梯应该先完成哪些资源的情况。
通过本程序，我第一次了解Java中多线程的使用，并加深了对多线程的了解。
当然这个程序还是有些地方写得不够好的,设计时思路有一些混乱,没有很好地降低耦合性.