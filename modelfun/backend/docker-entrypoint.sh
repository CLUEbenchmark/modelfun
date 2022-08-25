#!/bin/bash
# jvm相关的参数，包括内存、使用的gc，内存溢出错误等
JVM_OPS="-Xmx1024m -Xms1024m -XX:+UseG1GC"
# 启动参数
JAVA_OPS="-Dfile.encoding=utf-8 -Djava.security.egd=file:/dev/./urandom"
SPRING_OPS="--spring.config.additional-location=optional:file:/etc/app/config/"
# 获取环境变量，如果没有设置，则使用默认值
docker_get_env() {
  if [ -n "${REDIS_HOST}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.redis.host=${REDIS_HOST}"
  fi
  if [ -n "${REDIS_PORT}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.redis.port=${REDIS_PORT}"
  fi
  if [ -n "${REDIS_PASSWORD}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.redis.password=${REDIS_PASSWORD}"
  fi
  if [ -n "${REDIS_DATABASE}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.redis.database=${REDIS_DATABASE}"
  fi
  if [ -n "${MYSQL_URL}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.datasource.url=${MYSQL_URL}"
  fi
  if [ -n "${MYSQL_USER}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.datasource.username=${MYSQL_USER}"
  fi
  if [ -n "${MYSQL_PASS}" ]; then
    SPRING_OPS="${SPRING_OPS} --spring.datasource.password=${MYSQL_PASS}"
  fi
  if [ -n "${MINIO_HOST}" ]; then
    SPRING_OPS="${SPRING_OPS} --com.wl.xc.modelfun.oss.endpoint=${MINIO_HOST}"
  fi
  if [ -n "${MINIO_PORT}" ]; then
    SPRING_OPS="${SPRING_OPS} --com.wl.xc.modelfun.oss.port=${MINIO_PORT}"
  fi
  if [ -n "${MINIO_USER}" ]; then
    SPRING_OPS="${SPRING_OPS} --com.wl.xc.modelfun.oss.accessKeyId=${MINIO_USER}"
  fi
  if [ -n "${MINIO_PASS}" ]; then
    SPRING_OPS="${SPRING_OPS} --com.wl.xc.modelfun.oss.accessKeySecret=${MINIO_PASS}"
  fi
  if [ -n "${MINIO_BUCKET}" ]; then
    SPRING_OPS="${SPRING_OPS} --com.wl.xc.modelfun.oss.bucketName=${MINIO_BUCKET}"
  fi
}

docker_get_env
# 启动java应用
exec java $JVM_OPS $JAVA_OPS -jar /opt/app.jar $SPRING_OPS $@