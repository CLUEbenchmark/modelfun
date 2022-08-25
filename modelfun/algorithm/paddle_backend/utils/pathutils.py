import os
import shutil


def create_if_not_exists(folder):
    if not os.path.exists(folder):
        os.makedirs(folder)


def remove_if_exists(folder):
    if os.path.exists(folder):
         shutil.rmtree(folder)