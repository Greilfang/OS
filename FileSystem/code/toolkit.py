import pickle

def transform(data, to_type=None):
    if isinstance(data,str):
        data=bytes(data,encoding='utf-8')
    elif isinstance(data,dict):
        data=str(data)
        data=bytes(data,encoding='utf-8')

    elif isinstance(data,bytes):
        if to_type=="dir":
            data=eval(data)
        elif to_type=="text":
            #print("to text:",data)
            data=str(data,encoding='utf-8')
        else:
            data=data
    else:
        print("Data transform error!")
        return
    return data

def xy_to_index(y_length,x,y):
    return x*y_length+y

def index_to_xy(x_length,y_length,index):
    return int(index/y_length),index % y_length


def permanent_store(system,target):
    with open(target,'wb') as f:
        picklestring=pickle.dump(system,f)
        # f.write(picklestring)

def permanent_load(source):
    with open(source,'rb') as f:
        return pickle.load(f)