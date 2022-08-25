# modelfun镜像使用说明

该镜像是基于linux系统进行打包部署，未在windows上进行测试。
## 安装docker和docker compose
根据[文档](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html) 安装docker和nvidia-docker

安装docker-compose
```
pip install docker-compose
```

修改配置文件/etc/docker/daemon.json使docker-compose支持nvidia-docker
```json 
{
    "default-runtime": "nvidia",
    "runtimes": {
        "nvidia": {
            "path": "/usr/bin/nvidia-container-runtime",
            "runtimeArgs": []
        }
    },
}
```

## 运行启动说明

运行前配置：

用户需要手动指定一个minio的地址，默认情况下，minio只能内部访问，因为生成的模型不能下载，如果想要下载模型，需要在后台的额外配置文件中，指定minio的地址，该地址必须能被宿主机之外的机器访问。

比如说宿主机的地址为，192.168.0.2，并且该地址能被其他机器访问，那么minio的地址为http://192.168.0.2

具体步骤为：

1. 修改docker-compose.yaml文件，在后台服务的配置中，把容器内的/etc/app/config目录挂载到宿主机上

```yaml
backend:
    image: modelfun/backend
    container_name: backend
    restart: always
    volumes:
    ## 这里就是挂载
      - /opt/containers/backend/config:/etc/app/config
```

2. 挂载完成后，把backend目录下的application.yaml文件复制到宿主机目录下，修改minio的地址

```yaml
com:
  wl:
    xc:
      modelfun:
        oss:
          # minio 访问地址
          endpoint: http://192.168.0.2
```

编译镜像并运行
> **bash setup.sh**


成功后运行compose命令启动服务（已包含在以上脚本中）：

> **docker compose -f docker-compose.yaml up -d**

启动成功后访问http:{yourIp}，如果成功跳出页面表示服务启动成功。

默认账号密码：13012345678/123456

## 镜像说明

- modelfun/redis ：以redis:6.2.7镜像为基础的二次打包镜像，添加了默认配置文件到/etc/redis/redis.conf。
- modelfun/front： 以nginx镜像为基础，打包了页面代码和nginx配置文件，配置文件目录为/etc/nginx/conf.d。
- modelfun/backend：以openjdk:11.0.15-jdk镜像为基础，打包了后台服务。
- modelfun/alg： 以pytorch/pytorch:1.12.0-cuda11.3-cudnn8-runtime镜像为基础，打包了主要算法服务服务。
- modelfun/paddlealg：以registry.baidubce.com/paddlepaddle/paddle:2.3.1-gpu-cuda11.2-cudnn8镜像为基础，打包了涉及paddle的算法服务。


## 文件目录说明

解压之后所得的文件为：

- images.tar：包含所有镜像的tar包
- front：前端的文件夹
- backend：后台服务的文件夹
- alg：算法服务的文件夹
- docker-compose.yaml：docker compose文件

**以下是对各文件夹内容的说明**

### 前端

前端镜像是以nginx为基础制作的镜像，默认监听80端口，并在配置文件中配置了后台服务的反向代理。

**default.conf**文件：

该文件是nginx的配置文件，里面配置了默认的反向代理和监听端口。如果需要手动指定，需要在docker-compose.yaml文件中的front服务中添加volumes映射。

比如：

> **/opt/containers/front/conf:/etc/nginx/conf.d**

把/opt/containers/front/conf目录映射到/etc/nginx/conf.d（容器内目录不能修改），并且手动编写conf结尾的配置文件到指定目录下。

### 后台

后台服务是以openjdk:11.0.15-jdk为基础制作的镜像，依赖MySQL、Redis、Minio三个服务，默认监听8080端口，并且接口服务的path是以/modelfun开头，如果需要修改后台服务的监听端口和服务Path，需要同步修改前端配置文件。

该目录下有:

- application.yaml文件，该文件为用户可额外指定的配置，后台服务默认的配置会被该文件内的配置覆盖。

后台服务镜像内目录说明：

- /opt/app.jar: 后台jar包服务
- /var/log/backend：后台日志目录
- /etc/app/config: 额外配置文件目录

#### 镜像环境变量说明

**REDIS_HOST**

redis地址，默认为redis，即docker内部网络的服务名

**REDIS_PORT**

redis端口号，默认为6379

**REDIS_PASSWORD**

redis密码，默认为xha3RNgav7MDSYhU

**MYSQL_HOST**

mysql的地址，默认为mysql，即docker内部网络的mysql服务名

**MYSQL_PORT**

mysql端口号，默认3306

**MYSQL_DB**

mysql中的数据库名，默认为modelfun

**MYSQL_USER**

mysql账号，默认root

**MYSQL_PASS**

mysql密码，默认123456

**MINIO_HOST**

minio地址，默认http://minio，即docker内部网络的minio服务名称

**MINIO_PORT**

minio端口号，默认9000

**MINIO_USER**

minio用户名，默认minio-root-user

**MINIO_PASS**

minio密码，默认minio-root-password

**MINIO_BUCKET**

minio bucket名称，默认modelfun

#### 额外配置文件

以上为用户可以通过环境变量指定的配置，用户还能通过额外配置文件的形式，覆盖默认配置，额外配置文件为application.yaml，用户可以在docker-compose.yaml文件中进行映射：

```yaml
backend:
    image: modelfun/backend
    container_name: backend
    restart: always
    volumes:
    ## 把/opt/containers/backend/config映射到/etc/app/config，并且把application.yaml放进/opt/containers/backend/config中，后台服务会读取容器内部/etc/app/config文件夹下application.yaml的文件
      - /opt/containers/backend/config:/etc/app/config  
```

application.yaml文件说明可以看文件内部的注释。

#### 额外说明

当第一次启动后台服务时，会执行脚本去数据库中创建表，插入默认账号密码。建议在数据库已经存在表的情况下，修改配置文件，防止以后再次执行无用脚本。

在额外的配置文件中进行以下配置以修改：

```yaml
spring:
  sql:
    init:
      mode: never
```
### 算法
算法根据环境依赖分为两个独立镜像，直接部署即可。当前接口、路径均直接与后端匹配，直接运行即可。