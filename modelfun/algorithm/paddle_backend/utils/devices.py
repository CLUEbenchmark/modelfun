from pynvml import *
import os
device_list = os.getenv("CUDA_VISIBLE_DEVICES")
print(device_list)


def print_gpu_utilization():
    nvmlInit()
    handle = nvmlDeviceGetHandleByIndex(1)
    info = nvmlDeviceGetMemoryInfo(handle)
    print(f"GPU memory occupied: {info.used//1024**2} MB.")


def get_gpu_id():
    return 0

def get_gpu_id_cmd():
    nvmlInit()
    # deviceCount = nvmlDeviceGetCount()
    min_usage = 1000000000000
    min_usage_id = 0
    for idx, device in enumerate(device_list.split(',')):
        handle = nvmlDeviceGetHandleByIndex(int(device))
        info = nvmlDeviceGetMemoryInfo(handle)
        if min_usage > info.used:
            print(info.used)
            min_usage = info.used
            min_usage_id = int(device)
    print('best device is ', min_usage_id)
    return min_usage_id


if __name__ == '__main__':
    print(get_gpu_id_cmd())
