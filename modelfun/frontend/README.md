
# 2.环境依赖
具体可以参考package.js,Node.js>=14.16.0（不保证小于该版本的node能够运行成功）

## 3.项目启动&部署
1.首先运行命令`npm run install` 安装依赖，如果没有安装成功，请检查网络连接。<br>
2.运行命令`npm run build`对项目进行打包<br>
3.运行`docker build -t modelfun/front .`<br>