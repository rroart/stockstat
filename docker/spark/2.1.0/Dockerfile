FROM debian:stretch
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y openjdk-11-jdk curl procps
RUN curl -s https://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz | tar -xz -C /usr/local

WORKDIR /usr/local/spark-2.1.0-bin-hadoop2.7

ENV SPARK_MASTER_HOST 127.0.0.1

CMD /usr/local/spark-2.1.0-bin-hadoop2.7/sbin/start-master.sh --webui-port 8088; \
    /usr/local/spark-2.1.0-bin-hadoop2.7/sbin/start-slave.sh -c 1 -m 16G spark://127.0.0.1:7077; \
    while true; do sleep 3600; done 