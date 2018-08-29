FROM openjdk:8-jdk
MAINTAINER <roart@nvg.ntnu.no>
ENV EUREKA_SERVER_URI http://stockstat-eureka:8761/eureka
ENV EUREKA_PREFER_IPADDRESS true
ADD files/stockstat-core-0.5-SNAPSHOT.jar /usr/local/bin
RUN mkdir /usr/local/conf
ADD files/stockstat.xml /usr/local/conf
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
USER root
CMD SPARK_DRIVER_HOST=`hostname -i` java -Dconnection.url=jdbc:postgresql://postgresql-10-centos7:5432/stockstat -jar /usr/local/bin/stockstat-core-0.5-SNAPSHOT.jar

