FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y curl procps
RUN curl -s https://archive.apache.org/dist/spark/spark-2.4.0/spark-2.4.0-bin-hadoop2.7.tgz | tar -xz -C /tmp
RUN cp -pi /tmp/spark-2.4.0-bin-hadoop2.7/conf/log4j.properties.template /tmp/spark-2.4.0-bin-hadoop2.7/conf/log4j.properties
RUN sed -e "s/log4j.rootCategory=INFO/log4j.rootCategory=WARN/" -i /tmp/spark-2.4.0-bin-hadoop2.7/conf/log4j.properties
RUN ln -s /tmp /tmp/spark-2.4.0-bin-hadoop2.7/logs

WORKDIR /tmp/spark-2.4.0-bin-hadoop2.7

ENV SPARK_MASTER_HOST 127.0.0.1

USER root
EXPOSE 7077

CMD /tmp/spark-2.4.0-bin-hadoop2.7/sbin/start-master.sh -h 0.0.0.0; \
    /tmp/spark-2.4.0-bin-hadoop2.7/sbin/start-slave.sh -c 1 -m 16G spark://`hostname`:7077; \
    while true; do sleep 3600; done 
