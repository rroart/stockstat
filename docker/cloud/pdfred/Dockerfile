FROM maven:3.6.3-jdk-13
MAINTAINER <rroart@gmail.com>
RUN mkdir /app
WORKDIR /app
COPY main main
COPY input input
COPY common common
COPY iclij iclij
COPY eureka eureka
COPY web web
COPY webr webr
COPY distribution distribution
COPY pom.xml pom.xml
RUN mvn -pl input -am install -Dmaven.test.skip=true
RUN cp -p input/target/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /usr/local/bin

#FROM debian:stretch
FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>

ARG mydbserver
ENV MYDBSERVER ${mydbserver}

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3 python3-pip
#RUN apt-get install -y postgresql-client-10
RUN pip3 install pandas_datareader
RUN mkdir /usr/local/pdfred
ADD python/pdfred/*.py /usr/local/pdfred/
COPY --from=0 /usr/local/bin/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /usr/local/bin/

WORKDIR /usr/local/pdfred

#ENV SPARK_MASTER_HOST 127.0.0.1

CMD /usr/bin/python3 /usr/local/pdfred/generatexml.py; \
    java -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/stockstat -jar /usr/local/bin/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar /tmp/fred.xml; \
    echo finished; \
    while true; do sleep 3600; done 
