#FROM debian:stretch
FROM openjdk:16-jdk
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3 python3-pip
#RUN apt-get install -y postgresql-client-10
RUN pip3 install pandas_datareader
RUN mkdir /usr/local/pd
ADD files/*.py /usr/local/pd/
ADD files/*.jar /usr/local/pd/

WORKDIR /usr/local/pd

#ENV SPARK_MASTER_HOST 127.0.0.1

CMD /usr/bin/python3 /usr/local/pd/generatexml.py; \
    /usr/bin/java -Dconnection.url=jdbc:postgresql://postgresql-10-centos7:5432/stockstat -jar /usr/local/pd/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /tmp/nasdaq.xml; \
    echo finished; \
    while true; do sleep 3600; done 