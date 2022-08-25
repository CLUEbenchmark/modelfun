from urllib import response
from fastapi import APIRouter, FastAPI, BackgroundTasks,Header, Request
from pydantic import BaseModel
import uvicorn
import subprocess
import time, random
import sys
import requests
import json
import logging
import os, sys
from typing import List, Tuple, Dict, Optional, Union
from apis.info import ModelApiInput

router = APIRouter(
    prefix = '',
    tags = ['modelfunapi']
)

logger = logging.getLogger(__name__)
logging.basicConfig(level=logging.DEBUG)
app = FastAPI()

def ner_extract(input_data):
    input_data = input_data.strip()
    lines = input_data.split("\n")
    res = []
    prefix = []
    has_problem = False
    for line in lines:
        if "问题：" in line:
            values = line.split("问题：")
            if len(values) == 2:
                problems = values[1].split("、")
                multi_values = [f"问题：{problem}" for problem in problems]
                has_problem = True
                if values[0] != "":
                    prefix.append(values[0])
        elif "答案：" in line:
            values = line.split("答案：")
            if len(values) == 2 and values[0] != "":
                prefix.append(values[0])
            pass
        else:
            prefix.append(line)
    if not has_problem:
        return [input_data]
    prefix = "\n".join(prefix)
    res = [ prefix + "\n" + v + "\n答案：" for v in multi_values]
    return res

@router.post("/modelfunAI/serving/api", status_code=201)
async def request(request: ModelApiInput, request_header:Request):
    ip = request_header.headers.get("x-forwarded-for")
    logger.info(f"ip: {ip}")
    response = {}
    res_list = []
    logger.info(f"task_type: {request.task_type}, task_name: {request.task_name}")
    if request.task_type == "generate":
        items = ner_extract(request.input_data[0])
    else:
        items = request.input_data

    logger.info(f"items: {items}")
    generate_texts = []
    for item in items:
        res = single_request_process(item, request.labels,
         request.task_type, request.task_name, request.return_likelihoods, request.generate_config)
        if "generate_text" in res:
            generate_texts.append(res["generate_text"])
        logger.info(f"res:{res}")
        res_list.append(res) 
    if request.task_type == "generate":
        update_generate_text = "、".join(generate_texts)
        for res in res_list:
            res["generate_text"] = update_generate_text

    response["result"] = res_list
    response['state'] = True
    response['detail'] = ""
    return response

def single_request_process(data, label_list, task_type, 
        task_name, return_likelihoods, generate_config):
    inputs = {
        "query": data,
        "label_list": label_list,
        "task_name": task_name,
        "task_type": task_type,
        "return_likelihoods": return_likelihoods,
        "generate_config": generate_config
    }
    res = requests.post("http://10.0.1.162:8505/modelfunAI/serving/api", json=inputs)
    return json.loads(res.text)

