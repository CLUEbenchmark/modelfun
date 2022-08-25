import torch  # quite important for mixing error.
import os
from fastapi import FastAPI
import uvicorn
import argparse
from uvicorn.config import LOGGING_CONFIG
import sys
sys.path.append('.')
from apis import classification, modelfunapi, ner, cls_pretrain, ner_pretrain
IP_ADDR = os.getenv("IP") 
IP_ADDR = IP_ADDR if IP_ADDR is not None else 'localhost'



description = """
Mofun Algorithm Interface
# 交互方式
1. json格式交互，对于数据除了提供相应的文件地址外，其余的交互以id的方式进行
2. 后端需要将返回的结果存一下，比如说用户在相同数据集上重复调用可以直接返回之前结果

# 数据集格式
## 文件组织
以zip文件上传，解压后得到如下目录：
```
├── data
│   ├── dev.json [可无]
│   ├── test.json
│   └── train.json
```
每一个文件为一致的格式，每一行为一条数据：
```json
{"id": 8785, "label": "22", "sentence": "我已经寄出了，麻烦点一下同意换货", "label_des": "买家表示需要退货退款"}
```
"""
app = FastAPI(title="ModelFun",
              description=description,
              version="1.8.0",
              )
paddle_port = os.getenv("PADDLE_PORT")
paddle_port = int(paddle_port) if paddle_port is not None else 6685


app.include_router(classification.router)
app.include_router(ner.router)
app.include_router(cls_pretrain.router)
app.include_router(ner_pretrain.router)
app.include_router(modelfunapi.router)
@app.get('/modelfunAI/', response_model=str)
async def index():
    return 'Welcom to Modelfun!'


if __name__ == "__main__":
    """
    product: modelfun.westlake.ink:6001 这是外部请求的地址和端口，A40内部的服务端口请使用6664   对应的paddle端口为6684
    test: modelfun1.westlake.ink:6001，A40内部的服务端口请使用6674 对应paddle 6685
    """
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-d', '--debug', help='debug mode', action='store_true')
    parser.add_argument(
        '-p', '--port', help='port of this service', type=int, default=6674)
    parser.add_argument(
        '--paddle-port', help='port of paddle', type=int, default=6685)
    args = parser.parse_args()
    uvicorn.run("apiserver:app", reload=True, debug=args.debug,
                log_config='log_config.yaml',
                host=IP_ADDR, port=args.port)
