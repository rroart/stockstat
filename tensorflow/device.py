import tensorflow as tf

from tensorflow.python.client import device_lib

from multiprocessing import Queue

def get_available_gpus():
    local_device_protos = device_lib.list_local_devices()
    return [x.name for x in local_device_protos if x.device_type == 'GPU']

get_available_gpus()

def hasgpu(queue):
   hasgpu = any((x.device_type == 'GPU')
                for x in device_lib.list_local_devices())
   queue.put(hasgpu)

def has_gpu() -> bool:
   return any((x.device_type == 'GPU')
              for x in device_lib.list_local_devices())

def get_pu():
    if has_gpu():
        return "/gpu:*"
    else:
        return "/cpu:*"

