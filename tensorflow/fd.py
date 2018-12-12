import os
import sys
import errno

def close_fds(model_dir):
    if sys.platform != 'linux':
        print('Unsupported platform: %s' % sys.platform)
        return
    ret = {}
    base = '/proc/self/fd'
    for num in os.listdir(base):
        path = None
        try:
            path = os.readlink(os.path.join(base, num))
        except OSError as err:
            # Last FD is always the "listdir" one (which may be closed)
            if err.errno != errno.ENOENT:
                raise
        ret[int(num)] = path
        if (not (path is None) and path.startswith(model_dir)):
            os.close(int(num))
    #print(ret)
    
