FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y curl procps
RUN curl -s https://archive.apache.org/dist/spark/spark-3.1.2/spark-3.1.2-bin-hadoop3.2.tgz | tar -xz -C /tmp
RUN cp -pi /tmp/spark-3.1.2-bin-hadoop3.2/conf/log4j.properties.template /tmp/spark-3.1.2-bin-hadoop3.2/conf/log4j.properties
RUN sed -e "s/log4j.rootCategory=INFO/log4j.rootCategory=WARN/" -i /tmp/spark-3.1.2-bin-hadoop3.2/conf/log4j.properties
RUN ln -s /tmp /tmp/spark-3.1.2-bin-hadoop3.2/logs

WORKDIR /tmp/spark-3.1.2-bin-hadoop3.2

ENV SPARK_MASTER_HOST 127.0.0.1

USER root
EXPOSE 7077

CMD /tmp/spark-3.1.2-bin-hadoop3.2/sbin/start-master.sh -h 0.0.0.0; \
    /tmp/spark-3.1.2-bin-hadoop3.2/sbin/start-slave.sh -c 1 -m 16G spark://`hostname`:7077; \
    while true; do sleep 3600; done 
