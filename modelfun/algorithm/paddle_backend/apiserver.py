import requests
from fastapi import FastAPI, HTTPException, BackgroundTasks
from typing import List, Tuple, Dict, Optional
import uvicorn
import argparse
import traceback
import sys
import os
import pickle
import random
import time
sys.path.append('.')
# sys.path.append('../')
from apis import classification, ner
IP_ADDR = os.getenv("IP") 
IP_ADDR = IP_ADDR if IP_ADDR is not None else 'localhost'


PYTHON_PATH = 'python'

description = """
For paddle pre-trained model.

"""
app = FastAPI(title="paddle serving",
              description=description,
              version="1.2.1",
              )


app.include_router(classification.router)
app.include_router(ner.router)

@app.get('/', response_model=str)
async def index():
    return 'Welcom to Modelfun!'


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-d', '--debug', help='debug mode', action='store_true')
    parser.add_argument(
        '-p', '--port', help='debug mode', type=int, default=6685)
    args = parser.parse_args()
    uvicorn.run("apiserver:app", reload=True, debug=args.debug,
                log_config='log_config.yaml',
                host=IP_ADDR, port=args.port)
