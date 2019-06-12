import os
from managers import *
from toolkit import *
if __name__ == '__main__':
    if os.path.isfile('src_sys.pkl'):
        system=permanent_load('src_sys.pkl')
    else:
        system=FileSystem()
    system.welcome()
    system.recv_input()
    permanent_store(system, 'src_sys.pkl')
