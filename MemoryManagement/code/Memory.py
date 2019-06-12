import sys
from copy import deepcopy
from PyQt5.QtWidgets import QWidget, QApplication,QLabel,QInputDialog,QLineEdit,QPushButton,QRadioButton,QMessageBox
from PyQt5.QtGui import QPainter, QColor, QBrush,QIntValidator

import operator


class Example(QWidget):
    #默认属性和参数
    (mem_width,mem_height)=(150,1280)
    free_color=QColor(240, 255, 255, 100)
    busy_color=QColor(255,215,0,100)
    (start_x,start_y)=(0,0)
    ratio=2

    (MaxIndex,BlockIndex)=(640,1)
    #输入控件
    RequiredMemory = None
    RequiredIndex = None
    SubmitMemory=None
    SubmitIndex=None


    #已经被分配的内存块列表，paintEvent根据这个队伍扫描
    DeployList=None
    #由于被已经被分配的内存块分割产生的空闲块表
    FreeList=None

    def __init__(self):
        super().__init__()
        self.DeployList=[]
        self.FreeList=[]
        #初始化空闲表
        InitialFreeBlock=MemoryBlock()
        InitialFreeBlock.setProperty(addr=0,size=640,index=0,isDeploy=False)
        self.FreeList.append(InitialFreeBlock)
        self.initUI()

    def initUI(self):
        self.setGeometry(900, 900, 2400, 1500)
        self.setWindowTitle('MemoryManagement')
        self.start_x = (self.width() - self.mem_width) / 2
        self.start_y=self.height()*0.1
        self.setBasicNotes()
        self.createInputPanal()

        self.show()

    def paintEvent(self, e):
        #print("evoke")
        qp = QPainter()
        qp.begin(self)
        self.drawRectangles(qp,self.free_color)
        for task in self.DeployList:
            self.allocateMemory(qp,task)
        qp.end()

    def createInputPanal(self):
        label4=QLabel(self)
        label4.setText("分配内存块：")
        label4.move(100,100)
        self.RequiredMemory=QLineEdit(self)
        self.RequiredMemory.move(240,100)
        self.RequiredMemory.setPlaceholderText("输入内存(0-640整数)")
        self.RequiredMemory.setValidator(QIntValidator())
        self.SubmitMemory=QPushButton(self)
        self.SubmitMemory.setText("分配")
        self.SubmitMemory.move(500,92)
        self.SubmitMemory.clicked.connect(self.allocateStrategy)

        label5=QLabel(self)
        label5.setText("收回内存块：")
        label5.move(100,200)
        self.RequiredIndex=QLineEdit(self)
        self.RequiredIndex.move(240,200)
        self.RequiredIndex.setPlaceholderText("输入内存块编号")
        self.RequiredIndex.setValidator(QIntValidator())
        self.SubmitIndex = QPushButton(self)
        self.SubmitIndex.setText("回收")
        self.SubmitIndex.move(500, 192)
        self.SubmitIndex.clicked.connect(self.recycleStrategy)

        label6=QLabel(self)
        label6.setText("选择策略:")
        label6.move(100,300)

        self.ratiobutton1=QRadioButton('最先适应',self)
        self.ratiobutton1.move(250,300)
        self.ratiobutton2 = QRadioButton('最优适应', self)
        self.ratiobutton2.move(400, 300)


    def allocateStrategy(self):
        scale=int(self.RequiredMemory.text())
        self.RequiredMemory.clear()
        if self.ratiobutton1.isChecked():
            print("strategy_1")
            block=MemoryBlock()
            for space in self.FreeList:
                print("space "+str(space.address))
                if space.size>=scale:
                    block.setProperty(addr=space.address,size=scale,index=1,isDeploy=True)
                    self.DeployList.append(block)
                    space.address=space.address+block.size
                    space.size=space.size-block.size
                    break
        elif self.ratiobutton2.isChecked():
            print("strategy_2")
            block=MemoryBlock()
            anchor,gap=-1,640
            for i in range(len(self.FreeList)):
                if self.FreeList[i].size-scale>=0 and self.FreeList[i].size-scale<gap:
                    anchor =i
                    gap=self.FreeList[i].size-scale
            if anchor!=-1:
                block.setProperty(addr=self.FreeList[anchor].address,size=scale,index=1,isDeploy=True)
                self.DeployList.append(block)
                self.FreeList[anchor].size=self.FreeList[anchor].size-scale
                self.FreeList[anchor].address=self.FreeList[anchor].address+scale
        else:
            QMessageBox.information(self, "提示","请先选择分配策略",QMessageBox.Yes | QMessageBox.No)
        self.update()

    def recycleStrategy(self):
        recycler=int(self.RequiredIndex.text())
        self.RequiredIndex.clear()
        for i in range(len(self.DeployList)):
            if recycler==self.DeployList[i].index:
                self.FreeList.append(self.DeployList[i])
                while recycler in MemoryBlock.IndexPool:
                    MemoryBlock.IndexPool.remove(recycler)
                self.DeployList.pop(i)
                self.update()
                break
        cmpfun = operator.attrgetter('address')
        self.FreeList.sort(key=cmpfun)

        for j in range(len(self.FreeList)-1,0,-1):
            if self.FreeList[j].address==self.FreeList[j-1].address+self.FreeList[j-1].size:
                print("find_one")
                self.FreeList[j-1].size=self.FreeList[j-1].size+self.FreeList[j].size
                self.FreeList.pop(j)


    def setBasicNotes(self):
        label1 = QLabel(self)
        label1.setText("内存使用情况")
        label1.move(self.start_x,self.start_y-40)

        label2=QLabel(self)
        label2.setText("0K")
        label2.move(self.start_x-36,self.start_y-10)

        label3=QLabel(self)
        label3.setText("640K")
        label3.move(self.start_x-60,self.start_y+self.mem_height-10)

    def drawRectangles(self, qp,color):
        col = QColor(0, 0, 0)
        col.setNamedColor("black")
        qp.setPen(col)
        qp.setBrush(color)
        qp.drawRect(self.start_x, self.start_y, self.mem_width, self.mem_height)

    def allocateMemory(self,qp,task):
        if type(task)!=MemoryBlock:
            print("type error")
            return False

        col = QColor(0, 0, 0)
        col.setNamedColor("black")
        qp.setPen(col)
        qp.setBrush(self.busy_color)
        qp.drawRect(self.start_x, self.start_y+task.address*self.ratio, self.mem_width, task.size*self.ratio)
        qp.drawText(self.start_x+0.2*self.mem_width,self.start_y+task.address*self.ratio+task.size+5,"index:"+str(task.index))
        if task.address!=0 and task.address!=640:
            qp.drawText(self.start_x -40, self.start_y + task.address * self.ratio+7,str(task.address))
            qp.drawText(self.start_x - 40, self.start_y + (task.address+task.size) * self.ratio + 7, str(task.address+task.size))

class MemoryBlock():
    MaxIndex=640
    IndexPool=[]

    def _init_(self):
        self.address,self.size=0,0
        index=0
        isDeploy=False

    #设置要分配的内存块的参数
    def setProperty(self,addr,size,index,isDeploy):
        self.address=addr
        self.size=size
        self.isDeploy=isDeploy
        #说明不是空块
        if index!=0:
            for i in range(1,MemoryBlock.MaxIndex+1):
                if i not in MemoryBlock.IndexPool:
                    self.index=i
                    MemoryBlock.IndexPool.append(i)
                    break


if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = Example()
    print(ex.width(),ex.height())
    sys.exit(app.exec_())