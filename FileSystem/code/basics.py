#超级块，记录文件系统信息
class SuperBlock(object):
    def __init__(self):
        self.file_system_name = "GreilOS"
        self.bit = 8
        self.file_system_size = 1024*1024*1024 #1G的文件大小
        self.block_index_size = 4 #块索引的大小
        self.node_size = 128 #每一个数据块的大小
        self.node_num = 120 #最多存储120个文件
        self.data_block_size = 8 * 1024
        self.data_block_num = 12000
        self.__address_size = 4

class Node(object):
    def __init__(self,sign=None):
        self.file_size = 0
        self.block_num = 0
        self.sign=sign

        #用于表示其对应的数据块节点
        self.block_index = {}

        #采用ufs的索引结构
        for i in range(12):
            self.block_index[i]=None
        self.block_index[13]={}

    #文件大小信息处理函数
    def get_file_size(self):
        return self.file_size
    def set_file_size(self,file_size):
        self.file_size=file_size
    def set_sign(self,sign):
        self.sign=sign

    def set_block_indexs(self,block_indexs):
        #传入索引块数据列表
        self.block_num=len(block_indexs)
        count=0
        for index in block_indexs:
            count=count+1
            if count<13:
                self.block_index[count]=index
            elif count>=13 and count<2048+13:
                self.block_index[13][count-12]=index


    #用于返回i节点的信息
    def get_file_information(self):
        return{"size":self.file_size,"block_num":self.block_num}




    def get_block_indexs(self):
        index_dict=self.block_index
        block_indexs=[]

        count = 0
        for i in range(self.block_num):
            count = count+1
            if count < 13:
                block_indexs.append(self.block_index[count])
            elif count >= 13 and count < 2048 + 13:
                block_indexs.append(self.block_index[13][count-12])

        return block_indexs

class User(object):
    def __init__(self):
        self.dir_index = 0

    def set_dir_index(self,dir_index):
        self.dir_index = dir_index










